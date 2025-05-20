package com.lavi.tablearrangments.service;

import com.lavi.tablearrangments.domain.GuestGroup;
import com.lavi.tablearrangments.domain.SeatingTable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DomainManager manages the possible seating tables (domains) for each guest group.
 * It applies the AC-3 algorithm to enforce arc consistency and reduce invalid assignments.
 */
public class DomainManager {

    private final Map<GuestGroup, Set<SeatingTable>> originalDomains = new HashMap<>();

    private final Map<GuestGroup, Set<SeatingTable>> domains = new HashMap<>();

    /**
     * Initializes the domain for each guest group based on available tables and their capacities.
     *
     * @param guestGroups List of guest groups to assign.
     * @param tables      List of available seating tables.
     */
    public DomainManager(List<GuestGroup> guestGroups, List<SeatingTable> tables) {
        for (GuestGroup group : guestGroups) {
            Set<SeatingTable> possibleTables = tables
                .stream()
                .filter(table -> table.getMaxSeats() >= group.getTotalSeats())
                .collect(Collectors.toSet());
            domains.put(group, possibleTables);
            originalDomains.put(group, new HashSet<>(possibleTables));
        }
    }

    /**
     * Applies the AC-3 algorithm to enforce arc consistency across all guest groups,
     * reducing impossible assignments from their domains.
     */
    public void applyAC3() {
        Queue<Map.Entry<GuestGroup, GuestGroup>> arcs = new LinkedList<>();

        // Initialize all arcs between different groups
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
                if (domains.get(g1).isEmpty()) {
                    // ⚠️ Domain wipeout detected - restore previous state and exit AC-3
                    domains.clear();
                    originalDomains.forEach((k, v) -> domains.put(k, new HashSet<>(v)));
                    return;
                }

                // Add arcs back for neighboring groups to check again after revision
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
     * Removes tables from g1's domain if no consistent assignment exists with g2.
     *
     * @param g1 First guest group.
     * @param g2 Second guest group.
     * @return True if the domain of g1 was revised, false otherwise.
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
                        satisfiesConstraints(g1, table) &&
                        satisfiesConstraints(g2, t) &&
                        (!table.equals(t) || table.getMaxSeats() >= (g1.getTotalSeats() + g2.getTotalSeats()))
                );

            if (!hasConsistent) {
                toRemove.add(table);
                revised = true;
            }
        }

        domains.get(g1).removeAll(toRemove);
        return revised;
    }

    /**
     * Validates whether the specified seating table satisfies the constraints required by the guest group.
     *
     * @param group The guest group to check.
     * @param table The seating table being evaluated.
     * @return True if the table satisfies all constraints, false otherwise.
     */
    private boolean satisfiesConstraints(GuestGroup group, SeatingTable table) {
        if (table.getMaxSeats() < group.getTotalSeats()) return false;

        boolean requiresAccessibility = group.getGuests().stream().anyMatch(g -> Boolean.TRUE.equals(g.getAccessibility()));
        return !requiresAccessibility || Boolean.TRUE.equals(table.getAccessibility());
    }

    /**
     * Returns all guest groups currently managed by this DomainManager.
     *
     * @return Set of guest groups.
     */
    public Set<GuestGroup> getAllGroups() {
        return domains.keySet();
    }

    /**
     * Retrieves the current domain (possible seating tables) for a specific guest group.
     *
     * @param group The guest group.
     * @return Set of possible seating tables.
     */
    public Set<SeatingTable> getDomain(GuestGroup group) {
        return domains.getOrDefault(group, Collections.emptySet());
    }

    /**
     * Removes a table from all guest groups' domains after it has been assigned.
     *
     * @param table The table to remove from all domains.
     */
    public void removeTableFromAllDomains(SeatingTable table) {
        domains.values().forEach(domain -> domain.remove(table));
    }

    /**
     * Clones the current state of domains for backup before applying AC-3.
     */
    public Map<GuestGroup, Set<SeatingTable>> cloneDomains() {
        Map<GuestGroup, Set<SeatingTable>> backup = new HashMap<>();
        domains.forEach((k, v) -> backup.put(k, new HashSet<>(v)));
        return backup;
    }

    /**
     * Restores domains to a previously saved backup.
     */
    public void restoreDomains(Map<GuestGroup, Set<SeatingTable>> backup) {
        domains.clear();
        backup.forEach((k, v) -> domains.put(k, new HashSet<>(v)));
    }
}
