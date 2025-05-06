package com.tecknobit.brownie.services.shared.services;

import com.tecknobit.brownie.services.shared.entities.BrownieEvent;
import com.tecknobit.brownie.services.shared.repositories.EventsRepository;
import com.tecknobit.equinoxcore.annotations.Structure;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import com.tecknobit.equinoxcore.time.TimeFormatter;
import org.springframework.stereotype.Service;

import static com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController.generateIdentifier;

@Service
@Structure
public abstract class EventsRecorder<T extends Enum<T>, E extends BrownieEvent> {

    private final EventsRepository<E> repository;

    protected EventsRecorder(EventsRepository<E> repository) {
        this.repository = repository;
    }

    /**
     * Method used to calculate the running days since the last {@code RUNNING} event
     *
     * @param serviceId The identifier of the service
     * @return the running days since the last {@code RUNNING} event as {@code int}
     */
    protected int calculateRunningDays(String serviceId) {
        Long lastRunningEvent = repository.getLastRunningEvent(serviceId);
        if (lastRunningEvent == null)
            lastRunningEvent = 0L;
        return TimeFormatter.INSTANCE.daysUntilNow(lastRunningEvent);
    }

    /**
     * Method used to register any {@link T} event
     *
     * @param type      The type of the event to register
     * @param serviceId The identifier of the service
     */
    @Wrapper
    protected void registerEvent(T type, String serviceId) {
        registerEvent(type, serviceId, null);
    }

    /**
     * Method used to register any {@link T} event
     *
     * @param type   The type of the event to register
     * @param hostId The identifier of the host
     * @param extra  The extra information related to the event
     */
    protected void registerEvent(T type, String hostId, Object extra) {
        String eventId = generateIdentifier();
        if (extra == null)
            repository.registerEvent(eventId, type.name(), System.currentTimeMillis(), hostId);
        else
            repository.registerEvent(eventId, type.name(), System.currentTimeMillis(), extra.toString(), hostId);
    }

}
