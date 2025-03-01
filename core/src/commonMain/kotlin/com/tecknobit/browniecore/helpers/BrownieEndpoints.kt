package com.tecknobit.browniecore.helpers

import com.tecknobit.equinoxcore.network.EquinoxBaseEndpointsSet

object BrownieEndpoints : EquinoxBaseEndpointsSet() {

    const val CONNECT_ENDPOINT = "/connect"

    const val START_HOST_ENDPOINT = "/start"

    const val STOP_HOST_ENDPOINT = "/stop"

    const val REBOOT_HOST_ENDPOINT = "/reboot"

}