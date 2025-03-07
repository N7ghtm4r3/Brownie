package com.tecknobit.browniecore.enums

import kotlinx.serialization.Serializable

/**
 * `ServiceStatus` list of the available status related to a service
 */
@Serializable
enum class ServiceStatus {

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
    REBOOTING;

    /**
     * Method used to check whether the status matches to [RUNNING]
     *
     * @return whether the status is currently [RUNNING] as [Boolean]
     */
    fun isRunning(): Boolean {
        return this == RUNNING
    }

    /**
     * Method used to check whether the status matches to [STOPPED]
     *
     * @return whether the status is currently [STOPPED] as [Boolean]
     */
    fun isStopped(): Boolean {
        return this == STOPPED
    }

    /**
     * Method used to check whether the status matches to [REBOOTING]
     *
     * @return whether the status is currently [REBOOTING] as [Boolean]
     */
    fun isRebooting(): Boolean {
        return this == REBOOTING
    }

}