package io.pleo.antaeus.core.components

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.models.PaymentStatus
import io.pleo.antaeus.models.Invoice
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

internal class PaymentProviderWrapperTest {

    @MockK
    private lateinit var paymentProvider: PaymentProvider

    @MockK
    private lateinit var invoice: Invoice

    private lateinit var wrapper: PaymentProviderWrapper

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        wrapper = PaymentProviderWrapper(paymentProvider = paymentProvider)
    }

    @Test
    fun wrapperCallsProvider() {
        every { paymentProvider.charge(invoice)} returns true

        wrapper.charge(invoice)

        verify { paymentProvider.charge(invoice) }
    }

    @Test
    fun whenPaymentProviderReturnsTrueThenWrapperReturnsSuccess() {
        every { paymentProvider.charge(invoice)} returns true

        assertEquals(PaymentStatus.Success, wrapper.charge(invoice))

    }

    @Test
    fun whenPaymentProviderReturnsFalseThenWrapperReturnsInsufficientBalance() {
        every { paymentProvider.charge(invoice)} returns false

        assertEquals(PaymentStatus.InsufficientBalance, wrapper.charge(invoice))

    }

    @Test
    fun whenPaymentProviderThrowsCustomerNotFoundExceptionThenWrapperReturnsCorrectStatus() {
        every { paymentProvider.charge(invoice)} throws CustomerNotFoundException(1)
        every { invoice.id} returns 1

        assertEquals(PaymentStatus.CustomerNotFound, wrapper.charge(invoice))

    }

    @Test
    fun whenPaymentProviderThrowsCurrencyMismatchExceptionThenWrapperReturnsCorrectStatus() {
        every { paymentProvider.charge(invoice)} throws CurrencyMismatchException(1, 1)
        every { invoice.id} returns 1

        assertEquals(PaymentStatus.CurrencyMismatch, wrapper.charge(invoice))

    }

    @Test
    fun whenPaymentProviderThrowsCurrencyNetworkExceptionThenWrapperReturnsCorrectStatus() {
        every { paymentProvider.charge(invoice)} throws NetworkException()
        every { invoice.id} returns 1

        assertEquals(PaymentStatus.NetworkFailure, wrapper.charge(invoice))

    }
}