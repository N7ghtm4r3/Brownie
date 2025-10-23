package com.tecknobit.brownie.services.hosts.dtos

import com.tecknobit.equinoxcore.annotations.DTO

@DTO
data class RemoteHostData(
    val sshUser: String,
    val sshPassword: String,
    val macAddress: String,
    val broadcastIp: String,
)
