package io.pleo.antaeus.core.services

import io.mockk.MockKAnnotations
import io.pleo.antaeus.core.models.PaymentResult
import io.pleo.antaeus.core.models.PaymentStatus
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class SummaryServiceImplTest{

    private lateinit var summaryService: SummaryService

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)

        summaryService = SummaryServiceImpl { LocalDateTime.of(2022, 5, 29, 0, 0) }
    }

    @Test
    fun `not yet updated service returns summary with message`() {
        val summary = summaryService.getSummary()

        assertEquals("2022-05-29T00:00", summary.date)
        assertEquals("No payment done yet.", summary.results[0])
    }

    @Test
    fun `updated service returns results`() {
        summaryService.updateSummary(listOf(
                PaymentResult(
                        PaymentStatus.Success,
                        Invoice(1, 2, Money(BigDecimal.valueOf(3), Currency.EUR), InvoiceStatus.PAID))))

        val summary = summaryService.getSummary()

        assertEquals("2022-05-29T00:00", summary.date)
        assertEquals("Invoice(id=1, customerId=2, amount=Money(value=3, currency=EUR), status=PAID) payment status: Success", summary.results[0])
    }
}