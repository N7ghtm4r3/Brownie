package com.tecknobit.brownie.services.hosts.dtos.usages;

import com.tecknobit.brownie.services.hosts.dtos.BrownieHostStat;
import com.tecknobit.browniecore.enums.StorageType;
import com.tecknobit.equinoxcore.annotations.DTO;

import static com.tecknobit.browniecore.enums.StorageType.UKNOWN;

/**
 * The {@code StorageUsage} class represents the statistic related to the current usage of the storage of the host
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see BrownieHostStat
 */
@DTO
public class StorageUsage extends BrownieHostStat {

    /**
     * {@code type} the type of the storage the host has
     */
    private final StorageType type;

    /**
     * Constructor to instantiate the object with default values when the host is not reachable
     */
    public StorageUsage() {
        super();
        type = UKNOWN;
    }

    /**
     * Constructor to instantiate the object
     *
     * @param rawStats The statistic to retrieve from a raw format
     * @param rawType  The type of the storage raw formatted
     */
    public StorageUsage(String rawStats, String rawType) {
        super(rawStats);
        this.type = StorageType.valueOf(rawType);
    }

    /**
     * Method to get the {@link #type} instance
     *
     * @return the {@link #type} instance as {@link StorageType}
     */
    public StorageType getType() {
        return type;
    }

}
