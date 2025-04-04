package com.lavi.tablearrangments.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A Event.
 */
@Entity
@Table(name = "event")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "event_owners")
    private String eventOwners;

    @Column(name = "groom_parents")
    private String groomParents;

    @Column(name = "bride_parents")
    private String brideParents;

    @NotNull
    @Column(name = "wedding_date", nullable = false)
    private LocalDate weddingDate;

    @NotNull
    @Column(name = "reception_time", nullable = false)
    private Instant receptionTime;

    @NotNull
    @Column(name = "wedding_time", nullable = false)
    private Instant weddingTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Event id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventName() {
        return this.eventName;
    }

    public Event eventName(String eventName) {
        this.setEventName(eventName);
        return this;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventOwners() {
        return this.eventOwners;
    }

    public Event eventOwners(String eventOwners) {
        this.setEventOwners(eventOwners);
        return this;
    }

    public void setEventOwners(String eventOwners) {
        this.eventOwners = eventOwners;
    }

    public String getGroomParents() {
        return this.groomParents;
    }

    public Event groomParents(String groomParents) {
        this.setGroomParents(groomParents);
        return this;
    }

    public void setGroomParents(String groomParents) {
        this.groomParents = groomParents;
    }

    public String getBrideParents() {
        return this.brideParents;
    }

    public Event brideParents(String brideParents) {
        this.setBrideParents(brideParents);
        return this;
    }

    public void setBrideParents(String brideParents) {
        this.brideParents = brideParents;
    }

    public LocalDate getWeddingDate() {
        return this.weddingDate;
    }

    public Event weddingDate(LocalDate weddingDate) {
        this.setWeddingDate(weddingDate);
        return this;
    }

    public void setWeddingDate(LocalDate weddingDate) {
        this.weddingDate = weddingDate;
    }

    public Instant getReceptionTime() {
        return this.receptionTime;
    }

    public Event receptionTime(Instant receptionTime) {
        this.setReceptionTime(receptionTime);
        return this;
    }

    public void setReceptionTime(Instant receptionTime) {
        this.receptionTime = receptionTime;
    }

    public Instant getWeddingTime() {
        return this.weddingTime;
    }

    public Event weddingTime(Instant weddingTime) {
        this.setWeddingTime(weddingTime);
        return this;
    }

    public void setWeddingTime(Instant weddingTime) {
        this.weddingTime = weddingTime;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Event user(User user) {
        this.setUser(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Event)) {
            return false;
        }
        return getId() != null && getId().equals(((Event) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Event{" +
            "id=" + getId() +
            ", eventName='" + getEventName() + "'" +
            ", eventOwners='" + getEventOwners() + "'" +
            ", groomParents='" + getGroomParents() + "'" +
            ", brideParents='" + getBrideParents() + "'" +
            ", weddingDate='" + getWeddingDate() + "'" +
            ", receptionTime='" + getReceptionTime() + "'" +
            ", weddingTime='" + getWeddingTime() + "'" +
            "}";
    }
}
