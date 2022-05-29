package io.pleo.antaeus.core.components

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.models.PaymentStatus
import io.pleo.antaeus.models.Invoice
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class PaymentProviderWrapper(
        private val paymentProvider: PaymentProvider
) {

    fun charge(invoice: Invoice): PaymentStatus {
        return try {
            if (paymentProvider.charge(invoice))
                PaymentStatus.Success
            else PaymentStatus.InsufficientBalance
        } catch (notFound : CustomerNotFoundException) {
            logger.error ( "Customer not found for invoice=${invoice.id}", notFound)
            PaymentStatus.CustomerNotFound
        } catch (mismatch : CurrencyMismatchException) {
            logger.error ( "Currency does not match customer account for invoice=${invoice.id}", mismatch)
            PaymentStatus.CurrencyMismatch
        } catch (network : NetworkException) {
            logger.error ( "Communication error, could not charge invoice=${invoice.id}", network)
            PaymentStatus.NetworkFailure
        }
    }

}