package com.tecknobit.brownie.services.shared.services;

import com.tecknobit.brownie.services.shared.entities.BrownieEvent;
import com.tecknobit.brownie.services.shared.repositories.BrownieEventsRepository;
import com.tecknobit.equinoxcore.annotations.Structure;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import com.tecknobit.equinoxcore.time.TimeFormatter;
import org.springframework.stereotype.Service;

import static com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController.generateIdentifier;

@Service
@Structure
public abstract class BrownieEventsRecorder<T extends Enum<T>, E extends BrownieEvent> {

    private final BrownieEventsRepository<E> repository;

    protected BrownieEventsRecorder(BrownieEventsRepository<E> repository) {
        this.repository = repository;
    }

    /**
     * Method used to calculate the up days since the last up event
     *
     * @param eventOwnerId The identifier of the owner of the event
     * @return the running days since the last up event as {@code int}
     */
    protected int calculateUpDays(String eventOwnerId) {
        Long lastRunningEvent = repository.getLastUpEvent(eventOwnerId);
        System.out.println(lastRunningEvent);
        if (lastRunningEvent == null)
            lastRunningEvent = 0L;
        return TimeFormatter.INSTANCE.daysUntilNow(lastRunningEvent);
    }

    /**
     * Method used to register any {@link T} event
     *
     * @param type      The type of the event to register
     * @param eventOwnerId The identifier of the owner of the event
     */
    @Wrapper
    protected void registerEvent(T type, String eventOwnerId) {
        registerEvent(type, eventOwnerId, null);
    }

    /**
     * Method used to register any {@link E} event
     *
     * @param type   The type of the event to register
     * @param eventOwnerId The identifier of the owner of the event
     * @param extra  The extra information related to the event
     */
    protected void registerEvent(T type, String eventOwnerId, Object extra) {
        String eventId = generateIdentifier();
        if (extra == null)
            repository.registerEvent(eventId, type.name(), System.currentTimeMillis(), eventOwnerId);
        else
            repository.registerEvent(eventId, type.name(), System.currentTimeMillis(), extra.toString(), eventOwnerId);
    }

}
