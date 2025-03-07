package com.tecknobit.browniecore.enums

import kotlinx.serialization.Serializable

/**
 * `HostEventType` list of the available events related to the host lifecycle
 */
@Serializable
enum class HostEventType {

    /**
     * `ONLINE` the host is online and is reachable
     */
    ONLINE,

    /**
     * `OFFLINE` the host is offline and is not reachable
     */
    OFFLINE,

    /**
     * `REBOOTING` the host is rebooting and is not reachable
     */
    REBOOTING,

    /**
     * `RESTARTED` the host has been restarted and is reachable again
     */
    RESTARTED,

    /**
     * `SERVICE_ADDED` a new service have been attached to the current host
     */
    SERVICE_ADDED,

    /**
     * `SERVICE_REMOVED` a service have been removed from the current host
     */
    SERVICE_REMOVED,

    /**
     * `SERVICE_DELETED` a service have been removed from the current host and also from the physical machine of the
     * host
     */
    SERVICE_DELETED

}