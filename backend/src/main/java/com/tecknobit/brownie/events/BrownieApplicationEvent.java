package com.tecknobit.brownie.events;

import com.tecknobit.equinoxbackend.events.EquinoxApplicationEvent;
import com.tecknobit.equinoxcore.annotations.Wrapper;

/**
 * The {@code BrownieApplicationEvent} is the class used to share the information of an event emitted by the
 * {@link BrownieEventsEmitter} allowing the dedicated {@link BrownieEventsCollector} to retrieve and use that information
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxApplicationEvent
 * @see BrownieApplicationEventType
 * @since 1.0.1
 */
public class BrownieApplicationEvent extends EquinoxApplicationEvent<BrownieApplicationEventType> {

    /**
     * Constructor used to create the event to emit
     *
     * @param source The source object where the event has been emitted
     * @param eventType The type of the event emitted
     * @param extra Arguments shared with the event
     */
    @Wrapper
    public BrownieApplicationEvent(Object source, BrownieApplicationEventType eventType, Object... extra) {
        super(source, eventType, extra);
    }

}
