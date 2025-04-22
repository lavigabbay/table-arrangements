package com.lavi.tablearrangments.web.rest;

import com.lavi.tablearrangments.domain.Event;
import com.lavi.tablearrangments.domain.User;
import com.lavi.tablearrangments.repository.EventRepository;
import com.lavi.tablearrangments.repository.UserRepository;
import com.lavi.tablearrangments.security.SecurityUtils;
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
 * REST controller for managing {@link com.lavi.tablearrangments.domain.Event}.
 * Provides endpoints to create, update, delete, and retrieve events for the currently authenticated user.
 */
@RestController
@RequestMapping("/api/events")
@Transactional
public class EventResource {

    private static final Logger LOG = LoggerFactory.getLogger(EventResource.class);
    private static final String ENTITY_NAME = "event";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventResource(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    /**
     * {@code POST /events} : Create a new event and associate it with the current user.
     *
     * @param event the event to create.
     * @return the created event.
     * @throws URISyntaxException if the URI is incorrect.
     * @throws BadRequestAlertException if the event already has an ID or user not found.
     */
    @PostMapping("")
    public ResponseEntity<Event> createEvent(@Valid @RequestBody Event event) throws URISyntaxException {
        LOG.debug("REST request to save Event : {}", event);
        if (event.getId() != null) {
            throw new BadRequestAlertException("A new event cannot already have an ID", ENTITY_NAME, "idexists");
        }

        Optional<User> currentUser = SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLogin);
        event.setUser(currentUser.orElseThrow(() -> new BadRequestAlertException("Current user not found", ENTITY_NAME, "usernotfound")));

        event = eventRepository.save(event);
        return ResponseEntity.created(new URI("/api/events/" + event.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, event.getId().toString()))
            .body(event);
    }

    /**
     * {@code PUT /events/:id} : Update an existing event.
     *
     * @param id the ID of the event to update.
     * @param event the updated event object.
     * @return the updated event.
     * @throws URISyntaxException if the URI is incorrect.
     * @throws BadRequestAlertException if the ID is invalid or the event does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody Event event)
        throws URISyntaxException {
        LOG.debug("REST request to update Event : {}, {}", id, event);
        if (event.getId() == null || !Objects.equals(id, event.getId()) || !eventRepository.existsById(id)) {
            throw new BadRequestAlertException("Invalid or non-existing ID", ENTITY_NAME, "idinvalid");
        }

        event = eventRepository.save(event);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, event.getId().toString()))
            .body(event);
    }

    /**
     * {@code PATCH /events/:id} : Partially update fields of an existing event.
     *
     * @param id the ID of the event to update.
     * @param event the event with fields to be updated.
     * @return the updated event if found.
     * @throws URISyntaxException if the URI is incorrect.
     * @throws BadRequestAlertException if the ID is invalid or the event does not exist.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Event> partialUpdateEvent(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Event event
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Event : {}, {}", id, event);
        if (event.getId() == null || !Objects.equals(id, event.getId()) || !eventRepository.existsById(id)) {
            throw new BadRequestAlertException("Invalid or non-existing ID", ENTITY_NAME, "idinvalid");
        }

        Optional<Event> result = eventRepository
            .findById(event.getId())
            .map(existingEvent -> {
                if (event.getEventName() != null) existingEvent.setEventName(event.getEventName());
                if (event.getEventOwners() != null) existingEvent.setEventOwners(event.getEventOwners());
                if (event.getGroomParents() != null) existingEvent.setGroomParents(event.getGroomParents());
                if (event.getBrideParents() != null) existingEvent.setBrideParents(event.getBrideParents());
                if (event.getWeddingDate() != null) existingEvent.setWeddingDate(event.getWeddingDate());
                if (event.getReceptionTime() != null) existingEvent.setReceptionTime(event.getReceptionTime());
                if (event.getWeddingTime() != null) existingEvent.setWeddingTime(event.getWeddingTime());

                return existingEvent;
            })
            .map(eventRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, event.getId().toString())
        );
    }

    /**
     * {@code GET /events} : Get all events of the currently authenticated user.
     *
     * @param pageable the pagination information.
     * @param eagerload whether to eagerly load relationships (default true, ignored here).
     * @return list of events.
     */
    @GetMapping("")
    public ResponseEntity<List<Event>> getAllEvents(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Events");
        Page<Event> page = eventRepository.findAllByUserIsCurrentUser(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET /events/:id} : Get an event by ID.
     *
     * @param id the ID of the event to retrieve.
     * @return the event if found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Event : {}", id);
        Optional<Event> event = eventRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(event);
    }

    /**
     * {@code DELETE /events/:id} : Delete an event by ID.
     *
     * @param id the ID of the event to delete.
     * @return a 204 No Content response.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Event : {}", id);
        eventRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
