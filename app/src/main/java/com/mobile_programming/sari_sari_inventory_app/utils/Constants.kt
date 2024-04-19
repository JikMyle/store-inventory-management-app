package com.mobile_programming.sari_sari_inventory_app.utils

import com.mobile_programming.sari_sari_inventory_app.R

enum class SortingType {
    ByProductName,
    ByProductNumber,
    ByPrice,
    ByStock,
    ByRevenue,
    ByNumberSold
}

enum class TextInputErrorType(val stringResourceId: Int) {
    DuplicateFound(R.string.error_duplicate),
    MaximumExceeded(R.string.error_maximum_exceeded),
    MinimumNotReached(R.string.error_minimum_not_reached),
    FieldRequired(R.string.error_field_required)
}