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

    private final Map<Long, Integer> assignedSeats = new HashMap<>();

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
        // Step 1: Load guests and tables (Algorithm: —)
        List<String> warnings = new ArrayList<>();
        List<Guest> allGuests = guestRepository.findAllByEventUserIsCurrentUserList();
        List<SeatingTable> allTables = seatingTableRepository.findByUserIsCurrentUser();

        //reset last sort
        allGuests.forEach(g -> g.setTable(null));
        guestRepository.saveAll(allGuests);

        allGuests = allGuests
            .stream()
            .filter(g -> g.getStatus() != null && g.getStatus().name().equals("CONFIRMED"))
            .collect(Collectors.toList());

        log.info("[Step 1] ✅ Loaded {} guests and {} tables.", allGuests.size(), allTables.size());

        printTablesStatus(allTables);

        // Step 2: Validate setup (Algorithm: —)
        validateSetup(allGuests, allTables, warnings);

        log.info("[Step 2] ✅ Validation completed: accessibility, stage proximity, side balance.");

        // Step 3: Group guests by relation (Algorithm: Grouping)
        List<GuestGroup> guestGroups = groupGuestsByRelation(allGuests, allTables);
        int maxSeatsPerTable = allTables.stream().mapToInt(SeatingTable::getMaxSeats).max().orElse(4);
        guestGroups = splitLargeGroupsIfNeeded(guestGroups, maxSeatsPerTable);
        guestGroups = splitConflictingGroups(guestGroups, warnings);

        for (GuestGroup group : guestGroups) {
            log.info("[Step 3] 📦 Created group: {} ({} seats)", group.getNames(), group.getTotalSeats());
        }

        // Step 4: Split oversized groups if needed (Algorithm: Constraint Splitting)
        log.info("[Step 4] ✅ Split oversized groups according to max seats per table.");

        Map<Long, TableState> tableStates = initializeTableStates(allTables);
        assignedSeats.clear();
        allTables.forEach(table -> assignedSeats.put(table.getId(), 0));

        printTablesStatus(allTables);

        // Step 5: Start backtracking process (Algorithm: Backtracking + Forward Checking)
        log.info("[Step 5] 🚀 Starting backtracking process to assign guest groups.");

        Map<GuestGroup, SeatingTable> bestAssignment = new HashMap<>();
        int[] minOpenTables = { Integer.MAX_VALUE };

        backtrack(new HashMap<>(), guestGroups, tableStates, bestAssignment, minOpenTables);

        persistAssignment(bestAssignment);

        guestGroups
            .stream()
            .filter(group -> !bestAssignment.containsKey(group))
            .forEach(group -> warnings.add("⚠️ Could not assign group: " + group.getNames()));

        return warnings;
    }

    /**
     * Recursive method to explore possible assignments of guest groups to tables using backtracking.
     *
     * @param assignment Current partial assignment of groups to tables.
     * @param groups List of all guest groups to assign.
     * @param tableStates Current state of all tables.
     * @param bestAssignment Stores the best assignment found so far.
     * @param minOpenTables Tracks the minimum number of open tables found so far.
     */

    private void backtrack(
        Map<GuestGroup, SeatingTable> assignment,
        List<GuestGroup> groups,
        Map<Long, TableState> tableStates,
        Map<GuestGroup, SeatingTable> bestAssignment,
        int[] minOpenTables
    ) {
        // Step 6: Select the group with the fewest options (Algorithm: MRV - Minimum Remaining Values)
        log.debug("[Step 6] ↩️ Backtracking: {} groups assigned so far.", assignment.size());

        if (assignment.size() == groups.size()) {
            long openTables = tableStates.values().stream().filter(ts -> !ts.assignedGroups.isEmpty()).count();
            if (openTables < minOpenTables[0]) {
                minOpenTables[0] = (int) openTables;
                bestAssignment.clear();
                bestAssignment.putAll(assignment);
                log.info("[Step 6] 🥇 New best assignment found with {} open tables.", openTables);
            }
            return;
        }

        log.debug("[Step 6] 🎯 Selecting next group to assign using MRV heuristic...");
        GuestGroup nextGroup = selectGroupWithFewestOptions(groups, assignment, tableStates);
        if (nextGroup == null) return;

        log.info("[Step 6] 🎯 Selected group: {} ({} seats)", nextGroup.getNames(), nextGroup.getTotalSeats());

        // Step 7: Try assigning group to table (Algorithm: Backtracking)
        List<TableState> candidates = tableStates
            .values()
            .stream()
            .filter(ts -> ts.canFit(nextGroup))
            .filter(ts -> !ts.hasConflictWith(nextGroup))
            .sorted(Comparator.comparingInt(ts -> computePenalty(ts, nextGroup)))
            .toList();

        if (candidates.isEmpty()) {
            log.warn("❌ No available tables for group '{}'. Backtracking...", nextGroup.getNames());
            return;
        }

        for (TableState ts : candidates) {
            log.debug("[Step 7] 🪑 Trying to assign group '{}' to table '{}'.", nextGroup.getNames(), ts.getTable().getTableNumber());
            ts.assignGroup(nextGroup);
            assignment.put(nextGroup, ts.getTable());

            assignedSeats.compute(ts.getTable().getId(), (k, v) -> (v == null ? 0 : v) + nextGroup.getTotalSeats());

            printCurrentAssignments(tableStates);

            // Step 8: Check forward feasibility (Algorithm: Forward Checking)
            if (isFeasible(groups, assignment, tableStates)) {
                backtrack(assignment, groups, tableStates, bestAssignment, minOpenTables);
            }

            log.info(
                "[Step 8] 🔄 Backtracking: Removing group '{}' from table '{}'.",
                nextGroup.getNames(),
                ts.getTable().getTableNumber()
            );
            assignment.remove(nextGroup);
            ts.removeGroup(nextGroup);
            assignedSeats.compute(ts.getTable().getId(), (k, v) -> (v == null ? 0 : v) - nextGroup.getTotalSeats());

            printCurrentAssignments(tableStates);
        }
    }

    /**
     * Calculates a penalty score for assigning a group to a specific table based on multiple constraints.
     *
     * @param ts The current table state.
     * @param group The guest group to evaluate.
     * @return Penalty score representing how suitable this assignment is.
     */

    private int computePenalty(TableState ts, GuestGroup group) {
        int penalty = 0;
        int initialPenalty = penalty;

        if (group.requiresAccessibility() && !ts.getTable().getAccessibility()) {
            penalty += 1000;
            log.info("[Penalty] Accessibility requirement violated: +1000");
        }

        if (group.requiresNearStage() && !ts.getTable().getNearStage()) {
            penalty += 200;
            log.info("[Penalty] Near stage requirement violated: +200");
        }

        String relation = group.getRelation();
        int sameRelationCount = relation != null ? ts.countSameRelation(relation) : 0;
        if (sameRelationCount > 0) {
            int relationBonus = sameRelationCount * 250;
            penalty -= relationBonus;
            log.info("[Penalty] Same relation bonus: -{}", relationBonus);
        }

        int preferredGuestsCount = ts.countPreferredGuests(group);
        if (preferredGuestsCount > 0) {
            int preferBonus = preferredGuestsCount * 150;
            penalty -= preferBonus;
            log.info("[Penalty] Preferred guests bonus: -{}", preferBonus);
        }

        int freeSeatsLeft = ts.getFreeSeats() - group.getTotalSeats();
        int emptySeatsPenalty = freeSeatsLeft * freeSeatsLeft * freeSeatsLeft * 10;
        penalty += emptySeatsPenalty;
        log.info("[Penalty] Empty seats penalty (exponential): +{}", emptySeatsPenalty);

        if (ts.hasConflictWith(group)) {
            penalty += 2000;
            log.info("[Penalty] Conflict detected penalty: +2000");
        }

        log.info(
            "[Penalty] Total penalty for group '{}' at table '{}' = {} (Started at: {})",
            group.getNames(),
            ts.getTable().getTableNumber(),
            penalty,
            initialPenalty
        );
        return penalty;
    }

    // Step 9: Save best found assignment (Algorithm: Optimization)
    /**
     * Persists the final assignment of guests to tables in the database.
     *
     * @param assignment The best assignment of guest groups to tables found during backtracking.
     */
    private void persistAssignment(Map<GuestGroup, SeatingTable> assignment) {
        for (GuestGroup group : assignment.keySet()) {
            SeatingTable table = assignment.get(group);
            for (Guest guest : group.getGuests()) {
                guest.setTable(table);
                if (table != null) {
                    guest.setEvent(table.getEvent());
                }
            }
            guestRepository.saveAll(group.getGuests());
        }
        log.info("[Step 9] 💾 Persisted best assignment to guest records.");
    }

    /**
     * Splits a list of guests into groups that can fit within the maximum table size.
     *
     * @param guests List of guests to split.
     * @param maxSeatsPerTable The maximum number of seats allowed per table.
     * @return A list of guest groups that respect the seating constraints.
     */
    private List<GuestGroup> splitGroupsByMaxSeats(List<Guest> guests, int maxSeatsPerTable) {
        List<GuestGroup> result = new ArrayList<>();
        List<Guest> current = new ArrayList<>();
        int currentSeats = 0;

        for (Guest guest : guests) {
            int seats = guest.getNumberOfSeats();
            if (seats > maxSeatsPerTable) {
                throw new IllegalStateException("Guest " + guest.getLastNameAndFirstName() + " requires more seats than any table!");
            }
            if (currentSeats + seats > maxSeatsPerTable && !current.isEmpty()) {
                result.add(new GuestGroup(new ArrayList<>(current)));
                current.clear();
                currentSeats = 0;
            }
            current.add(guest);
            currentSeats += seats;
        }
        if (!current.isEmpty()) result.add(new GuestGroup(current));
        return result;
    }

    /**
     * Groups guests by their relation and splits them into groups that fit within the maximum table size.
     *
     * @param guests List of all guests.
     * @param tables List of all seating tables.
     * @return List of guest groups prepared for assignment.
     */
    private List<GuestGroup> groupGuestsByRelation(List<Guest> guests, List<SeatingTable> tables) {
        int maxSeatsPerTable = tables.stream().mapToInt(SeatingTable::getMaxSeats).max().orElse(4);
        Map<String, List<Guest>> relationGroups = guests
            .stream()
            .filter(g -> g.getRelation() != null)
            .collect(Collectors.groupingBy(g -> g.getRelation().name()));

        List<GuestGroup> result = new ArrayList<>();
        for (List<Guest> group : relationGroups.values()) {
            result.addAll(splitGroupsByMaxSeats(group, maxSeatsPerTable));
        }
        return result;
    }

    /**
     * Splits large guest groups into smaller groups to ensure they can fit on available tables.
     *
     * @param groups List of guest groups.
     * @param maxSeatsPerTable The maximum number of seats allowed per table.
     * @return Adjusted list of guest groups respecting table size constraints.
     */
    private List<GuestGroup> splitLargeGroupsIfNeeded(List<GuestGroup> groups, int maxSeatsPerTable) {
        List<GuestGroup> adjustedGroups = new ArrayList<>();
        for (GuestGroup guestGroup : groups) {
            adjustedGroups.addAll(splitGroupsByMaxSeats(guestGroup.getGuests(), maxSeatsPerTable));
        }
        return adjustedGroups;
    }

    /**
     * Initializes the state for each seating table to track current assignments and available seats.
     *
     * @param tables List of seating tables.
     * @return Map of table IDs to their respective TableState objects.
     */
    private Map<Long, TableState> initializeTableStates(List<SeatingTable> tables) {
        Map<Long, TableState> states = new HashMap<>();
        for (SeatingTable table : tables) {
            states.put(table.getId(), new TableState(table));
        }
        return states;
    }

    /**
     * Checks if the remaining guest groups can still be assigned to available tables.
     *
     * @param groups List of guest groups.
     * @param assignment Current guest-to-table assignments.
     * @param tableStates Current state of all tables.
     * @return True if there is at least one valid assignment option left for each group, otherwise false.
     */
    private boolean isFeasible(List<GuestGroup> groups, Map<GuestGroup, SeatingTable> assignment, Map<Long, TableState> tableStates) {
        for (GuestGroup group : groups) {
            if (!assignment.containsKey(group)) {
                boolean hasOption = tableStates.values().stream().anyMatch(ts -> ts.canFit(group) && !ts.hasConflictWith(group));
                if (!hasOption) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Selects the next guest group to assign based on the Minimum Remaining Values (MRV) heuristic.
     *
     * @param groups List of all guest groups.
     * @param assignment Current guest-to-table assignments.
     * @param tableStates Current state of all tables.
     * @return The guest group with the fewest assignment options.
     */

    private GuestGroup selectGroupWithFewestOptions(
        List<GuestGroup> groups,
        Map<GuestGroup, SeatingTable> assignment,
        Map<Long, TableState> tableStates
    ) {
        GuestGroup bestGroup = null;
        int minOptions = Integer.MAX_VALUE;

        for (GuestGroup group : groups) {
            if (!assignment.containsKey(group)) {
                long options = tableStates
                    .values()
                    .stream()
                    .filter(ts -> ts.canFit(group))
                    .filter(ts -> !ts.hasConflictWith(group))
                    .count();

                if (options < minOptions) {
                    minOptions = (int) options;
                    bestGroup = group;
                }
            }
        }

        return bestGroup;
    }

    /**
     * Validates the initial setup to ensure that guest requirements can be met
     * with the current available tables.
     *
     * @param guests List of all guests.
     * @param tables List of all seating tables.
     * @param warnings List to collect warning messages if validation fails.
     */

    private void validateSetup(List<Guest> guests, List<SeatingTable> tables, List<String> warnings) {
        long accessibilityGuests = guests.stream().filter(g -> Boolean.TRUE.equals(g.getAccessibility())).count();
        long nearStageGuests = guests.stream().filter(g -> Boolean.TRUE.equals(g.getNearStage())).count();
        long groomGuests = guests.stream().filter(g -> g.getSide() != null && g.getSide().name().equals("GROOM")).count();
        long brideGuests = guests.stream().filter(g -> g.getSide() != null && g.getSide().name().equals("BRIDE")).count();
        long accessibilityTables = tables.stream().filter(t -> Boolean.TRUE.equals(t.getAccessibility())).count();
        long nearStageTables = tables.stream().filter(t -> Boolean.TRUE.equals(t.getNearStage())).count();

        int halfTables = tables.size() / 2;

        if (accessibilityGuests > accessibilityTables) {
            String msg = "⚠️ Not enough accessible tables: needed " + accessibilityGuests + ", available " + accessibilityTables;
            warnings.add(msg);
            log.warn("[Validation] {}", msg);
        }

        if (nearStageGuests > nearStageTables) {
            String msg = "⚠️ Not enough near-stage tables: needed " + nearStageGuests + ", available " + nearStageTables;
            warnings.add(msg);
            log.warn("[Validation] {}", msg);
        }

        if (groomGuests > brideGuests + halfTables || brideGuests > groomGuests + halfTables) {
            String msg = "⚠️ Potential side imbalance: Groom Guests: " + groomGuests + ", Bride Guests: " + brideGuests;
            warnings.add(msg);
            log.warn("[Validation] {}", msg);
        }
    }

    /**
     * Represents a group of guests, typically related by family or preference.
     * Used to manage assignment constraints collectively.
     */

    public record GuestGroup(List<Guest> guests) {
        /**
         * Checks if there is a conflict (avoidance) within the group itself.
         * @return true if any guest in the group should avoid another guest in the same group.
         */
        public boolean hasInternalConflict() {
            for (Guest guest1 : guests) {
                for (Guest guest2 : guests) {
                    if (!guest1.equals(guest2)) {
                        if (guest1.getAvoidGuests().contains(guest2) || guest2.getAvoidGuests().contains(guest1)) {
                            log.warn(
                                "❌ Internal conflict detected within group between guest {} and guest {}",
                                guest1.getId(),
                                guest2.getId()
                            );
                            return true;
                        }
                    }
                }
            }
            return false;
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
     * Represents the current state of a seating table during the assignment process.
     * Tracks assigned groups and the number of used seats.
     */
    public static class TableState {

        private final SeatingTable table;
        private final List<GuestGroup> assignedGroups = new ArrayList<>();
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
            return (int) group
                .getGuests()
                .stream()
                .flatMap(g -> g.getPreferGuests().stream())
                .filter(g -> seatedGuestIds.contains(g.getId()))
                .count();
        }

        public int getFreeSeats() {
            return table.getMaxSeats() - usedSeats;
        }

        public boolean hasConflictWith(GuestGroup group) {
            for (Guest existingGuest : assignedGroups.stream().flatMap(g -> g.getGuests().stream()).toList()) {
                for (Guest newGuest : group.getGuests()) {
                    boolean conflict =
                        existingGuest.getAvoidGuests().stream().anyMatch(guest -> guest.getId().equals(newGuest.getId())) ||
                        newGuest.getAvoidGuests().stream().anyMatch(guest -> guest.getId().equals(existingGuest.getId()));

                    if (conflict) {
                        log.warn(
                            "[AvoidGuests] ❌ Conflict detected: Guest '{}' must avoid Guest '{}'",
                            existingGuest.getLastNameAndFirstName(),
                            newGuest.getLastNameAndFirstName()
                        );
                        return true;
                    }
                }
            }
            return false;
        }

        public SeatingTable getTable() {
            return table;
        }
    }

    /**
     * Logs the current assignment of guest groups to tables for monitoring purposes.
     *
     * @param tableStates Current state of all tables.
     */

    private void printCurrentAssignments(Map<Long, TableState> tableStates) {
        log.info("📋 Current Table Assignments:");
        for (TableState ts : tableStates.values()) {
            String guests = ts.assignedGroups
                .stream()
                .flatMap(g -> g.getGuests().stream())
                .map(Guest::getLastNameAndFirstName)
                .collect(Collectors.joining(", "));
            log.info("Table {} - {} seats used: {}", ts.getTable().getTableNumber(), ts.usedSeats, guests.isEmpty() ? "Empty" : guests);
        }
    }

    /**
     * Logs the initial status of all tables, including their maximum seating capacity.
     *
     * @param tables List of seating tables.
     */

    private void printTablesStatus(List<SeatingTable> tables) {
        log.info("📋 Table Status:");
        for (SeatingTable table : tables) {
            log.info("Table {} - Max Seats: {}", table.getTableNumber(), table.getMaxSeats());
        }
    }

    /**
     * Splits groups that have internal conflicts into single-guest groups.
     */
    private List<GuestGroup> splitConflictingGroups(List<GuestGroup> groups, List<String> warnings) {
        List<GuestGroup> splitGroups = new ArrayList<>();
        for (GuestGroup group : groups) {
            if (group.hasInternalConflict()) {
                String warningMsg = "⚠️ Group split due to internal conflicts: " + group.getNames();
                log.warn(warningMsg);
                warnings.add(warningMsg);
                for (Guest guest : group.getGuests()) {
                    splitGroups.add(new GuestGroup(Collections.singletonList(guest)));
                }
            } else {
                splitGroups.add(group);
            }
        }
        return splitGroups;
    }
}
