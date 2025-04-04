package com.lavi.tablearrangments.repository;

import com.lavi.tablearrangments.domain.VenueTable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the VenueTable entity.
 */
@Repository
public interface VenueTableRepository extends JpaRepository<VenueTable, Long> {
    @Query("select venueTable from VenueTable venueTable where venueTable.user.login = ?#{authentication.name}")
    List<VenueTable> findByUserIsCurrentUser();

    default Optional<VenueTable> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<VenueTable> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<VenueTable> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select venueTable from VenueTable venueTable left join fetch venueTable.user",
        countQuery = "select count(venueTable) from VenueTable venueTable"
    )
    Page<VenueTable> findAllWithToOneRelationships(Pageable pageable);

    @Query("select venueTable from VenueTable venueTable left join fetch venueTable.user")
    List<VenueTable> findAllWithToOneRelationships();

    @Query("select venueTable from VenueTable venueTable left join fetch venueTable.user where venueTable.id =:id")
    Optional<VenueTable> findOneWithToOneRelationships(@Param("id") Long id);
}
