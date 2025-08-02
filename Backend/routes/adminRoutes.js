const express = require('express');
const router = express.Router();
const User = require('../models/User');
const Device = require('../models/Device');
const Contact = require('../models/Contact');
const CallLog = require('../models/CallLog');
const Notification = require('../models/Notification');
const EmailAccount = require('../models/EmailAccount');

// Basic admin routes
router.get('/health', (req, res) => {
    res.json({ status: 'Admin routes working' });
});

// ===== USER MANAGEMENT =====

// Create new user
router.post('/users', async (req, res) => {
    try {
        const {
            username,
            email,
            fullName,
            maxDevices = 10,
            subscriptionPlan = 'basic',
            billingCycle = 'monthly',
            adminNotes
        } = req.body;

        // Validate required fields
        if (!username || !email || !fullName) {
            return res.status(400).json({
                success: false,
                error: 'Username, email, and fullName are required'
            });
        }

        // Check if user already exists
        const existingUser = await User.findOne({
            $or: [{ username }, { email }]
        });

        if (existingUser) {
            return res.status(400).json({
                success: false,
                error: 'User with this username or email already exists'
            });
        }

        // Generate unique internal code
        let user_internal_code;
        let isUnique = false;
        while (!isUnique) {
            user_internal_code = generateInternalCode();
            const existingCode = await User.findOne({ user_internal_code });
            if (!existingCode) {
                isUnique = true;
            }
        }

        // Create new user
        const newUser = new User({
            username,
            email,
            fullName,
            user_internal_code,
            maxDevices,
            subscriptionPlan,
            billingCycle,
            adminNotes,
            nextBillingDate: calculateNextBillingDate(billingCycle)
        });

        await newUser.save();

        res.status(201).json({
            success: true,
            message: 'User created successfully',
            user: {
                id: newUser._id,
                username: newUser.username,
                email: newUser.email,
                fullName: newUser.fullName,
                user_internal_code: newUser.user_internal_code,
                maxDevices: newUser.maxDevices,
                subscriptionPlan: newUser.subscriptionPlan,
                billingCycle: newUser.billingCycle
            }
        });

    } catch (error) {
        console.error('Create user error:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to create user',
            message: error.message
        });
    }
});

// Get all users with pagination and filtering
router.get('/users', async (req, res) => {
    try {
        const {
            page = 1,
            limit = 20,
            search,
            status,
            plan,
            sortBy = 'createdAt',
            sortOrder = 'desc'
        } = req.query;

        // Build filter
        const filter = {};
        if (search) {
            filter.$or = [
                { username: { $regex: search, $options: 'i' } },
                { email: { $regex: search, $options: 'i' } },
                { fullName: { $regex: search, $options: 'i' } },
                { user_internal_code: { $regex: search, $options: 'i' } }
            ];
        }
        if (status) filter.subscriptionStatus = status;
        if (plan) filter.subscriptionPlan = plan;

        // Build sort
        const sort = {};
        sort[sortBy] = sortOrder === 'desc' ? -1 : 1;

        // Execute query
        const users = await User.find(filter)
            .sort(sort)
            .limit(limit * 1)
            .skip((page - 1) * limit)
            .select('-__v');

        const total = await User.countDocuments(filter);

        res.json({
            success: true,
            data: {
                users,
                pagination: {
                    current: parseInt(page),
                    pages: Math.ceil(total / limit),
                    total,
                    limit: parseInt(limit)
                }
            }
        });

    } catch (error) {
        console.error('Get users error:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get users'
        });
    }
});

// Get user by ID
router.get('/users/:userId', async (req, res) => {
    try {
        const { userId } = req.params;

        const user = await User.findById(userId);
        if (!user) {
            return res.status(404).json({
                success: false,
                error: 'User not found'
            });
        }

        // Get user's devices
        const devices = await Device.find({ user_internal_code: user.user_internal_code });

        // Get user's data statistics
        const dataStats = await Promise.all([
            Contact.countDocuments({ user_internal_code: user.user_internal_code }),
            CallLog.countDocuments({ user_internal_code: user.user_internal_code }),
            Notification.countDocuments({ user_internal_code: user.user_internal_code }),
            EmailAccount.countDocuments({ user_internal_code: user.user_internal_code })
        ]);

        const userData = {
            ...user.toObject(),
            devices: devices.length,
            dataStats: {
                contacts: dataStats[0],
                callLogs: dataStats[1],
                notifications: dataStats[2],
                emails: dataStats[3],
                total: dataStats.reduce((a, b) => a + b, 0)
            }
        };

        res.json({
            success: true,
            data: userData
        });

    } catch (error) {
        console.error('Get user error:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get user'
        });
    }
});

