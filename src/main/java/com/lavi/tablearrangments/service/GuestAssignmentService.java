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

    private static class SeatPosition {

        Long tableId;
        int seatIndex;

        SeatPosition(Long tableId, int seatIndex) {
            this.tableId = tableId;
            this.seatIndex = seatIndex;
        }
    }

    public void assignAll() {
        List<Guest> guests = guestRepository.findAllByEventUserIsCurrentUserList();
        List<SeatingTable> tables = seatingTableRepository.findByUserIsCurrentUser();

        List<SeatPosition> seatMap = new ArrayList<>();
        Map<Long, SeatingTable> tableMap = new HashMap<>();
        tables.forEach(t -> tableMap.put(t.getId(), t));

        for (SeatingTable table : tables) {
            int seats = table.getMaxSeats() != null ? table.getMaxSeats() : 10;
            for (int i = 0; i < seats; i++) {
                seatMap.add(new SeatPosition(table.getId(), i));
            }
        }

        if (guests.size() > seatMap.size()) {
            throw new IllegalStateException("❌ אין מספיק כיסאות עבור כל האורחים");
        }

        int[][] costMatrix = new int[guests.size()][seatMap.size()];

        for (int i = 0; i < guests.size(); i++) {
            Guest guest = guests.get(i);
            for (int j = 0; j < seatMap.size(); j++) {
                SeatPosition seat = seatMap.get(j);
                SeatingTable table = tableMap.get(seat.tableId);
                List<Guest> simulated = List.of(guest);
                costMatrix[i][j] = calculateCost(guest, table, guests, simulated);
            }
        }

        int[] assignment = hungarianAlgorithm(costMatrix);
        Map<Long, Integer> tableCapacities = new HashMap<>();
        for (SeatingTable table : tables) {
            tableCapacities.put(table.getId(), table.getMaxSeats() != null ? table.getMaxSeats() : 10);
        }

        for (int i = 0; i < guests.size(); i++) {
            int seatIndex = assignment[i];
            if (seatIndex >= 0 && seatIndex < seatMap.size()) {
                SeatPosition seat = seatMap.get(seatIndex);
                SeatingTable table = tableMap.get(seat.tableId);
                int seatsLeft = tableCapacities.get(table.getId());
                if (seatsLeft > 0) {
                    guests.get(i).setTable(table);
                    tableCapacities.put(table.getId(), seatsLeft - 1);
                    log.info(
                        "✅ {} שובץ לשולחן {} (נותרו {} מקומות)",
                        guests.get(i).getLastNameAndFirstName(),
                        table.getTableNumber(),
                        seatsLeft - 1
                    );
                }
            }
        }

        guestRepository.saveAll(guests);
    }

    private int calculateCost(Guest guest, SeatingTable table, List<Guest> allGuests, List<Guest> currentGuests) {
        int numberOfSeats = guest.getNumberOfSeats() != null ? guest.getNumberOfSeats() : 1;

        if ((Boolean.TRUE.equals(guest.getAccessibility()) && !Boolean.TRUE.equals(table.getAccessibility()))) {
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
        }

        if (hasRelation) cost -= 50;
        if (hasPrefer) cost -= 60;

        return cost;
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
