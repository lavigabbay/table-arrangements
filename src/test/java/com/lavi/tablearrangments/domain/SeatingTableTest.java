package com.lavi.tablearrangments.domain;

import static com.lavi.tablearrangments.domain.EventTestSamples.*;
import static com.lavi.tablearrangments.domain.SeatingTableTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.lavi.tablearrangments.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SeatingTableTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SeatingTable.class);
        SeatingTable seatingTable1 = getSeatingTableSample1();
        SeatingTable seatingTable2 = new SeatingTable();
        assertThat(seatingTable1).isNotEqualTo(seatingTable2);

        seatingTable2.setId(seatingTable1.getId());
        assertThat(seatingTable1).isEqualTo(seatingTable2);

        seatingTable2 = getSeatingTableSample2();
        assertThat(seatingTable1).isNotEqualTo(seatingTable2);
    }

    @Test
    void eventTest() {
        SeatingTable seatingTable = getSeatingTableRandomSampleGenerator();
        Event eventBack = getEventRandomSampleGenerator();

        seatingTable.setEvent(eventBack);
        assertThat(seatingTable.getEvent()).isEqualTo(eventBack);

        seatingTable.event(null);
        assertThat(seatingTable.getEvent()).isNull();
    }
}
