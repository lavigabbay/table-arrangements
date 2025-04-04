package com.lavi.tablearrangments.web.rest;

import static com.lavi.tablearrangments.domain.SeatingTableAsserts.*;
import static com.lavi.tablearrangments.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lavi.tablearrangments.IntegrationTest;
import com.lavi.tablearrangments.domain.SeatingTable;
import com.lavi.tablearrangments.repository.SeatingTableRepository;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link SeatingTableResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class SeatingTableResourceIT {

    private static final Integer DEFAULT_TABLE_NUMBER = 1;
    private static final Integer UPDATED_TABLE_NUMBER = 2;

    private static final Integer DEFAULT_MAX_SEATS = 1;
    private static final Integer UPDATED_MAX_SEATS = 2;

    private static final Boolean DEFAULT_NEAR_STAGE = false;
    private static final Boolean UPDATED_NEAR_STAGE = true;

    private static final String ENTITY_API_URL = "/api/seating-tables";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SeatingTableRepository seatingTableRepository;

    @Mock
    private SeatingTableRepository seatingTableRepositoryMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSeatingTableMockMvc;

    private SeatingTable seatingTable;

    private SeatingTable insertedSeatingTable;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SeatingTable createEntity() {
        return new SeatingTable().tableNumber(DEFAULT_TABLE_NUMBER).maxSeats(DEFAULT_MAX_SEATS).nearStage(DEFAULT_NEAR_STAGE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SeatingTable createUpdatedEntity() {
        return new SeatingTable().tableNumber(UPDATED_TABLE_NUMBER).maxSeats(UPDATED_MAX_SEATS).nearStage(UPDATED_NEAR_STAGE);
    }

    @BeforeEach
    public void initTest() {
        seatingTable = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedSeatingTable != null) {
            seatingTableRepository.delete(insertedSeatingTable);
            insertedSeatingTable = null;
        }
    }

    @Test
    @Transactional
    void createSeatingTable() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the SeatingTable
        var returnedSeatingTable = om.readValue(
            restSeatingTableMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(seatingTable)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            SeatingTable.class
        );

        // Validate the SeatingTable in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertSeatingTableUpdatableFieldsEquals(returnedSeatingTable, getPersistedSeatingTable(returnedSeatingTable));

        insertedSeatingTable = returnedSeatingTable;
    }

    @Test
    @Transactional
    void createSeatingTableWithExistingId() throws Exception {
        // Create the SeatingTable with an existing ID
        seatingTable.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSeatingTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(seatingTable)))
            .andExpect(status().isBadRequest());

        // Validate the SeatingTable in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTableNumberIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        seatingTable.setTableNumber(null);

        // Create the SeatingTable, which fails.

        restSeatingTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(seatingTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkMaxSeatsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        seatingTable.setMaxSeats(null);

        // Create the SeatingTable, which fails.

        restSeatingTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(seatingTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSeatingTables() throws Exception {
        // Initialize the database
        insertedSeatingTable = seatingTableRepository.saveAndFlush(seatingTable);

        // Get all the seatingTableList
        restSeatingTableMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(seatingTable.getId().intValue())))
            .andExpect(jsonPath("$.[*].tableNumber").value(hasItem(DEFAULT_TABLE_NUMBER)))
            .andExpect(jsonPath("$.[*].maxSeats").value(hasItem(DEFAULT_MAX_SEATS)))
            .andExpect(jsonPath("$.[*].nearStage").value(hasItem(DEFAULT_NEAR_STAGE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSeatingTablesWithEagerRelationshipsIsEnabled() throws Exception {
        when(seatingTableRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSeatingTableMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(seatingTableRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSeatingTablesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(seatingTableRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSeatingTableMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(seatingTableRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getSeatingTable() throws Exception {
        // Initialize the database
        insertedSeatingTable = seatingTableRepository.saveAndFlush(seatingTable);

        // Get the seatingTable
        restSeatingTableMockMvc
            .perform(get(ENTITY_API_URL_ID, seatingTable.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(seatingTable.getId().intValue()))
            .andExpect(jsonPath("$.tableNumber").value(DEFAULT_TABLE_NUMBER))
            .andExpect(jsonPath("$.maxSeats").value(DEFAULT_MAX_SEATS))
            .andExpect(jsonPath("$.nearStage").value(DEFAULT_NEAR_STAGE));
    }

    @Test
    @Transactional
    void getNonExistingSeatingTable() throws Exception {
        // Get the seatingTable
        restSeatingTableMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSeatingTable() throws Exception {
        // Initialize the database
        insertedSeatingTable = seatingTableRepository.saveAndFlush(seatingTable);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the seatingTable
        SeatingTable updatedSeatingTable = seatingTableRepository.findById(seatingTable.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSeatingTable are not directly saved in db
        em.detach(updatedSeatingTable);
        updatedSeatingTable.tableNumber(UPDATED_TABLE_NUMBER).maxSeats(UPDATED_MAX_SEATS).nearStage(UPDATED_NEAR_STAGE);

        restSeatingTableMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedSeatingTable.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedSeatingTable))
            )
            .andExpect(status().isOk());

        // Validate the SeatingTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSeatingTableToMatchAllProperties(updatedSeatingTable);
    }

    @Test
    @Transactional
    void putNonExistingSeatingTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        seatingTable.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSeatingTableMockMvc
            .perform(
                put(ENTITY_API_URL_ID, seatingTable.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(seatingTable))
            )
            .andExpect(status().isBadRequest());

        // Validate the SeatingTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSeatingTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        seatingTable.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSeatingTableMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(seatingTable))
            )
            .andExpect(status().isBadRequest());

        // Validate the SeatingTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSeatingTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        seatingTable.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSeatingTableMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(seatingTable)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the SeatingTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSeatingTableWithPatch() throws Exception {
        // Initialize the database
        insertedSeatingTable = seatingTableRepository.saveAndFlush(seatingTable);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the seatingTable using partial update
        SeatingTable partialUpdatedSeatingTable = new SeatingTable();
        partialUpdatedSeatingTable.setId(seatingTable.getId());

        partialUpdatedSeatingTable.tableNumber(UPDATED_TABLE_NUMBER).nearStage(UPDATED_NEAR_STAGE);

        restSeatingTableMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSeatingTable.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSeatingTable))
            )
            .andExpect(status().isOk());

        // Validate the SeatingTable in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSeatingTableUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedSeatingTable, seatingTable),
            getPersistedSeatingTable(seatingTable)
        );
    }

    @Test
    @Transactional
    void fullUpdateSeatingTableWithPatch() throws Exception {
        // Initialize the database
        insertedSeatingTable = seatingTableRepository.saveAndFlush(seatingTable);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the seatingTable using partial update
        SeatingTable partialUpdatedSeatingTable = new SeatingTable();
        partialUpdatedSeatingTable.setId(seatingTable.getId());

        partialUpdatedSeatingTable.tableNumber(UPDATED_TABLE_NUMBER).maxSeats(UPDATED_MAX_SEATS).nearStage(UPDATED_NEAR_STAGE);

        restSeatingTableMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSeatingTable.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSeatingTable))
            )
            .andExpect(status().isOk());

        // Validate the SeatingTable in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSeatingTableUpdatableFieldsEquals(partialUpdatedSeatingTable, getPersistedSeatingTable(partialUpdatedSeatingTable));
    }

    @Test
    @Transactional
    void patchNonExistingSeatingTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        seatingTable.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSeatingTableMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, seatingTable.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(seatingTable))
            )
            .andExpect(status().isBadRequest());

        // Validate the SeatingTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSeatingTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        seatingTable.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSeatingTableMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(seatingTable))
            )
            .andExpect(status().isBadRequest());

        // Validate the SeatingTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSeatingTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        seatingTable.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSeatingTableMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(seatingTable)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the SeatingTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSeatingTable() throws Exception {
        // Initialize the database
        insertedSeatingTable = seatingTableRepository.saveAndFlush(seatingTable);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the seatingTable
        restSeatingTableMockMvc
            .perform(delete(ENTITY_API_URL_ID, seatingTable.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return seatingTableRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected SeatingTable getPersistedSeatingTable(SeatingTable seatingTable) {
        return seatingTableRepository.findById(seatingTable.getId()).orElseThrow();
    }

    protected void assertPersistedSeatingTableToMatchAllProperties(SeatingTable expectedSeatingTable) {
        assertSeatingTableAllPropertiesEquals(expectedSeatingTable, getPersistedSeatingTable(expectedSeatingTable));
    }

    protected void assertPersistedSeatingTableToMatchUpdatableProperties(SeatingTable expectedSeatingTable) {
        assertSeatingTableAllUpdatablePropertiesEquals(expectedSeatingTable, getPersistedSeatingTable(expectedSeatingTable));
    }
}
