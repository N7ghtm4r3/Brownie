package com.tecknobit.browniecore.helpers

import com.tecknobit.equinoxcore.network.EquinoxBaseEndpointsSet

/**
 * The `BrownieEndpoints` are all the available endpoints of the Brownie's system
 *
 * @author N7ghtm4r3 - Tecknobit
 */
object BrownieEndpoints : EquinoxBaseEndpointsSet() {

    /**
     * `CONNECT_ENDPOINT` endpoint used to connect to a session
     */
    const val CONNECT_ENDPOINT = "/connect"

    /**
     * `START_ENDPOINT` endpoint used to start a host or a service
     */
    const val START_ENDPOINT = "/start"

    /**
     * `STOP_ENDPOINT` endpoint used to stop a host or a service
     */
    const val STOP_ENDPOINT = "/stop"

    /**
     * `REBOOT_ENDPOINT` endpoint used to reboot a host or a service
     */
    const val REBOOT_ENDPOINT = "/reboot"

    /**
     * `OVERVIEW_ENDPOINT` endpoint used to retrieve an overview of a host
     */
    const val OVERVIEW_ENDPOINT = "/overview"

}