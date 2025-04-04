package com.lavi.tablearrangments.domain;

import static com.lavi.tablearrangments.domain.VenueTableTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.lavi.tablearrangments.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VenueTableTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(VenueTable.class);
        VenueTable venueTable1 = getVenueTableSample1();
        VenueTable venueTable2 = new VenueTable();
        assertThat(venueTable1).isNotEqualTo(venueTable2);

        venueTable2.setId(venueTable1.getId());
        assertThat(venueTable1).isEqualTo(venueTable2);

        venueTable2 = getVenueTableSample2();
        assertThat(venueTable1).isNotEqualTo(venueTable2);
    }
}
