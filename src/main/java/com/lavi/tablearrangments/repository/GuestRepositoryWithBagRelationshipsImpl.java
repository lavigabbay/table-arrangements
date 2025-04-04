package com.lavi.tablearrangments.repository;

import com.lavi.tablearrangments.domain.Guest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class GuestRepositoryWithBagRelationshipsImpl implements GuestRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String GUESTS_PARAMETER = "guests";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Guest> fetchBagRelationships(Optional<Guest> guest) {
        return guest.map(this::fetchAvoidGuests).map(this::fetchPreferGuests);
    }

    @Override
    public Page<Guest> fetchBagRelationships(Page<Guest> guests) {
        return new PageImpl<>(fetchBagRelationships(guests.getContent()), guests.getPageable(), guests.getTotalElements());
    }

    @Override
    public List<Guest> fetchBagRelationships(List<Guest> guests) {
        return Optional.of(guests).map(this::fetchAvoidGuests).map(this::fetchPreferGuests).orElse(Collections.emptyList());
    }

    Guest fetchAvoidGuests(Guest result) {
        return entityManager
            .createQuery("select guest from Guest guest left join fetch guest.avoidGuests where guest.id = :id", Guest.class)
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<Guest> fetchAvoidGuests(List<Guest> guests) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, guests.size()).forEach(index -> order.put(guests.get(index).getId(), index));
        List<Guest> result = entityManager
            .createQuery("select guest from Guest guest left join fetch guest.avoidGuests where guest in :guests", Guest.class)
            .setParameter(GUESTS_PARAMETER, guests)
            .getResultList();
        Collections.sort(result, (o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }

    Guest fetchPreferGuests(Guest result) {
        return entityManager
            .createQuery("select guest from Guest guest left join fetch guest.preferGuests where guest.id = :id", Guest.class)
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<Guest> fetchPreferGuests(List<Guest> guests) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, guests.size()).forEach(index -> order.put(guests.get(index).getId(), index));
        List<Guest> result = entityManager
            .createQuery("select guest from Guest guest left join fetch guest.preferGuests where guest in :guests", Guest.class)
            .setParameter(GUESTS_PARAMETER, guests)
            .getResultList();
        Collections.sort(result, (o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
