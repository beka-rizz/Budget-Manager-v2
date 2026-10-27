package com.example.pocketbudget.ui

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val moneyFormat = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US).apply {
    groupingSeparator = ' '
})

private val dateFormat = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.ENGLISH)

fun formatMoney(amount: Double): String = "${moneyFormat.format(amount)} ₸"

fun formatDate(dateTime: LocalDateTime): String = dateTime.format(dateFormat)

fun formatPercent(fraction: Float): String = "${(fraction * 100).toInt()}%"

/** Plain number for editable text fields, e.g. 1500.0 -> "1500", 12.5 -> "12.5". */
fun Double.toInputText(): String =
    if (this % 1.0 == 0.0) toLong().toString() else toString()
