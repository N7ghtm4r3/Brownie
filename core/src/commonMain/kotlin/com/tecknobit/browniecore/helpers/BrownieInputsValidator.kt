package com.tecknobit.browniecore.helpers

import com.tecknobit.equinoxcore.helpers.InputsValidator

object BrownieInputsValidator : InputsValidator() {

    private const val BROWNIE_ITEM_NAME_MAX_LENGTH = 30

    private const val HOST_ADDRESS_REGEX = "^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$|^[0-9]{1,3}(\\.[0-9]{1,3}){3}\$"

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