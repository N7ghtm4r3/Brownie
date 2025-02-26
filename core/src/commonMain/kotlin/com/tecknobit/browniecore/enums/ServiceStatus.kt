package com.tecknobit.browniecore.enums

enum class ServiceStatus {

    RUNNING,

    STOPPED,

    REBOOTING;

    fun isRunning(): Boolean {
        return this == RUNNING
    }

    fun isStopped(): Boolean {
        return this == STOPPED
    }

    fun isRebooting(): Boolean {
        return this == REBOOTING
    }

}