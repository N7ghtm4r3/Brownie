package com.tecknobit.brownie.services.hostservices.dtos;

import com.tecknobit.brownie.services.hosts.services.HostsService;
import com.tecknobit.browniecore.enums.ServiceStatus;
import com.tecknobit.equinoxcore.annotations.DTO;

/**
 * The {@code CurrentServiceStatus} record class is used as {@link DTO} to share the information about the current
 * {@link ServiceStatus} of a {@link HostsService}
 *
 * @param id     The identifier of the service
 * @param status The current status of the service
 * @param pid    The pid of the service
 * @author N7ghtm4r3 - Tecknobit
 */
@DTO
public record CurrentServiceStatus(String id, ServiceStatus status, long pid) {
}
