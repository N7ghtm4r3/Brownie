package com.tecknobit.brownie.services.hosts.dtos

import com.tecknobit.equinoxcore.annotations.DTO

/**
 * The `RemoteHostData` data class represents the sensitive data of a remote host
 *
 * @property sshUser       The user to use for the SSH connection
 * @property sshPassword   The password to use for the SSH connection
 * @property macAddress    The physical mac address of the remote host network interface
 * @property broadcastIp   The ip address of the remote host network interface
 *
 * @author N7ghtm4r3 - Tecknobit
 *
 * @since 1.0.4
 */
@DTO
data class RemoteHostData(
    val sshUser: String,
    val sshPassword: String,
    val macAddress: String,
    val broadcastIp: String,
)
