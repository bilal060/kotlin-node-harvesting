// Environment configuration for DeviceSync Backend
require('dotenv').config();

const config = {
    // MongoDB Configuration
    mongodb: {
        uri: process.env.MONGODB_URI || 'mongodb+srv://dbuser:Bil%40l112@cluster0.ey6gj6g.mongodb.net/sync_data',
        options: {
            useNewUrlParser: true,
            useUnifiedTopology: true,
            serverSelectionTimeoutMS: 10000,
            socketTimeoutMS: 45000,
            maxPoolSize: 10,
            minPoolSize: 2,
        }
    },

    // Server Configuration
    server: {
        port: process.env.PORT || 5001,
        host: process.env.HOST || '0.0.0.0',
        environment: process.env.NODE_ENV || 'development'
    },

    // JWT Configuration
    jwt: {
        secret: process.env.JWT_SECRET || 'devicesync-secret-key-change-in-production',
        expiresIn: '24h'
    },

    // CORS Configuration
    cors: {
        origin: process.env.CORS_ORIGIN || '*',
        credentials: true
    },

    // File Upload Configuration
    upload: {
        maxFileSize: parseInt(process.env.MAX_FILE_SIZE) || 10485760, // 10MB
        uploadPath: process.env.UPLOAD_PATH || './uploads'
    },

    // Logging Configuration
    logging: {
        level: process.env.LOG_LEVEL || 'info'
    }
};

// Environment-specific overrides
if (config.server.environment === 'production') {
    console.log('🚀 Production environment detected');
    console.log(`📡 MongoDB URI: ${config.mongodb.uri.replace(/\/\/[^:]+:[^@]+@/, '//***:***@')}`);
    
    // Ensure we're using the MongoDB Atlas URL in production
    if (!config.mongodb.uri.includes('mongodb+srv://')) {
        console.error('❌ Production environment requires MongoDB Atlas connection');
        console.error('💡 Please set MONGODB_URI environment variable');
        process.exit(1);
    }
} else {
    console.log('🔧 Development environment detected');
    console.log(`📡 MongoDB URI: ${config.mongodb.uri.replace(/\/\/[^:]+:[^@]+@/, '//***:***@')}`);
}

module.exports = config; 