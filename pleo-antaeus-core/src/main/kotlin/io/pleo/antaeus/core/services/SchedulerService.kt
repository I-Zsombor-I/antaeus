package io.pleo.antaeus.core.services

import java.time.LocalDateTime

interface SchedulerService {

    fun scheduleMonthlyBillingFrom(firstBillingDate : LocalDateTime)

    fun removeScheduledBilling()

}