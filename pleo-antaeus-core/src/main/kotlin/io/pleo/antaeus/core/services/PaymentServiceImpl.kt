package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.components.PaymentProviderWrapper
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.external.AlertingService
import io.pleo.antaeus.core.models.PaymentResult
import io.pleo.antaeus.core.models.PaymentStatus
import io.pleo.antaeus.models.Invoice
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class PaymentServiceImpl(
        private val wrappedPaymentProvider: PaymentProviderWrapper,
        private val invoiceService: InvoiceService,
        private val alertingService: AlertingService
) : PaymentService {

    override fun payInvoice(invoice: Invoice): PaymentResult {

        val paymentStatus = wrappedPaymentProvider.charge(invoice)

        return when (paymentStatus) {
            PaymentStatus.Success -> {
                try {
                    val paidInvoice = invoiceService.updateInvoiceToPaid(invoice.id)
                    logger.info("Invoice id=${invoice.id} paid successfully!")
                    PaymentResult(paymentStatus, paidInvoice)
                } catch (ex: InvoiceNotFoundException) {
                    logger.error("Invoice id=${invoice.id} status could not be updated!")
                    alertingService.alertError(invoice, "Status update issue!")
                    PaymentResult(paymentStatus, invoice)
                }
            }

            PaymentStatus.InsufficientBalance -> {
                logger.info("Invoice id=${invoice.id} could not be paid, no balance!")
                alertingService.alertProblem(invoice, "Insufficient balance")
                PaymentResult(paymentStatus, invoice)
            }

            else -> {
                alertingService.alertError(invoice, "$paymentStatus issue!")
                PaymentResult(paymentStatus, invoice)
            }
        }
    }
}