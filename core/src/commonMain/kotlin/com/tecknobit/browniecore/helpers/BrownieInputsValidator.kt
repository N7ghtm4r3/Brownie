package com.tecknobit.browniecore.helpers

import com.tecknobit.equinoxcore.helpers.InputsValidator

object BrownieInputsValidator : InputsValidator() {

    private const val BROWNIE_ITEM_NAME_MAX_LENGTH = 30

    fun isServiceNameValid(
        serviceName: String?,
    ): Boolean {
        return isInputValid(
            field = serviceName
        ) && serviceName!!.length <= BROWNIE_ITEM_NAME_MAX_LENGTH
    }

}