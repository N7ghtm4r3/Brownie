package com.tecknobit.brownie.services.hosts.dtos.usages;

import com.tecknobit.brownie.services.hosts.dtos.BrownieHostStat;
import com.tecknobit.equinoxcore.annotations.DTO;

@DTO
public class CPUUsage extends BrownieHostStat {

    private final double clock;

    public CPUUsage() {
        super();
        clock = 0;
    }

    public CPUUsage(String usageValue, String clock) {
        super(usageValue, 100);
        this.clock = Double.parseDouble(clock);
    }

    public double getClock() {
        return clock;
    }

}
