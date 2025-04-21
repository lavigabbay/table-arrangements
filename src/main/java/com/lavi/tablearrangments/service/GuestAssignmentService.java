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

    public void assignAll() {
        List<Guest> guests = guestRepository.findAllByEventUserIsCurrentUserList();
        List<SeatingTable> tables = seatingTableRepository.findByUserIsCurrentUser();

        int totalAvailableSeats = tables.stream().mapToInt(t -> t.getMaxSeats() != null ? t.getMaxSeats() : 10).sum();
        int totalNeededSeats = guests.stream().mapToInt(g -> g.getNumberOfSeats() != null ? g.getNumberOfSeats() : 1).sum();

        if (totalNeededSeats > totalAvailableSeats) {
            log.error("❌ אין מספיק מקומות באולם. נדרשים {}, קיימים {}", totalNeededSeats, totalAvailableSeats);
            throw new IllegalStateException(
                "❌ אין מספיק מקומות באולם עבור כל האורחים. נדרשים " + totalNeededSeats + ", קיימים רק " + totalAvailableSeats
            );
        }

        Map<Long, Integer> tableCapacities = initializeTableCapacities(tables);
        Map<Long, List<Guest>> guestsPerTable = new HashMap<>();

        int[][] costMatrix = buildCostMatrix(guests, tables, tableCapacities, guestsPerTable);
        int[] assignment = hungarianAlgorithm(costMatrix);
        assignGuestsToTables(guests, tables, assignment, tableCapacities, guestsPerTable);

        guestRepository.saveAll(guests);
    }

    private Map<Long, Integer> initializeTableCapacities(List<SeatingTable> tables) {
        Map<Long, Integer> capacities = new HashMap<>();
        for (SeatingTable table : tables) {
            capacities.put(table.getId(), table.getMaxSeats() != null ? table.getMaxSeats() : 10);
        }
        return capacities;
    }

    private int[][] buildCostMatrix(
        List<Guest> guests,
        List<SeatingTable> tables,
        Map<Long, Integer> capacities,
        Map<Long, List<Guest>> guestsPerTable
    ) {
        int[][] costMatrix = new int[guests.size()][tables.size()];

        for (int i = 0; i < guests.size(); i++) {
            for (int j = 0; j < tables.size(); j++) {
                List<Guest> simulatedTableGuests = new ArrayList<>(guestsPerTable.getOrDefault(tables.get(j).getId(), new ArrayList<>()));
                simulatedTableGuests.add(guests.get(i));
                int cost = calculateCost(guests.get(i), tables.get(j), guests, capacities, simulatedTableGuests);
                costMatrix[i][j] = cost;
                log.info("אורח {} -> שולחן {}: עלות = {}", guests.get(i).getLastNameAndFirstName(), tables.get(j).getTableNumber(), cost);
            }
        }

        return costMatrix;
    }

    private int calculateCost(
        Guest guest,
        SeatingTable table,
        List<Guest> allGuests,
        Map<Long, Integer> capacities,
        List<Guest> currentGuests
    ) {
        int tableCapacity = capacities.get(table.getId());
        int numberOfSeats = guest.getNumberOfSeats() != null ? guest.getNumberOfSeats() : 1;

        if (
            tableCapacity < numberOfSeats ||
            (Boolean.TRUE.equals(guest.getAccessibility()) && !Boolean.TRUE.equals(table.getAccessibility()))
        ) {
            return Integer.MAX_VALUE / 2;
        }

        int cost = 0;

        String side = table.getTableNumber() <= 10 ? "GROOM" : "BRIDE";
        if (guest.getSide() != null && !guest.getSide().equals(side)) {
            cost += 5;
        }

        if (Boolean.TRUE.equals(guest.getNearStage()) && !Boolean.TRUE.equals(table.getNearStage())) {
            cost += 10;
        }

        boolean hasRelation = false;
        boolean hasPrefer = false;

        log.debug(
            "בודק עלות לאורח {} מול שולחן {} (יושבים כרגע {} אורחים)",
            guest.getLastNameAndFirstName(),
            table.getTableNumber(),
            currentGuests.size()
        );

        if (currentGuests.isEmpty()) {
            cost += 150;
        }

        for (Guest current : currentGuests) {
            if (current.getId() != null && current.getId().equals(guest.getId())) continue;

            if (guest.getAvoidGuests() != null && guest.getAvoidGuests().contains(current)) {
                cost += 1000;
            }
            if (guest.getPreferGuests() != null && guest.getPreferGuests().contains(current)) {
                cost -= 120;
                hasPrefer = true;
            }
            if (guest.getRelation() != null && current.getRelation() != null && guest.getRelation().equals(current.getRelation())) {
                cost -= 100;
                hasRelation = true;
            } else {
                cost -= 8;
            }

            if (guest.getSide() != null && guest.getSide().equals(current.getSide())) {
                if (Boolean.TRUE.equals(guest.getNearStage()) == Boolean.TRUE.equals(current.getNearStage())) {
                    cost -= 3;
                }
            }
        }

        if (currentGuests.size() == 1 && !hasRelation && !hasPrefer) {
            cost += 20;
        } else if (!hasRelation && !hasPrefer) {
            cost -= 6;
        } else if (hasRelation && hasPrefer) {
            cost -= 30;
        } else if (currentGuests.size() > 1 && (hasRelation || hasPrefer)) {
            cost -= 25;
        }

        if (guest.getRelation() != null && currentGuests.stream().anyMatch(g -> guest.getRelation().equals(g.getRelation()))) {
            cost -= 60;
        }

        long sameGroupUnassigned = allGuests
            .stream()
            .filter(g -> !g.equals(guest))
            .filter(g -> g.getTable() == null)
            .filter(g -> g.getRelation() != null && g.getRelation().equals(guest.getRelation()))
            .count();

        if (sameGroupUnassigned > 0 && sameGroupUnassigned <= 3) {
            cost -= 30;
        }

        int remainingSeats = capacities.get(table.getId());
        int leftoverAfter = remainingSeats - numberOfSeats;
        int alreadySitting = table.getMaxSeats() - remainingSeats;

        if (leftoverAfter < 0) {
            log.warn(
                "שולחן {}: אין מספיק מקום לאורח {} (צריך {}, נשאר {})",
                table.getTableNumber(),
                guest.getLastNameAndFirstName(),
                numberOfSeats,
                remainingSeats
            );
            return Integer.MAX_VALUE / 2;
        }

        if (leftoverAfter == 0) {
            cost -= 60;
        } else if (remainingSeats < table.getMaxSeats() && leftoverAfter > 0) {
            cost -= 30;
        } else {
            cost += leftoverAfter * 4;
        }

        cost -= alreadySitting * 10;

        log.debug(
            "עלות סופית + כיסאות ריקים לאורח {} מול שולחן {}: {} (נותרו {} כיסאות)",
            guest.getLastNameAndFirstName(),
            table.getTableNumber(),
            cost,
            leftoverAfter
        );
        log.info("🧾 עלות כוללת: {} -> שולחן {} = {}", guest.getLastNameAndFirstName(), table.getTableNumber(), cost);

        return cost;
    }

    private void assignGuestsToTables(
        List<Guest> guests,
        List<SeatingTable> tables,
        int[] assignment,
        Map<Long, Integer> capacities,
        Map<Long, List<Guest>> guestsPerTable
    ) {
        for (int i = 0; i < guests.size(); i++) {
            int tableIndex = assignment[i];
            if (tableIndex >= 0 && tableIndex < tables.size()) {
                SeatingTable table = tables.get(tableIndex);
                int seatsNeeded = guests.get(i).getNumberOfSeats() != null ? guests.get(i).getNumberOfSeats() : 1;

                if (capacities.get(table.getId()) >= seatsNeeded) {
                    guests.get(i).setTable(table);
                    capacities.put(table.getId(), capacities.get(table.getId()) - seatsNeeded);
                    guestsPerTable.computeIfAbsent(table.getId(), k -> new ArrayList<>()).add(guests.get(i));
                    log.info(
                        "✅ שיבוץ: {} -> שולחן {} (נותרו {} מקומות)",
                        guests.get(i).getLastNameAndFirstName(),
                        table.getTableNumber(),
                        capacities.get(table.getId())
                    );
                } else {
                    log.warn("❌ אין מקום: {} לא שובץ לשולחן {}", guests.get(i).getLastNameAndFirstName(), table.getTableNumber());
                }
            }
        }
    }

    private int[] hungarianAlgorithm(int[][] cost) {
        int n = cost.length;
        int m = cost[0].length;
        int[] u = new int[n + 1];
        int[] v = new int[m + 1];
        int[] p = new int[m + 1];
        int[] way = new int[m + 1];

        for (int i = 1; i <= n; i++) {
            p[0] = i;
            int j0 = 0;
            int[] minv = new int[m + 1];
            boolean[] used = new boolean[m + 1];
            Arrays.fill(minv, Integer.MAX_VALUE);

            do {
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
            } while (p[j0] != 0);

            do {
                int j1 = way[j0];
                p[j0] = p[j1];
                j0 = j1;
            } while (j0 != 0);
        }

        int[] result = new int[n];
        for (int j = 1; j <= m; j++) {
            if (p[j] > 0) {
                result[p[j] - 1] = j - 1;
            }
        }

        return result;
    }
}
