package com.gustavobento.mygym.util

import java.time.DayOfWeek
import java.time.LocalDate

object DateUtils {

    /** Segunda-feira da semana da data informada. */
    fun startOfWeek(date: LocalDate): LocalDate {
        val daysSinceMonday = date.dayOfWeek.value - DayOfWeek.MONDAY.value
        return date.minusDays(daysSinceMonday.toLong())
    }

    /** Os 7 dias (segunda a domingo) da semana que começa em [monday]. */
    fun daysOfWeek(monday: LocalDate): List<LocalDate> {
        val days = mutableListOf<LocalDate>()
        for (i in 0..6) {
            days.add(monday.plusDays(i.toLong()))
        }
        return days
    }
}
