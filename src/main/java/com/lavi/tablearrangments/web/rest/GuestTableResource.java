package com.lavi.tablearrangments.web.rest;

import com.lavi.tablearrangments.domain.GuestTable;
import com.lavi.tablearrangments.repository.GuestTableRepository;
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
 * REST controller for managing {@link com.lavi.tablearrangments.domain.GuestTable}.
 */
@RestController
@RequestMapping("/api/guest-tables")
@Transactional
public class GuestTableResource {

    private static final Logger LOG = LoggerFactory.getLogger(GuestTableResource.class);

    private static final String ENTITY_NAME = "guestTable";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final GuestTableRepository guestTableRepository;

    public GuestTableResource(GuestTableRepository guestTableRepository) {
        this.guestTableRepository = guestTableRepository;
    }

    /**
     * {@code POST  /guest-tables} : Create a new guestTable.
     *
     * @param guestTable the guestTable to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new guestTable, or with status {@code 400 (Bad Request)} if the guestTable has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<GuestTable> createGuestTable(@Valid @RequestBody GuestTable guestTable) throws URISyntaxException {
        LOG.debug("REST request to save GuestTable : {}", guestTable);
        if (guestTable.getId() != null) {
            throw new BadRequestAlertException("A new guestTable cannot already have an ID", ENTITY_NAME, "idexists");
        }
        guestTable = guestTableRepository.save(guestTable);
        return ResponseEntity.created(new URI("/api/guest-tables/" + guestTable.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, guestTable.getId().toString()))
            .body(guestTable);
    }

    /**
     * {@code PUT  /guest-tables/:id} : Updates an existing guestTable.
     *
     * @param id the id of the guestTable to save.
     * @param guestTable the guestTable to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated guestTable,
     * or with status {@code 400 (Bad Request)} if the guestTable is not valid,
     * or with status {@code 500 (Internal Server Error)} if the guestTable couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<GuestTable> updateGuestTable(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody GuestTable guestTable
    ) throws URISyntaxException {
        LOG.debug("REST request to update GuestTable : {}, {}", id, guestTable);
        if (guestTable.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, guestTable.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!guestTableRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        guestTable = guestTableRepository.save(guestTable);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, guestTable.getId().toString()))
            .body(guestTable);
    }

    /**
     * {@code PATCH  /guest-tables/:id} : Partial updates given fields of an existing guestTable, field will ignore if it is null
     *
     * @param id the id of the guestTable to save.
     * @param guestTable the guestTable to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated guestTable,
     * or with status {@code 400 (Bad Request)} if the guestTable is not valid,
     * or with status {@code 404 (Not Found)} if the guestTable is not found,
     * or with status {@code 500 (Internal Server Error)} if the guestTable couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<GuestTable> partialUpdateGuestTable(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody GuestTable guestTable
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update GuestTable partially : {}, {}", id, guestTable);
        if (guestTable.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, guestTable.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!guestTableRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<GuestTable> result = guestTableRepository
            .findById(guestTable.getId())
            .map(existingGuestTable -> {
                if (guestTable.getLastNameAndFirstName() != null) {
                    existingGuestTable.setLastNameAndFirstName(guestTable.getLastNameAndFirstName());
                }
                if (guestTable.getNumberOfSeats() != null) {
                    existingGuestTable.setNumberOfSeats(guestTable.getNumberOfSeats());
                }
                if (guestTable.getPhone() != null) {
                    existingGuestTable.setPhone(guestTable.getPhone());
                }
                if (guestTable.getNearDanceFloor() != null) {
                    existingGuestTable.setNearDanceFloor(guestTable.getNearDanceFloor());
                }
                if (guestTable.getStatus() != null) {
                    existingGuestTable.setStatus(guestTable.getStatus());
                }
                if (guestTable.getSide() != null) {
                    existingGuestTable.setSide(guestTable.getSide());
                }
                if (guestTable.getRelation() != null) {
                    existingGuestTable.setRelation(guestTable.getRelation());
                }
                if (guestTable.getNotWithId() != null) {
                    existingGuestTable.setNotWithId(guestTable.getNotWithId());
                }
                if (guestTable.getWithId() != null) {
                    existingGuestTable.setWithId(guestTable.getWithId());
                }
                if (guestTable.getConditions() != null) {
                    existingGuestTable.setConditions(guestTable.getConditions());
                }
                if (guestTable.getAccessibility() != null) {
                    existingGuestTable.setAccessibility(guestTable.getAccessibility());
                }

                return existingGuestTable;
            })
            .map(guestTableRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, guestTable.getId().toString())
        );
    }

    /**
     * {@code GET  /guest-tables} : get all the guestTables.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of guestTables in body.
     */
    @GetMapping("")
    public ResponseEntity<List<GuestTable>> getAllGuestTables(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of GuestTables");
        Page<GuestTable> page;
        if (eagerload) {
            page = guestTableRepository.findAllWithEagerRelationships(pageable);
        } else {
            page = guestTableRepository.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /guest-tables/:id} : get the "id" guestTable.
     *
     * @param id the id of the guestTable to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the guestTable, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<GuestTable> getGuestTable(@PathVariable("id") Long id) {
        LOG.debug("REST request to get GuestTable : {}", id);
        Optional<GuestTable> guestTable = guestTableRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(guestTable);
    }

    /**
     * {@code DELETE  /guest-tables/:id} : delete the "id" guestTable.
     *
     * @param id the id of the guestTable to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGuestTable(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete GuestTable : {}", id);
        guestTableRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
