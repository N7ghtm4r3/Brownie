package com.tecknobit.browniecore.enums

import kotlinx.serialization.Serializable

@Serializable
enum class HostEventType {

    ONLINE,

    OFFLINE,

    REBOOTING,

    RESTARTED,

    SERVICE_ADDED,

    SERVICE_REMOVED,

    SERVICE_DELETED

}