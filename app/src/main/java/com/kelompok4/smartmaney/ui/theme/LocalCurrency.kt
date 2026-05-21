package com.kelompok4.smartmaney.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import com.kelompok4.smartmaney.data.local.preferences.CurrencyOption

val LocalCurrency = compositionLocalOf { CurrencyOption.IDR }

@Composable
@ReadOnlyComposable
fun formatMoney(amount: Int): String = LocalCurrency.current.format(amount)

@Composable
@ReadOnlyComposable
fun currencySymbol(): String = LocalCurrency.current.symbol
