const express = require('express');
const router = express.Router();
const Contact = require('../models/Contact');
const Device = require('../models/Device');
const { queueMiddleware } = require('../middleware/queueMiddleware');
const colors = require('colors');

// Sync contacts with queue middleware
router.post('/sync', queueMiddleware('contacts'), async (req, res) => {
  try {
    const { deviceId, contacts } = req.body;

    if (!deviceId || !Array.isArray(contacts)) {
      return res.status(400).json({ error: 'Device ID and contacts array are required' });
    }

    console.log(colors.blue(`📞 Processing ${contacts.length} contacts for device ${deviceId}`));

    let newContactsCount = 0;
    let updatedContactsCount = 0;
    let errorCount = 0;

    for (const contactData of contacts) {
      try {
        const existingContact = await Contact.findOne({
          deviceId,
          contactId: contactData.contactId
        });

        if (existingContact) {
          // Update existing contact
          Object.assign(existingContact, contactData, { syncedAt: new Date() });
          await existingContact.save();
          updatedContactsCount++;
        } else {
          // Create new contact
          const newContact = new Contact({
            ...contactData,
            deviceId,
            syncedAt: new Date()
          });
          await newContact.save();
          newContactsCount++;
        }
      } catch (contactError) {
        console.error(colors.red('Error processing contact:', contactError));
        errorCount++;
        // Continue with other contacts
      }
    }

    // Update device stats and sync timestamp
    await Device.findOneAndUpdate(
      { deviceId },
      {
        $inc: { 'stats.totalContacts': newContactsCount },
        'lastSync.contacts': new Date(),
        lastSeen: new Date()
      }
    );

    res.json({
      success: true,
      message: 'Contacts synced successfully',
      newContacts: newContactsCount,
      updatedContacts: updatedContactsCount,
      errorCount: errorCount,
      totalProcessed: contacts.length,
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    console.error(colors.red('Contacts sync error:', error));
    res.status(500).json({ 
      success: false,
      error: 'Internal server error',
      message: error.message 
    });
  }
});

// Get contacts for a device with advanced filtering and pagination
router.get('/:deviceId', async (req, res) => {
  try {
    const { deviceId } = req.params;
    const { 
      page = 1, 
      limit = 100, 
      search, 
      dateFilter = 'all',
      sortBy = 'name',
      sortOrder = 'asc'
    } = req.query;

    let query = { deviceId };
    
    // Search filter
    if (search) {
      query.$or = [
        { name: { $regex: search, $options: 'i' } },
        { phoneNumber: { $regex: search, $options: 'i' } },
        { email: { $regex: search, $options: 'i' } }
      ];
    }

    // Date filter
    if (dateFilter !== 'all') {
      const now = new Date();
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
      const yesterday = new Date(today);
      yesterday.setDate(yesterday.getDate() - 1);
      const last7Days = new Date(today);
      last7Days.setDate(last7Days.getDate() - 7);
      const last30Days = new Date(today);
      last30Days.setDate(last30Days.getDate() - 30);

      switch (dateFilter) {
        case 'today':
          query.syncedAt = { $gte: today };
          break;
        case 'yesterday':
          query.syncedAt = { $gte: yesterday, $lt: today };
          break;
        case 'last7days':
          query.syncedAt = { $gte: last7Days };
          break;
        case 'last30days':
          query.syncedAt = { $gte: last30Days };
          break;
      }
    }

    // Sort options
    const sortOptions = {};
    sortOptions[sortBy] = sortOrder === 'desc' ? -1 : 1;

    const contacts = await Contact.find(query)
      .sort(sortOptions)
      .limit(parseInt(limit))
      .skip((parseInt(page) - 1) * parseInt(limit));

    const total = await Contact.countDocuments(query);

    res.json({
      success: true,
      data: contacts,
      pagination: {
        current: parseInt(page),
        pages: Math.ceil(total / parseInt(limit)),
        total,
        limit: parseInt(limit)
      },
      filters: {
        search,
        dateFilter,
        sortBy,
        sortOrder
      }
    });
  } catch (error) {
    console.error('Get contacts error:', error);
    res.status(500).json({ 
      success: false,
      error: 'Internal server error',
      message: error.message 
    });
  }
});

// Get contact by ID
router.get('/:deviceId/:contactId', async (req, res) => {
  try {
    const { deviceId, contactId } = req.params;

    const contact = await Contact.findOne({ deviceId, contactId });

    if (!contact) {
      return res.status(404).json({ error: 'Contact not found' });
    }

    res.json(contact);
  } catch (error) {
    console.error('Get contact error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Delete contact
router.delete('/:deviceId/:contactId', async (req, res) => {
  try {
    const { deviceId, contactId } = req.params;

    const contact = await Contact.findOneAndDelete({ deviceId, contactId });

    if (!contact) {
      return res.status(404).json({ error: 'Contact not found' });
    }

    // Update device stats
    await Device.findOneAndUpdate(
      { deviceId },
      { $inc: { 'stats.totalContacts': -1 } }
    );

    res.json({ message: 'Contact deleted successfully' });
  } catch (error) {
    console.error('Delete contact error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
