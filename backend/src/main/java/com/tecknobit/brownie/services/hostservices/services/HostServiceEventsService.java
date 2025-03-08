package com.tecknobit.brownie.services.hostservices.services;

import com.tecknobit.brownie.services.hostservices.entities.ServiceEvent;
import com.tecknobit.brownie.services.hostservices.repositories.HostServiceEventsRepository;
import com.tecknobit.browniecore.enums.ServiceEventType;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import com.tecknobit.equinoxcore.time.TimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.tecknobit.browniecore.enums.ServiceEventType.*;
import static com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController.generateIdentifier;

/**
 * The {@code HostServiceEventsService} class is useful to manage all the {@link ServiceEvent} database operations
 *
 * @author N7ghtm4r3 - Tecknobit
 */
@Service
public class HostServiceEventsService {

    /**
     * {@code eventsRepository} instance used to access to the {@link SERVICE_EVENTS_KEY} table
     */
    @Autowired
    private HostServiceEventsRepository eventsRepository;

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
        registerEvent(STOPPED, serviceId, calculateRunningDays(serviceId));
    }

    /**
     * Method used to register the {@link ServiceEventType#REBOOTING} event
     *
     * @param serviceId The identifier of the service
     */
    @Wrapper
    public void registerServiceRebooted(String serviceId) {
        registerEvent(REBOOTING, serviceId, calculateRunningDays(serviceId));
    }

    /**
     * Method used to calculate the running days since the last {@link ServiceEventType#RUNNING} event
     *
     * @param serviceId The identifier of the service
     * @return the running days since the last {@link ServiceEventType#RUNNING} event as {@code int}
     */
    private int calculateRunningDays(String serviceId) {
        long lastRunningEvent = eventsRepository.getLastRunningEvent(serviceId);
        return TimeFormatter.INSTANCE.daysUntilNow(lastRunningEvent);
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

    /**
     * Method used to register any {@link ServiceEventType} event
     *
     * @param type The type of the event to register
     * @param serviceId The identifier of the service
     */
    @Wrapper
    private void registerEvent(ServiceEventType type, String serviceId) {
        registerEvent(type, serviceId, null);
    }

    /**
     * Method used to register any {@link ServiceEventType} event
     *
     * @param type The type of the event to register
     * @param serviceId The identifier of the service
     * @param extra The extra information related to the event
     */
    private void registerEvent(ServiceEventType type, String serviceId, Object extra) {
        String eventId = generateIdentifier();
        if (extra == null)
            eventsRepository.registerEvent(eventId, type.name(), System.currentTimeMillis(), serviceId);
        else
            eventsRepository.registerEvent(eventId, type.name(), System.currentTimeMillis(), extra.toString(), serviceId);
    }

}