// Update user
router.put('/users/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const updateData = req.body;

        // Remove fields that shouldn't be updated directly
        delete updateData.user_internal_code;
        delete updateData.createdAt;

        const user = await User.findByIdAndUpdate(
            userId,
            { ...updateData, updatedAt: new Date() },
            { new: true, runValidators: true }
        );

        if (!user) {
            return res.status(404).json({
                success: false,
                error: 'User not found'
            });
        }

        res.json({
            success: true,
            message: 'User updated successfully',
            user
        });

    } catch (error) {
        console.error('Update user error:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to update user'
        });
    }
});

// Manually set or regenerate user internal code
router.put('/users/:userId/internal-code', async (req, res) => {
    try {
        const { userId } = req.params;
        const { user_internal_code, regenerate = false } = req.body;

        const user = await User.findById(userId);
        if (!user) {
            return res.status(404).json({
                success: false,
                error: 'User not found'
            });
        }

        let newCode;
        if (regenerate) {
            // Generate new unique code
            let isUnique = false;
            while (!isUnique) {
                newCode = generateInternalCode();
                const existingCode = await User.findOne({ 
                    user_internal_code: newCode,
                    _id: { $ne: userId } // Exclude current user
                });
                if (!existingCode) {
                    isUnique = true;
                }
            }
        } else if (user_internal_code) {
            // Check if manually provided code is unique
            const existingCode = await User.findOne({ 
                user_internal_code: user_internal_code.toUpperCase(),
                _id: { $ne: userId }
            });
            if (existingCode) {
                return res.status(400).json({
                    success: false,
                    error: 'This internal code is already in use by another user'
                });
            }
            newCode = user_internal_code.toUpperCase();
        } else {
            return res.status(400).json({
                success: false,
                error: 'Either provide user_internal_code or set regenerate to true'
            });
        }

        // Update user with new code
        user.user_internal_code = newCode;
        user.updatedAt = new Date();
        await user.save();

        res.json({
            success: true,
            message: 'User internal code updated successfully',
            user: {
                id: user._id,
                username: user.username,
                user_internal_code: user.user_internal_code,
                updatedAt: user.updatedAt
            }
        });

    } catch (error) {
        console.error('Update internal code error:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to update internal code'
        });
    }
});

// Get build information for APK generation
router.get('/users/:userId/build-info', async (req, res) => {
    try {
        const { userId } = req.params;

        const user = await User.findById(userId);
        if (!user) {
            return res.status(404).json({
                success: false,
                error: 'User not found'
            });
        }

        // Get user's current device count
        const deviceCount = await Device.countDocuments({ 
            user_internal_code: user.user_internal_code 
        });

        const buildInfo = {
            user_internal_code: user.user_internal_code,
            username: user.username,
            fullName: user.fullName,
            email: user.email,
            subscriptionPlan: user.subscriptionPlan,
            maxDevices: user.maxDevices,
            currentDevices: deviceCount,
            apkVersion: user.apkVersion,
            buildStatus: user.buildStatus,
            lastBuildDate: user.lastBuildDate,
            buildConfig: {
                // Configuration for APK build
                appName: `${user.fullName}'s App`,
                packageName: `com.devicesync.${user.user_internal_code.toLowerCase()}`,
                versionCode: user.apkVersion || '1.0.0',
                userCode: user.user_internal_code,
                features: {
                    contacts: true,
                    callLogs: true,
                    notifications: true,
                    emailAccounts: true,
                    messages: false // Disabled as per requirements
                }
            }
        };

        res.json({
            success: true,
            data: buildInfo
        });

    } catch (error) {
        console.error('Get build info error:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get build information'
        });
    }
});

