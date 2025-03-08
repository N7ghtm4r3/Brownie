package com.tecknobit.brownie.services.hosts.dtos.usages;

import com.tecknobit.brownie.services.hosts.dtos.BrownieHostStat;
import com.tecknobit.equinoxcore.annotations.DTO;

/**
 * The {@code CPUUsage} class represents the statistic related to the current usage of the {@code CPU} of the host
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see BrownieHostStat
 */
@DTO
public class CPUUsage extends BrownieHostStat {

    /**
     * {@code clock} the current clock of the {@code CPU}
     */
    private final double clock;

    /**
     * Constructor to instantiate the object with default values when the host is not reachable
     */
    public CPUUsage() {
        super();
        clock = 0;
    }

    /**
     * Constructor to instantiate the object
     *
     * @param usageValue The current usage of a resource of the host
     * @param clock      The current clock of the {@code CPU}
     */
    public CPUUsage(String usageValue, String clock) {
        super(usageValue, 100);
        this.clock = Double.parseDouble(clock);
    }

    /**
     * Method to get the {@link #clock} instance
     *
     * @return the {@link #clock} instance as {@code double}
     */
    public double getClock() {
        return clock;
    }

}
