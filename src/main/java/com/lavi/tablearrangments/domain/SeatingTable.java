package com.lavi.tablearrangments.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;

/**
 * A SeatingTable.
 */
@Entity
@Table(name = "seating_table")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SeatingTable implements Serializable {

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

    @Column(name = "near_stage")
    private Boolean nearStage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "user" }, allowSetters = true)
    private Event event;

    @Column(name = "accessibility")
    private Boolean accessibility;

    public Boolean getAccessibility() {
        return this.accessibility;
    }

    public SeatingTable accessibility(Boolean accessibility) {
        this.setAccessibility(accessibility);
        return this;
    }

    public void setAccessibility(Boolean accessibility) {
        this.accessibility = accessibility;
    }

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public SeatingTable id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTableNumber() {
        return this.tableNumber;
    }

    public SeatingTable tableNumber(Integer tableNumber) {
        this.setTableNumber(tableNumber);
        return this;
    }

    public void setTableNumber(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }

    public Integer getMaxSeats() {
        return this.maxSeats;
    }

    public SeatingTable maxSeats(Integer maxSeats) {
        this.setMaxSeats(maxSeats);
        return this;
    }

    public void setMaxSeats(Integer maxSeats) {
        this.maxSeats = maxSeats;
    }

    public Boolean getNearStage() {
        return this.nearStage;
    }

    public SeatingTable nearStage(Boolean nearStage) {
        this.setNearStage(nearStage);
        return this;
    }

    public void setNearStage(Boolean nearStage) {
        this.nearStage = nearStage;
    }

    public Event getEvent() {
        return this.event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public SeatingTable event(Event event) {
        this.setEvent(event);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

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

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SeatingTable{" +
            "id=" + getId() +
            ", tableNumber=" + getTableNumber() +
            ", maxSeats=" + getMaxSeats() +
            ", nearStage='" + getNearStage() + "'" +
            "}";
    }
}
