package io.pleo.antaeus.core.models

enum class PaymentStatus {
    Success, NetworkFailure, InsufficientBalance, CurrencyMismatch, CustomerNotFound
}