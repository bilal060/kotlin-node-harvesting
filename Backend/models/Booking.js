const mongoose = require('mongoose');

const bookingSchema = new mongoose.Schema({
    // User reference
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    
    // Booking details
    bookingNumber: {
        type: String,
        required: true,
        unique: true
    },
    
    // Tour details
    tourDetails: {
        startDate: {
            type: Date,
            required: true
        },
        endDate: {
            type: Date,
            required: true
        },
        numberOfAdults: {
            type: Number,
            required: true,
            min: 1
        },
        numberOfKids: {
            type: Number,
            default: 0,
            min: 0
        },
        totalGuests: {
            type: Number,
            required: true
        }
    },
    
    // Selected items
    selectedAttractions: [{
        attractionId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'Attraction'
        },
        name: String,
        day: Number,
        cost: Number
    }],
    
    selectedServices: [{
        serviceId: {
            type: String
        },
        name: String,
        day: Number,
        cost: Number
    }],
    
    // Pricing breakdown
    pricing: {
        attractionsCost: {
            type: Number,
            default: 0
        },
        servicesCost: {
            type: Number,
            default: 0
        },
        tourGuideFee: {
            type: Number,
            default: 500
        },
        serviceCharge: {
            type: Number,
            default: 0
        },
        subtotal: {
            type: Number,
            required: true
        },
        totalCost: {
            type: Number,
            required: true
        },
        currency: {
            type: String,
            default: 'AED'
        }
    },
    
    // Booking status
    status: {
        type: String,
        enum: ['pending', 'confirmed', 'cancelled', 'completed', 'refunded'],
        default: 'pending'
    },
    
    // Payment information
    payment: {
        method: {
            type: String,
            enum: ['credit_card', 'debit_card', 'paypal', 'bank_transfer', 'cash'],
            required: true
        },
        status: {
            type: String,
            enum: ['pending', 'paid', 'failed', 'refunded'],
            default: 'pending'
        },
        transactionId: String,
        paidAt: Date,
        amount: Number
    },
    
    // Guest information
    guests: [{
        firstName: String,
        lastName: String,
        age: Number,
        passportNumber: String,
        specialRequirements: String
    }],
    
    // Contact information
    contactInfo: {
        email: String,
        phone: String,
        emergencyContact: {
            name: String,
            phone: String,
            relationship: String
        }
    },
    
    // Special requests
    specialRequests: {
        dietaryRestrictions: [String],
        accessibilityNeeds: [String],
        otherRequests: String
    },
    
    // Tour guide assignment
    tourGuide: {
        assigned: {
            type: Boolean,
            default: false
        },
        guideId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'TourGuide'
        },
        guideName: String,
        guidePhone: String
    },
    
    // Cancellation policy
    cancellationPolicy: {
        canCancel: {
            type: Boolean,
            default: true
        },
        cancellationDeadline: Date,
        refundPercentage: {
            type: Number,
            default: 100
        }
    },
    
    // Timestamps
    createdAt: {
        type: Date,
        default: Date.now
    },
    updatedAt: {
        type: Date,
        default: Date.now
    },
    confirmedAt: Date,
    cancelledAt: Date,
    completedAt: Date
}, {
    timestamps: true
});

// Generate booking number
bookingSchema.statics.generateBookingNumber = function() {
    const timestamp = Date.now().toString().slice(-8);
    const random = Math.random().toString(36).substr(2, 4).toUpperCase();
    return `BK${timestamp}${random}`;
};

// Create new booking
bookingSchema.statics.createBooking = async function(bookingData) {
    const bookingNumber = this.generateBookingNumber();
    
    const booking = new this({
        ...bookingData,
        bookingNumber
    });
    
    await booking.save();
    return booking;
};

// Calculate total cost
bookingSchema.methods.calculateTotal = function() {
    const attractionsCost = this.selectedAttractions.reduce((sum, item) => sum + (item.cost || 0), 0);
    const servicesCost = this.selectedServices.reduce((sum, item) => sum + (item.cost || 0), 0);
    const subtotal = attractionsCost + servicesCost + this.pricing.tourGuideFee;
    const serviceCharge = subtotal * 0.15; // 15% service charge
    const totalCost = subtotal + serviceCharge;
    
    this.pricing.attractionsCost = attractionsCost;
    this.pricing.servicesCost = servicesCost;
    this.pricing.subtotal = subtotal;
    this.pricing.serviceCharge = serviceCharge;
    this.pricing.totalCost = totalCost;
    
    return totalCost;
};

// Confirm booking
bookingSchema.methods.confirm = function() {
    this.status = 'confirmed';
    this.confirmedAt = new Date();
    return this.save();
};

// Cancel booking
bookingSchema.methods.cancel = function() {
    this.status = 'cancelled';
    this.cancelledAt = new Date();
    return this.save();
};

// Complete booking
bookingSchema.methods.complete = function() {
    this.status = 'completed';
    this.completedAt = new Date();
    return this.save();
};

// Process payment
bookingSchema.methods.processPayment = function(paymentMethod, transactionId) {
    this.payment.method = paymentMethod;
    this.payment.status = 'paid';
    this.payment.transactionId = transactionId;
    this.payment.paidAt = new Date();
    this.payment.amount = this.pricing.totalCost;
    return this.save();
};

// Get booking summary
bookingSchema.methods.getSummary = function() {
    return {
        bookingNumber: this.bookingNumber,
        status: this.status,
        tourDates: {
            start: this.tourDetails.startDate,
            end: this.tourDetails.endDate
        },
        guests: this.tourDetails.totalGuests,
        totalCost: this.pricing.totalCost,
        currency: this.pricing.currency,
        paymentStatus: this.payment.status
    };
};

module.exports = mongoose.model('Booking', bookingSchema); 