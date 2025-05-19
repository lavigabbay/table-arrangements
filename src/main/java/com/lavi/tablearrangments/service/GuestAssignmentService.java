package com.lavi.tablearrangments.service;

import com.lavi.tablearrangments.domain.Guest;
import com.lavi.tablearrangments.domain.GuestGroup;
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

    private static final PenaltyCalculator penaltyCalculator = new PenaltyCalculator();

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

        DomainManager domainManager = new DomainManager(guestGroups, allTables);
        domainManager.applyAC3(); // ניקוי ראשוני של דומיינים
        backtrack(new HashMap<>(), guestGroups, tableStates, bestAssignment, minOpenTables, domainManager);

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
        int[] minOpenTables,
        DomainManager domainManager
    ) {
        // Step 6: Select the group with the fewest options (Algorithm: MRV - Minimum Remaining Values)
        log.debug("[Step 6] ↩️ Backtracking: {} groups assigned so far.", assignment.size());

        if (assignment.size() == groups.size()) {
            long openTables = tableStates.values().stream().filter(ts -> !ts.assignedGroups.isEmpty()).count();
            if (openTables >= minOpenTables[0]) {
                return; // אין טעם להמשיך, לא נשיג תוצאה טובה יותר
            }
            if (openTables < minOpenTables[0]) {
                minOpenTables[0] = (int) openTables;
                bestAssignment.clear();
                bestAssignment.putAll(assignment);
                log.info("[Step 6] 🥇 New best assignment found with {} open tables.", openTables);
            }
            return;
        }

        log.debug("[Step 6] 🎯 Selecting next group to assign using MRV heuristic...");
        GuestGroup nextGroup = selectGroupWithFewestOptions(groups, assignment, tableStates, domainManager);

        if (nextGroup == null || domainManager.getDomain(nextGroup).isEmpty()) {
            log.warn("❌ No possible tables for group '{}'. Skipping this branch...", nextGroup != null ? nextGroup.getNames() : "UNKNOWN");
            return; // אין טעם להמשיך, אין אפשרות השמה לקבוצה זו
        }

        log.info("[Step 6] 🎯 Selected group: {} ({} seats)", nextGroup.getNames(), nextGroup.getTotalSeats());

        // Step 7: Try assigning group to table (Algorithm: Backtracking)
        List<SeatingTable> candidates = new ArrayList<>(domainManager.getDomain(nextGroup));

        candidates = candidates
            .stream()
            .filter(ts -> ts.getMaxSeats() >= nextGroup.getTotalSeats())
            .sorted(Comparator.comparingInt(ts -> calculateDomainReduction(ts, nextGroup, domainManager)))
            .toList();

        if (candidates.isEmpty()) {
            log.warn("❌ No available tables for group '{}'. Backtracking...", nextGroup.getNames());
            return;
        }

        for (SeatingTable table : candidates) {
            TableState ts = tableStates.get(table.getId());
            log.debug("[Step 7] 🪑 Trying to assign group '{}' to table '{}'.", nextGroup.getNames(), ts.getTable().getTableNumber());
            ts.assignGroup(nextGroup);
            assignment.put(nextGroup, ts.getTable());

            assignedSeats.compute(ts.getTable().getId(), (k, v) -> (v == null ? 0 : v) + nextGroup.getTotalSeats());

            domainManager.removeTableFromAllDomains(ts.getTable());

            domainManager.applyAC3(); // Propagation נוסף אחרי כל השמה

            assignedSeats.compute(ts.getTable().getId(), (k, v) -> (v == null ? 0 : v) + nextGroup.getTotalSeats());
            domainManager.removeTableFromAllDomains(ts.getTable());
            domainManager.applyAC3(); // Propagation נוסף אחרי כל השמה

            // Forward Checking: לוודא שיש עדיין אופציות לשאר הקבוצות
            if (!isFeasible(groups, assignment, tableStates)) {
                log.warn("⚠️ Forward Checking failed after assigning group '{}'. Backtracking immediately...", nextGroup.getNames());
                assignment.remove(nextGroup);
                ts.removeGroup(nextGroup);
                assignedSeats.compute(ts.getTable().getId(), (k, v) -> (v == null ? 0 : v) - nextGroup.getTotalSeats());
                continue; // מנסה את השולחן הבא
            }

            printCurrentAssignments(tableStates);

            // Step 8: Check forward feasibility (Algorithm: Forward Checking)
            if (isFeasible(groups, assignment, tableStates)) {
                backtrack(assignment, groups, tableStates, bestAssignment, minOpenTables, domainManager);
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
     * Calculates how much assigning a group to a table will reduce the domain of other groups.
     * Used for LCV (the least Constraining Value) heuristic.
     */
    private int calculateDomainReduction(SeatingTable table, GuestGroup group, DomainManager domainManager) {
        int reduction = 0;
        for (GuestGroup otherGroup : domainManager.getAllGroups()) {
            if (otherGroup.equals(group)) continue;

            Set<SeatingTable> domain = domainManager.getDomain(otherGroup);
            if (domain == null || domain.isEmpty()) continue;

            if (domain.contains(table)) {
                reduction++;
            }
        }

        return reduction; // כמה זה מצמצם את הדומיין של שאר הקבוצות – נעדיף ערכים שמצמצמים פחות
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
            // ✨ Split groups if some guests require accessibility and others don't
            List<Guest> accessibilityGuests = group.stream().filter(g -> Boolean.TRUE.equals(g.getAccessibility())).toList();
            List<Guest> nonAccessibilityGuests = group.stream().filter(g -> !Boolean.TRUE.equals(g.getAccessibility())).toList();

            if (!accessibilityGuests.isEmpty() && !nonAccessibilityGuests.isEmpty()) {
                result.addAll(splitGroupsByMaxSeats(accessibilityGuests, maxSeatsPerTable));
                result.addAll(splitGroupsByMaxSeats(nonAccessibilityGuests, maxSeatsPerTable));
            } else {
                result.addAll(splitGroupsByMaxSeats(group, maxSeatsPerTable));
            }
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
                boolean hasOption = tableStates.values().stream().anyMatch(ts -> ts.canFit(group) && ts.canAssignGroup(group));
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
        Map<Long, TableState> tableStates,
        DomainManager domainManager
    ) {
        GuestGroup bestGroup = null;
        long minOptions = Long.MAX_VALUE;

        for (GuestGroup group : groups) {
            if (!assignment.containsKey(group)) {
                long options = tableStates.values().stream().filter(ts -> ts.canFit(group)).filter(ts -> ts.canAssignGroup(group)).count();

                if (options < minOptions) {
                    minOptions = options;
                    bestGroup = group;
                } else if (options == minOptions && bestGroup != null) {
                    long currentDegree = domainManager.getDomain(group).size();
                    long bestDegree = domainManager.getDomain(bestGroup).size();
                    if (currentDegree > bestDegree) {
                        bestGroup = group;
                    }
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

        /**
         * Checks if the table can accommodate the given guest group based on seat availability
         * and accessibility requirements.
         *
         * @param group The guest group to check.
         * @return True if the table can fit the group, false otherwise.
         */
        public boolean canFit(GuestGroup group) {
            // אם יש מספיק מקומות בשולחן – בדוק האם מתאים לפי נגישות
            if (getFreeSeats() >= group.getTotalSeats()) {
                if (group.requiresAccessibility() && !Boolean.TRUE.equals(table.getAccessibility())) {
                    // יש מקום אבל השולחן לא נגיש – אפשרי, אבל בעדיפות נמוכה יותר (Soft Constraint)
                    log.warn(
                        "[Accessibility] ⚠️ Group '{}' requires accessibility but table '{}' is not accessible.",
                        group.getNames(),
                        table.getTableNumber()
                    );
                }
                return true; // תמיד מאפשר מבחינת כמות מקומות, גם אם לא עומד בנגישות
            }
            return false;
        }

        /**
         * Assigns a guest group to this table and updates the used seats count accordingly.
         *
         * @param group The guest group to assign.
         */
        public void assignGroup(GuestGroup group) {
            assignedGroups.add(group);
            usedSeats += group.getTotalSeats();
        }

        /**
         * Removes a guest group from this table and updates the used seats count accordingly.
         *
         * @param group The guest group to remove.
         */
        public void removeGroup(GuestGroup group) {
            assignedGroups.remove(group);
            usedSeats -= group.getTotalSeats();
        }

        /**
         * Counts the number of guests already seated at this table that have the specified relation.
         *
         * @param relation The relation to count (e.g., FAMILY, FRIEND).
         * @return Number of guests at this table with the specified relation.
         */

        public int countSameRelation(String relation) {
            return (int) assignedGroups
                .stream()
                .flatMap(g -> g.getGuests().stream())
                .filter(g -> g.getRelation() != null && g.getRelation().name().equals(relation))
                .count();
        }

        /**
         * Counts the number of guests already seated at this table that belong to the specified side (e.g., GROOM or BRIDE).
         * This is used to help maintain balance between guests from different sides during the seating assignment process.
         *
         * @param side The side to count guests for (GROOM, BRIDE, or BOTH).
         * @return Number of guests assigned to this table that belong to the specified side.
         */

        public int countSameSide(String side) {
            return (int) assignedGroups
                .stream()
                .flatMap(g -> g.getGuests().stream())
                .filter(g -> g.getSide() != null && g.getSide().name().equals(side))
                .count();
        }

        /**
         * Counts how many guests already seated at this table are preferred by the given guest group.
         * This can be used to prioritize seating arrangements that satisfy 'preferGuests' constraints.
         *
         * @param group The guest group being considered for seating.
         * @return Number of preferred guests already seated at this table.
         */

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

        /**
         * Calculates and returns the number of free seats remaining at this table.
         *
         * @return The number of free seats available.
         */
        public int getFreeSeats() {
            return table.getMaxSeats() - usedSeats;
        }

        /**
         * Determines if the given guest group can be assigned to this table
         * without violating any avoidance constraints (hard constraints).
         *
         * @param group The guest group to check.
         * @return True if no conflicts are found, false otherwise.
         */
        public boolean canAssignGroup(GuestGroup group) {
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
                        return false; // שינוי חשוב: מחזיר false במקרה של קונפליקט
                    }
                }
            }
            return true; // אין קונפליקט, אפשר לשבץ
        }

        /**
         * Retrieves the SeatingTable object associated with this table state.
         *
         * @return The associated SeatingTable entity.
         */
        public SeatingTable getTable() {
            return table;
        }
    }

    /**
     * Logs the current assignment of guest groups to tables, including used seats
     * and the names of assigned guests for each table.
     *
     * @param tableStates Map containing the current state of all tables.
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
     * Splits guest groups that have internal conflicts (avoidance constraints between guests)
     * into separate single-guest groups and records a warning.
     *
     * @param groups The list of guest groups to process.
     * @param warnings The list where warning messages will be added.
     * @return A new list of guest groups after splitting conflicting groups.
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
