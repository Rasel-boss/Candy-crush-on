package com.example.game.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUtilsTest {

    @Test
    fun `formatTime formats 0 seconds as 00 00`() {
        assertEquals("00:00", TimeUtils.formatTime(0L))
    }

    @Test
    fun `formatTime formats 7 seconds as 00 07`() {
        assertEquals("00:07", TimeUtils.formatTime(7L))
    }

    @Test
    fun `formatTime formats 59 seconds as 00 59`() {
        assertEquals("00:59", TimeUtils.formatTime(59L))
    }

    @Test
    fun `formatTime formats 60 seconds as 01 00`() {
        assertEquals("01:00", TimeUtils.formatTime(60L))
    }

    @Test
    fun `formatTime formats 125 seconds as 02 05`() {
        assertEquals("02:05", TimeUtils.formatTime(125L))
    }

    @Test
    fun `formatTime formats 3599 seconds as 59 59`() {
        assertEquals("59:59", TimeUtils.formatTime(3599L))
    }

    @Test
    fun `formatTime formats 3600 seconds as 01 00 00`() {
        assertEquals("01:00:00", TimeUtils.formatTime(3600L))
    }

    @Test
    fun `formatTime formats large durations in hours minutes seconds`() {
        // 3930 seconds = 1 hour, 5 minutes, 30 seconds
        assertEquals("01:05:30", TimeUtils.formatTime(3930L))
        // 7384 seconds = 2 hours, 3 minutes, 4 seconds
        assertEquals("02:03:04", TimeUtils.formatTime(7384L))
    }

    @Test
    fun `formatTime safely clamps negative values to 00 00`() {
        assertEquals("00:00", TimeUtils.formatTime(-1L))
        assertEquals("00:00", TimeUtils.formatTime(-100L))
    }
}
