package com.tecknobit.brownie.services.hosts.dtos;

import com.tecknobit.equinoxcore.annotations.DTO;

@DTO
public class CPUUsage extends BrownieHostStat {

    private final double clock;

    public CPUUsage(double usageValue, double clock) {
        super(usageValue, 100);
        this.clock = clock;
    }

    public double getClock() {
        return clock;
    }

}
