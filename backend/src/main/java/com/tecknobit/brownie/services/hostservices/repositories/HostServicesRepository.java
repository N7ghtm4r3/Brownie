package com.tecknobit.brownie.services.hostservices.repositories;

import com.tecknobit.brownie.services.hostservices.entity.BrownieHostService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.equinoxbackend.configuration.IndexesCreator._IN_BOOLEAN_MODE;
import static com.tecknobit.equinoxbackend.environment.services.builtin.service.EquinoxItemsHelper._WHERE_;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.IDENTIFIER_KEY;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.NAME_KEY;

@Repository
public interface HostServicesRepository extends JpaRepository<BrownieHostService, String> {

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "INSERT INTO " + SERVICES_KEY + " (" +
                    IDENTIFIER_KEY + "," +
                    NAME_KEY + "," +
                    STATUS_KEY + "," +
                    INSERTION_DATE_KEY + "," +
                    HOST_IDENTIFIER_KEY + "," +
                    SERVICE_PATH_KEY + ") VALUES (" +
                    ":" + IDENTIFIER_KEY + "," +
                    ":" + NAME_KEY + "," +
                    ":" + STATUS_KEY + "," +
                    ":" + INSERTION_DATE_KEY + "," +
                    ":" + HOST_IDENTIFIER_KEY + "," +
                    ":" + SERVICE_PATH_KEY + ")",
            nativeQuery = true
    )
    void storeService(
            @Param(IDENTIFIER_KEY) String serviceId,
            @Param(NAME_KEY) String serviceName,
            @Param(STATUS_KEY) String status,
            @Param(INSERTION_DATE_KEY) long insertionDate,
            @Param(HOST_IDENTIFIER_KEY) String hostId,
            @Param(SERVICE_PATH_KEY) String servicePath
    );

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE " + SERVICES_KEY + " SET " +
                    NAME_KEY + "=:" + NAME_KEY + "," +
                    SERVICE_PATH_KEY + "=:" + SERVICE_PATH_KEY +
                    _WHERE_ + IDENTIFIER_KEY + "=:" + IDENTIFIER_KEY,
            nativeQuery = true
    )
    void editService(
            @Param(IDENTIFIER_KEY) String serviceId,
            @Param(NAME_KEY) String serviceName,
            @Param(SERVICE_PATH_KEY) String servicePath
    );

    @Query(
            value = "SELECT COUNT(*) FROM " + SERVICES_KEY + _WHERE_ +
                    "( " +
                    "MATCH(" + NAME_KEY + "," + PID_KEY + ") AGAINST (:" + KEYWORDS_KEY + _IN_BOOLEAN_MODE + ") " +
                    "OR :" + KEYWORDS_KEY + " = ''" +
                    ") " +
                    "AND " + STATUS_KEY + " IN (:" + STATUSES_KEY + ") " +
                    "AND " + HOST_IDENTIFIER_KEY + "=:" + HOST_IDENTIFIER_KEY,
            nativeQuery = true
    )
    long countServices(
            @Param(HOST_IDENTIFIER_KEY) String hostId,
            @Param(KEYWORDS_KEY) String keywords,
            @Param(STATUSES_KEY) List<String> statuses
    );

    @Query(
            value = "SELECT * FROM " + SERVICES_KEY + _WHERE_ +
                    "( " +
                    "MATCH(" + NAME_KEY + "," + PID_KEY + ") AGAINST (:" + KEYWORDS_KEY + _IN_BOOLEAN_MODE + ") " +
                    "OR :" + KEYWORDS_KEY + " = ''" +
                    ") " +
                    "AND " + STATUS_KEY + " IN (:" + STATUSES_KEY + ") " +
                    "AND " + HOST_IDENTIFIER_KEY + "=:" + HOST_IDENTIFIER_KEY,
            nativeQuery = true
    )
    List<BrownieHostService> getServices(
            @Param(HOST_IDENTIFIER_KEY) String hostId,
            @Param(KEYWORDS_KEY) String keywords,
            @Param(STATUSES_KEY) List<String> statuses,
            Pageable pageable
    );

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE " + SERVICES_KEY + " SET " +
                    STATUS_KEY + "=:" + STATUS_KEY + "," +
                    PID_KEY + "=:" + PID_KEY +
                    _WHERE_ + IDENTIFIER_KEY + "=:" + IDENTIFIER_KEY,
            nativeQuery = true
    )
    void updateServiceStatus(
            @Param(IDENTIFIER_KEY) String serviceId,
            @Param(STATUS_KEY) String status,
            @Param(PID_KEY) long pid
    );

}
