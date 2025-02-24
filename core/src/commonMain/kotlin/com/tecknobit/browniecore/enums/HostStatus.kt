package com.tecknobit.browniecore.enums

import kotlinx.serialization.Serializable

@Serializable
enum class HostStatus {

    ONLINE,

    OFFLINE,

    REBOOTING;

    fun isOnline(): Boolean {
        return this == ONLINE
    }

    fun isOffline(): Boolean {
        return this == OFFLINE
    }

    fun isRebooting(): Boolean {
        return this == REBOOTING
    }

}