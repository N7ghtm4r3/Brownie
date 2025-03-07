package com.tecknobit.browniecore.enums

import kotlinx.serialization.Serializable

/**
 * `StorageType` list of the available types to categorize the storage the host has
 */
@Serializable
enum class StorageType {

    /**
     * `HARD_DISK` the storage unit of the host is a hard disk
     */
    HARD_DISK,

    /**
     * `SSD` the storage unit of the host is a solid state drive
     */
    SSD,

    /**
     * `SSD_NVMe` the storage unit of the host is a solid state drive of the NvMe type
     */
    SSD_NVMe,

    /**
     * `VIRTUAL_DISK` the storage unit of the host is a virtual disk, classic scenario of a VPS for example
     */
    VIRTUAL_DISK,

    /**
     * `SD_CARD` the storage unit of the host is a sd card, classic scenario of a VPS for Raspberries or embedded shields
     */
    SD_CARD,

    /**
     * `UKNOWN` fallback scenario when the specific type of the storage cannot be retrieved
     */
    UKNOWN

}