package com.lavi.tablearrangments.web.rest;

import com.lavi.tablearrangments.domain.EventTable;
import com.lavi.tablearrangments.repository.EventTableRepository;
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
 * REST controller for managing {@link com.lavi.tablearrangments.domain.EventTable}.
 */
@RestController
@RequestMapping("/api/event-tables")
@Transactional
public class EventTableResource {

    private static final Logger LOG = LoggerFactory.getLogger(EventTableResource.class);

    private static final String ENTITY_NAME = "eventTable";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EventTableRepository eventTableRepository;

    public EventTableResource(EventTableRepository eventTableRepository) {
        this.eventTableRepository = eventTableRepository;
    }

    /**
     * {@code POST  /event-tables} : Create a new eventTable.
     *
     * @param eventTable the eventTable to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new eventTable, or with status {@code 400 (Bad Request)} if the eventTable has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<EventTable> createEventTable(@Valid @RequestBody EventTable eventTable) throws URISyntaxException {
        LOG.debug("REST request to save EventTable : {}", eventTable);
        if (eventTable.getId() != null) {
            throw new BadRequestAlertException("A new eventTable cannot already have an ID", ENTITY_NAME, "idexists");
        }
        eventTable = eventTableRepository.save(eventTable);
        return ResponseEntity.created(new URI("/api/event-tables/" + eventTable.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, eventTable.getId().toString()))
            .body(eventTable);
    }

    /**
     * {@code PUT  /event-tables/:id} : Updates an existing eventTable.
     *
     * @param id the id of the eventTable to save.
     * @param eventTable the eventTable to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eventTable,
     * or with status {@code 400 (Bad Request)} if the eventTable is not valid,
     * or with status {@code 500 (Internal Server Error)} if the eventTable couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventTable> updateEventTable(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody EventTable eventTable
    ) throws URISyntaxException {
        LOG.debug("REST request to update EventTable : {}, {}", id, eventTable);
        if (eventTable.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventTable.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eventTableRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        eventTable = eventTableRepository.save(eventTable);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eventTable.getId().toString()))
            .body(eventTable);
    }

    /**
     * {@code PATCH  /event-tables/:id} : Partial updates given fields of an existing eventTable, field will ignore if it is null
     *
     * @param id the id of the eventTable to save.
     * @param eventTable the eventTable to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eventTable,
     * or with status {@code 400 (Bad Request)} if the eventTable is not valid,
     * or with status {@code 404 (Not Found)} if the eventTable is not found,
     * or with status {@code 500 (Internal Server Error)} if the eventTable couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<EventTable> partialUpdateEventTable(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody EventTable eventTable
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update EventTable partially : {}, {}", id, eventTable);
        if (eventTable.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventTable.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eventTableRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EventTable> result = eventTableRepository
            .findById(eventTable.getId())
            .map(existingEventTable -> {
                if (eventTable.getTableNumber() != null) {
                    existingEventTable.setTableNumber(eventTable.getTableNumber());
                }
                if (eventTable.getMaxSeats() != null) {
                    existingEventTable.setMaxSeats(eventTable.getMaxSeats());
                }

                return existingEventTable;
            })
            .map(eventTableRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eventTable.getId().toString())
        );
    }

    /**
     * {@code GET  /event-tables} : get all the eventTables.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of eventTables in body.
     */
    @GetMapping("")
    public ResponseEntity<List<EventTable>> getAllEventTables(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of EventTables");
        Page<EventTable> page;
        if (eagerload) {
            page = eventTableRepository.findAllWithEagerRelationships(pageable);
        } else {
            page = eventTableRepository.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /event-tables/:id} : get the "id" eventTable.
     *
     * @param id the id of the eventTable to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the eventTable, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventTable> getEventTable(@PathVariable("id") Long id) {
        LOG.debug("REST request to get EventTable : {}", id);
        Optional<EventTable> eventTable = eventTableRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(eventTable);
    }

    /**
     * {@code DELETE  /event-tables/:id} : delete the "id" eventTable.
     *
     * @param id the id of the eventTable to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventTable(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete EventTable : {}", id);
        eventTableRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
