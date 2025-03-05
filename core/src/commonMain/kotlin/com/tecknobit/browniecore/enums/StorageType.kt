package com.tecknobit.browniecore.enums

import kotlinx.serialization.Serializable

@Serializable
enum class StorageType {

    HARD_DISK,

    SSD,

    SSD_NVMe,

    VIRTUAL_DISK,

    SD_CARD,

    UKNOWN

}