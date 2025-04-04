package com.lavi.tablearrangments.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A VenueTable.
 */
@Entity
@Table(name = "venue_table")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VenueTable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "number_of_tables", nullable = false)
    private Integer numberOfTables;

    @NotNull
    @Column(name = "near_stage_tables", nullable = false)
    private Integer nearStageTables;

    @NotNull
    @Column(name = "venue_name", nullable = false)
    private String venueName;

    @NotNull
    @Column(name = "event_owners", nullable = false)
    private String eventOwners;

    @NotNull
    @Column(name = "groom_parents", nullable = false)
    private String groomParents;

    @NotNull
    @Column(name = "bride_parents", nullable = false)
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

    public VenueTable id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumberOfTables() {
        return this.numberOfTables;
    }

    public VenueTable numberOfTables(Integer numberOfTables) {
        this.setNumberOfTables(numberOfTables);
        return this;
    }

    public void setNumberOfTables(Integer numberOfTables) {
        this.numberOfTables = numberOfTables;
    }

    public Integer getNearStageTables() {
        return this.nearStageTables;
    }

    public VenueTable nearStageTables(Integer nearStageTables) {
        this.setNearStageTables(nearStageTables);
        return this;
    }

    public void setNearStageTables(Integer nearStageTables) {
        this.nearStageTables = nearStageTables;
    }

    public String getVenueName() {
        return this.venueName;
    }

    public VenueTable venueName(String venueName) {
        this.setVenueName(venueName);
        return this;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getEventOwners() {
        return this.eventOwners;
    }

    public VenueTable eventOwners(String eventOwners) {
        this.setEventOwners(eventOwners);
        return this;
    }

    public void setEventOwners(String eventOwners) {
        this.eventOwners = eventOwners;
    }

    public String getGroomParents() {
        return this.groomParents;
    }

    public VenueTable groomParents(String groomParents) {
        this.setGroomParents(groomParents);
        return this;
    }

    public void setGroomParents(String groomParents) {
        this.groomParents = groomParents;
    }

    public String getBrideParents() {
        return this.brideParents;
    }

    public VenueTable brideParents(String brideParents) {
        this.setBrideParents(brideParents);
        return this;
    }

    public void setBrideParents(String brideParents) {
        this.brideParents = brideParents;
    }

    public LocalDate getWeddingDate() {
        return this.weddingDate;
    }

    public VenueTable weddingDate(LocalDate weddingDate) {
        this.setWeddingDate(weddingDate);
        return this;
    }

    public void setWeddingDate(LocalDate weddingDate) {
        this.weddingDate = weddingDate;
    }

    public Instant getReceptionTime() {
        return this.receptionTime;
    }

    public VenueTable receptionTime(Instant receptionTime) {
        this.setReceptionTime(receptionTime);
        return this;
    }

    public void setReceptionTime(Instant receptionTime) {
        this.receptionTime = receptionTime;
    }

    public Instant getWeddingTime() {
        return this.weddingTime;
    }

    public VenueTable weddingTime(Instant weddingTime) {
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

    public VenueTable user(User user) {
        this.setUser(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VenueTable)) {
            return false;
        }
        return getId() != null && getId().equals(((VenueTable) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VenueTable{" +
            "id=" + getId() +
            ", numberOfTables=" + getNumberOfTables() +
            ", nearStageTables=" + getNearStageTables() +
            ", venueName='" + getVenueName() + "'" +
            ", eventOwners='" + getEventOwners() + "'" +
            ", groomParents='" + getGroomParents() + "'" +
            ", brideParents='" + getBrideParents() + "'" +
            ", weddingDate='" + getWeddingDate() + "'" +
            ", receptionTime='" + getReceptionTime() + "'" +
            ", weddingTime='" + getWeddingTime() + "'" +
            "}";
    }
}
