package com.tecknobit.brownie.services.hosts.dtos;

import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.browniecore.enums.HostStatus;
import com.tecknobit.equinoxcore.annotations.DTO;

/**
 * The {@code CurrentHostStatus} record class is used as {@link DTO} to share the information about the current
 * {@link HostStatus} of a {@link BrownieHost}
 *
 * @param id     The identifier of the host
 * @param status The current status of the host
 * @author N7ghtm4r3 - Tecknobit
 */
@DTO
public record CurrentHostStatus(String id, HostStatus status) {
}
