package com.lavi.tablearrangments.web.rest;

import static com.lavi.tablearrangments.domain.GuestAsserts.*;
import static com.lavi.tablearrangments.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lavi.tablearrangments.IntegrationTest;
import com.lavi.tablearrangments.domain.Guest;
import com.lavi.tablearrangments.domain.enumeration.GuestRelation;
import com.lavi.tablearrangments.domain.enumeration.GuestSide;
import com.lavi.tablearrangments.domain.enumeration.GuestStatus;
import com.lavi.tablearrangments.repository.GuestRepository;
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
 * Integration tests for the {@link GuestResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class GuestResourceIT {

    private static final String DEFAULT_LAST_NAME_AND_FIRST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LAST_NAME_AND_FIRST_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_NUMBER_OF_SEATS = 1;
    private static final Integer UPDATED_NUMBER_OF_SEATS = 2;

    private static final String DEFAULT_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_PHONE = "BBBBBBBBBB";

    private static final Boolean DEFAULT_NEAR_STAGE = false;
    private static final Boolean UPDATED_NEAR_STAGE = true;

    private static final GuestStatus DEFAULT_STATUS = GuestStatus.CONFIRMED;
    private static final GuestStatus UPDATED_STATUS = GuestStatus.WAITING_APPROVAL;

    private static final GuestSide DEFAULT_SIDE = GuestSide.GROOM;
    private static final GuestSide UPDATED_SIDE = GuestSide.BRIDE;

    private static final GuestRelation DEFAULT_RELATION = GuestRelation.GROOM_FAMILY;
    private static final GuestRelation UPDATED_RELATION = GuestRelation.BRIDE_FAMILY;

    private static final Boolean DEFAULT_ACCESSIBILITY = false;
    private static final Boolean UPDATED_ACCESSIBILITY = true;

    private static final String ENTITY_API_URL = "/api/guests";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private GuestRepository guestRepository;

    @Mock
    private GuestRepository guestRepositoryMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restGuestMockMvc;

    private Guest guest;

    private Guest insertedGuest;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Guest createEntity() {
        return new Guest()
            .lastNameAndFirstName(DEFAULT_LAST_NAME_AND_FIRST_NAME)
            .numberOfSeats(DEFAULT_NUMBER_OF_SEATS)
            .phone(DEFAULT_PHONE)
            .nearStage(DEFAULT_NEAR_STAGE)
            .status(DEFAULT_STATUS)
            .side(DEFAULT_SIDE)
            .relation(DEFAULT_RELATION)
            .accessibility(DEFAULT_ACCESSIBILITY);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Guest createUpdatedEntity() {
        return new Guest()
            .lastNameAndFirstName(UPDATED_LAST_NAME_AND_FIRST_NAME)
            .numberOfSeats(UPDATED_NUMBER_OF_SEATS)
            .phone(UPDATED_PHONE)
            .nearStage(UPDATED_NEAR_STAGE)
            .status(UPDATED_STATUS)
            .side(UPDATED_SIDE)
            .relation(UPDATED_RELATION)
            .accessibility(UPDATED_ACCESSIBILITY);
    }

    @BeforeEach
    public void initTest() {
        guest = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedGuest != null) {
            guestRepository.delete(insertedGuest);
            insertedGuest = null;
        }
    }

    @Test
    @Transactional
    void createGuest() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Guest
        var returnedGuest = om.readValue(
            restGuestMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Guest.class
        );

        // Validate the Guest in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertGuestUpdatableFieldsEquals(returnedGuest, getPersistedGuest(returnedGuest));

        insertedGuest = returnedGuest;
    }

    @Test
    @Transactional
    void createGuestWithExistingId() throws Exception {
        // Create the Guest with an existing ID
        guest.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restGuestMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guest)))
            .andExpect(status().isBadRequest());

        // Validate the Guest in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkLastNameAndFirstNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        guest.setLastNameAndFirstName(null);

        // Create the Guest, which fails.

        restGuestMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guest)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNumberOfSeatsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        guest.setNumberOfSeats(null);

        // Create the Guest, which fails.

        restGuestMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guest)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        guest.setStatus(null);

        // Create the Guest, which fails.

        restGuestMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guest)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkRelationIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        guest.setRelation(null);

        // Create the Guest, which fails.

        restGuestMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guest)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAccessibilityIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        guest.setAccessibility(null);

        // Create the Guest, which fails.

        restGuestMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guest)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllGuests() throws Exception {
        // Initialize the database
        insertedGuest = guestRepository.saveAndFlush(guest);

        // Get all the guestList
        restGuestMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(guest.getId().intValue())))
            .andExpect(jsonPath("$.[*].lastNameAndFirstName").value(hasItem(DEFAULT_LAST_NAME_AND_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].numberOfSeats").value(hasItem(DEFAULT_NUMBER_OF_SEATS)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].nearStage").value(hasItem(DEFAULT_NEAR_STAGE)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].side").value(hasItem(DEFAULT_SIDE.toString())))
            .andExpect(jsonPath("$.[*].relation").value(hasItem(DEFAULT_RELATION.toString())))
            .andExpect(jsonPath("$.[*].accessibility").value(hasItem(DEFAULT_ACCESSIBILITY)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllGuestsWithEagerRelationshipsIsEnabled() throws Exception {
        when(guestRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restGuestMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(guestRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllGuestsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(guestRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restGuestMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(guestRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getGuest() throws Exception {
        // Initialize the database
        insertedGuest = guestRepository.saveAndFlush(guest);

        // Get the guest
        restGuestMockMvc
            .perform(get(ENTITY_API_URL_ID, guest.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(guest.getId().intValue()))
            .andExpect(jsonPath("$.lastNameAndFirstName").value(DEFAULT_LAST_NAME_AND_FIRST_NAME))
            .andExpect(jsonPath("$.numberOfSeats").value(DEFAULT_NUMBER_OF_SEATS))
            .andExpect(jsonPath("$.phone").value(DEFAULT_PHONE))
            .andExpect(jsonPath("$.nearStage").value(DEFAULT_NEAR_STAGE))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.side").value(DEFAULT_SIDE.toString()))
            .andExpect(jsonPath("$.relation").value(DEFAULT_RELATION.toString()))
            .andExpect(jsonPath("$.accessibility").value(DEFAULT_ACCESSIBILITY));
    }

    @Test
    @Transactional
    void getNonExistingGuest() throws Exception {
        // Get the guest
        restGuestMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingGuest() throws Exception {
        // Initialize the database
        insertedGuest = guestRepository.saveAndFlush(guest);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the guest
        Guest updatedGuest = guestRepository.findById(guest.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedGuest are not directly saved in db
        em.detach(updatedGuest);
        updatedGuest
            .lastNameAndFirstName(UPDATED_LAST_NAME_AND_FIRST_NAME)
            .numberOfSeats(UPDATED_NUMBER_OF_SEATS)
            .phone(UPDATED_PHONE)
            .nearStage(UPDATED_NEAR_STAGE)
            .status(UPDATED_STATUS)
            .side(UPDATED_SIDE)
            .relation(UPDATED_RELATION)
            .accessibility(UPDATED_ACCESSIBILITY);

        restGuestMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedGuest.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedGuest))
            )
            .andExpect(status().isOk());

        // Validate the Guest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedGuestToMatchAllProperties(updatedGuest);
    }

    @Test
    @Transactional
    void putNonExistingGuest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        guest.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGuestMockMvc
            .perform(put(ENTITY_API_URL_ID, guest.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guest)))
            .andExpect(status().isBadRequest());

        // Validate the Guest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchGuest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        guest.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGuestMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(guest))
            )
            .andExpect(status().isBadRequest());

        // Validate the Guest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamGuest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        guest.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGuestMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(guest)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Guest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateGuestWithPatch() throws Exception {
        // Initialize the database
        insertedGuest = guestRepository.saveAndFlush(guest);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the guest using partial update
        Guest partialUpdatedGuest = new Guest();
        partialUpdatedGuest.setId(guest.getId());

        partialUpdatedGuest
            .lastNameAndFirstName(UPDATED_LAST_NAME_AND_FIRST_NAME)
            .phone(UPDATED_PHONE)
            .side(UPDATED_SIDE)
            .relation(UPDATED_RELATION)
            .accessibility(UPDATED_ACCESSIBILITY);

        restGuestMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGuest.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGuest))
            )
            .andExpect(status().isOk());

        // Validate the Guest in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertGuestUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedGuest, guest), getPersistedGuest(guest));
    }

    @Test
    @Transactional
    void fullUpdateGuestWithPatch() throws Exception {
        // Initialize the database
        insertedGuest = guestRepository.saveAndFlush(guest);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the guest using partial update
        Guest partialUpdatedGuest = new Guest();
        partialUpdatedGuest.setId(guest.getId());

        partialUpdatedGuest
            .lastNameAndFirstName(UPDATED_LAST_NAME_AND_FIRST_NAME)
            .numberOfSeats(UPDATED_NUMBER_OF_SEATS)
            .phone(UPDATED_PHONE)
            .nearStage(UPDATED_NEAR_STAGE)
            .status(UPDATED_STATUS)
            .side(UPDATED_SIDE)
            .relation(UPDATED_RELATION)
            .accessibility(UPDATED_ACCESSIBILITY);

        restGuestMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGuest.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGuest))
            )
            .andExpect(status().isOk());

        // Validate the Guest in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertGuestUpdatableFieldsEquals(partialUpdatedGuest, getPersistedGuest(partialUpdatedGuest));
    }

    @Test
    @Transactional
    void patchNonExistingGuest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        guest.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGuestMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, guest.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(guest))
            )
            .andExpect(status().isBadRequest());

        // Validate the Guest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchGuest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        guest.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGuestMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(guest))
            )
            .andExpect(status().isBadRequest());

        // Validate the Guest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamGuest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        guest.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGuestMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(guest)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Guest in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteGuest() throws Exception {
        // Initialize the database
        insertedGuest = guestRepository.saveAndFlush(guest);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the guest
        restGuestMockMvc
            .perform(delete(ENTITY_API_URL_ID, guest.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return guestRepository.count();
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

    protected Guest getPersistedGuest(Guest guest) {
        return guestRepository.findById(guest.getId()).orElseThrow();
    }

    protected void assertPersistedGuestToMatchAllProperties(Guest expectedGuest) {
        assertGuestAllPropertiesEquals(expectedGuest, getPersistedGuest(expectedGuest));
    }

    protected void assertPersistedGuestToMatchUpdatableProperties(Guest expectedGuest) {
        assertGuestAllUpdatablePropertiesEquals(expectedGuest, getPersistedGuest(expectedGuest));
    }
}
