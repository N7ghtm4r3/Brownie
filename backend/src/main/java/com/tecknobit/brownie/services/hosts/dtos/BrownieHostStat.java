package com.tecknobit.brownie.services.hosts.dtos;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.tecknobit.apimanager.trading.TradingTools;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.equinoxcore.annotations.DTO;
import com.tecknobit.equinoxcore.annotations.Structure;

import static com.tecknobit.browniecore.ConstantsKt.*;

/**
 * The {@code BrownieHostStat} class represents a statistic about {@link BrownieHost}
 *
 * @author N7ghtm4r3 - Tecknobit
 */
@DTO
@Structure
public class BrownieHostStat {

    /**
     * {@code usageValue} the current usage of a resource of the host
     */
    private final double usageValue;

    /**
     * {@code totalValue} the total amount of the resource available of the host
     */
    private final double totalValue;

    /**
     * {@code percentValue} the current percent value of the resource usage
     */
    private final double percentValue;

    /**
     * Constructor to instantiate the object with default values when the host is not reachable
     */
    public BrownieHostStat() {
        usageValue = 0;
        totalValue = 0;
        percentValue = 0;
    }

    /**
     * Constructor to instantiate the object
     *
     * @param rawStats The statistic to retrieve from a raw format
     */
    public BrownieHostStat(String rawStats) {
        String[] statsSlices = rawStats.split("/");
        usageValue = Double.parseDouble(statsSlices[0]);
        totalValue = Double.parseDouble(statsSlices[1]);
        percentValue = TradingTools.computeProportion(totalValue, this.usageValue, 2);
    }

    /**
     * Constructor to instantiate the object
     *
     * @param usageValue The current usage of a resource of the host
     * @param totalValue The total amount of the resource available of the host
     */
    public BrownieHostStat(String usageValue, long totalValue) {
        this.usageValue = Double.parseDouble(usageValue);
        this.totalValue = totalValue;
        percentValue = TradingTools.computeProportion(totalValue, this.usageValue, 2);
    }

    /**
     * Method to get the {@link #usageValue} instance
     *
     * @return the {@link #usageValue} instance as {@code double}
     */
    @JsonGetter(USAGE_VALUE_KEY)
    public double getUsageValue() {
        return usageValue;
    }

    /**
     * Method to get the {@link #totalValue} instance
     *
     * @return the {@link #totalValue} instance as {@code double}
     */
    @JsonGetter(TOTAL_VALUE_KEY)
    public double getTotalValue() {
        return totalValue;
    }

    /**
     * Method to get the {@link #percentValue} instance
     *
     * @return the {@link #percentValue} instance as {@code double}
     */
    @JsonGetter(PERCENT_VALUE_KEY)
    public double getPercentValue() {
        return percentValue;
    }

}
