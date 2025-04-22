package com.lavi.tablearrangments.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;

/**
 * Entity representing a seating table at an event.
 * <p>
 * Each seating table has a unique number, a maximum number of seats,
 * a flag indicating proximity to the stage, and accessibility information.
 * Each table belongs to a specific {@link Event}.
 * </p>
 */
@Entity
@Table(name = "seating_table")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SeatingTable implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key (ID) of the seating table.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * The number identifying the table within the event.
     */
    @NotNull
    @Column(name = "table_number", nullable = false)
    private Integer tableNumber;

    /**
     * The maximum number of seats available at this table.
     */
    @NotNull
    @Column(name = "max_seats", nullable = false)
    private Integer maxSeats;

    /**
     * Whether the table is near the stage.
     */
    @Column(name = "near_stage")
    private Boolean nearStage;

    /**
     * The event to which this seating table belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "user" }, allowSetters = true)
    private Event event;

    /**
     * Whether the table is accessible (e.g., for wheelchair users).
     */
    @Column(name = "accessibility")
    private Boolean accessibility;

    /**
     * Gets whether the table is accessible.
     */
    public Boolean getAccessibility() {
        return this.accessibility;
    }

    /**
     * Fluent setter for accessibility.
     */
    public SeatingTable accessibility(Boolean accessibility) {
        this.setAccessibility(accessibility);
        return this;
    }

    /**
     * Sets whether the table is accessible.
     */
    public void setAccessibility(Boolean accessibility) {
        this.accessibility = accessibility;
    }

    // jhipster-needle-entity-add-field - JHipster will add fields here

    /**
     * Gets the table ID.
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Fluent setter for ID.
     */
    public SeatingTable id(Long id) {
        this.setId(id);
        return this;
    }

    /**
     * Sets the table ID.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the table number.
     */
    public Integer getTableNumber() {
        return this.tableNumber;
    }

    /**
     * Fluent setter for table number.
     */
    public SeatingTable tableNumber(Integer tableNumber) {
        this.setTableNumber(tableNumber);
        return this;
    }

    /**
     * Sets the table number.
     */
    public void setTableNumber(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }

    /**
     * Gets the maximum number of seats at the table.
     */
    public Integer getMaxSeats() {
        return this.maxSeats;
    }

    /**
     * Fluent setter for maximum seats.
     */
    public SeatingTable maxSeats(Integer maxSeats) {
        this.setMaxSeats(maxSeats);
        return this;
    }

    /**
     * Sets the maximum number of seats at the table.
     */
    public void setMaxSeats(Integer maxSeats) {
        this.maxSeats = maxSeats;
    }

    /**
     * Gets whether the table is near the stage.
     */
    public Boolean getNearStage() {
        return this.nearStage;
    }

    /**
     * Fluent setter for nearStage.
     */
    public SeatingTable nearStage(Boolean nearStage) {
        this.setNearStage(nearStage);
        return this;
    }

    /**
     * Sets whether the table is near the stage.
     */
    public void setNearStage(Boolean nearStage) {
        this.nearStage = nearStage;
    }

    /**
     * Gets the event this table belongs to.
     */
    public Event getEvent() {
        return this.event;
    }

    /**
     * Sets the event this table belongs to.
     */
    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * Fluent setter for event.
     */
    public SeatingTable event(Event event) {
        this.setEvent(event);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    /**
     * Compares tables by ID for equality.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SeatingTable)) {
            return false;
        }
        return getId() != null && getId().equals(((SeatingTable) o).getId());
    }

    /**
     * Hash code based on class identity.
     */
    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    /**
     * String representation of the seating table (excluding relations).
     */
    @Override
    public String toString() {
        return (
            "SeatingTable{" +
            "id=" +
            getId() +
            ", tableNumber=" +
            getTableNumber() +
            ", maxSeats=" +
            getMaxSeats() +
            ", nearStage='" +
            getNearStage() +
            "'" +
            "}"
        );
    }
}
