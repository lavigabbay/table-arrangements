package com.lavi.tablearrangments.web.rest;

import static com.lavi.tablearrangments.domain.GuestTableAsserts.*;
import static com.lavi.tablearrangments.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lavi.tablearrangments.IntegrationTest;
import com.lavi.tablearrangments.domain.GuestTable;
import com.lavi.tablearrangments.domain.enumeration.GuestRelation;
import com.lavi.tablearrangments.domain.enumeration.GuestStatus;
import com.lavi.tablearrangments.repository.GuestTableRepository;
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
 * Integration tests for the {@link GuestTableResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class GuestTableResourceIT {

    private static final String DEFAULT_LAST_NAME_AND_FIRST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LAST_NAME_AND_FIRST_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_NUMBER_OF_SEATS = 1;
    private static final Integer UPDATED_NUMBER_OF_SEATS = 2;

    private static final String DEFAULT_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_PHONE = "BBBBBBBBBB";

    private static final Boolean DEFAULT_NEAR_DANCE_FLOOR = false;
    private static final Boolean UPDATED_NEAR_DANCE_FLOOR = true;

    private static final GuestStatus DEFAULT_STATUS = GuestStatus.CONFIRMED;
    private static final GuestStatus UPDATED_STATUS = GuestStatus.WAITING_APPROVAL;

    private static final String DEFAULT_SIDE = "AAAAAAAAAA";
    private static final String UPDATED_SIDE = "BBBBBBBBBB";

    private static final GuestRelation DEFAULT_RELATION = GuestRelation.GROOM_FAMILY;
    private static final GuestRelation UPDATED_RELATION = GuestRelation.BRIDE_FAMILY;

    private static final Long DEFAULT_NOT_WITH_ID = 1L;
    private static final Long UPDATED_NOT_WITH_ID = 2L;

    private static final Long DEFAULT_WITH_ID = 1L;
    private static final Long UPDATED_WITH_ID = 2L;

    private static final String DEFAULT_CONDITIONS = "AAAAAAAAAA";
    private static final String UPDATED_CONDITIONS = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACCESSIBILITY = false;
    private static final Boolean UPDATED_ACCESSIBILITY = true;

    private static final String ENTITY_API_URL = "/api/guest-tables";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private GuestTableRepository guestTableRepository;

    @Mock
    private GuestTableRepository guestTableRepositoryMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restGuestTableMockMvc;

    private GuestTable guestTable;

    private GuestTable insertedGuestTable;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static GuestTable createEntity() {
        return new GuestTable()
            .lastNameAndFirstName(DEFAULT_LAST_NAME_AND_FIRST_NAME)
            .numberOfSeats(DEFAULT_NUMBER_OF_SEATS)
            .phone(DEFAULT_PHONE)
            .nearDanceFloor(DEFAULT_NEAR_DANCE_FLOOR)
            .status(DEFAULT_STATUS)
            .side(DEFAULT_SIDE)
            .relation(DEFAULT_RELATION)
            .notWithId(DEFAULT_NOT_WITH_ID)
            .withId(DEFAULT_WITH_ID)
            .conditions(DEFAULT_CONDITIONS)
            .accessibility(DEFAULT_ACCESSIBILITY);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static GuestTable createUpdatedEntity() {
        return new GuestTable()
            .lastNameAndFirstName(UPDATED_LAST_NAME_AND_FIRST_NAME)
            .numberOfSeats(UPDATED_NUMBER_OF_SEATS)
            .phone(UPDATED_PHONE)
            .nearDanceFloor(UPDATED_NEAR_DANCE_FLOOR)
            .status(UPDATED_STATUS)
            .side(UPDATED_SIDE)
            .relation(UPDATED_RELATION)
            .notWithId(UPDATED_NOT_WITH_ID)
            .withId(UPDATED_WITH_ID)
            .conditions(UPDATED_CONDITIONS)
            .accessibility(UPDATED_ACCESSIBILITY);
    }

    @BeforeEach
    public void initTest() {
        guestTable = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedGuestTable != null) {
            guestTableRepository.delete(insertedGuestTable);
            insertedGuestTable = null;
        }
    }

    @Test
    @Transactional
    void createGuestTable() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the GuestTable
        var returnedGuestTable = om.readValue(
            restGuestTableMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guestTable)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            GuestTable.class
        );

        // Validate the GuestTable in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertGuestTableUpdatableFieldsEquals(returnedGuestTable, getPersistedGuestTable(returnedGuestTable));

        insertedGuestTable = returnedGuestTable;
    }

    @Test
    @Transactional
    void createGuestTableWithExistingId() throws Exception {
        // Create the GuestTable with an existing ID
        guestTable.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restGuestTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guestTable)))
            .andExpect(status().isBadRequest());

        // Validate the GuestTable in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkLastNameAndFirstNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        guestTable.setLastNameAndFirstName(null);

        // Create the GuestTable, which fails.

        restGuestTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guestTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNumberOfSeatsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        guestTable.setNumberOfSeats(null);

        // Create the GuestTable, which fails.

        restGuestTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guestTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPhoneIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        guestTable.setPhone(null);

        // Create the GuestTable, which fails.

        restGuestTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guestTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        guestTable.setStatus(null);

        // Create the GuestTable, which fails.

        restGuestTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guestTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSideIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        guestTable.setSide(null);

        // Create the GuestTable, which fails.

        restGuestTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guestTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkRelationIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        guestTable.setRelation(null);

        // Create the GuestTable, which fails.

        restGuestTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guestTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAccessibilityIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        guestTable.setAccessibility(null);

        // Create the GuestTable, which fails.

        restGuestTableMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guestTable)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllGuestTables() throws Exception {
        // Initialize the database
        insertedGuestTable = guestTableRepository.saveAndFlush(guestTable);

        // Get all the guestTableList
        restGuestTableMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(guestTable.getId().intValue())))
            .andExpect(jsonPath("$.[*].lastNameAndFirstName").value(hasItem(DEFAULT_LAST_NAME_AND_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].numberOfSeats").value(hasItem(DEFAULT_NUMBER_OF_SEATS)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].nearDanceFloor").value(hasItem(DEFAULT_NEAR_DANCE_FLOOR)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].side").value(hasItem(DEFAULT_SIDE)))
            .andExpect(jsonPath("$.[*].relation").value(hasItem(DEFAULT_RELATION.toString())))
            .andExpect(jsonPath("$.[*].notWithId").value(hasItem(DEFAULT_NOT_WITH_ID.intValue())))
            .andExpect(jsonPath("$.[*].withId").value(hasItem(DEFAULT_WITH_ID.intValue())))
            .andExpect(jsonPath("$.[*].conditions").value(hasItem(DEFAULT_CONDITIONS)))
            .andExpect(jsonPath("$.[*].accessibility").value(hasItem(DEFAULT_ACCESSIBILITY)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllGuestTablesWithEagerRelationshipsIsEnabled() throws Exception {
        when(guestTableRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restGuestTableMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(guestTableRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllGuestTablesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(guestTableRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restGuestTableMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(guestTableRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getGuestTable() throws Exception {
        // Initialize the database
        insertedGuestTable = guestTableRepository.saveAndFlush(guestTable);

        // Get the guestTable
        restGuestTableMockMvc
            .perform(get(ENTITY_API_URL_ID, guestTable.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(guestTable.getId().intValue()))
            .andExpect(jsonPath("$.lastNameAndFirstName").value(DEFAULT_LAST_NAME_AND_FIRST_NAME))
            .andExpect(jsonPath("$.numberOfSeats").value(DEFAULT_NUMBER_OF_SEATS))
            .andExpect(jsonPath("$.phone").value(DEFAULT_PHONE))
            .andExpect(jsonPath("$.nearDanceFloor").value(DEFAULT_NEAR_DANCE_FLOOR))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.side").value(DEFAULT_SIDE))
            .andExpect(jsonPath("$.relation").value(DEFAULT_RELATION.toString()))
            .andExpect(jsonPath("$.notWithId").value(DEFAULT_NOT_WITH_ID.intValue()))
            .andExpect(jsonPath("$.withId").value(DEFAULT_WITH_ID.intValue()))
            .andExpect(jsonPath("$.conditions").value(DEFAULT_CONDITIONS))
            .andExpect(jsonPath("$.accessibility").value(DEFAULT_ACCESSIBILITY));
    }

    @Test
    @Transactional
    void getNonExistingGuestTable() throws Exception {
        // Get the guestTable
        restGuestTableMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingGuestTable() throws Exception {
        // Initialize the database
        insertedGuestTable = guestTableRepository.saveAndFlush(guestTable);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the guestTable
        GuestTable updatedGuestTable = guestTableRepository.findById(guestTable.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedGuestTable are not directly saved in db
        em.detach(updatedGuestTable);
        updatedGuestTable
            .lastNameAndFirstName(UPDATED_LAST_NAME_AND_FIRST_NAME)
            .numberOfSeats(UPDATED_NUMBER_OF_SEATS)
            .phone(UPDATED_PHONE)
            .nearDanceFloor(UPDATED_NEAR_DANCE_FLOOR)
            .status(UPDATED_STATUS)
            .side(UPDATED_SIDE)
            .relation(UPDATED_RELATION)
            .notWithId(UPDATED_NOT_WITH_ID)
            .withId(UPDATED_WITH_ID)
            .conditions(UPDATED_CONDITIONS)
            .accessibility(UPDATED_ACCESSIBILITY);

        restGuestTableMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedGuestTable.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedGuestTable))
            )
            .andExpect(status().isOk());

        // Validate the GuestTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedGuestTableToMatchAllProperties(updatedGuestTable);
    }

    @Test
    @Transactional
    void putNonExistingGuestTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        guestTable.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGuestTableMockMvc
            .perform(
                put(ENTITY_API_URL_ID, guestTable.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guestTable))
            )
            .andExpect(status().isBadRequest());

        // Validate the GuestTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchGuestTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        guestTable.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGuestTableMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(guestTable))
            )
            .andExpect(status().isBadRequest());

        // Validate the GuestTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamGuestTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        guestTable.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGuestTableMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guestTable)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the GuestTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateGuestTableWithPatch() throws Exception {
        // Initialize the database
        insertedGuestTable = guestTableRepository.saveAndFlush(guestTable);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the guestTable using partial update
        GuestTable partialUpdatedGuestTable = new GuestTable();
        partialUpdatedGuestTable.setId(guestTable.getId());

        partialUpdatedGuestTable
            .lastNameAndFirstName(UPDATED_LAST_NAME_AND_FIRST_NAME)
            .numberOfSeats(UPDATED_NUMBER_OF_SEATS)
            .phone(UPDATED_PHONE)
            .side(UPDATED_SIDE)
            .relation(UPDATED_RELATION)
            .accessibility(UPDATED_ACCESSIBILITY);

        restGuestTableMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGuestTable.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGuestTable))
            )
            .andExpect(status().isOk());

        // Validate the GuestTable in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertGuestTableUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedGuestTable, guestTable),
            getPersistedGuestTable(guestTable)
        );
    }

    @Test
    @Transactional
    void fullUpdateGuestTableWithPatch() throws Exception {
        // Initialize the database
        insertedGuestTable = guestTableRepository.saveAndFlush(guestTable);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the guestTable using partial update
        GuestTable partialUpdatedGuestTable = new GuestTable();
        partialUpdatedGuestTable.setId(guestTable.getId());

        partialUpdatedGuestTable
            .lastNameAndFirstName(UPDATED_LAST_NAME_AND_FIRST_NAME)
            .numberOfSeats(UPDATED_NUMBER_OF_SEATS)
            .phone(UPDATED_PHONE)
            .nearDanceFloor(UPDATED_NEAR_DANCE_FLOOR)
            .status(UPDATED_STATUS)
            .side(UPDATED_SIDE)
            .relation(UPDATED_RELATION)
            .notWithId(UPDATED_NOT_WITH_ID)
            .withId(UPDATED_WITH_ID)
            .conditions(UPDATED_CONDITIONS)
            .accessibility(UPDATED_ACCESSIBILITY);

        restGuestTableMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGuestTable.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGuestTable))
            )
            .andExpect(status().isOk());

        // Validate the GuestTable in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertGuestTableUpdatableFieldsEquals(partialUpdatedGuestTable, getPersistedGuestTable(partialUpdatedGuestTable));
    }

    @Test
    @Transactional
    void patchNonExistingGuestTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        guestTable.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGuestTableMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, guestTable.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(guestTable))
            )
            .andExpect(status().isBadRequest());

        // Validate the GuestTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchGuestTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        guestTable.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGuestTableMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(guestTable))
            )
            .andExpect(status().isBadRequest());

        // Validate the GuestTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamGuestTable() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        guestTable.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGuestTableMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(guestTable)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the GuestTable in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteGuestTable() throws Exception {
        // Initialize the database
        insertedGuestTable = guestTableRepository.saveAndFlush(guestTable);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the guestTable
        restGuestTableMockMvc
            .perform(delete(ENTITY_API_URL_ID, guestTable.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return guestTableRepository.count();
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

    protected GuestTable getPersistedGuestTable(GuestTable guestTable) {
        return guestTableRepository.findById(guestTable.getId()).orElseThrow();
    }

    protected void assertPersistedGuestTableToMatchAllProperties(GuestTable expectedGuestTable) {
        assertGuestTableAllPropertiesEquals(expectedGuestTable, getPersistedGuestTable(expectedGuestTable));
    }

    protected void assertPersistedGuestTableToMatchUpdatableProperties(GuestTable expectedGuestTable) {
        assertGuestTableAllUpdatablePropertiesEquals(expectedGuestTable, getPersistedGuestTable(expectedGuestTable));
    }
}
