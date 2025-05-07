package com.tecknobit.brownie.events;

/**
 * Available types of the events shareable between services of the backend
 *
 * @since 1.0.1
 */
public enum BrownieApplicationEventType {

    /**
     * {@code SYNC_SERVICES} event emitted when the services of a host have to be synced due their real status inside
     * the host
     */
    SYNC_SERVICES

}
