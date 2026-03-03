// com.example.pulseguard.data.local.ConvertersTest
package com.example.pulseguard.data.local

import com.example.pulseguard.domain.model.MeasurementArm
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [Converters].
 *
 * Verifies that the TypeConverter correctly serialises and deserialises
 * all [MeasurementArm] enum values (round-trip integrity).
 */
class ConvertersTest {

    private val converters = Converters()

    @Test
    fun fromMeasurementArm_leftArm_returnsLeftString() {
        assertEquals("LEFT", converters.fromMeasurementArm(MeasurementArm.LEFT))
    }

    @Test
    fun fromMeasurementArm_rightArm_returnsRightString() {
        assertEquals("RIGHT", converters.fromMeasurementArm(MeasurementArm.RIGHT))
    }

    @Test
    fun toMeasurementArm_leftString_returnsLeftEnum() {
        assertEquals(MeasurementArm.LEFT, converters.toMeasurementArm("LEFT"))
    }

    @Test
    fun toMeasurementArm_rightString_returnsRightEnum() {
        assertEquals(MeasurementArm.RIGHT, converters.toMeasurementArm("RIGHT"))
    }

    @Test
    fun measurementArm_roundTrip_left() {
        val arm = MeasurementArm.LEFT
        assertEquals(arm, converters.toMeasurementArm(converters.fromMeasurementArm(arm)))
    }

    @Test
    fun measurementArm_roundTrip_right() {
        val arm = MeasurementArm.RIGHT
        assertEquals(arm, converters.toMeasurementArm(converters.fromMeasurementArm(arm)))
    }
}
