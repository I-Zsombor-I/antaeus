/*
    Defines the main() entry point of the app.
    Configures the database and sets up the REST web service.
 */

@file:JvmName("AntaeusApp")

package io.pleo.antaeus.app

import getAlertingService
import getPaymentProvider
import io.pleo.antaeus.core.components.PaymentProviderWrapper
import io.pleo.antaeus.core.services.*
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.data.CustomerTable
import io.pleo.antaeus.data.InvoiceTable
import io.pleo.antaeus.rest.AntaeusRest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import setupInitialData
import java.io.File
import java.sql.Connection
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

fun main() {
    // The tables to create in the database.
    val tables = arrayOf(InvoiceTable, CustomerTable)

    val dbFile: File = File.createTempFile("antaeus-db", ".sqlite")
    // Connect to the database and create the needed tables. Drop any existing data.
    val db = Database
        .connect(url = "jdbc:sqlite:${dbFile.absolutePath}",
            driver = "org.sqlite.JDBC",
            user = "root",
            password = "")
        .also {
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
            transaction(it) {
                addLogger(StdOutSqlLogger)
                // Drop all existing tables to ensure a clean slate on each run
                SchemaUtils.drop(*tables)
                // Create all tables
                SchemaUtils.create(*tables)
            }
        }

    // Set up data access layer.
    val dal = AntaeusDal(db = db)

    // Insert example data in the database.
    setupInitialData(dal = dal)

    // Get third parties
    val paymentProvider = getPaymentProvider()
    val wrappedPaymentProvider = PaymentProviderWrapper(paymentProvider = paymentProvider)
    val alertingService = getAlertingService()

    // Create core services
    val invoiceService = InvoiceService(dal = dal)
    val customerService = CustomerService(dal = dal)
    val paymentService = PaymentServiceImpl(
            wrappedPaymentProvider = wrappedPaymentProvider,
            invoiceService = invoiceService,
            alertingService = alertingService)
    val summaryService = SummaryServiceImpl (
            dateTimeProvider = { LocalDateTime.now() })

    // This is _your_ billing service to be included where you see fit
    val billingService = BillingService(
            invoiceService = invoiceService,
            paymentService = paymentService,
            summaryService = summaryService)

    val schedulerService = SchedulerServiceImpl(
            CoroutineScope(Dispatchers.Default),
            billingService = billingService,
            dateTimeProvider = { LocalDateTime.now() },
            scheduleNext = { true }
    )

    //Start scheduling the billing
    val firstOfNextMonth = LocalDateTime.now()
            .with(LocalTime.MIDNIGHT)
            .withDayOfMonth(1)
            .plusMonths(1)
    schedulerService.scheduleMonthlyBillingFrom(firstOfNextMonth)

    // Create REST web service
    AntaeusRest(
        invoiceService = invoiceService,
        customerService = customerService,
        billingService = billingService,
        schedulerService = schedulerService,
        summaryService = summaryService
    ).run()
}
