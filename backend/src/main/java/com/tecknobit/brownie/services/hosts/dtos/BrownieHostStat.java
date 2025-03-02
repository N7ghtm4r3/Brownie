package com.tecknobit.brownie.services.hosts.dtos;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.tecknobit.apimanager.trading.TradingTools;
import com.tecknobit.equinoxcore.annotations.DTO;
import com.tecknobit.equinoxcore.annotations.Structure;

import static com.tecknobit.browniecore.ConstantsKt.*;

@DTO
@Structure
public class BrownieHostStat {

    private final double usageValue;

    private final long totalValue;

    private final double percentValue;

    public BrownieHostStat() {
        usageValue = 0;
        totalValue = 0;
        percentValue = 0;
    }

    public BrownieHostStat(String rawStats) {
        String[] statsSlices = rawStats.split("/");
        usageValue = Double.parseDouble(statsSlices[0]);
        totalValue = Long.parseLong(statsSlices[1]);
        percentValue = TradingTools.computeProportion(totalValue, this.usageValue, 2);
    }

    public BrownieHostStat(String usageValue, long totalValue) {
        this.usageValue = Double.parseDouble(usageValue);
        this.totalValue = totalValue;
        percentValue = TradingTools.computeProportion(totalValue, this.usageValue, 2);
    }

    @JsonGetter(USAGE_VALUE_KEY)
    public double getUsageValue() {
        return usageValue;
    }

    @JsonGetter(TOTAL_VALUE_KEY)
    public long getTotalValue() {
        return totalValue;
    }

    @JsonGetter(PERCENT_VALUE_KEY)
    public double getPercentValue() {
        return percentValue;
    }

}
