package com.lavi.tablearrangments.domain;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a group of guests, typically related by family or seating preference.
 * Used to manage assignment constraints collectively, and maintains its own domain
 * of possible seating tables for constraint satisfaction algorithms.
 */
public class GuestGroup {

    private final List<Guest> guests;
    private Set<SeatingTable> domain = new HashSet<>();

    /**
     * Constructor to create a guest group.
     *
     * @param guests List of guests in this group.
     */
    public GuestGroup(List<Guest> guests) {
        this.guests = guests;
    }

    /**
     * Gets the guests in this group.
     *
     * @return List of guests.
     */
    public List<Guest> getGuests() {
        return guests;
    }

    /**
     * Sets the domain of possible seating tables for this group.
     *
     * @param domain Set of possible seating tables.
     */
    public void setDomain(Set<SeatingTable> domain) {
        this.domain = domain;
    }

    /**
     * Gets the current domain of possible seating tables.
     *
     * @return Set of possible seating tables.
     */
    public Set<SeatingTable> getDomain() {
        return domain;
    }

    /**
     * Removes a table from this group's domain.
     *
     * @param table The table to remove.
     */
    public void removeFromDomain(SeatingTable table) {
        domain.remove(table);
    }

    /**
     * Checks if there is a conflict (avoidance constraint) within the group itself.
     *
     * @return true if any guest in the group should avoid another guest in the same group.
     */
    public boolean hasInternalConflict() {
        for (Guest guest1 : guests) {
            for (Guest guest2 : guests) {
                if (!guest1.equals(guest2)) {
                    if (guest1.getAvoidGuests().contains(guest2) || guest2.getAvoidGuests().contains(guest1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Calculates the total number of seats required by this group.
     *
     * @return Total number of seats needed for this group.
     */
    public int getTotalSeats() {
        return guests.stream().mapToInt(Guest::getNumberOfSeats).sum();
    }

    /**
     * Concatenates the names of all guests in the group.
     *
     * @return A string containing the full names of all guests, separated by commas.
     */
    public String getNames() {
        return guests.stream().map(Guest::getLastNameAndFirstName).collect(Collectors.joining(", "));
    }

    /**
     * Determines if any guest in the group requires accessibility accommodations.
     *
     * @return true if at least one guest requires accessibility.
     */
    public boolean requiresAccessibility() {
        return guests.stream().anyMatch(g -> Boolean.TRUE.equals(g.getAccessibility()));
    }

    /**
     * Determines if any guest in the group prefers to sit near the stage.
     *
     * @return true if at least one guest prefers near-stage seating.
     */
    public boolean requiresNearStage() {
        return guests.stream().anyMatch(g -> Boolean.TRUE.equals(g.getNearStage()));
    }

    /**
     * Gets the relation type of the group based on the first guest.
     *
     * @return The relation name of the first guest, or null if the group is empty.
     */
    public String getRelation() {
        return guests.isEmpty() ? null : guests.get(0).getRelation().name();
    }
}
