package com.lavi.tablearrangments.web.rest;

import com.lavi.tablearrangments.domain.Guest;
import com.lavi.tablearrangments.repository.GuestRepository;
import com.lavi.tablearrangments.security.SecurityUtils;
import com.lavi.tablearrangments.service.GuestAssignmentService;
import com.lavi.tablearrangments.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.lavi.tablearrangments.domain.Guest}.
 */
@RestController
@RequestMapping("/api/guests")
@Transactional
public class GuestResource {

    private static final Logger LOG = LoggerFactory.getLogger(GuestResource.class);

    private static final String ENTITY_NAME = "guest";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final GuestRepository guestRepository;
    private final GuestAssignmentService guestAssignmentService;

    public GuestResource(GuestRepository guestRepository, GuestAssignmentService guestAssignmentService) {
        this.guestRepository = guestRepository;
        this.guestAssignmentService = guestAssignmentService;
    }

    @PostMapping("")
    public ResponseEntity<Guest> createGuest(@Valid @RequestBody Guest guest) throws URISyntaxException {
        LOG.debug("REST request to save Guest : {}", guest);
        if (guest.getId() != null) {
            throw new BadRequestAlertException("A new guest cannot already have an ID", ENTITY_NAME, "idexists");
        }
        guest = guestRepository.save(guest);
        return ResponseEntity.created(new URI("/api/guests/" + guest.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, guest.getId().toString()))
            .body(guest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Guest> updateGuest(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody Guest guest)
        throws URISyntaxException {
        LOG.debug("REST request to update Guest : {}, {}", id, guest);
        if (guest.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, guest.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!guestRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        guest = guestRepository.save(guest);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, guest.getId().toString()))
            .body(guest);
    }

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Guest> partialUpdateGuest(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Guest guest
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Guest partially : {}, {}", id, guest);
        if (guest.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, guest.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!guestRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Guest> result = guestRepository
            .findById(guest.getId())
            .map(existingGuest -> {
                if (guest.getLastNameAndFirstName() != null) {
                    existingGuest.setLastNameAndFirstName(guest.getLastNameAndFirstName());
                }
                if (guest.getNumberOfSeats() != null) {
                    existingGuest.setNumberOfSeats(guest.getNumberOfSeats());
                }
                if (guest.getPhone() != null) {
                    existingGuest.setPhone(guest.getPhone());
                }
                if (guest.getNearStage() != null) {
                    existingGuest.setNearStage(guest.getNearStage());
                }
                if (guest.getStatus() != null) {
                    existingGuest.setStatus(guest.getStatus());
                }
                if (guest.getSide() != null) {
                    existingGuest.setSide(guest.getSide());
                }
                if (guest.getRelation() != null) {
                    existingGuest.setRelation(guest.getRelation());
                }
                if (guest.getAccessibility() != null) {
                    existingGuest.setAccessibility(guest.getAccessibility());
                }

                return existingGuest;
            })
            .map(guestRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, guest.getId().toString())
        );
    }

    @GetMapping("")
    public ResponseEntity<List<Guest>> getAllGuests(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Guests by current user");
        Page<Guest> page = guestRepository.findAllByEventUserIsCurrentUser(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Guest> getGuest(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Guest : {}", id);
        Guest guest = guestRepository.findOneWithEagerRelationships(id).orElseThrow();

        if (!guest.getEvent().getUser().getLogin().equals(SecurityUtils.getCurrentUserLogin().orElse(""))) {
            return ResponseEntity.status(403).build();
        }

        return ResponseUtil.wrapOrNotFound(Optional.of(guest));
    }

    @PostMapping("/assign")
    public ResponseEntity<Void> assignGuestsToTables() {
        System.out.println("🔥 הגיע בקשה לשיבוץ אורחים");
        guestAssignmentService.assignAll();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGuest(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Guest : {}", id);
        Guest guest = guestRepository.findById(id).orElseThrow();

        if (!guest.getEvent().getUser().getLogin().equals(SecurityUtils.getCurrentUserLogin().orElse(""))) {
            return ResponseEntity.status(403).build();
        }

        guestRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
