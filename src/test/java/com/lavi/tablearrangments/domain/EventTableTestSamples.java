package com.lavi.tablearrangments.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class EventTableTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static EventTable getEventTableSample1() {
        return new EventTable().id(1L).tableNumber(1).maxSeats(1);
    }

    public static EventTable getEventTableSample2() {
        return new EventTable().id(2L).tableNumber(2).maxSeats(2);
    }

    public static EventTable getEventTableRandomSampleGenerator() {
        return new EventTable()
            .id(longCount.incrementAndGet())
            .tableNumber(intCount.incrementAndGet())
            .maxSeats(intCount.incrementAndGet());
    }
}
