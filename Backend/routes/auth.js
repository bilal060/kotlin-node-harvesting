const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const User = require('../models/User');
const Auth = require('../models/Auth');
const UserSettings = require('../models/UserSettings');
const UserProfile = require('../models/UserProfile');

// Register new user
router.post('/register', async (req, res) => {
    try {
        console.log('🔄 User registration request:', req.body.email);
        
        const { username, email, password, fullName, deviceInfo = {} } = req.body;
        
        // Validation
        if (!username || !email || !password || !fullName) {
            return res.status(400).json({
                success: false,
                message: 'All fields are required'
            });
        }
        
        // Check if user already exists
        const existingUser = await User.findOne({
            $or: [{ email }, { username }]
        });
        
        if (existingUser) {
            return res.status(400).json({
                success: false,
                message: 'User with this email or username already exists'
            });
        }
        
        // Create new user
        const user = new User({
            username,
            email,
            password,
            fullName,
            user_internal_code: User.generateInternalCode()
        });
        
        await user.save();
        
        // Create default settings and profile
        await UserSettings.createDefaultSettings(user._id);
        await UserProfile.createDefaultProfile(user._id, {
            firstName: fullName.split(' ')[0],
            lastName: fullName.split(' ').slice(1).join(' '),
            email: email
        });
        
        // Create session
        const session = await Auth.createSession(user._id, {
            ...deviceInfo,
            ipAddress: req.ip,
            userAgent: req.get('User-Agent')
        });
        
        console.log('✅ User registered successfully:', user.email);
        
        res.status(201).json({
            success: true,
            message: 'User registered successfully',
            data: {
                user: {
                    id: user._id,
                    username: user.username,
                    email: user.email,
                    fullName: user.fullName
                },
                tokens: {
                    accessToken: session.accessToken,
                    refreshToken: session.refreshToken,
                    expiresIn: 3600
                }
            }
        });
        
    } catch (error) {
        console.error('❌ Registration error:', error);
        res.status(500).json({
            success: false,
            message: 'Registration failed',
            error: error.message
        });
    }
});

// Login user
router.post('/login', async (req, res) => {
    try {
        console.log('🔄 User login request:', req.body.email);
        
        const { email, password, deviceInfo = {} } = req.body;
        
        // Validation
        if (!email || !password) {
            return res.status(400).json({
                success: false,
                message: 'Email and password are required'
            });
        }
        
        // Find user
        const user = await User.findOne({ email });
        if (!user) {
            return res.status(401).json({
                success: false,
                message: 'Invalid credentials'
            });
        }
        
        // Check password
        const isPasswordValid = await user.comparePassword(password);
        if (!isPasswordValid) {
            return res.status(401).json({
                success: false,
                message: 'Invalid credentials'
            });
        }
        
        // Create session
        const session = await Auth.createSession(user._id, {
            ...deviceInfo,
            ipAddress: req.ip,
            userAgent: req.get('User-Agent')
        });
        
        console.log('✅ User logged in successfully:', user.email);
        
        res.json({
            success: true,
            message: 'Login successful',
            data: {
                user: {
                    id: user._id,
                    username: user.username,
                    email: user.email,
                    fullName: user.fullName
                },
                tokens: {
                    accessToken: session.accessToken,
                    refreshToken: session.refreshToken,
                    expiresIn: 3600
                }
            }
        });
        
    } catch (error) {
        console.error('❌ Login error:', error);
        res.status(500).json({
            success: false,
            message: 'Login failed',
            error: error.message
        });
    }
});

// Logout user
router.post('/logout', async (req, res) => {
    try {
        console.log('🔄 User logout request');
        
        const authHeader = req.headers.authorization;
        if (!authHeader || !authHeader.startsWith('Bearer ')) {
            return res.status(401).json({
                success: false,
                message: 'Access token required'
            });
        }
        
        const accessToken = authHeader.substring(7);
        
        // Verify token
        const decoded = jwt.verify(accessToken, process.env.JWT_SECRET || 'your-secret-key');
        
        // Find and logout session
        const session = await Auth.findOne({
            sessionId: decoded.sessionId,
            accessToken: accessToken,
            isActive: true
        });
        
        if (session) {
            await session.logout();
            console.log('✅ User logged out successfully');
        }
        
        res.json({
            success: true,
            message: 'Logout successful'
        });
        
    } catch (error) {
        console.error('❌ Logout error:', error);
        res.status(500).json({
            success: false,
            message: 'Logout failed',
            error: error.message
        });
    }
});

// Get current user
router.get('/me', async (req, res) => {
    try {
        console.log('🔄 Get current user request');
        
        const authHeader = req.headers.authorization;
        if (!authHeader || !authHeader.startsWith('Bearer ')) {
            return res.status(401).json({
                success: false,
                message: 'Access token required'
            });
        }
        
        const accessToken = authHeader.substring(7);
        
        // Verify token
        const decoded = jwt.verify(accessToken, process.env.JWT_SECRET || 'your-secret-key');
        
        // Find session
        const session = await Auth.findOne({
            sessionId: decoded.sessionId,
            accessToken: accessToken,
            isActive: true
        });
        
        if (!session) {
            return res.status(401).json({
                success: false,
                message: 'Invalid session'
            });
        }
        
        // Get user data
        const user = await User.findById(session.userId).select('-password');
        if (!user) {
            return res.status(404).json({
                success: false,
                message: 'User not found'
            });
        }
        
        console.log('✅ Current user retrieved successfully');
        
        res.json({
            success: true,
            data: {
                user: {
                    id: user._id,
                    username: user.username,
                    email: user.email,
                    fullName: user.fullName
                }
            }
        });
        
    } catch (error) {
        console.error('❌ Get current user error:', error);
        res.status(401).json({
            success: false,
            message: 'Authentication failed',
            error: error.message
        });
    }
});

module.exports = router; 