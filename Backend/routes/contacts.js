const express = require('express');
const router = express.Router();
const Contact = require('../models/Contact');
const Device = require('../models/Device');

// Sync contacts
router.post('/sync', async (req, res) => {
  try {
    const { deviceId, contacts } = req.body;

    if (!deviceId || !Array.isArray(contacts)) {
      return res.status(400).json({ error: 'Device ID and contacts array are required' });
    }

    let newContactsCount = 0;
    let updatedContactsCount = 0;

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
        console.error('Error processing contact:', contactError);
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
      message: 'Contacts synced successfully',
      newContacts: newContactsCount,
      updatedContacts: updatedContactsCount,
      totalProcessed: contacts.length
    });
  } catch (error) {
    console.error('Contacts sync error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Get contacts for a device
router.get('/:deviceId', async (req, res) => {
  try {
    const { deviceId } = req.params;
    const { page = 1, limit = 100, search } = req.query;

    let query = { deviceId };
    
    if (search) {
      query.$or = [
        { name: { $regex: search, $options: 'i' } },
        { 'phoneNumbers.number': { $regex: search, $options: 'i' } },
        { 'emails.email': { $regex: search, $options: 'i' } }
      ];
    }

    const contacts = await Contact.find(query)
      .sort({ name: 1 })
      .limit(limit * 1)
      .skip((page - 1) * limit);

    const total = await Contact.countDocuments(query);

    res.json({
      contacts,
      pagination: {
        current: page,
        pages: Math.ceil(total / limit),
        total
      }
    });
  } catch (error) {
    console.error('Get contacts error:', error);
    res.status(500).json({ error: 'Internal server error' });
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