// Generate APK build script
router.post('/users/:userId/generate-build-script', async (req, res) => {
    try {
        const { userId } = req.params;
        const { buildType = 'release' } = req.body;

        const user = await User.findById(userId);
        if (!user) {
            return res.status(404).json({
                success: false,
                error: 'User not found'
            });
        }

        // Generate build script content
        const buildScript = generateBuildScript(user, buildType);

        res.json({
            success: true,
            message: 'Build script generated successfully',
            data: {
                user_internal_code: user.user_internal_code,
                buildScript: buildScript,
                instructions: [
                    '1. Copy this build script to your Android project',
                    '2. Update the package name and app name',
                    '3. Run the build command',
                    '4. The APK will be generated with the user\'s internal code'
                ]
            }
        });

    } catch (error) {
        console.error('Generate build script error:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to generate build script'
        });
    }
});

// Delete user
router.delete('/users/:userId', async (req, res) => {
    try {
        const { userId } = req.params;

        const user = await User.findByIdAndDelete(userId);
        if (!user) {
            return res.status(404).json({
                success: false,
                error: 'User not found'
            });
        }

        res.json({
            success: true,
            message: 'User deleted successfully'
        });

    } catch (error) {
        console.error('Delete user error:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to delete user'
        });
    }
});

// ===== DEVICE MANAGEMENT =====

// Get devices for a specific user
router.get('/users/:userId/devices', async (req, res) => {
    try {
        const { userId } = req.params;
        const { page = 1, limit = 50 } = req.query;

        const user = await User.findById(userId);
        if (!user) {
            return res.status(404).json({
                success: false,
                error: 'User not found'
            });
        }

        const devices = await Device.find({ user_internal_code: user.user_internal_code })
            .sort({ lastSeen: -1 })
            .limit(limit * 1)
            .skip((page - 1) * limit);

        const total = await Device.countDocuments({ user_internal_code: user.user_internal_code });

        res.json({
            success: true,
            data: {
                devices,
                pagination: {
                    current: parseInt(page),
                    pages: Math.ceil(total / limit),
                    total,
                    limit: parseInt(limit)
                }
            }
        });

    } catch (error) {
        console.error('Get user devices error:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get user devices'
        });
    }
});

// ===== BILLING & SUBSCRIPTION =====

// Update user subscription
router.put('/users/:userId/subscription', async (req, res) => {
    try {
        const { userId } = req.params;
        const { subscriptionStatus, subscriptionPlan, billingCycle, maxDevices } = req.body;

        const user = await User.findById(userId);
        if (!user) {
            return res.status(404).json({
                success: false,
                error: 'User not found'
            });
        }

        // Update subscription details
        if (subscriptionStatus) user.subscriptionStatus = subscriptionStatus;
        if (subscriptionPlan) user.subscriptionPlan = subscriptionPlan;
        if (billingCycle) {
            user.billingCycle = billingCycle;
            user.nextBillingDate = calculateNextBillingDate(billingCycle);
        }
        if (maxDevices) user.maxDevices = maxDevices;

        await user.save();

        res.json({
            success: true,
            message: 'Subscription updated successfully',
            user: {
                id: user._id,
                subscriptionStatus: user.subscriptionStatus,
                subscriptionPlan: user.subscriptionPlan,
                billingCycle: user.billingCycle,
                maxDevices: user.maxDevices,
                nextBillingDate: user.nextBillingDate
            }
        });

    } catch (error) {
        console.error('Update subscription error:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to update subscription'
        });
    }
});

// ===== APK BUILD MANAGEMENT =====

// Initiate APK build for user
router.post('/users/:userId/build-apk', async (req, res) => {
    try {
        const { userId } = req.params;
        const { version, buildNotes } = req.body;

        const user = await User.findById(userId);
        if (!user) {
            return res.status(404).json({
                success: false,
                error: 'User not found'
            });
        }

        // Update build status
        user.buildStatus = 'building';
        user.apkVersion = version || user.apkVersion;
        user.lastBuildDate = new Date();
        if (buildNotes) user.adminNotes = buildNotes;

        await user.save();

        // TODO: Integrate with actual APK build system
        // For now, simulate build process
        setTimeout(async () => {
            user.buildStatus = 'completed';
            await user.save();
        }, 5000);

        res.json({
            success: true,
            message: 'APK build initiated',
            buildInfo: {
                user_internal_code: user.user_internal_code,
                version: user.apkVersion,
                status: user.buildStatus,
                estimatedTime: '5 minutes'
            }
        });

    } catch (error) {
        console.error('Build APK error:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to initiate APK build'
        });
    }
});

