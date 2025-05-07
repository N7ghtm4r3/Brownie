package com.tecknobit.brownie.services.hostservices.repositories;

import com.tecknobit.brownie.services.hostservices.entities.ServiceEvent;
import com.tecknobit.brownie.services.shared.repositories.BrownieEventsRepository;
import com.tecknobit.browniecore.enums.ServiceStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.equinoxbackend.environment.services.builtin.service.EquinoxItemsHelper._WHERE_;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.IDENTIFIER_KEY;

/**
 * The {@code HostServiceEventsRepository} interface is useful to manage the queries of the {@link ServiceEvent}
 *
 * @author N7ghtm4r3 - Tecknobit
 *
 * @see JpaRepository
 * @see BrownieEventsRepository
 */
@Repository
public interface HostServiceEventsRepository extends BrownieEventsRepository<ServiceEvent> {

    /**
     * Query used to get the last {@link ServiceStatus#RUNNING} event
     *
     * @param serviceId The identifier of the service
     * @return the timestamp of the last {@link ServiceStatus#RUNNING} event as {@link Long}
     */
    @Query(
            value = "SELECT MAX(" + EVENT_DATE_KEY + ") FROM " + SERVICE_EVENTS_KEY +
                    _WHERE_ + TYPE_KEY + " IN ('RUNNING', 'RESTARTED') AND " +
                    SERVICE_IDENTIFIER_KEY + "=:" + SERVICE_IDENTIFIER_KEY,
            nativeQuery = true
    )
    @Override
    Long getLastUpEvent(
            @Param(SERVICE_IDENTIFIER_KEY) String serviceId
    );

    /**
     * Query used to register a new event
     *
     * @param eventId   The identifier of the event
     * @param type      The type of the event
     * @param eventDate The date when the event occurred
     * @param serviceId The identifier of the service owner of the event
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "INSERT INTO " + SERVICE_EVENTS_KEY + " (" +
                    IDENTIFIER_KEY + "," +
                    TYPE_KEY + "," +
                    EVENT_DATE_KEY + "," +
                    SERVICE_IDENTIFIER_KEY + ") VALUES (" +
                    ":" + IDENTIFIER_KEY + "," +
                    ":" + TYPE_KEY + "," +
                    ":" + EVENT_DATE_KEY + "," +
                    ":" + SERVICE_IDENTIFIER_KEY + ")",
            nativeQuery = true
    )
    @Override
    void registerEvent(
            @Param(IDENTIFIER_KEY) String eventId,
            @Param(TYPE_KEY) String type,
            @Param(EVENT_DATE_KEY) long eventDate,
            @Param(SERVICE_IDENTIFIER_KEY) String serviceId
    );

    /**
     * Query used to register a new event
     *
     * @param eventId   The identifier of the event
     * @param type      The type of the event
     * @param eventDate The date when the event occurred
     * @param extra     The extra information related to the event
     * @param serviceId The identifier of the service owner of the event
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "INSERT INTO " + SERVICE_EVENTS_KEY + " (" +
                    IDENTIFIER_KEY + "," +
                    TYPE_KEY + "," +
                    EVENT_DATE_KEY + "," +
                    EXTRA_KEY + "," +
                    SERVICE_IDENTIFIER_KEY + ") VALUES (" +
                    ":" + IDENTIFIER_KEY + "," +
                    ":" + TYPE_KEY + "," +
                    ":" + EVENT_DATE_KEY + "," +
                    ":" + EXTRA_KEY + "," +
                    ":" + SERVICE_IDENTIFIER_KEY + ")",
            nativeQuery = true
    )
    @Override
    void registerEvent(
            @Param(IDENTIFIER_KEY) String eventId,
            @Param(TYPE_KEY) String type,
            @Param(EVENT_DATE_KEY) long eventDate,
            @Param(EXTRA_KEY) String extra,
            @Param(SERVICE_IDENTIFIER_KEY) String serviceId
    );

}
