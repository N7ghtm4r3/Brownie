package com.tecknobit.brownie.services.shared.repositories;

import com.tecknobit.brownie.services.shared.entities.BrownieEvent;
import com.tecknobit.equinoxcore.annotations.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * The {@code BrownieEventsRepository} provides the methods to manage any {@link BrownieEvent} in the dedicated tables
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see JpaRepository
 * @see BrownieEvent
 * @since 1.0.1
 */
@Structure
@NoRepositoryBean
public interface BrownieEventsRepository<E extends BrownieEvent> extends JpaRepository<E, String> {

    /**
     * Query used to get the last up event
     *
     * @param eventOwnerId The identifier of the owner of the event
     *
     * @return the timestamp of the last up event as {@link Long}
     */
    Long getLastUpEvent(String eventOwnerId);

    /**
     * Query used to register a new event
     *
     * @param eventId   The identifier of the event
     * @param type      The type of the event
     * @param eventDate The date when the event occurred
     * @param eventOwnerId The identifier of the owner of the event
     */
    void registerEvent(String eventId, String type, long eventDate, String eventOwnerId);

    /**
     * Query used to register a new event
     *
     * @param eventId   The identifier of the event
     * @param type      The type of the event
     * @param eventDate The date when the event occurred
     * @param extra  The extra information related to the event
     * @param eventOwnerId The identifier of the owner of the event
     */
    void registerEvent(String eventId, String type, long eventDate, String extra, String eventOwnerId);

}
