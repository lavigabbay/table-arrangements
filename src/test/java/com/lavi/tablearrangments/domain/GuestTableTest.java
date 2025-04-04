package com.lavi.tablearrangments.domain;

import static com.lavi.tablearrangments.domain.EventTableTestSamples.*;
import static com.lavi.tablearrangments.domain.GuestTableTestSamples.*;
import static com.lavi.tablearrangments.domain.VenueTableTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.lavi.tablearrangments.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class GuestTableTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(GuestTable.class);
        GuestTable guestTable1 = getGuestTableSample1();
        GuestTable guestTable2 = new GuestTable();
        assertThat(guestTable1).isNotEqualTo(guestTable2);

        guestTable2.setId(guestTable1.getId());
        assertThat(guestTable1).isEqualTo(guestTable2);

        guestTable2 = getGuestTableSample2();
        assertThat(guestTable1).isNotEqualTo(guestTable2);
    }

    @Test
    void venueNameTest() {
        GuestTable guestTable = getGuestTableRandomSampleGenerator();
        VenueTable venueTableBack = getVenueTableRandomSampleGenerator();

        guestTable.setVenueName(venueTableBack);
        assertThat(guestTable.getVenueName()).isEqualTo(venueTableBack);

        guestTable.venueName(null);
        assertThat(guestTable.getVenueName()).isNull();
    }

    @Test
    void eventTableTest() {
        GuestTable guestTable = getGuestTableRandomSampleGenerator();
        EventTable eventTableBack = getEventTableRandomSampleGenerator();

        guestTable.setEventTable(eventTableBack);
        assertThat(guestTable.getEventTable()).isEqualTo(eventTableBack);

        guestTable.eventTable(null);
        assertThat(guestTable.getEventTable()).isNull();
    }
}
