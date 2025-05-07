package com.tecknobit.brownie.events;

import com.tecknobit.equinoxbackend.events.EquinoxEventsCollector;

/**
 * The {@code BrownieEventsCollector} is the interface that allows to collect all the events emitted by the
 * {@link BrownieEventsEmitter} and to handle each specific event
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxEventsCollector
 * @see BrownieApplicationEventType
 * @see BrownieApplicationEvent
 * @since 1.0.1
 */
@FunctionalInterface
public interface BrownieEventsCollector extends EquinoxEventsCollector<BrownieApplicationEventType, BrownieApplicationEvent> {
}
