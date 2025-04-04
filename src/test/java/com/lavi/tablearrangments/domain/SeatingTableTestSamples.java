package com.lavi.tablearrangments.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SeatingTableTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static SeatingTable getSeatingTableSample1() {
        return new SeatingTable().id(1L).tableNumber(1).maxSeats(1);
    }

    public static SeatingTable getSeatingTableSample2() {
        return new SeatingTable().id(2L).tableNumber(2).maxSeats(2);
    }

    public static SeatingTable getSeatingTableRandomSampleGenerator() {
        return new SeatingTable()
            .id(longCount.incrementAndGet())
            .tableNumber(intCount.incrementAndGet())
            .maxSeats(intCount.incrementAndGet());
    }
}
