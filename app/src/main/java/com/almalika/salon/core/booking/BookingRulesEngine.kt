package com.almalika.salon.core.booking

import com.almalika.salon.data.remote.config.Barber
import com.almalika.salon.data.remote.config.FeatureFlags
import com.almalika.salon.data.remote.config.HomeServiceBookingConfig
import com.almalika.salon.data.remote.config.TimeSlot

object BookingRulesEngine {

    data class PriceCalculation(
        val subtotal: Int,
        val serviceCharge: Int,
        val isServiceChargeWaived: Boolean,
        val total: Int
    )

    /**
     * Step 1 & 5: Computes pricing based on remote config rules:
     * - Adds serviceCharge unless subtotal >= freeServiceChargeAbove (when freeServiceChargeAbove > 0)
     */
    fun calculatePrice(subtotal: Int, bookingConfig: HomeServiceBookingConfig): PriceCalculation {
        val freeThreshold = bookingConfig.freeServiceChargeAbove
        val waiveServiceCharge = freeThreshold > 0 && subtotal >= freeThreshold
        val appliedCharge = if (waiveServiceCharge) 0 else bookingConfig.serviceCharge
        return PriceCalculation(
            subtotal = subtotal,
            serviceCharge = appliedCharge,
            isServiceChargeWaived = waiveServiceCharge,
            total = subtotal + appliedCharge
        )
    }

    /**
     * Checks if minimum booking amount requirement is met
     */
    fun isMinimumBookingMet(subtotal: Int, bookingConfig: HomeServiceBookingConfig): Boolean {
        return subtotal >= bookingConfig.minimumBookingAmount
    }

    /**
     * Step 2: Barber Selection Gating:
     * Only show barber picker if featureFlags.barberSelection == true AND barbers.size > 1.
     */
    fun shouldShowBarberPicker(flags: FeatureFlags, barbers: List<Barber>): Boolean {
        return flags.barberSelection && barbers.size > 1
    }

    /**
     * Step 4: Filters available time slots
     */
    fun getAvailableTimeSlots(slots: List<TimeSlot>): List<TimeSlot> {
        return slots.filter { it.isAvailable }
    }
}

