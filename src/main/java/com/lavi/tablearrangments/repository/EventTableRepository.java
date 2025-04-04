package com.lavi.tablearrangments.repository;

import com.lavi.tablearrangments.domain.EventTable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the EventTable entity.
 */
@Repository
public interface EventTableRepository extends JpaRepository<EventTable, Long> {
    default Optional<EventTable> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<EventTable> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<EventTable> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select eventTable from EventTable eventTable left join fetch eventTable.venue",
        countQuery = "select count(eventTable) from EventTable eventTable"
    )
    Page<EventTable> findAllWithToOneRelationships(Pageable pageable);

    @Query("select eventTable from EventTable eventTable left join fetch eventTable.venue")
    List<EventTable> findAllWithToOneRelationships();

    @Query("select eventTable from EventTable eventTable left join fetch eventTable.venue where eventTable.id =:id")
    Optional<EventTable> findOneWithToOneRelationships(@Param("id") Long id);
}
