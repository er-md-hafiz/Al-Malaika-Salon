package com.almalika.salon.core.booking

import com.almalika.salon.data.remote.config.Barber
import com.almalika.salon.data.remote.config.FeatureFlags
import com.almalika.salon.data.remote.config.HomeServiceBookingConfig
import com.almalika.salon.data.remote.config.TimeSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingRulesEngineTest {

    @Test
    fun testPriceCalculationWithServiceCharge() {
        val config = HomeServiceBookingConfig(
            serviceCharge = 99,
            freeServiceChargeAbove = 500
        )

        // Subtotal below threshold -> service charge applied
        val below = BookingRulesEngine.calculatePrice(300, config)
        assertEquals(300, below.subtotal)
        assertEquals(99, below.serviceCharge)
        assertFalse(below.isServiceChargeWaived)
        assertEquals(399, below.total)

        // Subtotal above threshold -> service charge waived
        val above = BookingRulesEngine.calculatePrice(600, config)
        assertEquals(600, above.subtotal)
        assertEquals(0, above.serviceCharge)
        assertTrue(above.isServiceChargeWaived)
        assertEquals(600, above.total)
    }

    @Test
    fun testFreeServiceChargeAlwaysFreeWhenThresholdIsOne() {
        val config = HomeServiceBookingConfig(
            serviceCharge = 50,
            freeServiceChargeAbove = 1
        )
        val calc = BookingRulesEngine.calculatePrice(100, config)
        assertEquals(0, calc.serviceCharge)
        assertTrue(calc.isServiceChargeWaived)
        assertEquals(100, calc.total)
    }

    @Test
    fun testBarberPickerGating() {
        // Flag false -> picker hidden even if multiple barbers
        val flagsDisabled = FeatureFlags(barberSelection = false)
        val multipleBarbers = listOf(
            Barber(id = 1, name = "Barber 1"),
            Barber(id = 2, name = "Barber 2")
        )
        assertFalse(BookingRulesEngine.shouldShowBarberPicker(flagsDisabled, multipleBarbers))

        // Flag true, but only 1 barber -> picker hidden
        val flagsEnabled = FeatureFlags(barberSelection = true)
        val singleBarber = listOf(Barber(id = 1, name = "Any Available"))
        assertFalse(BookingRulesEngine.shouldShowBarberPicker(flagsEnabled, singleBarber))

        // Flag true, multiple barbers -> picker shown
        assertTrue(BookingRulesEngine.shouldShowBarberPicker(flagsEnabled, multipleBarbers))
    }

    @Test
    fun testTimeSlotsFilteredByAvailability() {
        val slots = listOf(
            TimeSlot(id = 1, time = "10:00 AM", isAvailable = true),
            TimeSlot(id = 2, time = "11:00 AM", isAvailable = false),
            TimeSlot(id = 3, time = "12:00 PM", isAvailable = true)
        )
        val available = BookingRulesEngine.getAvailableTimeSlots(slots)
        assertEquals(2, available.size)
        assertEquals(listOf(1, 3), available.map { it.id })
    }
}

