package com.lavi.tablearrangments.web.rest;

import static com.lavi.tablearrangments.domain.VenueTableAsserts.*;
import static com.lavi.tablearrangments.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lavi.tablearrangments.IntegrationTest;
import com.lavi.tablearrangments.domain.VenueTable;
import com.lavi.tablearrangments.repository.UserRepository;
import com.lavi.tablearrangments.repository.VenueTableRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
 * Integration tests for the {@link VenueTableResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class VenueTableResourceIT {

    private static final Integer DEFAULT_NUMBER_OF_TABLES = 1;
    private static final Integer UPDATED_NUMBER_OF_TABLES = 2;

    private static final Integer DEFAULT_NEAR_STAGE_TABLES = 1;
    private static final Integer UPDATED_NEAR_STAGE_TABLES = 2;

    private static final String DEFAULT_VENUE_NAME = "AAAAAAAAAA";
    private static final String UPDATED_VENUE_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_EVENT_OWNERS = "AAAAAAAAAA";
    private static final String UPDATED_EVENT_OWNERS = "BBBBBBBBBB";

    private static final String DEFAULT_GROOM_PARENTS = "AAAAAAAAAA";
    private static final String UPDATED_GROOM_PARENTS = "BBBBBBBBBB";

    private static final String DEFAULT_BRIDE_PARENTS = "AAAAAAAAAA";
    private static final String UPDATED_BRIDE_PARENTS = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_WEDDING_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_WEDDING_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final Instant DEFAULT_RECEPTION_TIME = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_RECEPTION_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_WEDDING_TIME = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_WEDDING_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/venue-tables";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private VenueTableRepository venueTableRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private VenueTableRepository venueTableRepositoryMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restVenueTableMockMvc;

    private VenueTable venueTable;

    private VenueTable insertedVenueTable;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VenueTable createEntity() {
        return new VenueTable()
            .numberOfTables(DEFAULT_NUMBER_OF_TABLES)
            .nearStageTables(DEFAULT_NEAR_STAGE_TABLES)
            .venueName(DEFAULT_VENUE_NAME)
            .eventOwners(DEFAULT_EVENT_OWNERS)
            .groomParents(DEFAULT_GROOM_PARENTS)
            .brideParents(DEFAULT_BRIDE_PARENTS)
            .weddingDate(DEFAULT_WEDDING_DATE)
            .receptionTime(DEFAULT_RECEPTION_TIME)
            .weddingTime(DEFAULT_WEDDING_TIME);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VenueTable createUpdatedEntity() {
        return new VenueTable()
            .numberOfTables(UPDATED_NUMBER_OF_TABLES)
            .nearStageTables(UPDATED_NEAR_STAGE_TABLES)
            .venueName(UPDATED_VENUE_NAME)
            .eventOwners(UPDATED_EVENT_OWNERS)
            .groomParents(UPDATED_GROOM_PARENTS)
            .brideParents(UPDATED_BRIDE_PARENTS)
            .weddingDate(UPDATED_WEDDING_DATE)
            .receptionTime(UPDATED_RECEPTION_TIME)
            .weddingTime(UPDATED_WEDDING_TIME);
    }

    @BeforeEach
    public void initTest() {
        venueTable = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedVenueTable != null) {
            venueTableRepository.delete(insertedVenueTable);
            insertedVenueTable = null;
        }
    }

    @Test
    @Transactional
    void createVenueTable() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the VenueTable
        var returnedVenueTable = om.readValue(
            restVenueTableMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(venueTable)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            VenueTable.class
        );

        // Validate the VenueTable in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertVenueTableUpdatableFieldsEquals(returnedVenueTable, getPersistedVenueTable(returnedVenueTable));

        insertedVenueTable = returnedVenueTable;
    }

    @Test
    @Transactional
    void createVenueTableWithExistingId() throws Exception {
        // Create the VenueTable with an existing ID
        venueTable.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restVenueTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(venueTable)))
            .andExpect(status().isBadRequest());

        // Validate the VenueTable in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNumberOfTablesIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        venueTable.setNumberOfTables(null);

        // Create the VenueTable, which fails.

        restVenueTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(venueTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNearStageTablesIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        venueTable.setNearStageTables(null);

        // Create the VenueTable, which fails.

        restVenueTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(venueTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkVenueNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        venueTable.setVenueName(null);

        // Create the VenueTable, which fails.

        restVenueTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(venueTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEventOwnersIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        venueTable.setEventOwners(null);

        // Create the VenueTable, which fails.

        restVenueTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(venueTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkGroomParentsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        venueTable.setGroomParents(null);

        // Create the VenueTable, which fails.

        restVenueTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(venueTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkBrideParentsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        venueTable.setBrideParents(null);

        // Create the VenueTable, which fails.

        restVenueTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(venueTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkWeddingDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        venueTable.setWeddingDate(null);

        // Create the VenueTable, which fails.

        restVenueTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(venueTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkReceptionTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        venueTable.setReceptionTime(null);

        // Create the VenueTable, which fails.

        restVenueTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(venueTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkWeddingTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        venueTable.setWeddingTime(null);

        // Create the VenueTable, which fails.

        restVenueTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(venueTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllVenueTables() throws Exception {
        // Initialize the database
        insertedVenueTable = venueTableRepository.saveAndFlush(venueTable);

        // Get all the venueTableList
        restVenueTableMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(venueTable.getId().intValue())))
            .andExpect(jsonPath("$.[*].numberOfTables").value(hasItem(DEFAULT_NUMBER_OF_TABLES)))
            .andExpect(jsonPath("$.[*].nearStageTables").value(hasItem(DEFAULT_NEAR_STAGE_TABLES)))
            .andExpect(jsonPath("$.[*].venueName").value(hasItem(DEFAULT_VENUE_NAME)))
            .andExpect(jsonPath("$.[*].eventOwners").value(hasItem(DEFAULT_EVENT_OWNERS)))
            .andExpect(jsonPath("$.[*].groomParents").value(hasItem(DEFAULT_GROOM_PARENTS)))
            .andExpect(jsonPath("$.[*].brideParents").value(hasItem(DEFAULT_BRIDE_PARENTS)))
            .andExpect(jsonPath("$.[*].weddingDate").value(hasItem(DEFAULT_WEDDING_DATE.toString())))
            .andExpect(jsonPath("$.[*].receptionTime").value(hasItem(DEFAULT_RECEPTION_TIME.toString())))
            .andExpect(jsonPath("$.[*].weddingTime").value(hasItem(DEFAULT_WEDDING_TIME.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllVenueTablesWithEagerRelationshipsIsEnabled() throws Exception {
        when(venueTableRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restVenueTableMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(venueTableRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllVenueTablesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(venueTableRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restVenueTableMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(venueTableRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getVenueTable() throws Exception {
        // Initialize the database
        insertedVenueTable = venueTableRepository.saveAndFlush(venueTable);

        // Get the venueTable
        restVenueTableMockMvc
            .perform(get(ENTITY_API_URL_ID, venueTable.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(venueTable.getId().intValue()))
            .andExpect(jsonPath("$.numberOfTables").value(DEFAULT_NUMBER_OF_TABLES))
            .andExpect(jsonPath("$.nearStageTables").value(DEFAULT_NEAR_STAGE_TABLES))
            .andExpect(jsonPath("$.venueName").value(DEFAULT_VENUE_NAME))
            .andExpect(jsonPath("$.eventOwners").value(DEFAULT_EVENT_OWNERS))
            .andExpect(jsonPath("$.groomParents").value(DEFAULT_GROOM_PARENTS))
            .andExpect(jsonPath("$.brideParents").value(DEFAULT_BRIDE_PARENTS))
            .andExpect(jsonPath("$.weddingDate").value(DEFAULT_WEDDING_DATE.toString()))
            .andExpect(jsonPath("$.receptionTime").value(DEFAULT_RECEPTION_TIME.toString()))
            .andExpect(jsonPath("$.weddingTime").value(DEFAULT_WEDDING_TIME.toString()));
    }

    @Test
    @Transactional
    void getNonExistingVenueTable() throws Exception {
        // Get the venueTable
        restVenueTableMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingVenueTable() throws Exception {
        // Initialize the database
        insertedVenueTable = venueTableRepository.saveAndFlush(venueTable);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the venueTable
        VenueTable updatedVenueTable = venueTableRepository.findById(venueTable.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedVenueTable are not directly saved in db
        em.detach(updatedVenueTable);
        updatedVenueTable
            .numberOfTables(UPDATED_NUMBER_OF_TABLES)
            .nearStageTables(UPDATED_NEAR_STAGE_TABLES)
            .venueName(UPDATED_VENUE_NAME)
            .eventOwners(UPDATED_EVENT_OWNERS)
            .groomParents(UPDATED_GROOM_PARENTS)
            .brideParents(UPDATED_BRIDE_PARENTS)
            .weddingDate(UPDATED_WEDDING_DATE)
            .receptionTime(UPDATED_RECEPTION_TIME)
            .weddingTime(UPDATED_WEDDING_TIME);

        restVenueTableMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedVenueTable.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedVenueTable))
            )
            .andExpect(status().isOk());

        // Validate the VenueTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedVenueTableToMatchAllProperties(updatedVenueTable);
    }

    @Test
    @Transactional
    void putNonExistingVenueTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        venueTable.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVenueTableMockMvc
            .perform(
                put(ENTITY_API_URL_ID, venueTable.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(venueTable))
            )
            .andExpect(status().isBadRequest());

        // Validate the VenueTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchVenueTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        venueTable.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVenueTableMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(venueTable))
            )
            .andExpect(status().isBadRequest());

        // Validate the VenueTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamVenueTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        venueTable.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVenueTableMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(venueTable)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the VenueTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateVenueTableWithPatch() throws Exception {
        // Initialize the database
        insertedVenueTable = venueTableRepository.saveAndFlush(venueTable);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the venueTable using partial update
        VenueTable partialUpdatedVenueTable = new VenueTable();
        partialUpdatedVenueTable.setId(venueTable.getId());

        partialUpdatedVenueTable
            .numberOfTables(UPDATED_NUMBER_OF_TABLES)
            .nearStageTables(UPDATED_NEAR_STAGE_TABLES)
            .groomParents(UPDATED_GROOM_PARENTS)
            .receptionTime(UPDATED_RECEPTION_TIME);

        restVenueTableMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVenueTable.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedVenueTable))
            )
            .andExpect(status().isOk());

        // Validate the VenueTable in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVenueTableUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedVenueTable, venueTable),
            getPersistedVenueTable(venueTable)
        );
    }

    @Test
    @Transactional
    void fullUpdateVenueTableWithPatch() throws Exception {
        // Initialize the database
        insertedVenueTable = venueTableRepository.saveAndFlush(venueTable);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the venueTable using partial update
        VenueTable partialUpdatedVenueTable = new VenueTable();
        partialUpdatedVenueTable.setId(venueTable.getId());

        partialUpdatedVenueTable
            .numberOfTables(UPDATED_NUMBER_OF_TABLES)
            .nearStageTables(UPDATED_NEAR_STAGE_TABLES)
            .venueName(UPDATED_VENUE_NAME)
            .eventOwners(UPDATED_EVENT_OWNERS)
            .groomParents(UPDATED_GROOM_PARENTS)
            .brideParents(UPDATED_BRIDE_PARENTS)
            .weddingDate(UPDATED_WEDDING_DATE)
            .receptionTime(UPDATED_RECEPTION_TIME)
            .weddingTime(UPDATED_WEDDING_TIME);

        restVenueTableMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVenueTable.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedVenueTable))
            )
            .andExpect(status().isOk());

        // Validate the VenueTable in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVenueTableUpdatableFieldsEquals(partialUpdatedVenueTable, getPersistedVenueTable(partialUpdatedVenueTable));
    }

    @Test
    @Transactional
    void patchNonExistingVenueTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        venueTable.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVenueTableMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, venueTable.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(venueTable))
            )
            .andExpect(status().isBadRequest());

        // Validate the VenueTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchVenueTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        venueTable.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVenueTableMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(venueTable))
            )
            .andExpect(status().isBadRequest());

        // Validate the VenueTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamVenueTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        venueTable.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVenueTableMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(venueTable)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the VenueTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteVenueTable() throws Exception {
        // Initialize the database
        insertedVenueTable = venueTableRepository.saveAndFlush(venueTable);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the venueTable
        restVenueTableMockMvc
            .perform(delete(ENTITY_API_URL_ID, venueTable.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return venueTableRepository.count();
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

    protected VenueTable getPersistedVenueTable(VenueTable venueTable) {
        return venueTableRepository.findById(venueTable.getId()).orElseThrow();
    }

    protected void assertPersistedVenueTableToMatchAllProperties(VenueTable expectedVenueTable) {
        assertVenueTableAllPropertiesEquals(expectedVenueTable, getPersistedVenueTable(expectedVenueTable));
    }

    protected void assertPersistedVenueTableToMatchUpdatableProperties(VenueTable expectedVenueTable) {
        assertVenueTableAllUpdatablePropertiesEquals(expectedVenueTable, getPersistedVenueTable(expectedVenueTable));
    }
}
