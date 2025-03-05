package com.tecknobit.browniecore.helpers

import com.tecknobit.equinoxcore.helpers.InputsValidator

object BrownieInputsValidator : InputsValidator() {

    private const val BROWNIE_ITEM_NAME_MAX_LENGTH = 30

    private const val HOST_ADDRESS_REGEX =
        "^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$|^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\$"

    fun isItemNameValid(
        itemName: String?,
    ): Boolean {
        return isInputValid(
            field = itemName
        ) && itemName!!.length <= BROWNIE_ITEM_NAME_MAX_LENGTH
    }

    fun isHostAddressValid(
        hostAddress: String?,
    ): Boolean {
        val regex = Regex(HOST_ADDRESS_REGEX)
        return isInputValid(hostAddress) && regex.matches(hostAddress!!)
    }

}