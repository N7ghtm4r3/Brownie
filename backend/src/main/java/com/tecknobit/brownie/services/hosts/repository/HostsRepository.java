package com.tecknobit.brownie.services.hosts.repository;

import com.tecknobit.brownie.services.hosts.entity.BrownieHost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.equinoxbackend.configuration.IndexesCreator._IN_BOOLEAN_MODE;
import static com.tecknobit.equinoxbackend.environment.services.builtin.service.EquinoxItemsHelper._WHERE_;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.NAME_KEY;

@Repository
public interface HostsRepository extends JpaRepository<BrownieHost, String> {

    @Query(
            value = "SELECT COUNT(*) FROM " + HOSTS_KEY + _WHERE_ +
                    "MATCH(" + NAME_KEY + "," + HOST_ADDRESS_KEY + ") AGAINST (:" + KEYWORDS_KEY + _IN_BOOLEAN_MODE + ") " +
                    "OR :" + KEYWORDS_KEY + " = ''",
            nativeQuery = true
    )
    long countHosts(
            @Param(KEYWORDS_KEY) String keywords
    );

    @Query(
            value = "SELECT * FROM " + HOSTS_KEY + _WHERE_ +
                    "MATCH(" + NAME_KEY + "," + HOST_ADDRESS_KEY + ") AGAINST (:" + KEYWORDS_KEY + _IN_BOOLEAN_MODE + ") " +
                    "OR :" + KEYWORDS_KEY + " = ''",
            nativeQuery = true
    )
    List<BrownieHost> getHosts(
            @Param(KEYWORDS_KEY) String keywords,
            Pageable pageable
    );

}
