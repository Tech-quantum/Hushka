package com.techquantum.hushka

import org.junit.Assert.assertEquals
import org.junit.Test

class FabPositionTest {

    private val eps = 0.001f

    @Test
    fun clamp_pulls_back_from_left_and_top_edges() {
        val (x, y) = clampFabPosition(x = -40f, y = -25f, maxX = 900f, minY = 60f, maxY = 1500f)
        assertEquals(0f, x, eps)
        assertEquals(60f, y, eps)
    }

    @Test
    fun clamp_pulls_back_from_right_and_bottom_edges() {
        val (x, y) = clampFabPosition(x = 2000f, y = 2400f, maxX = 900f, minY = 60f, maxY = 1500f)
        assertEquals(900f, x, eps)
        assertEquals(1500f, y, eps)
    }

    @Test
    fun clamp_keeps_valid_position_untouched() {
        val (x, y) = clampFabPosition(x = 300f, y = 800f, maxX = 900f, minY = 60f, maxY = 1500f)
        assertEquals(300f, x, eps)
        assertEquals(800f, y, eps)
    }

    @Test
    fun clamp_survives_container_smaller_than_fab() {
        val (x, y) = clampFabPosition(x = 50f, y = 50f, maxX = 0f, minY = 0f, maxY = 0f)
        assertEquals(0f, x, eps)
        assertEquals(0f, y, eps)
    }

    @Test
    fun default_position_is_bottom_end_without_saved_fraction() {
        val (x, y) = initialFabPosition(fraction = null, maxX = 900f, minY = 60f, maxY = 1500f)
        assertEquals(900f, x, eps)
        assertEquals(1500f, y, eps)
    }

    @Test
    fun saved_top_start_fraction_restores_top_start() {
        val (x, y) = initialFabPosition(fraction = 0f to 0f, maxX = 900f, minY = 60f, maxY = 1500f)
        assertEquals(0f, x, eps)
        assertEquals(60f, y, eps)
    }

    @Test
    fun fraction_round_trip_keeps_position_after_rotation() {
        // موقعیت را روی صفحه‌ی عمودی ذخیره کن، روی افقی بازخوانی کن
        val fx = fabFraction(450f, 900f)
        val fy = fabFraction(780f - 60f, 1500f - 60f)

        val (x, y) = initialFabPosition(fx to fy, maxX = 1800f, minY = 60f, maxY = 800f)

        assertEquals(900f, x, eps)
        assertEquals(60f + 0.5f * (800f - 60f), y, eps)
    }

    @Test
    fun fraction_of_zero_range_is_safe() {
        assertEquals(0f, fabFraction(120f, 0f), eps)
        assertEquals(0f, fabFraction(120f, -5f), eps)
    }

    @Test
    fun fraction_is_clamped_to_unit_range() {
        assertEquals(1f, fabFraction(2000f, 900f), eps)
        assertEquals(0f, fabFraction(-30f, 900f), eps)
    }
}
