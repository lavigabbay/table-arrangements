package com.lavi.tablearrangments.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class VenueTableTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static VenueTable getVenueTableSample1() {
        return new VenueTable()
            .id(1L)
            .numberOfTables(1)
            .nearStageTables(1)
            .venueName("venueName1")
            .eventOwners("eventOwners1")
            .groomParents("groomParents1")
            .brideParents("brideParents1");
    }

    public static VenueTable getVenueTableSample2() {
        return new VenueTable()
            .id(2L)
            .numberOfTables(2)
            .nearStageTables(2)
            .venueName("venueName2")
            .eventOwners("eventOwners2")
            .groomParents("groomParents2")
            .brideParents("brideParents2");
    }

    public static VenueTable getVenueTableRandomSampleGenerator() {
        return new VenueTable()
            .id(longCount.incrementAndGet())
            .numberOfTables(intCount.incrementAndGet())
            .nearStageTables(intCount.incrementAndGet())
            .venueName(UUID.randomUUID().toString())
            .eventOwners(UUID.randomUUID().toString())
            .groomParents(UUID.randomUUID().toString())
            .brideParents(UUID.randomUUID().toString());
    }
}
