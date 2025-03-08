package com.tecknobit.brownie.services.hostservices.repositories;

import com.tecknobit.brownie.services.hostservices.dtos.CurrentServiceStatus;
import com.tecknobit.brownie.services.hostservices.entities.BrownieHostService;
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

/**
 * The {@code HostServicesRepository} interface is useful to manage the queries of the {@link BrownieHostService}
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see JpaRepository
 */
@Repository
public interface HostServicesRepository extends JpaRepository<BrownieHostService, String> {

    /**
     * Query used to store a new service
     *
     * @param serviceId     The identifier of the service
     * @param serviceName   The name of the service
     * @param status        The status of the service
     * @param insertionDate The date when the service has been stored
     * @param hostId        The identifier of the host owner of the service
     * @param servicePath   The path of the service inside the filesystem of the host
     */
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

    /**
     * Query used to edit an existing service
     *
     * @param serviceId The identifier of the service
     * @param serviceName The name of the service
     * @param servicePath The path of the service inside the filesystem of the host
     */
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

    /**
     * Query used to count the services related to a host
     *
     * @param hostId The identifier of the host
     * @param keywords The keywords used to filter the results
     * @param statuses The statuses used to filter the results
     * @return the count of the services related to a host as {@code long}
     */
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

    /**
     * Query used to count the services related to a host
     *
     * @param hostId The identifier of the host
     * @param keywords The keywords used to filter the results
     * @param statuses The statuses used to filter the results
     * @param pageable The parameters to paginate the query
     *
     * @return the services related to a host as {@link List} of {@link BrownieHostService}
     */
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

    /**
     * Query used to retrieve the current status of the specified services
     *
     * @param services The services to retrieve their current status
     *
     * @return the current services status as {@link List} of {@link CurrentServiceStatus}
     */
    @Query(
            value = "SELECT new com.tecknobit.brownie.services.hostservices.dtos.CurrentServiceStatus(" +
                    "s." + IDENTIFIER_KEY + "," +
                    "s." + STATUS_KEY + "," +
                    "s." + PID_KEY +
                    ") FROM BrownieHostService s" + _WHERE_ +
                    "s." + IDENTIFIER_KEY + " IN (:" + SERVICES_KEY + ")"
    )
    List<CurrentServiceStatus> getServicesStatus(
            @Param(SERVICES_KEY) List<String> services
    );

    /**
     * Query used to update a service status and its pid
     *
     * @param serviceId The identifier of the service
     * @param status The status to set
     * @param pid The pid to set
     */
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

    /**
     * Query used to remove a service
     *
     * @param serviceId The identifier of the service
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "DELETE FROM " + SERVICES_KEY + _WHERE_ + IDENTIFIER_KEY + "=:" + IDENTIFIER_KEY,
            nativeQuery = true
    )
    void removeService(
            @Param(IDENTIFIER_KEY) String serviceId
    );

}
