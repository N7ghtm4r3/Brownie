package com.tecknobit.brownie.services.hosts.services;

import com.tecknobit.brownie.services.hosts.entities.HostHistoryEvent;
import com.tecknobit.brownie.services.hosts.repositories.HostEventsRepository;
import com.tecknobit.brownie.services.shared.services.BrownieEventsRecorder;
import com.tecknobit.browniecore.ConstantsKt;
import com.tecknobit.browniecore.enums.HostEventType;
import com.tecknobit.browniecore.enums.HostStatus;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.tecknobit.browniecore.enums.HostEventType.*;

/**
 * The {@code HostEventsService} class is useful to manage all the {@link HostHistoryEvent} database operations
 *
 * @author N7ghtm4r3 - Tecknobit
 *
 * @see BrownieEventsRecorder
 */
@Service
public class HostEventsService extends BrownieEventsRecorder<HostEventType, HostHistoryEvent> {

    /**
     * Constructor to instantiate the service
     *
     * @param hostEventsRepository The instance used to register the events in the {@link ConstantsKt#HOST_EVENTS_KEY} table
     */
    @Autowired
    protected HostEventsService(HostEventsRepository hostEventsRepository) {
        super(hostEventsRepository);
    }

    /**
     * Method used to register a change of the status of a host
     *
     * @param hostId The identifier of the host
     * @param status The status of the host
     */
    @Wrapper
    public void registerHostStatusChangedEvent(String hostId, HostStatus status) {
        if (status.isOnline())
            registerEvent(ONLINE, hostId);
        else {
            HostEventType type;
            if (status.isOffline())
                type = OFFLINE;
            else
                type = REBOOTING;
            registerEvent(type, hostId, calculateUpDays(hostId));
        }
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

}
