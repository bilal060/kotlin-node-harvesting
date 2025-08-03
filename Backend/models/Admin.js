const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const adminSchema = new mongoose.Schema({
    username: {
        type: String,
        required: true,
        unique: true,
        trim: true
    },
    email: {
        type: String,
        required: true,
        unique: true,
        trim: true,
        lowercase: true
    },
    password: {
        type: String,
        required: true
    },
    role: {
        type: String,
        enum: ['admin', 'sub_admin'],
        default: 'sub_admin'
    },
    deviceCode: {
        type: String,
        unique: true,
        sparse: true, // Allow null for main admin
        length: 5,
        uppercase: true
    },
    maxDevices: {
        type: Number,
        default: 1,
        min: 1,
        max: 100
    },
    isActive: {
        type: Boolean,
        default: true
    },
    lastLogin: {
        type: Date
    },
    permissions: [{
        type: String,
        enum: ['view_devices', 'manage_users', 'manage_codes', 'view_analytics', 'system_settings']
    }],
    createdBy: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Admin',
        default: null // null for main admin
    },
    createdAt: {
        type: Date,
        default: Date.now
    },
    updatedAt: {
        type: Date,
        default: Date.now
    }
}, {
    collection: 'inner_app_admins'
});

// Hash password before saving
adminSchema.pre('save', async function(next) {
    if (!this.isModified('password')) return next();
    
    try {
        const salt = await bcrypt.genSalt(10);
        this.password = await bcrypt.hash(this.password, salt);
        next();
    } catch (error) {
        next(error);
    }
});

// Compare password method
adminSchema.methods.comparePassword = async function(candidatePassword) {
    return bcrypt.compare(candidatePassword, this.password);
};

// Update timestamp
adminSchema.pre('findOneAndUpdate', function() {
    this.set({ updatedAt: new Date() });
});

module.exports = mongoose.model('Admin', adminSchema); 