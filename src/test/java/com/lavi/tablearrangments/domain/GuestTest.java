package com.lavi.tablearrangments.domain;

import static com.lavi.tablearrangments.domain.EventTestSamples.*;
import static com.lavi.tablearrangments.domain.GuestTestSamples.*;
import static com.lavi.tablearrangments.domain.GuestTestSamples.*;
import static com.lavi.tablearrangments.domain.SeatingTableTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.lavi.tablearrangments.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GuestTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Guest.class);
        Guest guest1 = getGuestSample1();
        Guest guest2 = new Guest();
        assertThat(guest1).isNotEqualTo(guest2);

        guest2.setId(guest1.getId());
        assertThat(guest1).isEqualTo(guest2);

        guest2 = getGuestSample2();
        assertThat(guest1).isNotEqualTo(guest2);
    }

    @Test
    void eventTest() {
        Guest guest = getGuestRandomSampleGenerator();
        Event eventBack = getEventRandomSampleGenerator();

        guest.setEvent(eventBack);
        assertThat(guest.getEvent()).isEqualTo(eventBack);

        guest.event(null);
        assertThat(guest.getEvent()).isNull();
    }

    @Test
    void tableTest() {
        Guest guest = getGuestRandomSampleGenerator();
        SeatingTable seatingTableBack = getSeatingTableRandomSampleGenerator();

        guest.setTable(seatingTableBack);
        assertThat(guest.getTable()).isEqualTo(seatingTableBack);

        guest.table(null);
        assertThat(guest.getTable()).isNull();
    }

    @Test
    void avoidGuestsTest() {
        Guest guest = getGuestRandomSampleGenerator();
        Guest guestBack = getGuestRandomSampleGenerator();

        guest.addAvoidGuests(guestBack);
        assertThat(guest.getAvoidGuests()).containsOnly(guestBack);

        guest.removeAvoidGuests(guestBack);
        assertThat(guest.getAvoidGuests()).doesNotContain(guestBack);

        guest.avoidGuests(new HashSet<>(Set.of(guestBack)));
        assertThat(guest.getAvoidGuests()).containsOnly(guestBack);

        guest.setAvoidGuests(new HashSet<>());
        assertThat(guest.getAvoidGuests()).doesNotContain(guestBack);
    }

    @Test
    void preferGuestsTest() {
        Guest guest = getGuestRandomSampleGenerator();
        Guest guestBack = getGuestRandomSampleGenerator();

        guest.addPreferGuests(guestBack);
        assertThat(guest.getPreferGuests()).containsOnly(guestBack);

        guest.removePreferGuests(guestBack);
        assertThat(guest.getPreferGuests()).doesNotContain(guestBack);

        guest.preferGuests(new HashSet<>(Set.of(guestBack)));
        assertThat(guest.getPreferGuests()).containsOnly(guestBack);

        guest.setPreferGuests(new HashSet<>());
        assertThat(guest.getPreferGuests()).doesNotContain(guestBack);
    }

    @Test
    void avoidedByTest() {
        Guest guest = getGuestRandomSampleGenerator();
        Guest guestBack = getGuestRandomSampleGenerator();

        guest.addAvoidedBy(guestBack);
        assertThat(guest.getAvoidedBies()).containsOnly(guestBack);
        assertThat(guestBack.getAvoidGuests()).containsOnly(guest);

        guest.removeAvoidedBy(guestBack);
        assertThat(guest.getAvoidedBies()).doesNotContain(guestBack);
        assertThat(guestBack.getAvoidGuests()).doesNotContain(guest);

        guest.avoidedBies(new HashSet<>(Set.of(guestBack)));
        assertThat(guest.getAvoidedBies()).containsOnly(guestBack);
        assertThat(guestBack.getAvoidGuests()).containsOnly(guest);

        guest.setAvoidedBies(new HashSet<>());
        assertThat(guest.getAvoidedBies()).doesNotContain(guestBack);
        assertThat(guestBack.getAvoidGuests()).doesNotContain(guest);
    }

    @Test
    void preferredByTest() {
        Guest guest = getGuestRandomSampleGenerator();
        Guest guestBack = getGuestRandomSampleGenerator();

        guest.addPreferredBy(guestBack);
        assertThat(guest.getPreferredBies()).containsOnly(guestBack);
        assertThat(guestBack.getPreferGuests()).containsOnly(guest);

        guest.removePreferredBy(guestBack);
        assertThat(guest.getPreferredBies()).doesNotContain(guestBack);
        assertThat(guestBack.getPreferGuests()).doesNotContain(guest);

        guest.preferredBies(new HashSet<>(Set.of(guestBack)));
        assertThat(guest.getPreferredBies()).containsOnly(guestBack);
        assertThat(guestBack.getPreferGuests()).containsOnly(guest);

        guest.setPreferredBies(new HashSet<>());
        assertThat(guest.getPreferredBies()).doesNotContain(guestBack);
        assertThat(guestBack.getPreferGuests()).doesNotContain(guest);
    }
}
