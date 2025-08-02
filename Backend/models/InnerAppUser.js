const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const innerAppUserSchema = new mongoose.Schema({
  email: {
    type: String,
    required: true,
    unique: true,
    lowercase: true,
    trim: true,
    match: [/^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/, 'Please enter a valid email']
  },
  password: {
    type: String,
    required: true,
    minlength: 6
  },
  firstName: {
    type: String,
    required: true,
    trim: true,
    maxlength: 50
  },
  lastName: {
    type: String,
    required: true,
    trim: true,
    maxlength: 50
  },
  phoneNumber: {
    type: String,
    trim: true,
    match: [/^\+?[\d\s-()]+$/, 'Please enter a valid phone number']
  },
  dateOfBirth: {
    type: Date
  },
  nationality: {
    type: String,
    trim: true
  },
  profilePicture: {
    type: String,
    default: null
  },
  preferences: {
    language: {
      type: String,
      enum: ['en', 'ar', 'zh', 'mn', 'kk'],
      default: 'en'
    },
    theme: {
      type: String,
      enum: ['light', 'dark', 'system'],
      default: 'system'
    },
    notifications: {
      type: Boolean,
      default: true
    },
    emailUpdates: {
      type: Boolean,
      default: true
    }
  },
  favorites: {
    attractions: [{
      type: mongoose.Schema.Types.ObjectId,
      ref: 'InnerAppAttraction'
    }],
    services: [{
      type: mongoose.Schema.Types.ObjectId,
      ref: 'InnerAppService'
    }],
    packages: [{
      type: mongoose.Schema.Types.ObjectId,
      ref: 'InnerAppTourPackage'
    }]
  },
  itineraries: [{
    name: String,
    startDate: Date,
    endDate: Date,
    attractions: [{
      attractionId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'InnerAppAttraction'
      },
      visitDate: Date,
      timeSlot: String,
      tickets: Number
    }],
    services: [{
      serviceId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'InnerAppService'
      },
      serviceDate: Date,
      quantity: Number
    }],
    accommodation: {
      hotel: String,
      checkIn: Date,
      checkOut: Date,
      roomType: String
    },
    meals: {
      breakfast: Boolean,
      lunch: Boolean,
      dinner: Boolean
    },
    transport: {
      type: String,
      details: String
    },
    totalCost: Number,
    status: {
      type: String,
      enum: ['draft', 'confirmed', 'completed', 'cancelled'],
      default: 'draft'
    }
  }],
  isEmailVerified: {
    type: Boolean,
    default: false
  },
  emailVerificationToken: String,
  emailVerificationExpires: Date,
  passwordResetToken: String,
  passwordResetExpires: Date,
  lastLogin: Date,
  loginAttempts: {
    type: Number,
    default: 0
  },
  lockUntil: Date,
  isActive: {
    type: Boolean,
    default: true
  },
  role: {
    type: String,
    enum: ['user', 'admin'],
    default: 'user'
  }
}, {
  timestamps: true
});

// Index for better query performance
innerAppUserSchema.index({ email: 1 });
innerAppUserSchema.index({ 'preferences.language': 1 });
innerAppUserSchema.index({ 'preferences.theme': 1 });

// Virtual for full name
innerAppUserSchema.virtual('fullName').get(function() {
  return `${this.firstName} ${this.lastName}`;
});

// Hash password before saving
innerAppUserSchema.pre('save', async function(next) {
  if (!this.isModified('password')) return next();
  
  try {
    const salt = await bcrypt.genSalt(12);
    this.password = await bcrypt.hash(this.password, salt);
    next();
  } catch (error) {
    next(error);
  }
});

// Method to compare password
innerAppUserSchema.methods.comparePassword = async function(candidatePassword) {
  return bcrypt.compare(candidatePassword, this.password);
};

// Method to check if account is locked
innerAppUserSchema.methods.isLocked = function() {
  return !!(this.lockUntil && this.lockUntil > Date.now());
};

// Method to increment login attempts
innerAppUserSchema.methods.incLoginAttempts = function() {
  if (this.lockUntil && this.lockUntil < Date.now()) {
    return this.updateOne({
      $unset: { lockUntil: 1 },
      $set: { loginAttempts: 1 }
    });
  }
  
  const updates = { $inc: { loginAttempts: 1 } };
  if (this.loginAttempts + 1 >= 5 && !this.isLocked()) {
    updates.$set = { lockUntil: Date.now() + 2 * 60 * 60 * 1000 }; // 2 hours
  }
  return this.updateOne(updates);
};

// Method to reset login attempts
innerAppUserSchema.methods.resetLoginAttempts = function() {
  return this.updateOne({
    $unset: { loginAttempts: 1, lockUntil: 1 }
  });
};

// Method to get public profile (without sensitive data)
innerAppUserSchema.methods.getPublicProfile = function() {
  const userObject = this.toObject();
  delete userObject.password;
  delete userObject.emailVerificationToken;
  delete userObject.emailVerificationExpires;
  delete userObject.passwordResetToken;
  delete userObject.passwordResetExpires;
  delete userObject.loginAttempts;
  delete userObject.lockUntil;
  return userObject;
};

module.exports = mongoose.model('InnerAppUser', innerAppUserSchema); 