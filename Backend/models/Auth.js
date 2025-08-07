const mongoose = require('mongoose');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');

const authSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    
    // Session management
    sessionId: {
        type: String,
        required: true,
        unique: true
    },
    
    // Token management
    accessToken: {
        type: String,
        required: true
    },
    
    refreshToken: {
        type: String,
        required: true
    },
    
    // Device information
    deviceInfo: {
        deviceId: String,
        deviceName: String,
        platform: String,
        appVersion: String,
        ipAddress: String,
        userAgent: String
    },
    
    // Session status
    isActive: {
        type: Boolean,
        default: true
    },
    
    // Expiration times
    accessTokenExpiresAt: {
        type: Date,
        required: true
    },
    
    refreshTokenExpiresAt: {
        type: Date,
        required: true
    },
    
    // Last activity
    lastActivity: {
        type: Date,
        default: Date.now
    },
    
    // Login history
    loginHistory: [{
        timestamp: {
            type: Date,
            default: Date.now
        },
        action: {
            type: String,
            enum: ['login', 'logout', 'refresh', 'expired'],
            required: true
        },
        ipAddress: String,
        userAgent: String
    }],
    
    // Timestamps
    createdAt: {
        type: Date,
        default: Date.now
    },
    updatedAt: {
        type: Date,
        default: Date.now
    }
}, {
    timestamps: true
});

// Generate session ID
authSchema.statics.generateSessionId = function() {
    return 'sess_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
};

// Generate tokens
authSchema.statics.generateTokens = function(userId, deviceInfo = {}) {
    const sessionId = this.generateSessionId();
    const accessToken = jwt.sign(
        { userId, sessionId, type: 'access' },
        process.env.JWT_SECRET || 'your-secret-key',
        { expiresIn: '1h' }
    );
    
    const refreshToken = jwt.sign(
        { userId, sessionId, type: 'refresh' },
        process.env.JWT_REFRESH_SECRET || 'your-refresh-secret',
        { expiresIn: '7d' }
    );
    
    const now = new Date();
    const accessTokenExpiresAt = new Date(now.getTime() + (60 * 60 * 1000)); // 1 hour
    const refreshTokenExpiresAt = new Date(now.getTime() + (7 * 24 * 60 * 60 * 1000)); // 7 days
    
    return {
        sessionId,
        accessToken,
        refreshToken,
        accessTokenExpiresAt,
        refreshTokenExpiresAt
    };
};

// Create new session
authSchema.statics.createSession = async function(userId, deviceInfo = {}) {
    const tokens = this.generateTokens(userId, deviceInfo);
    
    const session = new this({
        userId,
        sessionId: tokens.sessionId,
        accessToken: tokens.accessToken,
        refreshToken: tokens.refreshToken,
        deviceInfo,
        accessTokenExpiresAt: tokens.accessTokenExpiresAt,
        refreshTokenExpiresAt: tokens.refreshTokenExpiresAt
    });
    
    // Add login entry
    session.loginHistory.push({
        action: 'login',
        ipAddress: deviceInfo.ipAddress,
        userAgent: deviceInfo.userAgent
    });
    
    await session.save();
    return session;
};

// Refresh access token
authSchema.methods.refreshAccessToken = function() {
    const newAccessToken = jwt.sign(
        { userId: this.userId, sessionId: this.sessionId, type: 'access' },
        process.env.JWT_SECRET || 'your-secret-key',
        { expiresIn: '1h' }
    );
    
    this.accessToken = newAccessToken;
    this.accessTokenExpiresAt = new Date(Date.now() + (60 * 60 * 1000));
    this.lastActivity = new Date();
    
    this.loginHistory.push({
        action: 'refresh',
        ipAddress: this.deviceInfo.ipAddress,
        userAgent: this.deviceInfo.userAgent
    });
    
    return this.save();
};

// Logout session
authSchema.methods.logout = function() {
    this.isActive = false;
    this.lastActivity = new Date();
    
    this.loginHistory.push({
        action: 'logout',
        ipAddress: this.deviceInfo.ipAddress,
        userAgent: this.deviceInfo.userAgent
    });
    
    return this.save();
};

// Check if token is expired
authSchema.methods.isAccessTokenExpired = function() {
    return new Date() > this.accessTokenExpiresAt;
};

authSchema.methods.isRefreshTokenExpired = function() {
    return new Date() > this.refreshTokenExpiresAt;
};

// Update last activity
authSchema.methods.updateActivity = function() {
    this.lastActivity = new Date();
    return this.save();
};

// Clean up expired sessions
authSchema.statics.cleanupExpiredSessions = async function() {
    const now = new Date();
    const result = await this.updateMany(
        { refreshTokenExpiresAt: { $lt: now } },
        { isActive: false }
    );
    
    console.log(`🧹 Cleaned up ${result.modifiedCount} expired sessions`);
    return result;
};

module.exports = mongoose.model('Auth', authSchema); 