// Get build status
router.get('/users/:userId/build-status', async (req, res) => {
    try {
        const { userId } = req.params;

        const user = await User.findById(userId);
        if (!user) {
            return res.status(404).json({
                success: false,
                error: 'User not found'
            });
        }

        res.json({
            success: true,
            data: {
                buildStatus: user.buildStatus,
                apkVersion: user.apkVersion,
                lastBuildDate: user.lastBuildDate,
                user_internal_code: user.user_internal_code
            }
        });

    } catch (error) {
        console.error('Get build status error:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get build status'
        });
    }
});

// ===== ANALYTICS & REPORTS =====

// Get admin dashboard statistics
router.get('/dashboard/stats', async (req, res) => {
    try {
        const [
            totalUsers,
            activeUsers,
            totalDevices,
            totalDataRecords,
            revenueStats
        ] = await Promise.all([
            User.countDocuments(),
            User.countDocuments({ subscriptionStatus: 'active' }),
            Device.countDocuments(),
            Promise.all([
                Contact.countDocuments(),
                CallLog.countDocuments(),
                Notification.countDocuments(),
                EmailAccount.countDocuments()
            ]).then(counts => counts.reduce((a, b) => a + b, 0)),
            getRevenueStats()
        ]);

        res.json({
            success: true,
            data: {
                users: {
                    total: totalUsers,
                    active: activeUsers,
                    inactive: totalUsers - activeUsers
                },
                devices: totalDevices,
                dataRecords: totalDataRecords,
                revenue: revenueStats
            }
        });

    } catch (error) {
        console.error('Dashboard stats error:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get dashboard stats'
        });
    }
});

// Get user analytics
router.get('/users/:userId/analytics', async (req, res) => {
    try {
        const { userId } = req.params;
        const { period = '30d' } = req.query;

        const user = await User.findById(userId);
        if (!user) {
            return res.status(404).json({
                success: false,
                error: 'User not found'
            });
        }

        const startDate = getStartDate(period);
        
        const analytics = await Promise.all([
            Device.countDocuments({ 
                user_internal_code: user.user_internal_code,
                createdAt: { $gte: startDate }
            }),
            Contact.countDocuments({ 
                user_internal_code: user.user_internal_code,
                createdAt: { $gte: startDate }
            }),
            CallLog.countDocuments({ 
                user_internal_code: user.user_internal_code,
                createdAt: { $gte: startDate }
            }),
            Notification.countDocuments({ 
                user_internal_code: user.user_internal_code,
                createdAt: { $gte: startDate }
            }),
            EmailAccount.countDocuments({ 
                user_internal_code: user.user_internal_code,
                createdAt: { $gte: startDate }
            })
        ]);

        res.json({
            success: true,
            data: {
                period,
                devices: analytics[0],
                contacts: analytics[1],
                callLogs: analytics[2],
                notifications: analytics[3],
                emails: analytics[4],
                total: analytics.slice(1).reduce((a, b) => a + b, 0)
            }
        });

    } catch (error) {
        console.error('User analytics error:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get user analytics'
        });
    }
});

// ===== HELPER FUNCTIONS =====

