package com.lavi.tablearrangments.web.rest;

import com.lavi.tablearrangments.domain.VenueTable;
import com.lavi.tablearrangments.repository.VenueTableRepository;
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
 * REST controller for managing {@link com.lavi.tablearrangments.domain.VenueTable}.
 */
@RestController
@RequestMapping("/api/venue-tables")
@Transactional
public class VenueTableResource {

    private static final Logger LOG = LoggerFactory.getLogger(VenueTableResource.class);

    private static final String ENTITY_NAME = "venueTable";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final VenueTableRepository venueTableRepository;

    public VenueTableResource(VenueTableRepository venueTableRepository) {
        this.venueTableRepository = venueTableRepository;
    }

    /**
     * {@code POST  /venue-tables} : Create a new venueTable.
     *
     * @param venueTable the venueTable to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new venueTable, or with status {@code 400 (Bad Request)} if the venueTable has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<VenueTable> createVenueTable(@Valid @RequestBody VenueTable venueTable) throws URISyntaxException {
        LOG.debug("REST request to save VenueTable : {}", venueTable);
        if (venueTable.getId() != null) {
            throw new BadRequestAlertException("A new venueTable cannot already have an ID", ENTITY_NAME, "idexists");
        }
        venueTable = venueTableRepository.save(venueTable);
        return ResponseEntity.created(new URI("/api/venue-tables/" + venueTable.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, venueTable.getId().toString()))
            .body(venueTable);
    }

    /**
     * {@code PUT  /venue-tables/:id} : Updates an existing venueTable.
     *
     * @param id the id of the venueTable to save.
     * @param venueTable the venueTable to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated venueTable,
     * or with status {@code 400 (Bad Request)} if the venueTable is not valid,
     * or with status {@code 500 (Internal Server Error)} if the venueTable couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<VenueTable> updateVenueTable(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody VenueTable venueTable
    ) throws URISyntaxException {
        LOG.debug("REST request to update VenueTable : {}, {}", id, venueTable);
        if (venueTable.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, venueTable.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!venueTableRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        venueTable = venueTableRepository.save(venueTable);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, venueTable.getId().toString()))
            .body(venueTable);
    }

    /**
     * {@code PATCH  /venue-tables/:id} : Partial updates given fields of an existing venueTable, field will ignore if it is null
     *
     * @param id the id of the venueTable to save.
     * @param venueTable the venueTable to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated venueTable,
     * or with status {@code 400 (Bad Request)} if the venueTable is not valid,
     * or with status {@code 404 (Not Found)} if the venueTable is not found,
     * or with status {@code 500 (Internal Server Error)} if the venueTable couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<VenueTable> partialUpdateVenueTable(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody VenueTable venueTable
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update VenueTable partially : {}, {}", id, venueTable);
        if (venueTable.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, venueTable.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!venueTableRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<VenueTable> result = venueTableRepository
            .findById(venueTable.getId())
            .map(existingVenueTable -> {
                if (venueTable.getNumberOfTables() != null) {
                    existingVenueTable.setNumberOfTables(venueTable.getNumberOfTables());
                }
                if (venueTable.getNearStageTables() != null) {
                    existingVenueTable.setNearStageTables(venueTable.getNearStageTables());
                }
                if (venueTable.getVenueName() != null) {
                    existingVenueTable.setVenueName(venueTable.getVenueName());
                }
                if (venueTable.getEventOwners() != null) {
                    existingVenueTable.setEventOwners(venueTable.getEventOwners());
                }
                if (venueTable.getGroomParents() != null) {
                    existingVenueTable.setGroomParents(venueTable.getGroomParents());
                }
                if (venueTable.getBrideParents() != null) {
                    existingVenueTable.setBrideParents(venueTable.getBrideParents());
                }
                if (venueTable.getWeddingDate() != null) {
                    existingVenueTable.setWeddingDate(venueTable.getWeddingDate());
                }
                if (venueTable.getReceptionTime() != null) {
                    existingVenueTable.setReceptionTime(venueTable.getReceptionTime());
                }
                if (venueTable.getWeddingTime() != null) {
                    existingVenueTable.setWeddingTime(venueTable.getWeddingTime());
                }

                return existingVenueTable;
            })
            .map(venueTableRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, venueTable.getId().toString())
        );
    }

    /**
     * {@code GET  /venue-tables} : get all the venueTables.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of venueTables in body.
     */
    @GetMapping("")
    public ResponseEntity<List<VenueTable>> getAllVenueTables(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of VenueTables");
        Page<VenueTable> page;
        if (eagerload) {
            page = venueTableRepository.findAllWithEagerRelationships(pageable);
        } else {
            page = venueTableRepository.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /venue-tables/:id} : get the "id" venueTable.
     *
     * @param id the id of the venueTable to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the venueTable, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VenueTable> getVenueTable(@PathVariable("id") Long id) {
        LOG.debug("REST request to get VenueTable : {}", id);
        Optional<VenueTable> venueTable = venueTableRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(venueTable);
    }

    /**
     * {@code DELETE  /venue-tables/:id} : delete the "id" venueTable.
     *
     * @param id the id of the venueTable to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenueTable(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete VenueTable : {}", id);
        venueTableRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
