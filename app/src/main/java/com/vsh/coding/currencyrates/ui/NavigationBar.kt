package com.vsh.coding.currencyrates.ui

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vsh.coding.currencyrates.R
import com.vsh.coding.currencyrates.utils.hideDay
import java.util.*

@Composable
fun NavigationBar(
    context: Context,
    currentRateDate: Date,
    onYearChange: (date: Date) -> Unit
) {
    val currentRateDateCalendar: Calendar = Calendar.getInstance().apply { time = currentRateDate }
    val currentRateYear = currentRateDateCalendar.get(Calendar.YEAR)
    var currentRateYearState by rememberSaveable { mutableStateOf(currentRateYear) }
    val currentDateCalendar = Calendar.getInstance()
    val minDateCalendar = Calendar.getInstance()
    minDateCalendar.set(Calendar.YEAR, 2000)
    val maxYear = currentDateCalendar.get(Calendar.YEAR)
    val minYear = minDateCalendar.get(Calendar.YEAR)

    val date = remember { mutableStateOf("") }
    val yearPickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, _: Int, _: Int ->
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            date.value = currentRateYearState.toString()
            currentRateYearState = year

            onYearChange(currentRateDateCalendar.apply {
                set(
                    Calendar.YEAR,
                    year
                )
            }.time)
        }, currentRateYearState, 0, 1
    )

    yearPickerDialog.datePicker.hideDay()
    yearPickerDialog.datePicker.maxDate = currentDateCalendar.time.time
    yearPickerDialog.datePicker.minDate = minDateCalendar.time.time

    Row(
        modifier = Modifier
            .background(color = colorResource(id = R.color.purple_200))
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {

        if (currentRateYearState > minYear) {
            IconButton(modifier = Modifier.padding(start = 5.dp),
                onClick = {
                    val newValue =
                        backYear(currentRateYearState, minYear)
                    currentRateYearState = newValue
                    if (newValue != currentRateYear) {
                        onYearChange(currentRateDateCalendar.apply {
                            set(
                                Calendar.YEAR,
                                newValue
                            )
                        }.time)
                    }
                }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_move_year_back)
                )
            }
        }
        Text(
            text = "$currentRateYearState",
            modifier = Modifier
                .weight(1f)
                .clickable(
                    onClick = {
                        yearPickerDialog.show()
                    },
                ),
            textAlign = TextAlign.Center,

            )
        if (currentRateYearState < maxYear) {
            IconButton(modifier = Modifier.padding(start = 5.dp),
                onClick = {
                    val newValue = forwardYear(currentRateYearState, maxYear)
                    currentRateYearState = newValue
                    if (newValue != currentRateYear) {
                        onYearChange(currentRateDateCalendar.apply {
                            set(
                                Calendar.YEAR,
                                newValue
                            )
                        }.time)
                    }
                }) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.cd_move_year_forward)
                )
            }
        }
    }
}

private fun backYear(currentRateYear: Int, minYear: Int): Int {
    return if (currentRateYear > minYear) currentRateYear - 1 else currentRateYear
}

private fun forwardYear(currentRateYear: Int, maxYear: Int): Int {

    return if (currentRateYear < maxYear) currentRateYear + 1 else currentRateYear
}