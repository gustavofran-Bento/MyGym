package com.gustavobento.mygym.util

import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Formatação no padrão brasileiro: 1.420 · 22,5 · Domingo, 27 de setembro. */
object Formatters {

    private val BRAZIL: Locale = Locale.forLanguageTag("pt-BR")

    /** Até 1 casa decimal e sem ",0" desnecessário: 30.0 → "30"; 22.5 → "22,5". */
    fun number(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        val format = NumberFormat.getNumberInstance(BRAZIL)
        format.maximumFractionDigits = 1
        format.minimumFractionDigits = 0
        return format.format(value)
    }

    /** Número inteiro com separador de milhar: 1420.4 → "1.420". */
    fun integer(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        val format = NumberFormat.getIntegerInstance(BRAZIL)
        return format.format(Math.round(value))
    }

    /** "Domingo, 27 de setembro" */
    fun fullDate(date: LocalDate): String {
        val text = date.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", BRAZIL))
        return capitalize(text)
    }

    /** "Hoje", "Ontem" ou "Sex, 25 set" */
    fun relativeDate(date: LocalDate, today: LocalDate = LocalDate.now()): String {
        if (date == today) return "Hoje"
        if (date == today.minusDays(1)) return "Ontem"
        if (date == today.plusDays(1)) return "Amanhã"
        val text = date.format(DateTimeFormatter.ofPattern("EEE, d MMM", BRAZIL)).replace(".", "")
        return capitalize(text)
    }

    /** Letra do dia da semana para o gráfico: S, T, Q, Q, S, S, D */
    fun dayLetter(day: DayOfWeek): String {
        return day.getDisplayName(TextStyle.NARROW, BRAZIL).uppercase(BRAZIL)
    }

    /** Abreviação do dia: Seg, Ter, Qua… */
    fun dayShort(day: DayOfWeek): String {
        val text = day.getDisplayName(TextStyle.SHORT, BRAZIL).replace(".", "")
        return capitalize(text)
    }

    /** Intervalo da semana: "21 – 27 set" ou "28 set – 4 out" */
    fun weekRange(start: LocalDate, end: LocalDate): String {
        val month = DateTimeFormatter.ofPattern("MMM", BRAZIL)
        val startMonth = start.format(month).replace(".", "")
        val endMonth = end.format(month).replace(".", "")
        return if (start.month == end.month) {
            "${start.dayOfMonth} – ${end.dayOfMonth} $endMonth"
        } else {
            "${start.dayOfMonth} $startMonth – ${end.dayOfMonth} $endMonth"
        }
    }

    private fun capitalize(text: String): String {
        return text.replaceFirstChar { it.titlecase(BRAZIL) }
    }
}
