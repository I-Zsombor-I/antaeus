package io.pleo.antaeus.core.services

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.pleo.antaeus.core.models.PaymentResult
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import kotlin.test.Test
import kotlin.test.BeforeTest

internal class BillingServiceTest {

    @MockK
    private lateinit var  invoiceService: InvoiceService

    @MockK
    private lateinit var  paymentService: PaymentService

    @RelaxedMockK
    private lateinit var  summaryService: SummaryService

    @MockK
    private lateinit var invoice1:Invoice

    @MockK
    private lateinit var invoice2:Invoice

    @MockK
    private lateinit var invoice3:Invoice

    @MockK
    private lateinit var result:PaymentResult

    private lateinit var billingService: BillingService

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        billingService = BillingService(invoiceService, paymentService, summaryService)
    }

    @Test
    fun `paymentService is called for every invoice`() {
        every { invoiceService.fetchAllWithStatus(InvoiceStatus.PENDING)} returns listOf(invoice1, invoice2, invoice3)
        every { paymentService.payInvoice(any())} returns result

        billingService.payPendingInvoices()

        verify {
            invoiceService.fetchAllWithStatus(InvoiceStatus.PENDING)
            paymentService.payInvoice(invoice1)
            paymentService.payInvoice(invoice2)
            paymentService.payInvoice(invoice3)
            summaryService.updateSummary(any())
        }
    }

    @Test
    fun `paymentResults are passed to summaryService`() {
        every { invoiceService.fetchAllWithStatus(InvoiceStatus.PENDING)} returns listOf(invoice1, invoice2, invoice3)
        every { paymentService.payInvoice(any())} returns result

        billingService.payPendingInvoices()

        verify {
            summaryService.updateSummary(listOf(result, result, result))
        }
    }
}