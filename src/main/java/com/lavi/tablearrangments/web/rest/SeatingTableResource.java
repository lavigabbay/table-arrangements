package com.lavi.tablearrangments.web.rest;

import com.lavi.tablearrangments.domain.SeatingTable;
import com.lavi.tablearrangments.repository.SeatingTableRepository;
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
 * REST controller for managing {@link com.lavi.tablearrangments.domain.SeatingTable}.
 */
@RestController
@RequestMapping("/api/seating-tables")
@Transactional
public class SeatingTableResource {

    private static final Logger LOG = LoggerFactory.getLogger(SeatingTableResource.class);

    private static final String ENTITY_NAME = "seatingTable";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SeatingTableRepository seatingTableRepository;

    public SeatingTableResource(SeatingTableRepository seatingTableRepository) {
        this.seatingTableRepository = seatingTableRepository;
    }

    /**
     * {@code POST  /seating-tables} : Create a new seatingTable.
     *
     * @param seatingTable the seatingTable to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new seatingTable, or with status {@code 400 (Bad Request)} if the seatingTable has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<SeatingTable> createSeatingTable(@Valid @RequestBody SeatingTable seatingTable) throws URISyntaxException {
        LOG.debug("REST request to save SeatingTable : {}", seatingTable);
        if (seatingTable.getId() != null) {
            throw new BadRequestAlertException("A new seatingTable cannot already have an ID", ENTITY_NAME, "idexists");
        }
        seatingTable = seatingTableRepository.save(seatingTable);
        return ResponseEntity.created(new URI("/api/seating-tables/" + seatingTable.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, seatingTable.getId().toString()))
            .body(seatingTable);
    }

    /**
     * {@code PUT  /seating-tables/:id} : Updates an existing seatingTable.
     *
     * @param id the id of the seatingTable to save.
     * @param seatingTable the seatingTable to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated seatingTable,
     * or with status {@code 400 (Bad Request)} if the seatingTable is not valid,
     * or with status {@code 500 (Internal Server Error)} if the seatingTable couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SeatingTable> updateSeatingTable(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody SeatingTable seatingTable
    ) throws URISyntaxException {
        LOG.debug("REST request to update SeatingTable : {}, {}", id, seatingTable);
        if (seatingTable.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, seatingTable.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!seatingTableRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        seatingTable = seatingTableRepository.save(seatingTable);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, seatingTable.getId().toString()))
            .body(seatingTable);
    }

    /**
     * {@code PATCH  /seating-tables/:id} : Partial updates given fields of an existing seatingTable, field will ignore if it is null
     *
     * @param id the id of the seatingTable to save.
     * @param seatingTable the seatingTable to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated seatingTable,
     * or with status {@code 400 (Bad Request)} if the seatingTable is not valid,
     * or with status {@code 404 (Not Found)} if the seatingTable is not found,
     * or with status {@code 500 (Internal Server Error)} if the seatingTable couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SeatingTable> partialUpdateSeatingTable(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody SeatingTable seatingTable
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update SeatingTable partially : {}, {}", id, seatingTable);
        if (seatingTable.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, seatingTable.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!seatingTableRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SeatingTable> result = seatingTableRepository
            .findById(seatingTable.getId())
            .map(existingSeatingTable -> {
                if (seatingTable.getTableNumber() != null) {
                    existingSeatingTable.setTableNumber(seatingTable.getTableNumber());
                }
                if (seatingTable.getMaxSeats() != null) {
                    existingSeatingTable.setMaxSeats(seatingTable.getMaxSeats());
                }
                if (seatingTable.getNearStage() != null) {
                    existingSeatingTable.setNearStage(seatingTable.getNearStage());
                }

                return existingSeatingTable;
            })
            .map(seatingTableRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, seatingTable.getId().toString())
        );
    }

    /**
     * {@code GET  /seating-tables} : get all the seatingTables.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of seatingTables in body.
     */
    @GetMapping("")
    public ResponseEntity<List<SeatingTable>> getAllSeatingTables(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of SeatingTables");
        Page<SeatingTable> page = seatingTableRepository.findAllByEventUserIsCurrentUser(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /seating-tables/:id} : get the "id" seatingTable.
     *
     * @param id the id of the seatingTable to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the seatingTable, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SeatingTable> getSeatingTable(@PathVariable("id") Long id) {
        LOG.debug("REST request to get SeatingTable : {}", id);
        Optional<SeatingTable> seatingTable = seatingTableRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(seatingTable);
    }

    /**
     * {@code DELETE  /seating-tables/:id} : delete the "id" seatingTable.
     *
     * @param id the id of the seatingTable to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeatingTable(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete SeatingTable : {}", id);
        seatingTableRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
