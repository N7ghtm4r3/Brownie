package com.tecknobit.brownie.services.hosts.dtos.usages;

import com.tecknobit.brownie.services.hosts.dtos.BrownieHostStat;
import com.tecknobit.browniecore.enums.StorageType;
import com.tecknobit.equinoxcore.annotations.DTO;

import static com.tecknobit.browniecore.enums.StorageType.UKNOWN;

@DTO
public class StorageUsage extends BrownieHostStat {

    private final StorageType type;

    public StorageUsage() {
        super();
        type = UKNOWN;
    }

    public StorageUsage(String rawStats, String rawType) {
        super(rawStats);
        this.type = StorageType.valueOf(rawType);
    }

    public StorageType getType() {
        return type;
    }

}
