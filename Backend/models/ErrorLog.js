const mongoose = require('mongoose');

const errorLogSchema = new mongoose.Schema({
  app: {
    type: String,
    required: true,
    enum: ['backend', 'mobile', 'web'],
    default: 'backend'
  },
  reason: {
    type: String,
    required: true,
    maxlength: 1000
  },
  trace: {
    type: String,
    required: true,
    maxlength: 5000
  },
  level: {
    type: String,
    enum: ['error', 'warning', 'info', 'critical'],
    default: 'error'
  },
  source: {
    type: String,
    required: false,
    maxlength: 200
  },
  userId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: false
  },
  deviceId: {
    type: String,
    required: false,
    maxlength: 100
  },
  ipAddress: {
    type: String,
    required: false,
    maxlength: 45
  },
  userAgent: {
    type: String,
    required: false,
    maxlength: 500
  },
  endpoint: {
    type: String,
    required: false,
    maxlength: 200
  },
  method: {
    type: String,
    required: false,
    enum: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS', 'HEAD']
  },
  statusCode: {
    type: Number,
    required: false,
    min: 100,
    max: 599
  },
  requestBody: {
    type: mongoose.Schema.Types.Mixed,
    required: false
  },
  responseBody: {
    type: mongoose.Schema.Types.Mixed,
    required: false
  },
  metadata: {
    type: mongoose.Schema.Types.Mixed,
    required: false
  }
}, {
  timestamps: true,
  collection: 'error_logs'
});

// Indexes for better query performance
errorLogSchema.index({ app: 1, createdAt: -1 });
errorLogSchema.index({ level: 1, createdAt: -1 });
errorLogSchema.index({ userId: 1, createdAt: -1 });
errorLogSchema.index({ deviceId: 1, createdAt: -1 });
errorLogSchema.index({ createdAt: -1 });

// Pre-save middleware to truncate long fields if needed
errorLogSchema.pre('save', function(next) {
  if (this.reason && this.reason.length > 1000) {
    this.reason = this.reason.substring(0, 1000);
  }
  if (this.trace && this.trace.length > 5000) {
    this.trace = this.trace.substring(0, 5000);
  }
  next();
});

// Static method to log errors
errorLogSchema.statics.logError = async function(errorData) {
  try {
    const errorLog = new this(errorData);
    return await errorLog.save();
  } catch (error) {
    console.error('Failed to log error:', error);
    // Fallback to console if database logging fails
    console.error('Original error:', errorData);
    return null;
  }
};

// Static method to get recent errors
errorLogSchema.statics.getRecentErrors = async function(limit = 100, app = null) {
  const query = app ? { app } : {};
  return await this.find(query)
    .sort({ createdAt: -1 })
    .limit(limit)
    .select('-__v');
};

// Static method to get errors by level
errorLogSchema.statics.getErrorsByLevel = async function(level, limit = 100) {
  return await this.find({ level })
    .sort({ createdAt: -1 })
    .limit(limit)
    .select('-__v');
};

// Static method to get errors for a specific user
errorLogSchema.statics.getUserErrors = async function(userId, limit = 100) {
  return await this.find({ userId })
    .sort({ createdAt: -1 })
    .limit(limit)
    .select('-__v');
};

// Static method to get errors for a specific device
errorLogSchema.statics.getDeviceErrors = async function(deviceId, limit = 100) {
  return await this.find({ deviceId })
    .sort({ createdAt: -1 })
    .limit(limit)
    .select('-__v');
};

// Static method to clean old error logs (older than specified days)
errorLogSchema.statics.cleanOldLogs = async function(daysOld = 30) {
  const cutoffDate = new Date();
  cutoffDate.setDate(cutoffDate.getDate() - daysOld);
  
  const result = await this.deleteMany({ createdAt: { $lt: cutoffDate } });
  return result;
};

const ErrorLog = mongoose.model('ErrorLog', errorLogSchema);

module.exports = ErrorLog; 