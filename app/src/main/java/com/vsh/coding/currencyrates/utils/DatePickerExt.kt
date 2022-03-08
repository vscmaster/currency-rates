package com.vsh.coding.currencyrates.utils

import android.content.res.Resources
import android.util.Log
import android.view.View
import android.widget.DatePicker

@Deprecated("Use proper way to manage fields visibility")
fun DatePicker.hideDay() {
    try {
        val headerDayId: Int =
            Resources.getSystem().getIdentifier("date_picker_header_date", "id", "android")
        val dayPickerId: Int =
            Resources.getSystem().getIdentifier("date_picker_day_picker", "id", "android")
        val yearPickerId: Int =
            Resources.getSystem().getIdentifier("date_picker_year_picker", "id", "android")

        val dayHeaderView: View? = findViewById(headerDayId)
        val dayPickerView: View? = findViewById(dayPickerId)
        val yearPickerView: View? = findViewById(yearPickerId)
        if (dayHeaderView != null) {
            dayHeaderView.visibility = View.GONE
        }

        if (dayPickerView != null) {
            dayPickerView.visibility = View.GONE
        }

        if (yearPickerView != null) {
            yearPickerView.visibility = View.VISIBLE
        }
    } catch (e: SecurityException) {
        Log.w("ERROR", e.message, e)
    } catch (e: IllegalArgumentException) {
        Log.w("ERROR", e.message, e)
    } catch (e: IllegalAccessException) {
        Log.w("ERROR", e.message, e)
    }
}