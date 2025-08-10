const mongoose = require('mongoose');

const tripDaySchema = new mongoose.Schema({
  dayIndex: { type: Number, required: true },
  services: [{ id: String, option: String }],
  attractions: [{ id: String }]
}, { _id: false });

const tripSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: false },
  user_internal_code: { type: String, index: true },
  title: { type: String, default: 'Custom Trip' },
  startDate: { type: Date, required: true },
  endDate: { type: Date, required: true },
  adults: { type: Number, default: 1 },
  kids: { type: Number, default: 0 },
  days: [tripDaySchema],
  totalCost: { type: Number, default: 0 },
  createdAt: { type: Date, default: Date.now }
}, { timestamps: true });

tripSchema.index({ user_internal_code: 1, createdAt: -1 });

module.exports = mongoose.model('Trip', tripSchema);