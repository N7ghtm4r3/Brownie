package com.tecknobit.brownie.services.hostservices.dtos;

import com.tecknobit.browniecore.enums.ServiceStatus;
import com.tecknobit.equinoxcore.annotations.DTO;

@DTO
public record CurrentServiceStatus(String id, ServiceStatus status, long pid) {
}
