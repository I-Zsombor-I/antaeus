package io.pleo.antaeus.core.external

import io.pleo.antaeus.models.Invoice

interface AlertingService {

    /*
       I imagine if there is some issue with the payment, the code needs to raise some flags.
       This interface is a representation of an external service we would call,
       which probably would be more sophisticated than this one.
    */

    fun alertProblem(invoice: Invoice, message: String)

    fun alertError(invoice: Invoice, message: String)

}