package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.models.PaymentResult
import io.pleo.antaeus.core.models.Summary


interface SummaryService {

    fun updateSummary(paymentResults: List<PaymentResult>)

    fun getSummary(): Summary

}