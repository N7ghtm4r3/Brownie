package com.tecknobit.brownie.services.hosts.repositories;

import com.tecknobit.brownie.services.hosts.entities.HostHistoryEvent;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.IDENTIFIER_KEY;

/**
 * The {@code HostEventsRepository} interface is useful to manage the queries of the {@link HostHistoryEvent}
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see JpaRepository
 */
@Repository
public interface HostEventsRepository extends JpaRepository<HostHistoryEvent, String> {

    /**
     * Query used to register a new event
     *
     * @param eventId   The identifier of the event
     * @param type      The type of the event
     * @param eventDate The date when the event occurred
     * @param hostId    The identifier of the host owner of the event
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "INSERT INTO " + HOST_EVENTS_KEY + " (" +
                    IDENTIFIER_KEY + "," +
                    TYPE_KEY + "," +
                    EVENT_DATE_KEY + "," +
                    HOST_IDENTIFIER_KEY + ") VALUES (" +
                    ":" + IDENTIFIER_KEY + "," +
                    ":" + TYPE_KEY + "," +
                    ":" + EVENT_DATE_KEY + "," +
                    ":" + HOST_IDENTIFIER_KEY + ")",
            nativeQuery = true
    )
    void registerEvent(
            @Param(IDENTIFIER_KEY) String eventId,
            @Param(TYPE_KEY) String type,
            @Param(EVENT_DATE_KEY) long eventDate,
            @Param(HOST_IDENTIFIER_KEY) String hostId
    );

    /**
     * Query used to register a new event
     *
     * @param eventId The identifier of the event
     * @param type The type of the event
     * @param eventDate The date when the event occurred
     * @param extra The extra information related to the event
     * @param hostId The identifier of the host owner of the event
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "INSERT INTO " + HOST_EVENTS_KEY + " (" +
                    IDENTIFIER_KEY + "," +
                    TYPE_KEY + "," +
                    EVENT_DATE_KEY + "," +
                    EXTRA_KEY + "," +
                    HOST_IDENTIFIER_KEY + ") VALUES (" +
                    ":" + IDENTIFIER_KEY + "," +
                    ":" + TYPE_KEY + "," +
                    ":" + EVENT_DATE_KEY + "," +
                    ":" + EXTRA_KEY + "," +
                    ":" + HOST_IDENTIFIER_KEY + ")",
            nativeQuery = true
    )
    void registerEvent(
            @Param(IDENTIFIER_KEY) String eventId,
            @Param(TYPE_KEY) String type,
            @Param(EVENT_DATE_KEY) long eventDate,
            @Param(EXTRA_KEY) String extra,
            @Param(HOST_IDENTIFIER_KEY) String hostId
    );

}
