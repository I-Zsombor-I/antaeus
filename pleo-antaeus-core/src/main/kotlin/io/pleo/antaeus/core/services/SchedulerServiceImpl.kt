package io.pleo.antaeus.core.services

import kotlinx.coroutines.CoroutineScope
import java.time.LocalDateTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.time.Duration

private val logger = KotlinLogging.logger {}

class SchedulerServiceImpl(
        private val coroutineScope: CoroutineScope,
        private val billingService: BillingService,
        private val dateTimeProvider: () -> LocalDateTime,
        private val scheduleNext: () -> Boolean) : SchedulerService {

    private var billingJob: Job = Job()

    override fun scheduleMonthlyBillingFrom(firstBillingDate: LocalDateTime) {
        val now = dateTimeProvider.invoke()
        val duration = Duration.between(now, firstBillingDate)
        if (duration.isNegative) throw IllegalArgumentException("Billing date already past!")

        logger.info { "Scheduling the billing to $firstBillingDate in ${duration.toMinutes()} minutes or ${duration.toDays()} days" }
        billingJob = coroutineScope.launch {
            delay(duration.toMillis())
            billingService.payPendingInvoices()

            var date = firstBillingDate
            while (scheduleNext.invoke()) {
                val nextDateTime = date.plusMonths(1)
                logger.info { "Scheduling the next billing to $nextDateTime" }
                delay(Duration.between(date, nextDateTime).toMillis())
                billingService.payPendingInvoices()
                date = nextDateTime
            }
        }

    }

    override fun removeScheduledBilling() {
        logger.info { "Cancelling scheduled billing." }
        billingJob.cancel()
    }
}