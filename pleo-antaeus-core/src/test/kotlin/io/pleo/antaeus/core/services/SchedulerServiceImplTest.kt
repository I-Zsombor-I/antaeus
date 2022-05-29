package io.pleo.antaeus.core.services

import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.BeforeTest


class SchedulerServiceImplTest{

    @MockK
    private lateinit var coroutineScope: CoroutineScope

    @RelaxedMockK
    private lateinit var billingService: BillingService

    @MockK
    private lateinit var scheduleNext : () -> Boolean

    private lateinit var schedulerService: SchedulerService

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        schedulerService = SchedulerServiceImpl(
                coroutineScope,
                billingService,
                { LocalDateTime.of(2022, 5, 29, 0, 0) },
                scheduleNext)
    }

    @Test
    fun `when first billing date is in the past exception is thrown`() {
        assertThrows<IllegalArgumentException> {
            schedulerService.scheduleMonthlyBillingFrom(LocalDateTime.of(2022, 5, 28, 0, 0))
        }

        verify { billingService wasNot Called }
    }

    @Test
    fun `when delay is done billing is called`() = runBlockingTest {
        every { scheduleNext.invoke() } returnsMany listOf(true, false)

        val testCoroutineScope = TestCoroutineScope()
        schedulerService = SchedulerServiceImpl(
                testCoroutineScope,
                billingService,
                { LocalDateTime.of(2022, 5, 29, 0, 0) },
                scheduleNext)

        schedulerService.scheduleMonthlyBillingFrom(LocalDateTime.of(2022, 5, 30, 0, 0))

        testCoroutineScope.advanceTimeBy(1440*60*1000 + 1)
        verify { billingService.payPendingInvoices() }

        testCoroutineScope.advanceUntilIdle()
        verify { billingService.payPendingInvoices() }
    }
}