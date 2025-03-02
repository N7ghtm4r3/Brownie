package com.tecknobit.brownie.services.hosts.dtos;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.equinoxcore.annotations.DTO;

import static com.tecknobit.browniecore.ConstantsKt.CPU_USAGE_KEY;

@DTO
public class BrownieHostOverview extends BrownieHost {

    private final CPUUsage cpuUsage;

    public BrownieHostOverview(CPUUsage cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    @JsonGetter(CPU_USAGE_KEY)
    public CPUUsage getCpuUsage() {
        return cpuUsage;
    }

}
