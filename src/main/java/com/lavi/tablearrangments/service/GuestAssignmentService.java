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

    public List<String> assignAll() {
        List<String> warnings = new ArrayList<>();
        List<Guest> allGuests = guestRepository.findAllByEventUserIsCurrentUserList();
        List<SeatingTable> allTables = seatingTableRepository.findByUserIsCurrentUser();

        List<GuestGroup> guestGroups = groupGuestsByRelation(allGuests, allTables);
        guestGroups.forEach(g -> log.debug("👥 Group: {} | Seats: {}", g.getNames(), g.getTotalSeats()));

        Map<Long, TableState> tableStates = initializeTableStates(allTables);

        Map<GuestGroup, SeatingTable> bestAssignment = new HashMap<>();
        int[] minOpenTables = { Integer.MAX_VALUE };

        backtrack(new HashMap<>(), guestGroups, tableStates, warnings, bestAssignment, minOpenTables);

        if (bestAssignment.isEmpty()) {
            warnings.add("⚠️ לא ניתן היה לשבץ את כל האורחים לפי האילוצים.");
            log.warn("❌ Guest assignment failed despite available options.");
        }

        persistAssignment(bestAssignment);
        return warnings;
    }

    private void backtrack(
        Map<GuestGroup, SeatingTable> assignment,
        List<GuestGroup> groups,
        Map<Long, TableState> tableStates,
        List<String> warnings,
        Map<GuestGroup, SeatingTable> bestAssignment,
        int[] minOpenTables
    ) {
        if (assignment.size() == groups.size()) {
            long openTables = tableStates.values().stream().filter(ts -> !ts.assignedGroups.isEmpty()).count();
            if (openTables < minOpenTables[0]) {
                minOpenTables[0] = (int) openTables;
                bestAssignment.clear();
                bestAssignment.putAll(assignment);
                log.info("✅ New best assignment with {} open tables", openTables);
            }
            return;
        }

        for (GuestGroup group : groups) {
            if (!assignment.containsKey(group)) {
                List<TableState> candidates = tableStates
                    .values()
                    .stream()
                    .filter(ts -> ts.canFit(group))
                    .sorted(
                        Comparator.comparingInt((TableState ts) -> {
                            int penalty = 0;

                            // נגישות – הכי חשוב
                            if (group.requiresAccessibility() && !ts.getTable().getAccessibility()) penalty += 1000;

                            // קרבה לבמה – פחות חשוב
                            if (group.requiresNearStage() && !ts.getTable().getNearStage()) penalty += 200;

                            // קשרי משפחה – תגמול
                            String relation = group.getRelation();
                            int sameRelationCount = relation != null ? ts.countSameRelation(relation) : 0;
                            log.debug(
                                "🔎 Evaluating group: {} | Relation: {} | SameRelationCount: {}",
                                group.getNames(),
                                relation,
                                sameRelationCount
                            );

                            penalty -= sameRelationCount * 0;
                            penalty += ts.getFreeSeats() - group.getTotalSeats();

                            return penalty;
                        })
                    )
                    .collect(Collectors.toList());

                boolean groupAssigned = false;

                for (TableState ts : candidates) {
                    SeatingTable table = ts.getTable();
                    ts.assignGroup(group);
                    assignment.put(group, table);

                    backtrack(assignment, groups, tableStates, warnings, bestAssignment, minOpenTables);

                    assignment.remove(group);
                    ts.removeGroup(group);
                    groupAssigned = true;
                }

                if (!groupAssigned) {
                    log.warn("⚠️ Could not assign group: {}", group.getNames());
                }

                return; // סיום ענף אחרי ניסיון שיבוץ אחד
            }
        }
    }

    private GuestGroup selectUnassignedGroup(
        List<GuestGroup> groups,
        Map<GuestGroup, SeatingTable> assignment,
        Map<Long, TableState> tableStates
    ) {
        GuestGroup selected = null;
        int minOptions = Integer.MAX_VALUE;

        for (GuestGroup group : groups) {
            if (!assignment.containsKey(group)) {
                List<SeatingTable> validTables = new ArrayList<>();
                List<String> rejectedReasons = new ArrayList<>();

                for (TableState tableState : tableStates.values()) {
                    boolean canAssign = true;

                    if (!tableState.canFit(group)) {
                        rejectedReasons.add("Table " + tableState.getTable().getTableNumber() + ": insufficient space");
                        canAssign = false;
                    }

                    if (canAssign && tableState.hasConflictWith(group)) {
                        rejectedReasons.add("Table " + tableState.getTable().getTableNumber() + ": conflict with existing group");
                        canAssign = false;
                    }

                    if (canAssign) {
                        validTables.add(tableState.getTable());
                    }
                }

                log.debug(
                    "🧠 Group: {} | Valid tables: {} | Rejected: {}",
                    group.getNames(),
                    validTables.stream().map(SeatingTable::getTableNumber).toList(),
                    rejectedReasons
                );

                if (!validTables.isEmpty() && validTables.size() < minOptions) {
                    minOptions = validTables.size();
                    selected = group;
                }
            }
        }

        return selected;
    }

    private List<SeatingTable> orderDomainByHeuristics(GuestGroup group, Map<Long, TableState> tableStates) {
        return tableStates
            .values()
            .stream()
            .filter(ts -> ts.canFit(group) && !ts.hasConflictWith(group))
            .sorted(
                Comparator.comparingInt(ts -> {
                    int penalty = 0;
                    boolean needsAccessibility = group.getGuests().stream().anyMatch(Guest::getAccessibility);
                    if (needsAccessibility && !ts.getTable().getAccessibility()) penalty += 100;
                    boolean wantsStage = group.getGuests().stream().anyMatch(Guest::getNearStage);
                    if (wantsStage && !ts.getTable().getNearStage()) penalty += 50;
                    if (!ts.prefersGroup(group)) penalty += 30;
                    penalty += ts.getFreeSeats() - group.getTotalSeats();
                    return penalty;
                })
            )
            .map(TableState::getTable)
            .collect(Collectors.toList());
    }

    private boolean isConsistent(
        GuestGroup group,
        SeatingTable table,
        Map<GuestGroup, SeatingTable> assignment,
        Map<Long, TableState> tableStates
    ) {
        TableState state = tableStates.get(table.getId());
        if (!state.canFit(group)) return false;
        if (state.hasConflictWith(group)) return false;
        boolean needsAccessibility = group.getGuests().stream().anyMatch(Guest::getAccessibility);
        if (needsAccessibility && !table.getAccessibility()) return false;
        return true;
    }

    private void persistAssignment(Map<GuestGroup, SeatingTable> assignment) {
        for (Map.Entry<GuestGroup, SeatingTable> entry : assignment.entrySet()) {
            GuestGroup group = entry.getKey();
            SeatingTable table = entry.getValue();
            for (Guest guest : group.getGuests()) {
                guest.setTable(table);
                guest.setEvent(table.getEvent());
            }
            guestRepository.saveAll(group.getGuests());
        }
    }

    private List<GuestGroup> groupGuestsByRelation(List<Guest> guests, List<SeatingTable> tables) {
        int maxSeatsPerTable = tables.stream().mapToInt(SeatingTable::getMaxSeats).max().orElse(4); // ברירת מחדל למקרה שאין שולחנות

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

            if (!current.isEmpty()) {
                result.add(new GuestGroup(current));
            }
        }

        return result;
    }

    private Map<Long, TableState> initializeTableStates(List<SeatingTable> tables) {
        Map<Long, TableState> states = new HashMap<>();
        for (SeatingTable table : tables) {
            states.put(table.getId(), new TableState(table));
        }
        return states;
    }

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

        public boolean hasConflictWith(GuestGroup other) {
            for (Guest g1 : guests) {
                for (Guest g2 : other.guests) {
                    if (g1.getAvoidGuests().contains(g2.getId()) || g2.getAvoidGuests().contains(g1.getId())) return true;
                }
            }
            return false;
        }

        public boolean prefersGroup(GuestGroup other) {
            for (Guest g1 : guests) {
                for (Guest g2 : other.guests) {
                    if (g1.getPreferGuests().contains(g2.getId())) return true;
                }
            }
            return false;
        }

        public String getNames() {
            return guests.stream().map(Guest::getLastNameAndFirstName).collect(Collectors.joining(", "));
        }

        public boolean requiresAccessibility() {
            return guests.stream().anyMatch(g -> Boolean.TRUE.equals(g.getAccessibility()));
        }

        // בתוך GuestGroup
        public String getRelation() {
            return guests.isEmpty() ? null : guests.get(0).getRelation().name();
        }

        public boolean requiresNearStage() {
            return guests.stream().anyMatch(g -> Boolean.TRUE.equals(g.getNearStage()));
        }
    }

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

        public int countPreferredGuests(GuestGroup group) {
            return (int) group
                .getGuests()
                .stream()
                .flatMap(g -> g.getPreferGuests().stream())
                .filter(preferredId ->
                    assignedGroups
                        .stream()
                        .flatMap(ag -> ag.getGuests().stream())
                        .map(Guest::getId)
                        .collect(Collectors.toSet())
                        .contains(preferredId)
                )
                .count();
        }

        public int countAvoidedGuests(GuestGroup group) {
            return (int) group
                .getGuests()
                .stream()
                .flatMap(g -> g.getAvoidGuests().stream())
                .filter(avoidId ->
                    assignedGroups
                        .stream()
                        .flatMap(ag -> ag.getGuests().stream())
                        .map(Guest::getId)
                        .collect(Collectors.toSet())
                        .contains(avoidId)
                )
                .count();
        }

        // בתוך TableState
        public int countSameRelation(String relation) {
            return (int) assignedGroups
                .stream()
                .flatMap(g -> g.getGuests().stream())
                .filter(g -> g.getRelation().name().equals(relation))
                .count();
        }

        public int getFreeSeats() {
            return table.getMaxSeats() - usedSeats;
        }

        public boolean hasConflictWith(GuestGroup group) {
            return assignedGroups.stream().anyMatch(existing -> existing.hasConflictWith(group));
        }

        public boolean isRelationCompatible(String relation) {
            return assignedGroups.stream().flatMap(g -> g.getGuests().stream()).allMatch(g -> g.getRelation().name().equals(relation));
        }

        public boolean prefersGroup(GuestGroup group) {
            return assignedGroups.stream().anyMatch(existing -> existing.prefersGroup(group));
        }

        public SeatingTable getTable() {
            return table;
        }
    }
}
