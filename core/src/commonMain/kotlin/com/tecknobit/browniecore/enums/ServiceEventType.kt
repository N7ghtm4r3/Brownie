package com.tecknobit.browniecore.enums

import kotlinx.serialization.Serializable

/**
 * `ServiceEventType` list of the available events related to the service lifecycle
 */
@Serializable
enum class ServiceEventType {

    /**
     * `RUNNING` the service is currently running
     */
    RUNNING,

    /**
     * `STOPPED` the service has been stopped
     */
    STOPPED,

    /**
     * `REBOOTING` the service is currently rebooting
     */
    REBOOTING,

    /**
     * `RESTARTED` the service has been restarted after a [REBOOTING]
     */
    RESTARTED

}