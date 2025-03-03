package com.tecknobit.brownie.services.hostservices.repositories;

import com.tecknobit.brownie.services.hosts.entities.ServiceEvent;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.equinoxbackend.environment.services.builtin.service.EquinoxItemsHelper._WHERE_;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.IDENTIFIER_KEY;

@Repository
public interface HostServiceEventsRepository extends JpaRepository<ServiceEvent, String> {

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
    void registerEvent(
            @Param(IDENTIFIER_KEY) String eventId,
            @Param(TYPE_KEY) String type,
            @Param(EVENT_DATE_KEY) long eventDate,
            @Param(SERVICE_IDENTIFIER_KEY) String serviceId
    );

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
    void registerEvent(
            @Param(IDENTIFIER_KEY) String eventId,
            @Param(TYPE_KEY) String type,
            @Param(EVENT_DATE_KEY) long eventDate,
            @Param(EXTRA_KEY) String extra,
            @Param(SERVICE_IDENTIFIER_KEY) String serviceId
    );

    @Query(
            value = "SELECT MAX(" + EVENT_DATE_KEY + ") FROM " + SERVICE_EVENTS_KEY +
                    _WHERE_ + TYPE_KEY + " IN ('RUNNING', 'RESTARTED') AND " +
                    SERVICE_IDENTIFIER_KEY + "=:" + SERVICE_IDENTIFIER_KEY,
            nativeQuery = true
    )
    long getLastRunningEvent(
            @Param(SERVICE_IDENTIFIER_KEY) String serviceId
    );

}
