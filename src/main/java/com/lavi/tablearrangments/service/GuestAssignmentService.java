package com.lavi.tablearrangments.service;

import com.lavi.tablearrangments.domain.Guest;
import com.lavi.tablearrangments.domain.SeatingTable;
import com.lavi.tablearrangments.repository.GuestRepository;
import com.lavi.tablearrangments.repository.SeatingTableRepository;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for assigning guests to seating tables at an event,
 * considering constraints like accessibility, near-stage preference,
 * relation grouping, preferGuests, side (GROOM/BRIDE/BOTH), and conflict avoidance.
 */
@Service
@Transactional
public class GuestAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(GuestAssignmentService.class);

    private final GuestRepository guestRepository;
    private final SeatingTableRepository seatingTableRepository;

    public GuestAssignmentService(GuestRepository guestRepository, SeatingTableRepository seatingTableRepository) {
        this.guestRepository = guestRepository;
        this.seatingTableRepository = seatingTableRepository;
    }

    /**
     * Main method to assign all guests to tables according to constraints.
     * @return List of warning messages for unassigned guests.
     */
    public List<String> assignAll() {
        List<String> warnings = new ArrayList<>();
        List<Guest> allGuests = guestRepository.findAllByEventUserIsCurrentUserList();
        List<SeatingTable> allTables = seatingTableRepository.findByUserIsCurrentUser();

        allGuests = allGuests
            .stream()
            .filter(g -> g.getStatus() != null && g.getStatus().name().equals("CONFIRMED"))
            .collect(Collectors.toList());

        validateSetup(allGuests, allTables, warnings);

        List<GuestGroup> guestGroups = groupGuestsByRelation(allGuests, allTables);
        int maxSeatsPerTable = allTables.stream().mapToInt(SeatingTable::getMaxSeats).max().orElse(4);
        guestGroups = splitLargeGroupsIfNeeded(guestGroups, maxSeatsPerTable);

        guestGroups.forEach(g -> log.debug("Group: {} | Seats: {}", g.getNames(), g.getTotalSeats()));

        Map<Long, TableState> tableStates = initializeTableStates(allTables);
        Map<String, List<SeatingTable>> sideTables = splitTablesBySide(allTables);

        Map<GuestGroup, SeatingTable> bestAssignment = new HashMap<>();
        int[] minOpenTables = { Integer.MAX_VALUE };

        backtrack(new HashMap<>(), guestGroups, tableStates, sideTables, bestAssignment, minOpenTables);

        if (bestAssignment.isEmpty()) {
            warnings.add("\u26a0\ufe0f לא ניתן היה לשבץ את האורחים.");
            log.warn("No valid assignment found.");
        }

        persistAssignment(bestAssignment);

        guestGroups
            .stream()
            .filter(group -> !bestAssignment.containsKey(group))
            .forEach(group -> warnings.add("\u26a0\ufe0f לא ניתן היה לשבץ את הקבוצה: " + group.getNames()));

        return warnings;
    }

    /**
     * Recursive backtracking to assign guests optimally to tables.
     */
    private void backtrack(
        Map<GuestGroup, SeatingTable> assignment,
        List<GuestGroup> groups,
        Map<Long, TableState> tableStates,
        Map<String, List<SeatingTable>> sideTables,
        Map<GuestGroup, SeatingTable> bestAssignment,
        int[] minOpenTables
    ) {
        if (assignment.size() == groups.size()) {
            long openTables = tableStates.values().stream().filter(ts -> !ts.assignedGroups.isEmpty()).count();
            if (openTables < minOpenTables[0]) {
                minOpenTables[0] = (int) openTables;
                bestAssignment.clear();
                bestAssignment.putAll(assignment);
                log.info("New best assignment with {} open tables.", openTables);
            }
            return;
        }

        // MRV: לבחור את הקבוצה עם הכי פחות אפשרויות חוקיות
        GuestGroup nextGroup = selectGroupWithFewestOptions(groups, assignment, tableStates, sideTables);
        if (nextGroup == null) return;

        List<TableState> candidates = tableStates
            .values()
            .stream()
            .filter(ts -> ts.canFit(nextGroup))
            .filter(ts -> !ts.hasConflictWith(nextGroup))
            .filter(ts -> matchesSide(nextGroup, ts.getTable(), sideTables))
            .sorted(Comparator.comparingInt(ts -> computePenalty(ts, nextGroup, sideTables)))
            .collect(Collectors.toList());

        for (TableState ts : candidates) {
            ts.assignGroup(nextGroup);
            assignment.put(nextGroup, ts.getTable());

            // Forward Checking: לבדוק אחרי השיבוץ שיש עדיין אפשרויות לכל הקבוצות שלא שובצו
            if (isFeasible(groups, assignment, tableStates, sideTables)) {
                backtrack(assignment, groups, tableStates, sideTables, bestAssignment, minOpenTables);
            }

            assignment.remove(nextGroup);
            ts.removeGroup(nextGroup);
        }
    }

    /**
     * Compute penalty for seating a group at a table.
     */
    private int computePenalty(TableState ts, GuestGroup group, Map<String, List<SeatingTable>> sideTables) {
        int penalty = 0;
        if (group.requiresAccessibility() && !ts.getTable().getAccessibility()) penalty += 1000;
        if (group.requiresNearStage() && !ts.getTable().getNearStage()) penalty += 200;
        String relation = group.getRelation();
        penalty -= relation != null ? ts.countSameRelation(relation) * 250 : 0;
        penalty -= ts.countPreferredGuests(group) * 150;
        penalty += ts.getFreeSeats() - group.getTotalSeats();
        if (!matchesSide(group, ts.getTable(), sideTables)) penalty += 300;
        return penalty;
    }

    /**
     * Save the assignment to the database.
     */
    private void persistAssignment(Map<GuestGroup, SeatingTable> assignment) {
        for (GuestGroup group : assignment.keySet()) {
            SeatingTable table = assignment.get(group);
            for (Guest guest : group.getGuests()) {
                guest.setTable(table); // אם אין שולחן, ישאר null
                if (table != null) {
                    guest.setEvent(table.getEvent());
                }
            }
            guestRepository.saveAll(group.getGuests());
        }
    }

    private List<GuestGroup> groupGuestsByRelation(List<Guest> guests, List<SeatingTable> tables) {
        int maxSeatsPerTable = tables.stream().mapToInt(SeatingTable::getMaxSeats).max().orElse(4);
        Map<String, List<Guest>> relationGroups = guests
            .stream()
            .filter(g -> g.getRelation() != null)
            .collect(Collectors.groupingBy(g -> g.getRelation().name()));

        List<GuestGroup> result = new ArrayList<>();
        for (List<Guest> group : relationGroups.values()) {
            List<Guest> current = new ArrayList<>();
            int currentSeats = 0;
            for (Guest guest : group) {
                int seats = guest.getNumberOfSeats();
                if (currentSeats + seats > maxSeatsPerTable && !current.isEmpty()) {
                    result.add(new GuestGroup(new ArrayList<>(current)));
                    current.clear();
                    currentSeats = 0;
                }
                current.add(guest);
                currentSeats += seats;
            }
            if (!current.isEmpty()) result.add(new GuestGroup(current));
        }
        return result;
    }

    private List<GuestGroup> splitLargeGroupsIfNeeded(List<GuestGroup> groups, int maxSeatsPerTable) {
        List<GuestGroup> adjustedGroups = new ArrayList<>();
        for (GuestGroup group : groups) {
            List<Guest> current = new ArrayList<>();
            int currentSeats = 0;
            for (Guest guest : group.getGuests()) {
                int seats = guest.getNumberOfSeats();
                if (seats > maxSeatsPerTable) {
                    throw new IllegalStateException("Guest " + guest.getLastNameAndFirstName() + " requires more seats than any table!");
                }
                if (currentSeats + seats > maxSeatsPerTable && !current.isEmpty()) {
                    adjustedGroups.add(new GuestGroup(new ArrayList<>(current)));
                    current.clear();
                    currentSeats = 0;
                }
                current.add(guest);
                currentSeats += seats;
            }
            if (!current.isEmpty()) adjustedGroups.add(new GuestGroup(current));
        }
        return adjustedGroups;
    }

    private Map<Long, TableState> initializeTableStates(List<SeatingTable> tables) {
        Map<Long, TableState> states = new HashMap<>();
        for (SeatingTable table : tables) {
            states.put(table.getId(), new TableState(table));
        }
        return states;
    }

    /**
     * בוחר את הקבוצה עם הכי מעט אפשרויות חוקיות (MRV)
     */
    private GuestGroup selectGroupWithFewestOptions(
        List<GuestGroup> groups,
        Map<GuestGroup, SeatingTable> assignment,
        Map<Long, TableState> tableStates,
        Map<String, List<SeatingTable>> sideTables
    ) {
        GuestGroup bestGroup = null;
        int minOptions = Integer.MAX_VALUE;

        for (GuestGroup group : groups) {
            if (assignment.containsKey(group)) continue;
            long options = tableStates
                .values()
                .stream()
                .filter(ts -> ts.canFit(group))
                .filter(ts -> !ts.hasConflictWith(group))
                .filter(ts -> matchesSide(group, ts.getTable(), sideTables))
                .count();
            if (options < minOptions) {
                minOptions = (int) options;
                bestGroup = group;
            }
        }
        return bestGroup;
    }

    /**
     * אחרי כל השמה לבדוק אם נותרו אפשרויות חוקיות לכל קבוצה שלא שובצה
     */
    private boolean isFeasible(
        List<GuestGroup> groups,
        Map<GuestGroup, SeatingTable> assignment,
        Map<Long, TableState> tableStates,
        Map<String, List<SeatingTable>> sideTables
    ) {
        for (GuestGroup group : groups) {
            if (assignment.containsKey(group)) continue;
            boolean hasOption = tableStates
                .values()
                .stream()
                .anyMatch(ts -> ts.canFit(group) && !ts.hasConflictWith(group) && matchesSide(group, ts.getTable(), sideTables));
            if (!hasOption) {
                return false; // אין אפשרות חוקית - חייבים לחזור אחורה
            }
        }
        return true;
    }

    private void validateSetup(List<Guest> guests, List<SeatingTable> tables, List<String> warnings) {
        long accessibilityGuests = guests.stream().filter(g -> Boolean.TRUE.equals(g.getAccessibility())).count();
        long nearStageGuests = guests.stream().filter(g -> Boolean.TRUE.equals(g.getNearStage())).count();
        long groomGuests = guests.stream().filter(g -> g.getSide() != null && g.getSide().name().equals("GROOM")).count();
        long brideGuests = guests.stream().filter(g -> g.getSide() != null && g.getSide().name().equals("BRIDE")).count();

        long accessibilityTables = tables.stream().filter(t -> Boolean.TRUE.equals(t.getAccessibility())).count();
        long nearStageTables = tables.stream().filter(t -> Boolean.TRUE.equals(t.getNearStage())).count();

        int halfTables = tables.size() / 2;

        if (accessibilityGuests > accessibilityTables) {
            String msg = "\u26a0\ufe0f אין מספיק שולחנות נגישים: צריכים " + accessibilityGuests + ", ויש רק " + accessibilityTables;
            warnings.add(msg);
            log.warn(msg);
        }

        if (nearStageGuests > nearStageTables) {
            String msg = "\u26a0\ufe0f אין מספיק שולחנות ליד הבמה: צריכים " + nearStageGuests + ", ויש רק " + nearStageTables;
            warnings.add(msg);
            log.warn(msg);
        }

        if (groomGuests > brideGuests + halfTables || brideGuests > groomGuests + halfTables) {
            String msg = "\u26a0\ufe0f ייתכן חוסר איזון בין צד החתן לצד הכלה. אורחי חתן: " + groomGuests + ", אורחי כלה: " + brideGuests;
            warnings.add(msg);
            log.warn(msg);
        }
    }

    private Map<String, List<SeatingTable>> splitTablesBySide(List<SeatingTable> allTables) {
        List<SeatingTable> sortedTables = allTables
            .stream()
            .sorted(Comparator.comparingInt(SeatingTable::getTableNumber))
            .collect(Collectors.toList());
        int mid = sortedTables.size() / 2;
        Map<String, List<SeatingTable>> sides = new HashMap<>();
        sides.put("GROOM", sortedTables.subList(0, mid));
        sides.put("BRIDE", sortedTables.subList(mid, sortedTables.size()));
        return sides;
    }

    private boolean matchesSide(GuestGroup group, SeatingTable table, Map<String, List<SeatingTable>> sideTables) {
        String groupSide = group.getGuests().get(0).getSide() != null ? group.getGuests().get(0).getSide().name() : "BOTH";
        if (groupSide.equals("BOTH")) return true;
        List<SeatingTable> allowedTables = sideTables.get(groupSide);
        return allowedTables.contains(table);
    }

    /**
     * Represents a group of guests.
     */
    public static class GuestGroup {

        private List<Guest> guests;

        public GuestGroup(List<Guest> guests) {
            this.guests = guests;
        }

        public List<Guest> getGuests() {
            return guests;
        }

        public int getTotalSeats() {
            return guests.stream().mapToInt(Guest::getNumberOfSeats).sum();
        }

        public String getNames() {
            return guests.stream().map(Guest::getLastNameAndFirstName).collect(Collectors.joining(", "));
        }

        public boolean requiresAccessibility() {
            return guests.stream().anyMatch(g -> Boolean.TRUE.equals(g.getAccessibility()));
        }

        public boolean requiresNearStage() {
            return guests.stream().anyMatch(g -> Boolean.TRUE.equals(g.getNearStage()));
        }

        public String getRelation() {
            return guests.isEmpty() ? null : guests.get(0).getRelation().name();
        }
    }

    /**
     * Represents the state of a table during assignment.
     */
    public static class TableState {

        private SeatingTable table;
        private List<GuestGroup> assignedGroups = new ArrayList<>();
        private int usedSeats = 0;

        public TableState(SeatingTable table) {
            this.table = table;
        }

        public boolean canFit(GuestGroup group) {
            return getFreeSeats() >= group.getTotalSeats();
        }

        public void assignGroup(GuestGroup group) {
            assignedGroups.add(group);
            usedSeats += group.getTotalSeats();
        }

        public void removeGroup(GuestGroup group) {
            assignedGroups.remove(group);
            usedSeats -= group.getTotalSeats();
        }

        public int countSameRelation(String relation) {
            return (int) assignedGroups
                .stream()
                .flatMap(g -> g.getGuests().stream())
                .filter(g -> g.getRelation().name().equals(relation))
                .count();
        }

        public int countPreferredGuests(GuestGroup group) {
            Set<Long> seatedGuestIds = assignedGroups
                .stream()
                .flatMap(g -> g.getGuests().stream())
                .map(Guest::getId)
                .collect(Collectors.toSet());
            return (int) group.getGuests().stream().flatMap(g -> g.getPreferGuests().stream()).filter(seatedGuestIds::contains).count();
        }

        public int getFreeSeats() {
            return table.getMaxSeats() - usedSeats;
        }

        public boolean hasConflictWith(GuestGroup group) {
            return assignedGroups
                .stream()
                .flatMap(g -> g.getGuests().stream())
                .anyMatch(existingGuest ->
                    group
                        .getGuests()
                        .stream()
                        .anyMatch(
                            newGuest ->
                                existingGuest.getAvoidGuests().contains(newGuest.getId()) ||
                                newGuest.getAvoidGuests().contains(existingGuest.getId())
                        )
                );
        }

        public SeatingTable getTable() {
            return table;
        }
    }
}
