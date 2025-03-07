package com.tecknobit.browniecore.enums

import kotlinx.serialization.Serializable

/**
 * `HostStatus` list of the available status related to a host
 */
@Serializable
enum class HostStatus {

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
    REBOOTING;

    /**
     * Method used to check whether the status matches to [ONLINE]
     *
     * @return whether the status is currently [ONLINE] as [Boolean]
     */
    fun isOnline(): Boolean {
        return this == ONLINE
    }

    /**
     * Method used to check whether the status matches to [OFFLINE]
     *
     * @return whether the status is currently [OFFLINE] as [Boolean]
     */
    fun isOffline(): Boolean {
        return this == OFFLINE
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