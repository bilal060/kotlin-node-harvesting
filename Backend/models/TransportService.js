const mongoose = require('mongoose');

const transportServiceSchema = new mongoose.Schema(
  {
    seats: { type: Number, required: true },
    price_in_dubai: { type: Number, required: true },
    price_in_other_city: { type: Number, required: true },
    hours: { type: Number },
    isActive: { type: Boolean, default: true },
  },
  {
    timestamps: true,
    collection: 'transport_services',
  }
);

// Ensure uniqueness by seats + hours combo to prevent duplicates in defaults
transportServiceSchema.index({ seats: 1, hours: 1 }, { unique: true, sparse: true });

module.exports = mongoose.model('TransportService', transportServiceSchema);

