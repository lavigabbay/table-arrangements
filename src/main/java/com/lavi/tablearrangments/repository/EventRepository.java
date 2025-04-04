package com.lavi.tablearrangments.repository;

import com.lavi.tablearrangments.domain.Event;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Event entity.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("select event from Event event where event.user.login = ?#{authentication.name}")
    List<Event> findByUserIsCurrentUser();

    default Optional<Event> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Event> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Event> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select event from Event event left join fetch event.user", countQuery = "select count(event) from Event event")
    Page<Event> findAllWithToOneRelationships(Pageable pageable);

    @Query("select event from Event event left join fetch event.user")
    List<Event> findAllWithToOneRelationships();

    @Query("select event from Event event left join fetch event.user where event.id =:id")
    Optional<Event> findOneWithToOneRelationships(@Param("id") Long id);
}
