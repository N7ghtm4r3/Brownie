package com.tecknobit.brownie.services.hosts.dtos;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.tecknobit.apimanager.trading.TradingTools;
import com.tecknobit.equinoxcore.annotations.DTO;
import com.tecknobit.equinoxcore.annotations.Structure;

import static com.tecknobit.browniecore.ConstantsKt.*;

@DTO
@Structure
public abstract class BrownieHostStat {

    private final double usageValue;

    private final long totalValue;

    private final double percentValue;

    public BrownieHostStat(double usageValue, long totalValue) {
        this.usageValue = usageValue;
        this.totalValue = totalValue;
        percentValue = TradingTools.computeProportion(totalValue, usageValue, 2);
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
