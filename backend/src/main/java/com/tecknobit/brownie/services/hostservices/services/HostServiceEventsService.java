package com.tecknobit.brownie.services.hostservices.services;

import com.tecknobit.brownie.services.hostservices.entities.ServiceEvent;
import com.tecknobit.brownie.services.hostservices.repositories.HostServiceEventsRepository;
import com.tecknobit.brownie.services.shared.services.BrownieEventsRecorder;
import com.tecknobit.browniecore.ConstantsKt;
import com.tecknobit.browniecore.enums.ServiceEventType;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.tecknobit.browniecore.enums.ServiceEventType.*;

/**
 * The {@code HostServiceEventsService} class is useful to manage all the {@link ServiceEvent} database operations
 *
 * @author N7ghtm4r3 - Tecknobit
 *
 * @see BrownieEventsRecorder
 */
@Service
public class HostServiceEventsService extends BrownieEventsRecorder<ServiceEventType, ServiceEvent> {

    /**
     * Constructor to instantiate the service
     *
     * @param eventsRepository The instance used to register the events in the {@link ConstantsKt#SERVICE_EVENTS_KEY} table
     */
    @Autowired
    public HostServiceEventsService(HostServiceEventsRepository eventsRepository) {
        super(eventsRepository);
    }

    /**
     * Method used to register the {@link ServiceEventType#RUNNING} event
     *
     * @param serviceId The identifier of the service
     * @param pid       The pid of the service started
     */
    @Wrapper
    public void registerServiceStarted(String serviceId, long pid) {
        registerEvent(RUNNING, serviceId, pid);
    }

    /**
     * Method used to register the {@link ServiceEventType#STOPPED} event
     *
     * @param serviceId The identifier of the service
     */
    @Wrapper
    public void registerServiceStopped(String serviceId) {
        registerEvent(STOPPED, serviceId, calculateUpDays(serviceId));
    }

    /**
     * Method used to register the {@link ServiceEventType#REBOOTING} event
     *
     * @param serviceId The identifier of the service
     */
    @Wrapper
    public void registerServiceRebooted(String serviceId) {
        registerEvent(REBOOTING, serviceId, calculateUpDays(serviceId));
    }

    /**
     * Method used to register the {@link ServiceEventType#RESTARTED} event
     *
     * @param serviceId The identifier of the service
     * @param pid The pid of the service restarted
     */
    @Wrapper
    public void registerServiceRestarted(String serviceId, long pid) {
        registerEvent(RESTARTED, serviceId, pid);
    }

}
