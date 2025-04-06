package com.lavi.tablearrangments.service;

import com.lavi.tablearrangments.domain.Guest;
import com.lavi.tablearrangments.domain.SeatingTable;
import com.lavi.tablearrangments.repository.GuestRepository;
import com.lavi.tablearrangments.repository.SeatingTableRepository;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GuestAssignmentService {

    private final GuestRepository guestRepository;
    private final SeatingTableRepository seatingTableRepository;

    public GuestAssignmentService(GuestRepository guestRepository, SeatingTableRepository seatingTableRepository) {
        this.guestRepository = guestRepository;
        this.seatingTableRepository = seatingTableRepository;
    }

    public void assignAll() {
        List<Guest> guests = guestRepository.findAllByEventUserIsCurrentUserList();
        List<SeatingTable> tables = seatingTableRepository.findByUserIsCurrentUser();

        Map<Long, Integer> tableCapacities = new HashMap<>();
        for (SeatingTable t : tables) {
            tableCapacities.put(t.getId(), t.getMaxSeats() != null ? t.getMaxSeats() : 10);
        }

        int[][] costMatrix = new int[guests.size()][tables.size()];

        for (int i = 0; i < guests.size(); i++) {
            Guest guest = guests.get(i);
            for (int j = 0; j < tables.size(); j++) {
                SeatingTable table = tables.get(j);

                if (
                    (tableCapacities.get(table.getId()) < guest.getNumberOfSeats()) ||
                    (guest.getAccessibility() != null && guest.getAccessibility() && !Boolean.TRUE.equals(table.getAccessibility()))
                ) {
                    costMatrix[i][j] = Integer.MAX_VALUE / 2;
                    continue;
                }

                int cost = 0;
                String side = table.getTableNumber() <= 10 ? "GROOM" : "BRIDE";

                if (guest.getSide() != null && !guest.getSide().equals(side)) {
                    cost += 5;
                }
                if (guest.getNearStage() != null && guest.getNearStage() && (table.getNearStage() == null || !table.getNearStage())) {
                    cost += 10;
                }

                for (Guest other : guests) {
                    if (other.getId().equals(guest.getId())) continue;

                    if (guest.getPreferGuests() != null && guest.getPreferGuests().contains(other)) {
                        cost -= 30; // חיזוק קשרים חיוביים
                    }
                    if (guest.getAvoidGuests() != null && guest.getAvoidGuests().contains(other)) {
                        cost += 1000; // הרחקת אורחים שלא מסתדרים
                    }

                    if (guest.getRelation() != null && other.getRelation() != null && !guest.getRelation().equals(other.getRelation())) {
                        cost += 5;
                    }
                }

                costMatrix[i][j] = cost;
            }
        }

        int[] assignment = hungarianAlgorithm(costMatrix);

        for (int i = 0; i < guests.size(); i++) {
            int tableIndex = assignment[i];
            if (tableIndex >= 0 && tableIndex < tables.size()) {
                SeatingTable table = tables.get(tableIndex);
                if (tableCapacities.get(table.getId()) >= guests.get(i).getNumberOfSeats()) {
                    guests.get(i).setTable(table);
                    tableCapacities.put(table.getId(), tableCapacities.get(table.getId()) - guests.get(i).getNumberOfSeats());
                }
            }
        }

        guestRepository.saveAll(guests);
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
