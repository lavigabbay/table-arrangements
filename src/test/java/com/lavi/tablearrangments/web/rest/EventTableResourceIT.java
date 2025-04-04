package com.lavi.tablearrangments.web.rest;

import static com.lavi.tablearrangments.domain.EventTableAsserts.*;
import static com.lavi.tablearrangments.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lavi.tablearrangments.IntegrationTest;
import com.lavi.tablearrangments.domain.EventTable;
import com.lavi.tablearrangments.repository.EventTableRepository;
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
 * Integration tests for the {@link EventTableResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class EventTableResourceIT {

    private static final Integer DEFAULT_TABLE_NUMBER = 1;
    private static final Integer UPDATED_TABLE_NUMBER = 2;

    private static final Integer DEFAULT_MAX_SEATS = 1;
    private static final Integer UPDATED_MAX_SEATS = 2;

    private static final String ENTITY_API_URL = "/api/event-tables";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EventTableRepository eventTableRepository;

    @Mock
    private EventTableRepository eventTableRepositoryMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restEventTableMockMvc;

    private EventTable eventTable;

    private EventTable insertedEventTable;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventTable createEntity() {
        return new EventTable().tableNumber(DEFAULT_TABLE_NUMBER).maxSeats(DEFAULT_MAX_SEATS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventTable createUpdatedEntity() {
        return new EventTable().tableNumber(UPDATED_TABLE_NUMBER).maxSeats(UPDATED_MAX_SEATS);
    }

    @BeforeEach
    public void initTest() {
        eventTable = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedEventTable != null) {
            eventTableRepository.delete(insertedEventTable);
            insertedEventTable = null;
        }
    }

    @Test
    @Transactional
    void createEventTable() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the EventTable
        var returnedEventTable = om.readValue(
            restEventTableMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventTable)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            EventTable.class
        );

        // Validate the EventTable in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertEventTableUpdatableFieldsEquals(returnedEventTable, getPersistedEventTable(returnedEventTable));

        insertedEventTable = returnedEventTable;
    }

    @Test
    @Transactional
    void createEventTableWithExistingId() throws Exception {
        // Create the EventTable with an existing ID
        eventTable.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restEventTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventTable)))
            .andExpect(status().isBadRequest());

        // Validate the EventTable in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTableNumberIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        eventTable.setTableNumber(null);

        // Create the EventTable, which fails.

        restEventTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkMaxSeatsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        eventTable.setMaxSeats(null);

        // Create the EventTable, which fails.

        restEventTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllEventTables() throws Exception {
        // Initialize the database
        insertedEventTable = eventTableRepository.saveAndFlush(eventTable);

        // Get all the eventTableList
        restEventTableMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventTable.getId().intValue())))
            .andExpect(jsonPath("$.[*].tableNumber").value(hasItem(DEFAULT_TABLE_NUMBER)))
            .andExpect(jsonPath("$.[*].maxSeats").value(hasItem(DEFAULT_MAX_SEATS)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllEventTablesWithEagerRelationshipsIsEnabled() throws Exception {
        when(eventTableRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restEventTableMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(eventTableRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllEventTablesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(eventTableRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restEventTableMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(eventTableRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getEventTable() throws Exception {
        // Initialize the database
        insertedEventTable = eventTableRepository.saveAndFlush(eventTable);

        // Get the eventTable
        restEventTableMockMvc
            .perform(get(ENTITY_API_URL_ID, eventTable.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(eventTable.getId().intValue()))
            .andExpect(jsonPath("$.tableNumber").value(DEFAULT_TABLE_NUMBER))
            .andExpect(jsonPath("$.maxSeats").value(DEFAULT_MAX_SEATS));
    }

    @Test
    @Transactional
    void getNonExistingEventTable() throws Exception {
        // Get the eventTable
        restEventTableMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingEventTable() throws Exception {
        // Initialize the database
        insertedEventTable = eventTableRepository.saveAndFlush(eventTable);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the eventTable
        EventTable updatedEventTable = eventTableRepository.findById(eventTable.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedEventTable are not directly saved in db
        em.detach(updatedEventTable);
        updatedEventTable.tableNumber(UPDATED_TABLE_NUMBER).maxSeats(UPDATED_MAX_SEATS);

        restEventTableMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedEventTable.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedEventTable))
            )
            .andExpect(status().isOk());

        // Validate the EventTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedEventTableToMatchAllProperties(updatedEventTable);
    }

    @Test
    @Transactional
    void putNonExistingEventTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventTable.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEventTableMockMvc
            .perform(
                put(ENTITY_API_URL_ID, eventTable.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventTable))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchEventTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventTable.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventTableMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(eventTable))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamEventTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventTable.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventTableMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventTable)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the EventTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateEventTableWithPatch() throws Exception {
        // Initialize the database
        insertedEventTable = eventTableRepository.saveAndFlush(eventTable);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the eventTable using partial update
        EventTable partialUpdatedEventTable = new EventTable();
        partialUpdatedEventTable.setId(eventTable.getId());

        partialUpdatedEventTable.tableNumber(UPDATED_TABLE_NUMBER).maxSeats(UPDATED_MAX_SEATS);

        restEventTableMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEventTable.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEventTable))
            )
            .andExpect(status().isOk());

        // Validate the EventTable in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEventTableUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedEventTable, eventTable),
            getPersistedEventTable(eventTable)
        );
    }

    @Test
    @Transactional
    void fullUpdateEventTableWithPatch() throws Exception {
        // Initialize the database
        insertedEventTable = eventTableRepository.saveAndFlush(eventTable);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the eventTable using partial update
        EventTable partialUpdatedEventTable = new EventTable();
        partialUpdatedEventTable.setId(eventTable.getId());

        partialUpdatedEventTable.tableNumber(UPDATED_TABLE_NUMBER).maxSeats(UPDATED_MAX_SEATS);

        restEventTableMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEventTable.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEventTable))
            )
            .andExpect(status().isOk());

        // Validate the EventTable in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEventTableUpdatableFieldsEquals(partialUpdatedEventTable, getPersistedEventTable(partialUpdatedEventTable));
    }

    @Test
    @Transactional
    void patchNonExistingEventTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventTable.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEventTableMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, eventTable.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(eventTable))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchEventTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventTable.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventTableMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(eventTable))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamEventTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventTable.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventTableMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(eventTable)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the EventTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteEventTable() throws Exception {
        // Initialize the database
        insertedEventTable = eventTableRepository.saveAndFlush(eventTable);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the eventTable
        restEventTableMockMvc
            .perform(delete(ENTITY_API_URL_ID, eventTable.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return eventTableRepository.count();
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

    protected EventTable getPersistedEventTable(EventTable eventTable) {
        return eventTableRepository.findById(eventTable.getId()).orElseThrow();
    }

    protected void assertPersistedEventTableToMatchAllProperties(EventTable expectedEventTable) {
        assertEventTableAllPropertiesEquals(expectedEventTable, getPersistedEventTable(expectedEventTable));
    }

    protected void assertPersistedEventTableToMatchUpdatableProperties(EventTable expectedEventTable) {
        assertEventTableAllUpdatablePropertiesEquals(expectedEventTable, getPersistedEventTable(expectedEventTable));
    }
}
