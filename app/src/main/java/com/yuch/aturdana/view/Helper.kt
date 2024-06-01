package com.yuch.aturdana.view

import android.widget.TextView
import java.text.NumberFormat
import java.util.Locale

fun Double.toCurrencyFormat(): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(this)
}

fun TextView.setFormattedCurrency(amount: Double) {
    this.text = amount.toCurrencyFormat()
}