package com.lavi.tablearrangments.repository;

import com.lavi.tablearrangments.domain.Guest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface GuestRepositoryWithBagRelationships {
    Optional<Guest> fetchBagRelationships(Optional<Guest> guest);

    List<Guest> fetchBagRelationships(List<Guest> guests);

    Page<Guest> fetchBagRelationships(Page<Guest> guests);
}
