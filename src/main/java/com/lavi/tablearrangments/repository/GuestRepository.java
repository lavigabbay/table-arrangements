package com.lavi.tablearrangments.repository;

import com.lavi.tablearrangments.domain.Guest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Guest entity.
 *
 * When extending this class, extend GuestRepositoryWithBagRelationships too.
 * For more information refer to https://github.com/jhipster/generator-jhipster/issues/17990.
 */
@Repository
public interface GuestRepository extends GuestRepositoryWithBagRelationships, JpaRepository<Guest, Long> {
    default Optional<Guest> findOneWithEagerRelationships(Long id) {
        return this.fetchBagRelationships(this.findOneWithToOneRelationships(id));
    }

    default List<Guest> findAllWithEagerRelationships() {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships());
    }

    default Page<Guest> findAllWithEagerRelationships(Pageable pageable) {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships(pageable));
    }

    @Query(
        value = "select guest from Guest guest left join fetch guest.event left join fetch guest.table where guest.event.user.login = ?#{authentication.name}",
        countQuery = "select count(guest) from Guest guest where guest.event.user.login = ?#{authentication.name}"
    )
    Page<Guest> findAllByEventUserIsCurrentUser(Pageable pageable);

    @Query(
        value = "select guest from Guest guest left join fetch guest.event left join fetch guest.table",
        countQuery = "select count(guest) from Guest guest"
    )
    Page<Guest> findAllWithToOneRelationships(Pageable pageable);

    @Query("select guest from Guest guest left join fetch guest.event left join fetch guest.table")
    List<Guest> findAllWithToOneRelationships();

    @Query("select guest from Guest guest left join fetch guest.event left join fetch guest.table where guest.id =:id")
    Optional<Guest> findOneWithToOneRelationships(@Param("id") Long id);

    @EntityGraph(attributePaths = { "preferGuests", "avoidGuests" })
    @Query("select g from Guest g left join fetch g.event where g.event.user.login = ?#{authentication.name}")
    List<Guest> findAllByEventUserIsCurrentUserList();
}
