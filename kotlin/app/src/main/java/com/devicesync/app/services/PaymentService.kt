package com.devicesync.app.services

import android.content.Context
import com.devicesync.app.data.*
import kotlinx.coroutines.delay
import java.util.*

class PaymentService(private val context: Context) {
    
    private val notificationService = NotificationService(context)
    
    suspend fun processPayment(paymentRequest: PaymentRequest): Result<Payment> {
        return try {
            // Simulate payment processing delay
            delay(2000)
            
            // Simulate payment success/failure (90% success rate)
            val isSuccess = Math.random() > 0.1
            
            if (isSuccess) {
                val payment = Payment(
                    id = UUID.randomUUID().toString(),
                    amount = paymentRequest.amount,
                    currency = paymentRequest.currency,
                    status = PaymentStatus.COMPLETED,
                    method = paymentRequest.method,
                    bookingId = paymentRequest.bookingId,
                    timestamp = System.currentTimeMillis(),
                    description = paymentRequest.description,
                    transactionId = "TXN_${System.currentTimeMillis()}"
                )
                
                // Show success notification
                val notification = PushNotification(
                    id = UUID.randomUUID().toString(),
                    title = "Payment Successful",
                    message = "Your payment of ${paymentRequest.currency} ${paymentRequest.amount} has been processed successfully.",
                    type = NotificationType.PAYMENT_SUCCESS,
                    data = mapOf(
                        "payment_id" to payment.id,
                        "amount" to paymentRequest.amount,
                        "booking_id" to paymentRequest.bookingId
                    ),
                    timestamp = System.currentTimeMillis(),
                    priority = NotificationPriority.HIGH
                )
                
                notificationService.showNotification(notification)
                
                Result.success(payment)
            } else {
                val payment = Payment(
                    id = UUID.randomUUID().toString(),
                    amount = paymentRequest.amount,
                    currency = paymentRequest.currency,
                    status = PaymentStatus.FAILED,
                    method = paymentRequest.method,
                    bookingId = paymentRequest.bookingId,
                    timestamp = System.currentTimeMillis(),
                    description = paymentRequest.description,
                    errorMessage = "Payment failed. Please try again."
                )
                
                // Show failure notification
                val notification = PushNotification(
                    id = UUID.randomUUID().toString(),
                    title = "Payment Failed",
                    message = "Your payment could not be processed. Please try again or contact support.",
                    type = NotificationType.PAYMENT_FAILED,
                    data = mapOf(
                        "payment_id" to payment.id,
                        "amount" to paymentRequest.amount,
                        "booking_id" to paymentRequest.bookingId
                    ),
                    timestamp = System.currentTimeMillis(),
                    priority = NotificationPriority.HIGH
                )
                
                notificationService.showNotification(notification)
                
                Result.failure(Exception("Payment failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun refundPayment(paymentId: String, amount: Double): Result<Payment> {
        return try {
            // Simulate refund processing
            delay(1500)
            
            val refundPayment = Payment(
                id = UUID.randomUUID().toString(),
                amount = amount,
                currency = "USD",
                status = PaymentStatus.REFUNDED,
                method = PaymentMethod.CREDIT_CARD,
                bookingId = "REFUND_$paymentId",
                timestamp = System.currentTimeMillis(),
                description = "Refund for payment $paymentId",
                transactionId = "REF_${System.currentTimeMillis()}"
            )
            
            // Show refund notification
            val notification = PushNotification(
                id = UUID.randomUUID().toString(),
                title = "Refund Processed",
                message = "Your refund of $${amount} has been processed successfully.",
                type = NotificationType.PAYMENT_SUCCESS,
                data = mapOf(
                    "payment_id" to refundPayment.id,
                    "amount" to amount,
                    "original_payment_id" to paymentId
                ),
                timestamp = System.currentTimeMillis(),
                priority = NotificationPriority.NORMAL
            )
            
            notificationService.showNotification(notification)
            
            Result.success(refundPayment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getPaymentMethods(): List<PaymentMethod> {
        return listOf(
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.DEBIT_CARD,
            PaymentMethod.PAYPAL,
            PaymentMethod.APPLE_PAY,
            PaymentMethod.GOOGLE_PAY,
            PaymentMethod.BANK_TRANSFER,
            PaymentMethod.CASH
        )
    }
    
    fun validatePaymentRequest(paymentRequest: PaymentRequest): Boolean {
        return paymentRequest.amount > 0 &&
                paymentRequest.bookingId.isNotEmpty() &&
                paymentRequest.customerEmail.isNotEmpty() &&
                paymentRequest.customerName.isNotEmpty()
    }
    
    fun calculateTax(amount: Double, taxRate: Double = 0.05): Double {
        return amount * taxRate
    }
    
    fun calculateTotal(amount: Double, taxRate: Double = 0.05, serviceFee: Double = 0.02): Double {
        val tax = calculateTax(amount, taxRate)
        val fee = amount * serviceFee
        return amount + tax + fee
    }
} 