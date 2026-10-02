package com.gustavobento.mygym.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class FormattersAndDatesTest {

    @Test
    fun `numeros no padrao brasileiro`() {
        assertEquals("22,5", Formatters.number(22.5))
        assertEquals("30", Formatters.number(30.0))
        assertEquals("1.420", Formatters.integer(1420.4))
        assertEquals("0", Formatters.number(Double.NaN))
    }

    @Test
    fun `datas relativas`() {
        val hoje = LocalDate.of(2026, 9, 27)
        assertEquals("Hoje", Formatters.relativeDate(hoje, hoje))
        assertEquals("Ontem", Formatters.relativeDate(hoje.minusDays(1), hoje))
        assertEquals("Domingo, 27 de setembro", Formatters.fullDate(hoje))
    }

    @Test
    fun `intervalo da semana`() {
        assertEquals("21 – 27 set", Formatters.weekRange(LocalDate.of(2026, 9, 21), LocalDate.of(2026, 9, 27)))
        assertEquals("28 set – 4 out", Formatters.weekRange(LocalDate.of(2026, 9, 28), LocalDate.of(2026, 10, 4)))
    }

    @Test
    fun `semana comeca na segunda-feira`() {
        val domingo = LocalDate.of(2026, 9, 27)
        val segunda = DateUtils.startOfWeek(domingo)
        assertEquals(LocalDate.of(2026, 9, 21), segunda)
        assertEquals(DayOfWeek.MONDAY, segunda.dayOfWeek)
        assertEquals(segunda, DateUtils.startOfWeek(segunda))
        assertEquals(7, DateUtils.daysOfWeek(segunda).size)
    }
}
