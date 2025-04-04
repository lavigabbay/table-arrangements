package com.lavi.tablearrangments.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lavi.tablearrangments.domain.enumeration.GuestRelation;
import com.lavi.tablearrangments.domain.enumeration.GuestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;

/**
 * A GuestTable.
 */
@Entity
@Table(name = "guest_table")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class GuestTable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "last_name_and_first_name", nullable = false)
    private String lastNameAndFirstName;

    @NotNull
    @Column(name = "number_of_seats", nullable = false)
    private Integer numberOfSeats;

    @NotNull
    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "near_dance_floor")
    private Boolean nearDanceFloor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GuestStatus status;

    @NotNull
    @Column(name = "side", nullable = false)
    private String side;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "relation", nullable = false)
    private GuestRelation relation;

    @Column(name = "not_with_id")
    private Long notWithId;

    @Column(name = "with_id")
    private Long withId;

    @Column(name = "conditions")
    private String conditions;

    @NotNull
    @Column(name = "accessibility", nullable = false)
    private Boolean accessibility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "user" }, allowSetters = true)
    private VenueTable venueName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "venue" }, allowSetters = true)
    private EventTable eventTable;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public GuestTable id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastNameAndFirstName() {
        return this.lastNameAndFirstName;
    }

    public GuestTable lastNameAndFirstName(String lastNameAndFirstName) {
        this.setLastNameAndFirstName(lastNameAndFirstName);
        return this;
    }

    public void setLastNameAndFirstName(String lastNameAndFirstName) {
        this.lastNameAndFirstName = lastNameAndFirstName;
    }

    public Integer getNumberOfSeats() {
        return this.numberOfSeats;
    }

    public GuestTable numberOfSeats(Integer numberOfSeats) {
        this.setNumberOfSeats(numberOfSeats);
        return this;
    }

    public void setNumberOfSeats(Integer numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public String getPhone() {
        return this.phone;
    }

    public GuestTable phone(String phone) {
        this.setPhone(phone);
        return this;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getNearDanceFloor() {
        return this.nearDanceFloor;
    }

    public GuestTable nearDanceFloor(Boolean nearDanceFloor) {
        this.setNearDanceFloor(nearDanceFloor);
        return this;
    }

    public void setNearDanceFloor(Boolean nearDanceFloor) {
        this.nearDanceFloor = nearDanceFloor;
    }

    public GuestStatus getStatus() {
        return this.status;
    }

    public GuestTable status(GuestStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(GuestStatus status) {
        this.status = status;
    }

    public String getSide() {
        return this.side;
    }

    public GuestTable side(String side) {
        this.setSide(side);
        return this;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public GuestRelation getRelation() {
        return this.relation;
    }

    public GuestTable relation(GuestRelation relation) {
        this.setRelation(relation);
        return this;
    }

    public void setRelation(GuestRelation relation) {
        this.relation = relation;
    }

    public Long getNotWithId() {
        return this.notWithId;
    }

    public GuestTable notWithId(Long notWithId) {
        this.setNotWithId(notWithId);
        return this;
    }

    public void setNotWithId(Long notWithId) {
        this.notWithId = notWithId;
    }

    public Long getWithId() {
        return this.withId;
    }

    public GuestTable withId(Long withId) {
        this.setWithId(withId);
        return this;
    }

    public void setWithId(Long withId) {
        this.withId = withId;
    }

    public String getConditions() {
        return this.conditions;
    }

    public GuestTable conditions(String conditions) {
        this.setConditions(conditions);
        return this;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public Boolean getAccessibility() {
        return this.accessibility;
    }

    public GuestTable accessibility(Boolean accessibility) {
        this.setAccessibility(accessibility);
        return this;
    }

    public void setAccessibility(Boolean accessibility) {
        this.accessibility = accessibility;
    }

    public VenueTable getVenueName() {
        return this.venueName;
    }

    public void setVenueName(VenueTable venueTable) {
        this.venueName = venueTable;
    }

    public GuestTable venueName(VenueTable venueTable) {
        this.setVenueName(venueTable);
        return this;
    }

    public EventTable getEventTable() {
        return this.eventTable;
    }

    public void setEventTable(EventTable eventTable) {
        this.eventTable = eventTable;
    }

    public GuestTable eventTable(EventTable eventTable) {
        this.setEventTable(eventTable);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GuestTable)) {
            return false;
        }
        return getId() != null && getId().equals(((GuestTable) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "GuestTable{" +
            "id=" + getId() +
            ", lastNameAndFirstName='" + getLastNameAndFirstName() + "'" +
            ", numberOfSeats=" + getNumberOfSeats() +
            ", phone='" + getPhone() + "'" +
            ", nearDanceFloor='" + getNearDanceFloor() + "'" +
            ", status='" + getStatus() + "'" +
            ", side='" + getSide() + "'" +
            ", relation='" + getRelation() + "'" +
            ", notWithId=" + getNotWithId() +
            ", withId=" + getWithId() +
            ", conditions='" + getConditions() + "'" +
            ", accessibility='" + getAccessibility() + "'" +
            "}";
    }
}
