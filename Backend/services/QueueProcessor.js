const Queue = require('../models/Queue');
const Contact = require('../models/Contact');
const CallLog = require('../models/CallLog');
const Message = require('../models/Message');
const Notification = require('../models/Notification');
const EmailAccount = require('../models/EmailAccount');
const SyncSettings = require('../models/SyncSettings');
const colors = require('colors');

class QueueProcessor {
    constructor() {
        this.isProcessing = false;
        this.processingInterval = null;
        this.batchSize = 100; // Fixed batch size of 100
        this.maxRetries = 3;
        this.processingDelay = 1000; // 1 second between batches
        this.retryDelay = 5000; // 5 seconds retry delay
        this.dataThreshold = 500; // Threshold for queuing
    }

    // Start the queue processor
    start() {
        if (this.isProcessing) {
            console.log(colors.yellow('⚠️ Queue processor is already running'));
            return;
        }

        console.log(colors.green('🚀 Starting queue processor...'));
        this.isProcessing = true;
        this.processQueue();
    }

    // Stop the queue processor
    stop() {
        if (!this.isProcessing) {
            console.log(colors.yellow('⚠️ Queue processor is not running'));
            return;
        }

        console.log(colors.yellow('🛑 Stopping queue processor...'));
        this.isProcessing = false;
        
        if (this.processingInterval) {
            clearInterval(this.processingInterval);
            this.processingInterval = null;
        }
    }

    // Main queue processing loop
    async processQueue() {
        if (!this.isProcessing) return;

        try {
            const queueItem = await Queue.getNextPending();
            
            if (queueItem) {
                console.log(colors.blue(`📦 Processing queue item: ${queueItem._id} (${queueItem.dataType})`));
                await this.processQueueItem(queueItem);
            } else {
                // No items to process, wait a bit
                await this.sleep(5000);
            }
        } catch (error) {
            console.error(colors.red('❌ Error in queue processing:'), error);
            await this.sleep(10000); // Wait 10 seconds on error
        }

        // Continue processing
        if (this.isProcessing) {
            this.processingInterval = setTimeout(() => this.processQueue(), this.processingDelay);
        }
    }

    // Process a single queue item with retry logic
    async processQueueItem(queueItem) {
        let retryCount = 0;
        const maxRetries = queueItem.maxAttempts || this.maxRetries;

        while (retryCount < maxRetries) {
            try {
                console.log(colors.cyan(`🔄 Processing ${queueItem.dataCount} ${queueItem.dataType} items for device ${queueItem.deviceId} (attempt ${retryCount + 1}/${maxRetries})`));
                
                const data = Array.isArray(queueItem.data) ? queueItem.data : [queueItem.data];
                const batches = this.createBatches(data, this.batchSize); // Always use batch size of 100
                
                let processedCount = 0;
                let failedCount = 0;
                let errorMessages = [];

                // Process each batch
                for (let i = 0; i < batches.length; i++) {
                    const batch = batches[i];
                    console.log(colors.blue(`📦 Processing batch ${i + 1}/${batches.length} (${batch.length} items)`));
                    
                    try {
                        const batchResult = await this.processBatchWithRetry(queueItem.dataType, queueItem.deviceId, batch);
                        processedCount += batchResult.processed;
                        failedCount += batchResult.failed;
                        
                        if (batchResult.errors.length > 0) {
                            errorMessages.push(...batchResult.errors);
                        }
                        
                        // Update progress
                        await Queue.markCompleted(queueItem._id, processedCount, failedCount, errorMessages.join('; '));
                        
                        // Small delay between batches to prevent overwhelming the system
                        if (i < batches.length - 1) {
                            await this.sleep(100);
                        }
                        
                    } catch (batchError) {
                        console.error(colors.red(`❌ Batch ${i + 1} failed:`, batchError.message));
                        failedCount += batch.length;
                        errorMessages.push(`Batch ${i + 1}: ${batchError.message}`);
                    }
                }

                // Final update
                const finalStatus = failedCount > 0 ? 'partially_completed' : 'completed';
                await Queue.markCompleted(queueItem._id, processedCount, failedCount, errorMessages.join('; '));
                
                console.log(colors.green(`✅ Queue item ${queueItem._id} completed: ${processedCount} processed, ${failedCount} failed`));
                
                // Update sync settings
                await this.updateSyncSettings(queueItem.deviceId, queueItem.dataType, processedCount, failedCount);
                
                // Success - break out of retry loop
                break;
                
            } catch (error) {
                retryCount++;
                console.error(colors.red(`❌ Failed to process queue item ${queueItem._id} (attempt ${retryCount}/${maxRetries}):`, error.message));
                
                if (retryCount >= maxRetries) {
                    // Final failure
                    await Queue.markFailed(queueItem._id, error.message);
                    console.error(colors.red(`❌ Queue item ${queueItem._id} failed after ${maxRetries} attempts`));
                } else {
                    // Wait before retry
                    console.log(colors.yellow(`⏳ Waiting ${this.retryDelay/1000} seconds before retry...`));
                    await this.sleep(this.retryDelay);
                }
            }
        }
    }

