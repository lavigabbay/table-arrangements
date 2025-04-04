package com.lavi.tablearrangments.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;

/**
 * A EventTable.
 */
@Entity
@Table(name = "event_table")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventTable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "table_number", nullable = false)
    private Integer tableNumber;

    @NotNull
    @Column(name = "max_seats", nullable = false)
    private Integer maxSeats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "user" }, allowSetters = true)
    private VenueTable venue;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public EventTable id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTableNumber() {
        return this.tableNumber;
    }

    public EventTable tableNumber(Integer tableNumber) {
        this.setTableNumber(tableNumber);
        return this;
    }

    public void setTableNumber(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }

    public Integer getMaxSeats() {
        return this.maxSeats;
    }

    public EventTable maxSeats(Integer maxSeats) {
        this.setMaxSeats(maxSeats);
        return this;
    }

    public void setMaxSeats(Integer maxSeats) {
        this.maxSeats = maxSeats;
    }

    public VenueTable getVenue() {
        return this.venue;
    }

    public void setVenue(VenueTable venueTable) {
        this.venue = venueTable;
    }

    public EventTable venue(VenueTable venueTable) {
        this.setVenue(venueTable);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventTable)) {
            return false;
        }
        return getId() != null && getId().equals(((EventTable) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EventTable{" +
            "id=" + getId() +
            ", tableNumber=" + getTableNumber() +
            ", maxSeats=" + getMaxSeats() +
            "}";
    }
}
