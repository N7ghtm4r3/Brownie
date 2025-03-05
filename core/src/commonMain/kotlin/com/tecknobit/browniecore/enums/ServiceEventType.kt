package com.tecknobit.browniecore.enums

import kotlinx.serialization.Serializable

@Serializable
enum class ServiceEventType {

    RUNNING,

    STOPPED,

    REBOOTING,

    RESTARTED

}