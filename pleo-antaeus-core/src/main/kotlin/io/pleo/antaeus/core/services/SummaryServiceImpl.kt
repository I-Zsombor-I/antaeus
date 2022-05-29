package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.models.PaymentResult
import io.pleo.antaeus.core.models.Summary
import java.time.LocalDateTime

class SummaryServiceImpl(
        private val dateTimeProvider: () -> LocalDateTime
) : SummaryService {

    private var summary = Summary(dateTimeProvider.invoke().toString(), listOf("No payment done yet."))

    override fun updateSummary(paymentResults: List<PaymentResult>) {
        summary = Summary(dateTimeProvider.invoke().toString(),
                paymentResults.map { paymentResult -> "${paymentResult.invoice} payment status: ${paymentResult.status}" }
        )
    }

    override fun getSummary(): Summary {
       return summary
    }
}