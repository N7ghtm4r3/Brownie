package com.tecknobit.brownie.services.hosts.dtos;

import com.tecknobit.browniecore.enums.HostStatus;
import com.tecknobit.equinoxcore.annotations.DTO;

@DTO
public class CurrentHostStatus {

    private final String id;

    private final HostStatus status;

    public CurrentHostStatus(String id, HostStatus status) {
        this.id = id;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public HostStatus getStatus() {
        return status;
    }

}