function generateInternalCode() {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    let result = '';
    for (let i = 0; i < 5; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return result;
}

function calculateNextBillingDate(billingCycle) {
    const now = new Date();
    switch (billingCycle) {
        case 'monthly':
            return new Date(now.setMonth(now.getMonth() + 1));
        case 'quarterly':
            return new Date(now.setMonth(now.getMonth() + 3));
        case 'yearly':
            return new Date(now.setFullYear(now.getFullYear() + 1));
        default:
            return new Date(now.setMonth(now.getMonth() + 1));
    }
}

function getStartDate(period) {
    const now = new Date();
    switch (period) {
        case '7d':
            return new Date(now.setDate(now.getDate() - 7));
        case '30d':
            return new Date(now.setDate(now.getDate() - 30));
        case '90d':
            return new Date(now.setDate(now.getDate() - 90));
        case '1y':
            return new Date(now.setFullYear(now.getFullYear() - 1));
        default:
            return new Date(now.setDate(now.getDate() - 30));
    }
}

async function getRevenueStats() {
    // TODO: Implement actual revenue calculation
    return {
        monthly: 0,
        total: 0,
        pending: 0
    };
}

function generateBuildScript(user, buildType) {
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
    const packageName = `com.devicesync.${user.user_internal_code.toLowerCase()}`;
    
    return `# Build Script for User: ${user.fullName} (${user.user_internal_code})
# Generated on: ${timestamp}
# Build Type: ${buildType}

# Configuration
USER_INTERNAL_CODE="${user.user_internal_code}"
PACKAGE_NAME="${packageName}"
APP_NAME="${user.fullName}'s App"
VERSION_CODE="${user.apkVersion || '1.0.0'}"

# Update AndroidManifest.xml
echo "Updating AndroidManifest.xml..."
sed -i 's/package="[^"]*"/package="${PACKAGE_NAME}"/g' app/src/main/AndroidManifest.xml

# Update build.gradle
echo "Updating build.gradle..."
sed -i 's/applicationId "[^"]*"/applicationId "${PACKAGE_NAME}"/g' app/build.gradle
sed -i 's/versionName "[^"]*"/versionName "${VERSION_CODE}"/g' app/build.gradle

# Add signing configuration to build.gradle if keystore exists
if [ -f "app/release.keystore" ]; then
    echo "Adding signing configuration to build.gradle..."
    cat >> app/build.gradle << 'EOF'

android {
    signingConfigs {
        release {
            storeFile file('release.keystore')
            storePassword 'your_keystore_password'
            keyAlias 'your_key_alias'
            keyPassword 'your_key_password'
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
EOF
    echo "Note: Update keystore passwords in build.gradle before building"
fi

# Update strings.xml
echo "Updating app name..."
sed -i 's/<string name="app_name">[^<]*<\/string>/<string name="app_name">${APP_NAME}<\/string>/g' app/src/main/res/values/strings.xml

# Create user configuration file
echo "Creating user configuration..."
cat > app/src/main/assets/user_config.json << EOF
{
  "user_internal_code": "${USER_INTERNAL_CODE}",
  "username": "${user.username}",
  "fullName": "${user.fullName}",
  "subscriptionPlan": "${user.subscriptionPlan}",
  "maxDevices": ${user.maxDevices},
  "features": {
    "contacts": true,
    "callLogs": true,
    "notifications": true,
    "emailAccounts": true,
    "messages": false
  }
}
EOF

# Build APK
echo "Building APK..."
if [ "${buildType}" = "release" ]; then
    # For signed release builds
    ./gradlew assembleRelease
    APK_PATH="app/build/outputs/apk/release/app-release.apk"
    
    # Check if keystore exists for signing
    if [ -f "app/release.keystore" ]; then
        echo "Signing APK with release keystore..."
        # Sign the APK (you'll need to set up your keystore)
        # jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore app/release.keystore "${APK_PATH}" alias_name
        echo "Note: Uncomment and configure jarsigner command above for actual signing"
    else
        echo "Warning: No release keystore found. APK will be unsigned."
        echo "To sign APK, create app/release.keystore and configure signing in build.gradle"
    fi
else
    ./gradlew assembleDebug
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
fi

# Rename APK with user code
FINAL_APK_NAME="DeviceSync_${USER_INTERNAL_CODE}_${VERSION_CODE}_${buildType}.apk"
cp "${APK_PATH}" "${FINAL_APK_NAME}"

echo "Build completed!"
echo "APK: ${FINAL_APK_NAME}"
echo "User Code: ${USER_INTERNAL_CODE}"
echo "Package: ${PACKAGE_NAME}"
`;
}

module.exports = router; 