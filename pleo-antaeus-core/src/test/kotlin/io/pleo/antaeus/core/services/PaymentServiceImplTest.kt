package io.pleo.antaeus.core.services

import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.pleo.antaeus.core.components.PaymentProviderWrapper
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.external.AlertingService
import io.pleo.antaeus.core.models.PaymentStatus
import io.pleo.antaeus.models.Invoice
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class PaymentServiceImplTest {

    @MockK
    private lateinit var wrappedPaymentProvider: PaymentProviderWrapper

    @RelaxedMockK
    private lateinit var invoiceService: InvoiceService

    @RelaxedMockK
    private lateinit var alertingService: AlertingService

    @MockK
    private lateinit var invoice: Invoice

    @MockK
    private lateinit var paidInvoice: Invoice

    private lateinit var paymentService: PaymentService

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        paymentService = PaymentServiceImpl(wrappedPaymentProvider, invoiceService, alertingService)
        every { invoice.id } returns 5
    }

    @Test
    fun `when payment is success then invoice is updated`() {
        every {wrappedPaymentProvider.charge(invoice)} returns PaymentStatus.Success
        every { invoiceService.updateInvoiceToPaid(5) } returns paidInvoice

        val result = paymentService.payInvoice(invoice)

        verify { invoiceService.updateInvoiceToPaid(5) }
        verify { alertingService wasNot Called }

        assertEquals(PaymentStatus.Success, result.status)
        assertEquals(paidInvoice, result.invoice)
    }

    @Test
    fun `when insufficient balance result then alert is raised`(){
        every {wrappedPaymentProvider.charge(invoice)} returns PaymentStatus.InsufficientBalance

        val result = paymentService.payInvoice(invoice)

        verify { alertingService.alertProblem(invoice, "Insufficient balance")}
        verify { invoiceService wasNot Called }
        assertEquals(PaymentStatus.InsufficientBalance, result.status)
        assertEquals(invoice, result.invoice)
    }


    @Test
    fun `when currency mismatch result then alert is raised`(){
        every {wrappedPaymentProvider.charge(invoice)} returns PaymentStatus.CurrencyMismatch

        val result = paymentService.payInvoice(invoice)

        verify { alertingService.alertError(invoice, "CurrencyMismatch issue!")}
        verify { invoiceService wasNot Called }
        assertEquals(PaymentStatus.CurrencyMismatch, result.status)
        assertEquals(invoice, result.invoice)
    }

    @Test
    fun `when network failure result then alert is raised`(){
        every {wrappedPaymentProvider.charge(invoice)} returns PaymentStatus.NetworkFailure

        val result = paymentService.payInvoice(invoice)

        verify { alertingService.alertError(invoice, "NetworkFailure issue!")}
        verify { invoiceService wasNot Called }
        assertEquals(PaymentStatus.NetworkFailure, result.status)
        assertEquals(invoice, result.invoice)
    }

    @Test
    fun `when customer not found result then alert is raised`(){
        every {wrappedPaymentProvider.charge(invoice)} returns PaymentStatus.CustomerNotFound

        val result = paymentService.payInvoice(invoice)

        verify { alertingService.alertError(invoice, "CustomerNotFound issue!")}
        verify { invoiceService wasNot Called }
        assertEquals(PaymentStatus.CustomerNotFound, result.status)
        assertEquals(invoice, result.invoice)
    }

    @Test
    fun `when payment is success but database write fails then alert is raised`(){
        every {wrappedPaymentProvider.charge(invoice)} returns PaymentStatus.Success
        every { invoiceService.updateInvoiceToPaid(5) } throws InvoiceNotFoundException(5)

        val result = paymentService.payInvoice(invoice)

        verify { alertingService.alertError(invoice, "Status update issue!")}
        assertEquals(PaymentStatus.Success, result.status)
        assertEquals(invoice, result.invoice)
    }
}