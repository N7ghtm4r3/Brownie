package com.tecknobit.brownie.services.hosts.dtos;

import com.tecknobit.browniecore.enums.HostStatus;
import com.tecknobit.equinoxcore.annotations.DTO;

@DTO
public record CurrentHostStatus(String id, HostStatus status) {
}