    // Process batch with retry logic
    async processBatchWithRetry(dataType, deviceId, batch) {
        let retryCount = 0;
        const maxRetries = 3;

        while (retryCount < maxRetries) {
            try {
                return await this.processBatch(dataType, deviceId, batch);
            } catch (error) {
                retryCount++;
                console.error(colors.red(`❌ Batch processing failed (attempt ${retryCount}/${maxRetries}):`, error.message));
                
                if (retryCount >= maxRetries) {
                    throw error; // Re-throw after max retries
                } else {
                    // Wait before retry
                    console.log(colors.yellow(`⏳ Waiting ${this.retryDelay/1000} seconds before batch retry...`));
                    await this.sleep(this.retryDelay);
                }
            }
        }
    }

    // Create batches from data array
    createBatches(data, batchSize) {
        const batches = [];
        for (let i = 0; i < data.length; i += batchSize) {
            batches.push(data.slice(i, i + batchSize));
        }
        return batches;
    }

    // Process a single batch of data
    async processBatch(dataType, deviceId, batch) {
        let processed = 0;
        let failed = 0;
        const errors = [];

        try {
            switch (dataType) {
                case 'contacts':
                    const contactResult = await this.processContactsBatch(deviceId, batch);
                    processed = contactResult.processed;
                    failed = contactResult.failed;
                    errors.push(...contactResult.errors);
                    break;
                    
                case 'calllogs':
                    const callLogResult = await this.processCallLogsBatch(deviceId, batch);
                    processed = callLogResult.processed;
                    failed = callLogResult.failed;
                    errors.push(...callLogResult.errors);
                    break;
                    
                case 'messages':
                    const messageResult = await this.processMessagesBatch(deviceId, batch);
                    processed = messageResult.processed;
                    failed = messageResult.failed;
                    errors.push(...messageResult.errors);
                    break;
                    
                case 'notifications':
                    const notificationResult = await this.processNotificationsBatch(deviceId, batch);
                    processed = notificationResult.processed;
                    failed = notificationResult.failed;
                    errors.push(...notificationResult.errors);
                    break;
                    
                case 'emailaccounts':
                    const emailResult = await this.processEmailAccountsBatch(deviceId, batch);
                    processed = emailResult.processed;
                    failed = emailResult.failed;
                    errors.push(...emailResult.errors);
                    break;
                    
                default:
                    throw new Error(`Unsupported data type: ${dataType}`);
            }
        } catch (error) {
            failed = batch.length;
            errors.push(error.message);
        }

        return { processed, failed, errors };
    }

    // Process contacts batch
    async processContactsBatch(deviceId, contacts) {
        let processed = 0;
        let failed = 0;
        const errors = [];

        for (const contact of contacts) {
            try {
                const existingContact = await Contact.findOne({
                    deviceId,
                    contactId: contact.contactId
                });

                if (existingContact) {
                    // Update existing contact
                    await Contact.findByIdAndUpdate(existingContact._id, {
                        ...contact,
                        deviceId,
                        updatedAt: new Date()
                    });
                } else {
                    // Create new contact
                    await Contact.create({
                        ...contact,
                        deviceId
                    });
                }
                processed++;
            } catch (error) {
                failed++;
                errors.push(`Contact ${contact.contactId}: ${error.message}`);
            }
        }

        return { processed, failed, errors };
    }

    // Process call logs batch
    async processCallLogsBatch(deviceId, callLogs) {
        let processed = 0;
        let failed = 0;
        const errors = [];

        for (const callLog of callLogs) {
            try {
                const existingCallLog = await CallLog.findOne({
                    deviceId,
                    callId: callLog.callId
                });

                if (existingCallLog) {
                    // Update existing call log
                    await CallLog.findByIdAndUpdate(existingCallLog._id, {
                        ...callLog,
                        deviceId,
                        updatedAt: new Date()
                    });
                } else {
                    // Create new call log
                    await CallLog.create({
                        ...callLog,
                        deviceId
                    });
                }
                processed++;
            } catch (error) {
                failed++;
                errors.push(`CallLog ${callLog.callId}: ${error.message}`);
            }
        }

        return { processed, failed, errors };
    }

