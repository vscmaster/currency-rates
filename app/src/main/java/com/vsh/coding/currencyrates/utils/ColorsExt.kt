package com.vsh.coding.currencyrates.utils

import androidx.compose.material.Colors
import androidx.compose.ui.graphics.Color
import com.vsh.coding.currencyrates.theme.LightGray200
import com.vsh.coding.currencyrates.theme.LightGray400

val Colors.rateRowColor: Color
    get() = if (isLight) LightGray400 else LightGray200