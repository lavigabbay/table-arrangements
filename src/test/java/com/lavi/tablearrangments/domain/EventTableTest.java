package com.lavi.tablearrangments.domain;

import static com.lavi.tablearrangments.domain.EventTableTestSamples.*;
import static com.lavi.tablearrangments.domain.VenueTableTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.lavi.tablearrangments.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EventTableTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(EventTable.class);
        EventTable eventTable1 = getEventTableSample1();
        EventTable eventTable2 = new EventTable();
        assertThat(eventTable1).isNotEqualTo(eventTable2);

        eventTable2.setId(eventTable1.getId());
        assertThat(eventTable1).isEqualTo(eventTable2);

        eventTable2 = getEventTableSample2();
        assertThat(eventTable1).isNotEqualTo(eventTable2);
    }

    @Test
    void venueTest() {
        EventTable eventTable = getEventTableRandomSampleGenerator();
        VenueTable venueTableBack = getVenueTableRandomSampleGenerator();

        eventTable.setVenue(venueTableBack);
        assertThat(eventTable.getVenue()).isEqualTo(venueTableBack);

        eventTable.venue(null);
        assertThat(eventTable.getVenue()).isNull();
    }
}
