package com.tecknobit.brownie.events;

import com.tecknobit.equinoxbackend.events.EquinoxEventsEmitter;
import org.springframework.stereotype.Service;

/**
 * The {@code BrownieEventsEmitter} class is useful to share events between
 * services in a strict and controlled way by leveraging the {@code enums}
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxEventsEmitter
 * @see BrownieApplicationEventType
 * @since 1.0.1
 */
@Service
public abstract class BrownieEventsEmitter extends EquinoxEventsEmitter<BrownieApplicationEventType> {
}
