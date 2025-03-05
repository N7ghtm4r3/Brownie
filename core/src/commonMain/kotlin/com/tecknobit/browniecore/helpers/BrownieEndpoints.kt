package com.tecknobit.browniecore.helpers

import com.tecknobit.equinoxcore.network.EquinoxBaseEndpointsSet

object BrownieEndpoints : EquinoxBaseEndpointsSet() {

    const val CONNECT_ENDPOINT = "/connect"

    const val START_ENDPOINT = "/start"

    const val STOP_ENDPOINT = "/stop"

    const val REBOOT_ENDPOINT = "/reboot"

    const val OVERVIEW_ENDPOINT = "/overview"

}