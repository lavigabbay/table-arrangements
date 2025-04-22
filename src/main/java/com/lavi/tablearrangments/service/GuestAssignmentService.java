package com.lavi.tablearrangments.service;

import com.lavi.tablearrangments.domain.Guest;
import com.lavi.tablearrangments.domain.SeatingTable;
import com.lavi.tablearrangments.repository.GuestRepository;
import com.lavi.tablearrangments.repository.SeatingTableRepository;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for assigning guests to seating tables using an optimization algorithm
 * based on various constraints such as accessibility, side preference, stage proximity, relations, and guest preferences.
 */
@Service
@Transactional
public class GuestAssignmentService {

    private final GuestRepository guestRepository;
    private final SeatingTableRepository seatingTableRepository;

    private static final Logger log = LoggerFactory.getLogger(GuestAssignmentService.class);

    public GuestAssignmentService(GuestRepository guestRepository, SeatingTableRepository seatingTableRepository) {
        this.guestRepository = guestRepository;
        this.seatingTableRepository = seatingTableRepository;
    }

    /**
     * Represents a block of consecutive seats available at a given table.
     */
    private static class SeatBlock {

        Long tableId;
        int startIndex;
        int length;

        SeatBlock(Long tableId, int startIndex, int length) {
            this.tableId = tableId;
            this.startIndex = startIndex;
            this.length = length;
        }
    }

    /**
     * Assigns all guests of the current user to available tables while respecting constraints.
     *
     * @return list of warning messages about guests who could not be assigned.
     */
    public List<String> assignAll() {
        List<String> unassignedMessages = new ArrayList<>();
        List<Guest> guests = guestRepository.findAllByEventUserIsCurrentUserList();
        List<SeatingTable> tables = seatingTableRepository.findByUserIsCurrentUser();

        int n = guests.size();
        if (n == 0 || tables.isEmpty()) return unassignedMessages;

        guests.forEach(g -> g.setTable(null));

        Map<Long, SeatingTable> tableMap = new HashMap<>();
        for (SeatingTable table : tables) {
            tableMap.put(table.getId(), table);
        }

        Map<Long, Set<Integer>> usedSeatsPerTable = new HashMap<>();
        for (SeatingTable table : tables) {
            usedSeatsPerTable.put(table.getId(), new HashSet<>());
        }

        List<List<SeatBlock>> guestSeatBlockOptions = new ArrayList<>();
        for (Guest guest : guests) {
            int neededSeats = Optional.ofNullable(guest.getNumberOfSeats()).orElse(1);
            List<SeatBlock> validBlocks = new ArrayList<>();
            for (SeatingTable table : tables) {
                int max = table.getMaxSeats();
                for (int i = 0; i <= max - neededSeats; i++) {
                    validBlocks.add(new SeatBlock(table.getId(), i, neededSeats));
                }
            }
            guestSeatBlockOptions.add(validBlocks);
        }

        int maxBlocks = guestSeatBlockOptions.stream().mapToInt(List::size).max().orElse(0);
        int[][] costMatrix = new int[n][maxBlocks];
        int[] assignment = new int[n];
        Arrays.fill(assignment, -1);

        for (int i = 0; i < n; i++) {
            List<SeatBlock> blocks = guestSeatBlockOptions.get(i);
            for (int j = 0; j < blocks.size(); j++) {
                costMatrix[i][j] = calculateCost(guests.get(i), blocks.get(j), guests, tableMap, assignment, i);
            }
            for (int j = blocks.size(); j < maxBlocks; j++) {
                costMatrix[i][j] = Integer.MAX_VALUE / 2;
            }
        }

        int[] result = hungarianAlgorithm(costMatrix);
        Map<Long, Integer> tableCapacities = new HashMap<>();
        for (SeatingTable table : tables) {
            tableCapacities.put(table.getId(), table.getMaxSeats());
        }

        for (int i = 0; i < n; i++) {
            int seatIndex = result[i];
            List<SeatBlock> blocks = guestSeatBlockOptions.get(i);
            if (seatIndex >= 0 && seatIndex < blocks.size()) {
                SeatBlock block = blocks.get(seatIndex);
                SeatingTable table = tableMap.get(block.tableId);
                int neededSeats = Optional.ofNullable(guests.get(i).getNumberOfSeats()).orElse(1);
                int remaining = tableCapacities.getOrDefault(table.getId(), 0);

                boolean blockFree = true;
                for (int k = 0; k < neededSeats; k++) {
                    if (usedSeatsPerTable.get(table.getId()).contains(block.startIndex + k)) {
                        blockFree = false;
                        break;
                    }
                }

                if (remaining >= neededSeats && blockFree) {
                    guests.get(i).setTable(table);
                    tableCapacities.put(table.getId(), remaining - neededSeats);
                    for (int k = 0; k < neededSeats; k++) {
                        usedSeatsPerTable.get(table.getId()).add(block.startIndex + k);
                    }
                } else {
                    String msg = String.format(
                        "\u26A0\uFE0F Guest '%s' not assigned due to insufficient space: %d seats needed",
                        guests.get(i).getLastNameAndFirstName(),
                        neededSeats
                    );
                    log.warn(msg);
                    unassignedMessages.add(msg);
                }
            }
        }

        guestRepository.saveAll(guests);
        return unassignedMessages;
    }

