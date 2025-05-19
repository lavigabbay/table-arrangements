package com.lavi.tablearrangments.service;

import com.lavi.tablearrangments.domain.GuestGroup;
import com.lavi.tablearrangments.domain.SeatingTable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages domains (possible seating tables) for each guest group.
 * Includes basic AC-3 algorithm to enforce arc consistency.
 */
public class DomainManager {

    private final Map<GuestGroup, Set<SeatingTable>> domains = new HashMap<>();

    /**
     * Initializes the domains for all guest groups based on available tables.
     */
    public DomainManager(List<GuestGroup> guestGroups, List<SeatingTable> tables) {
        for (GuestGroup group : guestGroups) {
            Set<SeatingTable> possibleTables = tables
                .stream()
                .filter(table -> table.getMaxSeats() >= group.getTotalSeats())
                .collect(Collectors.toSet());
            domains.put(group, possibleTables);
        }
    }

    /**
     * Applies basic AC-3 algorithm to reduce domains after an assignment.
     */
    public void applyAC3() {
        Queue<Map.Entry<GuestGroup, GuestGroup>> arcs = new LinkedList<>();

        // Initialize arcs (all pairs of groups)
        for (GuestGroup g1 : domains.keySet()) {
            for (GuestGroup g2 : domains.keySet()) {
                if (!g1.equals(g2)) {
                    arcs.add(Map.entry(g1, g2));
                }
            }
        }

        while (!arcs.isEmpty()) {
            var arc = arcs.poll();
            GuestGroup g1 = arc.getKey();
            GuestGroup g2 = arc.getValue();

            if (revise(g1, g2)) {
                if (domains.get(g1).isEmpty()) return; // Domain wipeout, no solution possible

                // Add all neighbors of g1 back to queue
                for (GuestGroup neighbor : domains.keySet()) {
                    if (!neighbor.equals(g1) && !neighbor.equals(g2)) {
                        arcs.add(Map.entry(neighbor, g1));
                    }
                }
            }
        }
    }

    /**
     * Revises the domain of g1 to ensure consistency with g2.
     */
    private boolean revise(GuestGroup g1, GuestGroup g2) {
        boolean revised = false;
        Set<SeatingTable> toRemove = new HashSet<>();

        for (SeatingTable table : domains.get(g1)) {
            boolean hasConsistent = domains
                .get(g2)
                .stream()
                .anyMatch(
                    t ->
                        !t.equals(table) &&
                        t.getMaxSeats() >= g2.getTotalSeats() &&
                        satisfiesConstraints(g1, t) &&
                        satisfiesConstraints(g2, t)
                );
            if (!hasConsistent) {
                toRemove.add(table);
                revised = true;
            }
        }

        domains.get(g1).removeAll(toRemove);
        return revised;
    }

    public Set<GuestGroup> getAllGroups() {
        return domains.keySet();
    }

    private boolean satisfiesConstraints(GuestGroup group, SeatingTable table) {
        // בדיקת מקומות פנויים
        if (table.getMaxSeats() < group.getTotalSeats()) {
            return false;
        }

        // בדיקת נגישות
        boolean anyRequiresAccessibility = group.getGuests().stream().anyMatch(g -> Boolean.TRUE.equals(g.getAccessibility()));
        if (anyRequiresAccessibility && !Boolean.TRUE.equals(table.getAccessibility())) return false;

        return true;
    }

    /**
     * Returns the current domain of a guest group.
     */
    public Set<SeatingTable> getDomain(GuestGroup group) {
        return domains.getOrDefault(group, Collections.emptySet());
    }

    /**
     * Removes a table from all domains after assignment.
     */
    public void removeTableFromAllDomains(SeatingTable table) {
        domains.values().forEach(domain -> domain.remove(table));
    }
}
