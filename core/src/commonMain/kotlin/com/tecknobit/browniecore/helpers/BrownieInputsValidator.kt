package com.tecknobit.browniecore.helpers

import com.tecknobit.equinoxcore.helpers.InputsValidator

/**
 * The `BrownieInputsValidator` class is useful to validate the inputs
 *
 * @author N7ghtm4r3 - Tecknobit
 *
 * @see InputsValidator
 */
object BrownieInputsValidator : InputsValidator() {

    /**
     * `BROWNIE_ITEM_NAME_MAX_LENGTH` the max length allowed for a title of a Brownie's item
     */
    private const val BROWNIE_ITEM_NAME_MAX_LENGTH = 30

    /**
     * `HOST_ADDRESS_REGEX` regex used to validate the host address
     */
    private const val HOST_ADDRESS_REGEX =
        "^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$|^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\$"

    /**
     * Method used to check the validity of the name of a Brownie's item
     *
     * @param itemName The name to check
     *
     * @return whether the name is valid as [Boolean]
     */
    fun isItemNameValid(
        itemName: String?,
    ): Boolean {
        return isInputValid(
            input = itemName
        ) && itemName!!.length <= BROWNIE_ITEM_NAME_MAX_LENGTH
    }

    /**
     * Method used to check the validity of the host address
     *
     * @param hostAddress The host address to check
     *
     * @return whether the host address is valid as [Boolean]
     */
    fun isHostAddressValid(
        hostAddress: String?,
    ): Boolean {
        val regex = Regex(HOST_ADDRESS_REGEX)
        return isInputValid(hostAddress) && regex.matches(hostAddress!!)
    }

}