    /**
     * Calculates the cost of assigning a guest to a specific seat block.
     */
    private int calculateCost(
        Guest guest,
        SeatBlock block,
        List<Guest> guests,
        Map<Long, SeatingTable> tableMap,
        int[] assignment,
        int guestIndex
    ) {
        SeatingTable targetTable = tableMap.get(block.tableId);

        List<Guest> simulated = new ArrayList<>();
        for (int k = 0; k < guestIndex; k++) {
            if (assignment[k] != -1 && guests.get(k).getTable() != null && guests.get(k).getTable().getId().equals(block.tableId)) {
                simulated.add(guests.get(k));
            }
        }
        simulated.add(guest);

        int cost = 0;
        boolean hasRelation = false;
        boolean hasPrefer = false;

        for (Guest other : simulated) {
            if (!guest.equals(other)) {
                int preferCost = costForPreference(guest, other);
                if (preferCost < 0) hasPrefer = true;
                cost += preferCost;

                int relationCost = costForRelation(guest, other);
                if (relationCost < 0) hasRelation = true;
                cost += relationCost;

                cost += costForSide(guest, other, targetTable);
            }
        }

        if (hasRelation) cost -= 50;
        if (hasPrefer) cost -= 60;

        cost += costForNearStage(guest, targetTable);
        cost += costForAccessibility(guest, targetTable);
        cost += costForDensity(targetTable, simulated.size());

        return cost;
    }

    /**
     * Returns a cost based on guest preference or avoidance of another guest.
     */
    private int costForPreference(Guest guest, Guest other) {
        int cost = 0;
        if (guest.getPreferGuests() != null && guest.getPreferGuests().contains(other)) cost -= 1000;
        if (other.getPreferGuests() != null && other.getPreferGuests().contains(guest)) cost -= 500;
        if (guest.getAvoidGuests() != null && guest.getAvoidGuests().contains(other)) cost += 5000;
        if (other.getAvoidGuests() != null && other.getAvoidGuests().contains(guest)) cost += 2500;
        return cost;
    }

    /**
     * Returns a cost based on whether the guest and the other guest share a relation type.
     */
    private int costForRelation(Guest guest, Guest other) {
        return guest.getRelation() == other.getRelation() ? -300 : 100;
    }

    /**
     * Returns a cost based on whether the guest belongs to the correct side (GROOM or BRIDE).
     */
    private int costForSide(Guest guest, Guest other, SeatingTable table) {
        String side = table.getTableNumber() <= 10 ? "GROOM" : "BRIDE";
        return (guest.getSide() != null && !guest.getSide().equals(side)) ? 100 : 0;
    }

    /**
     * Returns a cost if a guest requiring accessibility is not seated at an accessible table.
     */
    private int costForAccessibility(Guest guest, SeatingTable table) {
        return guest.getAccessibility() && !table.getAccessibility() ? Integer.MAX_VALUE / 2 : 0;
    }

    /**
     * Returns a cost if a guest prefers or does not prefer proximity to stage.
     */
    private int costForNearStage(Guest guest, SeatingTable table) {
        return (guest.getNearStage() != null && !guest.getNearStage().equals(table.getNearStage())) ? 500 : 0;
    }

    /**
     * Returns a cost based on how densely the table is being filled.
     */
    private int costForDensity(SeatingTable table, int guestsAtTable) {
        int max = table.getMaxSeats();
        if (guestsAtTable == max) return -100;
        if (guestsAtTable >= max - 1) return -50;
        return 0;
    }

    /**
     * Solves the assignment problem using the Hungarian algorithm.
     *
     * @param cost the cost matrix
     * @return an array of assignments (guest index → seat block index)
     */
    private int[] hungarianAlgorithm(int[][] cost) {
        int n = cost.length, m = cost[0].length;
        int[] u = new int[n + 1], v = new int[m + 1], p = new int[m + 1], way = new int[m + 1];

        for (int i = 1; i <= n; i++) {
            p[0] = i;
            int j0 = 0;
            int[] minv = new int[m + 1];
            boolean[] used = new boolean[m + 1];
            Arrays.fill(minv, Integer.MAX_VALUE);

            while (true) {
                used[j0] = true;
                int i0 = p[j0], delta = Integer.MAX_VALUE, j1 = -1;
                for (int j = 1; j <= m; j++) {
                    if (!used[j]) {
                        int cur = cost[i0 - 1][j - 1] - u[i0] - v[j];
                        if (cur < minv[j]) {
                            minv[j] = cur;
                            way[j] = j0;
                        }
                        if (minv[j] < delta) {
                            delta = minv[j];
                            j1 = j;
                        }
                    }
                }
                for (int j = 0; j <= m; j++) {
                    if (used[j]) {
                        u[p[j]] += delta;
                        v[j] -= delta;
                    } else {
                        minv[j] -= delta;
                    }
                }
                j0 = j1;
                if (p[j0] == 0) break;
            }

            do {
                int j1 = way[j0];
                p[j0] = p[j1];
                j0 = j1;
            } while (j0 != 0);
        }

        int[] result = new int[n];
        Arrays.fill(result, -1);
        for (int j = 1; j <= m; j++) {
            if (p[j] > 0) {
                result[p[j] - 1] = j - 1;
            }
        }
        return result;
    }
}
