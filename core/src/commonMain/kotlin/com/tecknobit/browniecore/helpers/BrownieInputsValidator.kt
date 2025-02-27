package com.tecknobit.browniecore.helpers

import com.tecknobit.equinoxcore.helpers.InputsValidator

object BrownieInputsValidator : InputsValidator() {

    private const val BROWNIE_ITEM_NAME_MAX_LENGTH = 30

    fun isItemNameValid(
        itemName: String?,
    ): Boolean {
        return isInputValid(
            field = itemName
        ) && itemName!!.length <= BROWNIE_ITEM_NAME_MAX_LENGTH
    }

}