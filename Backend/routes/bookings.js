const express = require('express');
const router = express.Router();
const Booking = require('../models/Booking');
const auth = require('../middleware/auth');

// Create new booking
router.post('/', auth, async (req, res) => {
    try {
        console.log('🔄 Creating new booking for user:', req.user.id);
        
        const bookingData = {
            userId: req.user.id,
            ...req.body
        };
        
        // Calculate total cost
        const booking = await Booking.createBooking(bookingData);
        const totalCost = booking.calculateTotal();
        await booking.save();
        
        console.log('✅ Booking created successfully:', booking.bookingNumber);
        
        res.status(201).json({
            success: true,
            message: 'Booking created successfully',
            data: {
                booking: booking,
                summary: booking.getSummary()
            }
        });
        
    } catch (error) {
        console.error('❌ Booking creation error:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to create booking',
            error: error.message
        });
    }
});

// Get user's bookings
router.get('/', auth, async (req, res) => {
    try {
        console.log('🔍 Fetching bookings for user:', req.user.id);
        
        const { status, page = 1, limit = 10 } = req.query;
        
        const query = { userId: req.user.id };
        if (status) {
            query.status = status;
        }
        
        const bookings = await Booking.find(query)
            .sort({ createdAt: -1 })
            .limit(limit * 1)
            .skip((page - 1) * limit)
            .populate('selectedAttractions.attractionId');
        
        const total = await Booking.countDocuments(query);
        
        console.log('✅ Retrieved bookings successfully');
        
        res.json({
            success: true,
            data: {
                bookings: bookings,
                pagination: {
                    currentPage: page,
                    totalPages: Math.ceil(total / limit),
                    totalBookings: total
                }
            }
        });
        
    } catch (error) {
        console.error('❌ Error fetching bookings:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to fetch bookings',
            error: error.message
        });
    }
});

// Get specific booking
router.get('/:bookingId', auth, async (req, res) => {
    try {
        console.log('🔍 Fetching booking:', req.params.bookingId);
        
        const booking = await Booking.findOne({
            _id: req.params.bookingId,
            userId: req.user.id
        }).populate('selectedAttractions.attractionId');
        
        if (!booking) {
            return res.status(404).json({
                success: false,
                message: 'Booking not found'
            });
        }
        
        console.log('✅ Booking retrieved successfully');
        
        res.json({
            success: true,
            data: booking
        });
        
    } catch (error) {
        console.error('❌ Error fetching booking:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to fetch booking',
            error: error.message
        });
    }
});

// Update booking
router.put('/:bookingId', auth, async (req, res) => {
    try {
        console.log('🔄 Updating booking:', req.params.bookingId);
        
        const booking = await Booking.findOne({
            _id: req.params.bookingId,
            userId: req.user.id
        });
        
        if (!booking) {
            return res.status(404).json({
                success: false,
                message: 'Booking not found'
            });
        }
        
        // Only allow updates for pending bookings
        if (booking.status !== 'pending') {
            return res.status(400).json({
                success: false,
                message: 'Cannot update confirmed or completed bookings'
            });
        }
        
        // Update booking data
        Object.keys(req.body).forEach(key => {
            if (key !== 'userId' && key !== '_id' && key !== 'bookingNumber') {
                booking[key] = req.body[key];
            }
        });
        
        // Recalculate total cost
        booking.calculateTotal();
        await booking.save();
        
        console.log('✅ Booking updated successfully');
        
        res.json({
            success: true,
            message: 'Booking updated successfully',
            data: booking
        });
        
    } catch (error) {
        console.error('❌ Error updating booking:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to update booking',
            error: error.message
        });
    }
});

// Cancel booking
router.post('/:bookingId/cancel', auth, async (req, res) => {
    try {
        console.log('🔄 Cancelling booking:', req.params.bookingId);
        
        const booking = await Booking.findOne({
            _id: req.params.bookingId,
            userId: req.user.id
        });
        
        if (!booking) {
            return res.status(404).json({
                success: false,
                message: 'Booking not found'
            });
        }
        
        if (booking.status === 'cancelled') {
            return res.status(400).json({
                success: false,
                message: 'Booking is already cancelled'
            });
        }
        
        if (booking.status === 'completed') {
            return res.status(400).json({
                success: false,
                message: 'Cannot cancel completed booking'
            });
        }
        
        await booking.cancel();
        
        console.log('✅ Booking cancelled successfully');
        
        res.json({
            success: true,
            message: 'Booking cancelled successfully',
            data: booking
        });
        
    } catch (error) {
        console.error('❌ Error cancelling booking:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to cancel booking',
            error: error.message
        });
    }
});

// Process payment
router.post('/:bookingId/payment', auth, async (req, res) => {
    try {
        console.log('🔄 Processing payment for booking:', req.params.bookingId);
        
        const { paymentMethod, transactionId } = req.body;
        
        const booking = await Booking.findOne({
            _id: req.params.bookingId,
            userId: req.user.id
        });
        
        if (!booking) {
            return res.status(404).json({
                success: false,
                message: 'Booking not found'
            });
        }
        
        if (booking.payment.status === 'paid') {
            return res.status(400).json({
                success: false,
                message: 'Payment already processed'
            });
        }
        
        // Process payment
        await booking.processPayment(paymentMethod, transactionId);
        
        // Confirm booking
        await booking.confirm();
        
        console.log('✅ Payment processed successfully');
        
        res.json({
            success: true,
            message: 'Payment processed successfully',
            data: {
                booking: booking,
                summary: booking.getSummary()
            }
        });
        
    } catch (error) {
        console.error('❌ Error processing payment:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to process payment',
            error: error.message
        });
    }
});

// Get booking statistics
router.get('/stats/summary', auth, async (req, res) => {
    try {
        console.log('📊 Getting booking statistics for user:', req.user.id);
        
        const stats = await Booking.aggregate([
            { $match: { userId: req.user._id } },
            {
                $group: {
                    _id: null,
                    totalBookings: { $sum: 1 },
                    totalSpent: { $sum: '$pricing.totalCost' },
                    pendingBookings: {
                        $sum: { $cond: [{ $eq: ['$status', 'pending'] }, 1, 0] }
                    },
                    confirmedBookings: {
                        $sum: { $cond: [{ $eq: ['$status', 'confirmed'] }, 1, 0] }
                    },
                    completedBookings: {
                        $sum: { $cond: [{ $eq: ['$status', 'completed'] }, 1, 0] }
                    },
                    cancelledBookings: {
                        $sum: { $cond: [{ $eq: ['$status', 'cancelled'] }, 1, 0] }
                    }
                }
            }
        ]);
        
        const summary = stats[0] || {
            totalBookings: 0,
            totalSpent: 0,
            pendingBookings: 0,
            confirmedBookings: 0,
            completedBookings: 0,
            cancelledBookings: 0
        };
        
        console.log('✅ Booking statistics retrieved successfully');
        
        res.json({
            success: true,
            data: summary
        });
        
    } catch (error) {
        console.error('❌ Error fetching booking statistics:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to fetch booking statistics',
            error: error.message
        });
    }
});

module.exports = router; 