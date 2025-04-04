package com.lavi.tablearrangments.repository;

import com.lavi.tablearrangments.domain.SeatingTable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the SeatingTable entity.
 */
@Repository
public interface SeatingTableRepository extends JpaRepository<SeatingTable, Long> {
    default Optional<SeatingTable> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<SeatingTable> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<SeatingTable> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select seatingTable from SeatingTable seatingTable left join fetch seatingTable.event",
        countQuery = "select count(seatingTable) from SeatingTable seatingTable"
    )
    Page<SeatingTable> findAllWithToOneRelationships(Pageable pageable);

    @Query("select seatingTable from SeatingTable seatingTable left join fetch seatingTable.event")
    List<SeatingTable> findAllWithToOneRelationships();

    @Query("select seatingTable from SeatingTable seatingTable left join fetch seatingTable.event where seatingTable.id =:id")
    Optional<SeatingTable> findOneWithToOneRelationships(@Param("id") Long id);
}
