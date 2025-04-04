package com.lavi.tablearrangments.repository;

import com.lavi.tablearrangments.domain.GuestTable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the GuestTable entity.
 */
@Repository
public interface GuestTableRepository extends JpaRepository<GuestTable, Long> {
    default Optional<GuestTable> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<GuestTable> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<GuestTable> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select guestTable from GuestTable guestTable left join fetch guestTable.venueName left join fetch guestTable.eventTable",
        countQuery = "select count(guestTable) from GuestTable guestTable"
    )
    Page<GuestTable> findAllWithToOneRelationships(Pageable pageable);

    @Query("select guestTable from GuestTable guestTable left join fetch guestTable.venueName left join fetch guestTable.eventTable")
    List<GuestTable> findAllWithToOneRelationships();

    @Query(
        "select guestTable from GuestTable guestTable left join fetch guestTable.venueName left join fetch guestTable.eventTable where guestTable.id =:id"
    )
    Optional<GuestTable> findOneWithToOneRelationships(@Param("id") Long id);
}
