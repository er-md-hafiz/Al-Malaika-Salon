package com.almalika.salon.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SalonUseCasesTest {

    private val formatWhatsAppMessageUseCase = FormatWhatsAppMessageUseCase()

    @Test
    fun `test formatWhatsAppMessage replaces all template variables accurately`() {
        val template = "Hello {salonName}! Name: {name}, Phone: {phone}, Service: {service}, Date: {date}, Time: {time}, Branch: {branch}"
        val salonName = "Al-Malaika Life Style Salon"
        val values = mapOf(
            "name" to "Hafiz",
            "phone" to "09708101242",
            "service" to "Signature Haircut & Styling",
            "date" to "2026-09-20",
            "time" to "11:00 AM",
            "branch" to "Pugmil Main Studio"
        )

        val output = formatWhatsAppMessageUseCase(
            template = template,
            salonName = salonName,
            values = values
        )

        assertTrue(output.contains("Al-Malaika Life Style Salon"))
        assertTrue(output.contains("Hafiz"))
        assertTrue(output.contains("Signature Haircut & Styling"))
        assertTrue(output.contains("Pugmil Main Studio"))
    }

    @Test
    fun `test offer isExpired returns false for future validTill date`() {
        val offer = com.almalika.salon.domain.model.Offer(
            id = "test_01",
            title = "Special Offer",
            subtitle = "Discount",
            description = "Details",
            imageUrl = "",
            discount = "20% OFF",
            discountType = com.almalika.salon.domain.model.DiscountType.PERCENTAGE,
            validFrom = "2026-01-01",
            validTill = "2099-12-31",
            terms = "",
            ctaText = "Book",
            ctaAction = "booking",
            enabled = true
        )

        assertEquals(false, offer.isExpired)
    }

    @Test
    fun `test offer isExpired returns true for past validTill date`() {
        val offer = com.almalika.salon.domain.model.Offer(
            id = "test_02",
            title = "Expired Offer",
            subtitle = "Discount",
            description = "Details",
            imageUrl = "",
            discount = "50% OFF",
            discountType = com.almalika.salon.domain.model.DiscountType.PERCENTAGE,
            validFrom = "2020-01-01",
            validTill = "2021-01-01",
            terms = "",
            ctaText = "Book",
            ctaAction = "booking",
            enabled = true
        )

        assertEquals(true, offer.isExpired)
    }
}
