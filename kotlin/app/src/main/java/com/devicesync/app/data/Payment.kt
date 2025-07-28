package com.devicesync.app.data

data class Payment(
    val id: String,
    val amount: Double,
    val currency: String = "USD",
    val status: PaymentStatus,
    val method: PaymentMethod,
    val bookingId: String,
    val timestamp: Long,
    val description: String,
    val transactionId: String? = null,
    val errorMessage: String? = null
)

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUNDED
}

enum class PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    PAYPAL,
    APPLE_PAY,
    GOOGLE_PAY,
    BANK_TRANSFER,
    CASH
}

data class PaymentRequest(
    val amount: Double,
    val currency: String = "USD",
    val method: PaymentMethod,
    val bookingId: String,
    val description: String,
    val customerEmail: String,
    val customerName: String
) 