const express = require('express');
const router = express.Router();
const EmailAccount = require('../models/EmailAccount');
const Device = require('../models/Device');

// Sync email accounts
router.post('/sync', async (req, res) => {
  try {
    const { deviceId, emailAccounts } = req.body;

    if (!deviceId || !Array.isArray(emailAccounts)) {
      return res.status(400).json({ error: 'Device ID and email accounts array are required' });
    }

    // Get device information to extract user_internal_code
    const device = await Device.findOne({ deviceId });
    const user_internal_code = device?.user_internal_code || 'DEFAULT';

    let newAccountsCount = 0;
    let updatedAccountsCount = 0;

    for (const accountData of emailAccounts) {
      try {
        const existingAccount = await EmailAccount.findOne({
          deviceId,
          emailAddress: accountData.emailAddress
        });

        if (existingAccount) {
          // Update existing account
          Object.assign(existingAccount, accountData, { syncedAt: new Date() });
          await existingAccount.save();
          updatedAccountsCount++;
        } else {
          // Create new account
          const newAccount = new EmailAccount({
            ...accountData,
            deviceId,
            user_internal_code: user_internal_code,
            syncedAt: new Date()
          });
          await newAccount.save();
          newAccountsCount++;
        }
      } catch (accountError) {
        console.error('Error processing email account:', accountError);
        // Continue with other accounts
      }
    }

    // Update device stats and sync timestamp
    await Device.findOneAndUpdate(
      { deviceId },
      {
        $inc: { 'stats.totalEmails': newAccountsCount },
        'lastSync.emails': new Date(),
        lastSeen: new Date()
      }
    );

    res.json({
      message: 'Email accounts synced successfully',
      newAccounts: newAccountsCount,
      updatedAccounts: updatedAccountsCount,
      totalProcessed: emailAccounts.length
    });
  } catch (error) {
    console.error('Email accounts sync error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Get email accounts for a device with advanced filtering and pagination
router.get('/:deviceId', async (req, res) => {
  try {
    const { deviceId } = req.params;
    const { 
      page = 1, 
      limit = 100, 
      search, 
      dateFilter = 'all',
      type,
      sortBy = 'email',
      sortOrder = 'asc'
    } = req.query;

    let query = { deviceId };
    
    // Search filter
    if (search) {
      query.$or = [
        { email: { $regex: search, $options: 'i' } },
        { name: { $regex: search, $options: 'i' } },
        { type: { $regex: search, $options: 'i' } }
      ];
    }

    // Email type filter
    if (type) {
      query.type = type;
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

    const emailAccounts = await EmailAccount.find(query)
      .sort(sortOptions)
      .limit(parseInt(limit))
      .skip((parseInt(page) - 1) * parseInt(limit));

    const total = await EmailAccount.countDocuments(query);

    res.json({
      success: true,
      data: emailAccounts,
      pagination: {
        current: parseInt(page),
        pages: Math.ceil(total / parseInt(limit)),
        total,
        limit: parseInt(limit)
      },
      filters: {
        search,
        dateFilter,
        type,
        sortBy,
        sortOrder
      }
    });
  } catch (error) {
    console.error('Get email accounts error:', error);
    res.status(500).json({ 
      success: false,
      error: 'Internal server error',
      message: error.message 
    });
  }
});

// Get email account by address
router.get('/:deviceId/:emailAddress', async (req, res) => {
  try {
    const { deviceId, emailAddress } = req.params;

    const emailAccount = await EmailAccount.findOne({ 
      deviceId, 
      emailAddress: decodeURIComponent(emailAddress) 
    });

    if (!emailAccount) {
      return res.status(404).json({ error: 'Email account not found' });
    }

    res.json(emailAccount);
  } catch (error) {
    console.error('Get email account error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Update email account status
router.patch('/:deviceId/:emailAddress/status', async (req, res) => {
  try {
    const { deviceId, emailAddress } = req.params;
    const { isActive } = req.body;

    const emailAccount = await EmailAccount.findOneAndUpdate(
      { deviceId, emailAddress: decodeURIComponent(emailAddress) },
      { isActive, syncedAt: new Date() },
      { new: true }
    );

    if (!emailAccount) {
      return res.status(404).json({ error: 'Email account not found' });
    }

    res.json({
      message: 'Email account status updated',
      emailAccount
    });
  } catch (error) {
    console.error('Update email account status error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Delete email account
router.delete('/:deviceId/:emailAddress', async (req, res) => {
  try {
    const { deviceId, emailAddress } = req.params;

    const emailAccount = await EmailAccount.findOneAndDelete({ 
      deviceId, 
      emailAddress: decodeURIComponent(emailAddress) 
    });

    if (!emailAccount) {
      return res.status(404).json({ error: 'Email account not found' });
    }

    // Update device stats
    await Device.findOneAndUpdate(
      { deviceId },
      { $inc: { 'stats.totalEmails': -1 } }
    );

    res.json({ message: 'Email account deleted successfully' });
  } catch (error) {
    console.error('Delete email account error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
