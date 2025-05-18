package com.lavi.tablearrangments.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lavi.tablearrangments.domain.enumeration.GuestRelation;
import com.lavi.tablearrangments.domain.enumeration.GuestSide;
import com.lavi.tablearrangments.domain.enumeration.GuestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A Guest.
 */
@Entity
@Table(name = "guest")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Guest implements Serializable {

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

    @Column(name = "phone")
    private String phone;

    @Column(name = "near_stage")
    private Boolean nearStage;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GuestStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "side")
    private GuestSide side;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "relation", nullable = false)
    private GuestRelation relation;

    @NotNull
    @Column(name = "accessibility", nullable = false)
    private Boolean accessibility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "user" }, allowSetters = true)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "event" }, allowSetters = true)
    private SeatingTable table;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "rel_guest__avoid_guests",
        joinColumns = @JoinColumn(name = "guest_id"),
        inverseJoinColumns = @JoinColumn(name = "avoid_guests_id")
    )
    @JsonIgnoreProperties(value = { "event", "table", "avoidGuests", "preferGuests", "avoidedBies", "preferredBies" }, allowSetters = true)
    private Set<Guest> avoidGuests = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "rel_guest__prefer_guests",
        joinColumns = @JoinColumn(name = "guest_id"),
        inverseJoinColumns = @JoinColumn(name = "prefer_guests_id")
    )
    @JsonIgnoreProperties(value = { "event", "table", "avoidGuests", "preferGuests", "avoidedBies", "preferredBies" }, allowSetters = true)
    private Set<Guest> preferGuests = new HashSet<>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "avoidGuests")
    @JsonIgnoreProperties(value = { "event", "table", "avoidGuests", "preferGuests", "avoidedBies", "preferredBies" }, allowSetters = true)
    private Set<Guest> avoidedBies = new HashSet<>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "preferGuests")
    @JsonIgnoreProperties(value = { "event", "table", "avoidGuests", "preferGuests", "avoidedBies", "preferredBies" }, allowSetters = true)
    private Set<Guest> preferredBies = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Guest id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastNameAndFirstName() {
        return this.lastNameAndFirstName;
    }

    public Guest lastNameAndFirstName(String lastNameAndFirstName) {
        this.setLastNameAndFirstName(lastNameAndFirstName);
        return this;
    }

    public void setLastNameAndFirstName(String lastNameAndFirstName) {
        this.lastNameAndFirstName = lastNameAndFirstName;
    }

    public Integer getNumberOfSeats() {
        return this.numberOfSeats;
    }

    public Guest numberOfSeats(Integer numberOfSeats) {
        this.setNumberOfSeats(numberOfSeats);
        return this;
    }

    public void setNumberOfSeats(Integer numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public String getPhone() {
        return this.phone;
    }

    public Guest phone(String phone) {
        this.setPhone(phone);
        return this;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getNearStage() {
        return this.nearStage;
    }

    public Guest nearStage(Boolean nearStage) {
        this.setNearStage(nearStage);
        return this;
    }

    public void setNearStage(Boolean nearStage) {
        this.nearStage = nearStage;
    }

    public GuestStatus getStatus() {
        return this.status;
    }

    public Guest status(GuestStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(GuestStatus status) {
        this.status = status;
    }

    public GuestSide getSide() {
        return this.side;
    }

    public Guest side(GuestSide side) {
        this.setSide(side);
        return this;
    }

    public void setSide(GuestSide side) {
        this.side = side;
    }

    public GuestRelation getRelation() {
        return this.relation;
    }

    public Guest relation(GuestRelation relation) {
        this.setRelation(relation);
        return this;
    }

    public void setRelation(GuestRelation relation) {
        this.relation = relation;
    }

    public Boolean getAccessibility() {
        return this.accessibility;
    }

    public Guest accessibility(Boolean accessibility) {
        this.setAccessibility(accessibility);
        return this;
    }

    public void setAccessibility(Boolean accessibility) {
        this.accessibility = accessibility;
    }

    public Event getEvent() {
        return this.event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Guest event(Event event) {
        this.setEvent(event);
        return this;
    }

    public SeatingTable getTable() {
        return this.table;
    }

    public void setTable(SeatingTable seatingTable) {
        this.table = seatingTable;
    }

    public Guest table(SeatingTable seatingTable) {
        this.setTable(seatingTable);
        return this;
    }

    public Set<Guest> getAvoidGuests() {
        return this.avoidGuests;
    }

    public void setAvoidGuests(Set<Guest> guests) {
        this.avoidGuests = guests;
    }

    public Guest avoidGuests(Set<Guest> guests) {
        this.setAvoidGuests(guests);
        return this;
    }

    public Guest addAvoidGuests(Guest guest) {
        this.avoidGuests.add(guest);
        return this;
    }

    public Guest removeAvoidGuests(Guest guest) {
        this.avoidGuests.remove(guest);
        return this;
    }

    public Set<Guest> getPreferGuests() {
        return this.preferGuests;
    }

    public void setPreferGuests(Set<Guest> guests) {
        this.preferGuests = guests;
    }

    public Guest preferGuests(Set<Guest> guests) {
        this.setPreferGuests(guests);
        return this;
    }

    public Guest addPreferGuests(Guest guest) {
        this.preferGuests.add(guest);
        return this;
    }

    public Guest removePreferGuests(Guest guest) {
        this.preferGuests.remove(guest);
        return this;
    }

    public Set<Guest> getAvoidedBies() {
        return this.avoidedBies;
    }

    public void setAvoidedBies(Set<Guest> guests) {
        if (this.avoidedBies != null) {
            this.avoidedBies.forEach(i -> i.removeAvoidGuests(this));
        }
        if (guests != null) {
            guests.forEach(i -> i.addAvoidGuests(this));
        }
        this.avoidedBies = guests;
    }

    public Guest avoidedBies(Set<Guest> guests) {
        this.setAvoidedBies(guests);
        return this;
    }

    public Guest addAvoidedBy(Guest guest) {
        this.avoidedBies.add(guest);
        guest.getAvoidGuests().add(this);
        return this;
    }

    public Guest removeAvoidedBy(Guest guest) {
        this.avoidedBies.remove(guest);
        guest.getAvoidGuests().remove(this);
        return this;
    }

    public Set<Guest> getPreferredBies() {
        return this.preferredBies;
    }

    public void setPreferredBies(Set<Guest> guests) {
        if (this.preferredBies != null) {
            this.preferredBies.forEach(i -> i.removePreferGuests(this));
        }
        if (guests != null) {
            guests.forEach(i -> i.addPreferGuests(this));
        }
        this.preferredBies = guests;
    }

    public Guest preferredBies(Set<Guest> guests) {
        this.setPreferredBies(guests);
        return this;
    }

    public Guest addPreferredBy(Guest guest) {
        this.preferredBies.add(guest);
        guest.getPreferGuests().add(this);
        return this;
    }

    public Guest removePreferredBy(Guest guest) {
        this.preferredBies.remove(guest);
        guest.getPreferGuests().remove(this);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Guest)) {
            return false;
        }
        return getId() != null && getId().equals(((Guest) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Guest{" +
            "id=" + getId() +
            ", lastNameAndFirstName='" + getLastNameAndFirstName() + "'" +
            ", numberOfSeats=" + getNumberOfSeats() +
            ", phone='" + getPhone() + "'" +
            ", nearStage='" + getNearStage() + "'" +
            ", status='" + getStatus() + "'" +
            ", side='" + getSide() + "'" +
            ", relation='" + getRelation() + "'" +
            ", accessibility='" + getAccessibility() + "'" +
            "}";
    }
}
