package com.tecknobit.brownie.services.hosts.services;

import com.tecknobit.brownie.services.hosts.entities.HostHistoryEvent;
import com.tecknobit.brownie.services.hosts.repositories.HostEventsRepository;
import com.tecknobit.browniecore.enums.HostEventType;
import com.tecknobit.browniecore.enums.HostStatus;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.tecknobit.browniecore.enums.HostEventType.*;
import static com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController.generateIdentifier;

/**
 * The {@code HostEventsService} class is useful to manage all the {@link HostHistoryEvent} database operations
 *
 * @author N7ghtm4r3 - Tecknobit
 */
@Service
public class HostEventsService {

    /**
     * {@code hostEventsRepository} instance used to access to the {@link HOST_EVENTS_KEY} table
     */
    @Autowired
    private HostEventsRepository hostEventsRepository;

    /**
     * Method used to register a change of the status of a host
     *
     * @param hostId The identifier of the host
     * @param status The status of the host
     */
    @Wrapper
    public void registerHostStatusChangedEvent(String hostId, HostStatus status) {
        HostEventType type = null;
        switch (status) {
            case ONLINE -> type = ONLINE;
            case OFFLINE -> type = OFFLINE;
            case REBOOTING -> type = REBOOTING;
        }
        registerEvent(type, hostId);
    }

    /**
     * Method used to register the {@link HostEventType#RESTARTED} event
     *
     * @param hostId The identifier of the host
     */
    @Wrapper
    public void registerHostRestartedEvent(String hostId) {
        registerEvent(RESTARTED, hostId);
    }

    /**
     * Method used to register the {@link HostEventType#SERVICE_ADDED} event
     *
     * @param hostId      The identifier of the host
     * @param serviceName The name of the service added
     */
    @Wrapper
    public void registerServiceAddedEvent(String hostId, String serviceName) {
        registerEvent(SERVICE_ADDED, hostId, serviceName);
    }

    /**
     * Method used to register the {@link HostEventType#SERVICE_REMOVED} event
     *
     * @param hostId The identifier of the host
     * @param serviceName The name of the service removed
     * @param removeFromTheHost Whether the removing include also the removing from the filesystem of the host
     */
    @Wrapper
    public void registerServiceRemovedEvent(String hostId, String serviceName, boolean removeFromTheHost) {
        HostEventType type = SERVICE_REMOVED;
        if (removeFromTheHost)
            type = SERVICE_DELETED;
        registerEvent(type, hostId, serviceName);
    }

    /**
     * Method used to register any {@link HostEventType} event
     *
     * @param type The type of the event to register
     * @param hostId The identifier of the host
     */
    @Wrapper
    private void registerEvent(HostEventType type, String hostId) {
        registerEvent(type, hostId, null);
    }

    /**
     * Method used to register any {@link HostEventType} event
     *
     * @param type The type of the event to register
     * @param hostId The identifier of the host
     * @param extra The extra information related to the event
     */
    private void registerEvent(HostEventType type, String hostId, Object extra) {
        String eventId = generateIdentifier();
        if (extra == null)
            hostEventsRepository.registerEvent(eventId, type.name(), System.currentTimeMillis(), hostId);
        else
            hostEventsRepository.registerEvent(eventId, type.name(), System.currentTimeMillis(), extra.toString(), hostId);
    }

}