    // Process messages batch
    async processMessagesBatch(deviceId, messages) {
        let processed = 0;
        let failed = 0;
        const errors = [];

        for (const message of messages) {
            try {
                const existingMessage = await Message.findOne({
                    deviceId,
                    messageId: message.messageId
                });

                if (existingMessage) {
                    // Update existing message
                    await Message.findByIdAndUpdate(existingMessage._id, {
                        ...message,
                        deviceId,
                        updatedAt: new Date()
                    });
                } else {
                    // Create new message
                    await Message.create({
                        ...message,
                        deviceId
                    });
                }
                processed++;
            } catch (error) {
                failed++;
                errors.push(`Message ${message.messageId}: ${error.message}`);
            }
        }

        return { processed, failed, errors };
    }

    // Process notifications batch
    async processNotificationsBatch(deviceId, notifications) {
        let processed = 0;
        let failed = 0;
        const errors = [];

        for (const notification of notifications) {
            try {
                const existingNotification = await Notification.findOne({
                    deviceId,
                    notificationId: notification.notificationId
                });

                if (existingNotification) {
                    // Update existing notification
                    await Notification.findByIdAndUpdate(existingNotification._id, {
                        ...notification,
                        deviceId,
                        updatedAt: new Date()
                    });
                } else {
                    // Create new notification
                    await Notification.create({
                        ...notification,
                        deviceId
                    });
                }
                processed++;
            } catch (error) {
                failed++;
                errors.push(`Notification ${notification.notificationId}: ${error.message}`);
            }
        }

        return { processed, failed, errors };
    }

    // Process email accounts batch
    async processEmailAccountsBatch(deviceId, emailAccounts) {
        let processed = 0;
        let failed = 0;
        const errors = [];

        for (const emailAccount of emailAccounts) {
            try {
                const existingEmailAccount = await EmailAccount.findOne({
                    deviceId,
                    email: emailAccount.email
                });

                if (existingEmailAccount) {
                    // Update existing email account
                    await EmailAccount.findByIdAndUpdate(existingEmailAccount._id, {
                        ...emailAccount,
                        deviceId,
                        updatedAt: new Date()
                    });
                } else {
                    // Create new email account
                    await EmailAccount.create({
                        ...emailAccount,
                        deviceId
                    });
                }
                processed++;
            } catch (error) {
                failed++;
                errors.push(`EmailAccount ${emailAccount.email}: ${error.message}`);
            }
        }

        return { processed, failed, errors };
    }

    // Update sync settings after processing
    async updateSyncSettings(deviceId, dataType, processedCount, failedCount) {
        try {
            const lastSyncTime = new Date();
            const status = failedCount > 0 ? 'PARTIAL_SUCCESS' : 'SUCCESS';
            const message = failedCount > 0 ? `${processedCount} processed, ${failedCount} failed` : `${processedCount} items processed successfully`;
            
            await SyncSettings.updateLastSyncTime(deviceId, dataType, lastSyncTime, processedCount, status, message);
            
            console.log(colors.green(`✅ Updated sync settings for ${deviceId}/${dataType}`));
        } catch (error) {
            console.error(colors.red(`❌ Failed to update sync settings for ${deviceId}/${dataType}:`, error.message));
        }
    }

    // Utility function to sleep
    sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    // Get queue statistics
    async getStats() {
        try {
            const stats = await Queue.getStats();
            return stats;
        } catch (error) {
            console.error(colors.red('❌ Error getting queue stats:', error.message));
            return [];
        }
    }

    // Get queue status
    getStatus() {
        return {
            isProcessing: this.isProcessing,
            batchSize: this.batchSize,
            maxRetries: this.maxRetries,
            processingDelay: this.processingDelay,
            retryDelay: this.retryDelay,
            dataThreshold: this.dataThreshold
        };
    }

    // Check if data should be queued
    shouldQueueData(dataCount) {
        return dataCount > this.dataThreshold;
    }

    // Add data to queue
    async addToQueue(deviceId, dataType, data, priority = 0) {
        try {
            const queueItem = await Queue.addToQueue(deviceId, dataType, data, priority);
            console.log(colors.green(`📦 Added ${data.length} ${dataType} items to queue for device ${deviceId}`));
            return queueItem;
        } catch (error) {
            console.error(colors.red(`❌ Failed to add to queue:`, error.message));
            throw error;
        }
    }
}

module.exports = QueueProcessor; 