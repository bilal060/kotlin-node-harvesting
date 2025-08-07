const mongoose = require('mongoose');

const messageSchema = new mongoose.Schema({
    sender: {
        type: String,
        enum: ['user', 'agent', 'system'],
        required: true
    },
    
    senderId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    
    content: {
        type: String,
        required: true
    },
    
    messageType: {
        type: String,
        enum: ['text', 'image', 'file', 'location', 'booking'],
        default: 'text'
    },
    
    // For file/image messages
    attachments: [{
        url: String,
        filename: String,
        size: Number,
        type: String
    }],
    
    // For booking messages
    bookingReference: {
        bookingNumber: String,
        bookingId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'Booking'
        }
    },
    
    // Message status
    status: {
        type: String,
        enum: ['sent', 'delivered', 'read'],
        default: 'sent'
    },
    
    // Timestamps
    createdAt: {
        type: Date,
        default: Date.now
    },
    
    readAt: Date
}, {
    timestamps: true
});

const chatSchema = new mongoose.Schema({
    // User reference
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    
    // Chat session
    sessionId: {
        type: String,
        required: true,
        unique: true
    },
    
    // Chat status
    status: {
        type: String,
        enum: ['active', 'waiting', 'closed', 'resolved'],
        default: 'active'
    },
    
    // Agent assignment
    agent: {
        assigned: {
            type: Boolean,
            default: false
        },
        agentId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'Agent'
        },
        agentName: String,
        assignedAt: Date
    },
    
    // Chat metadata
    subject: {
        type: String,
        default: 'General Inquiry'
    },
    
    category: {
        type: String,
        enum: ['general', 'booking', 'payment', 'technical', 'complaint'],
        default: 'general'
    },
    
    priority: {
        type: String,
        enum: ['low', 'medium', 'high', 'urgent'],
        default: 'medium'
    },
    
    // Messages
    messages: [messageSchema],
    
    // Chat statistics
    stats: {
        totalMessages: {
            type: Number,
            default: 0
        },
        userMessages: {
            type: Number,
            default: 0
        },
        agentMessages: {
            type: Number,
            default: 0
        },
        averageResponseTime: {
            type: Number,
            default: 0
        }
    },
    
    // Timestamps
    createdAt: {
        type: Date,
        default: Date.now
    },
    
    updatedAt: {
        type: Date,
        default: Date.now
    },
    
    closedAt: Date,
    
    resolvedAt: Date
}, {
    timestamps: true
});

// Generate session ID
chatSchema.statics.generateSessionId = function() {
    return 'chat_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
};

// Create new chat session
chatSchema.statics.createChat = async function(userId, subject = 'General Inquiry', category = 'general') {
    const sessionId = this.generateSessionId();
    
    const chat = new this({
        userId,
        sessionId,
        subject,
        category
    });
    
    await chat.save();
    return chat;
};

// Add message to chat
chatSchema.methods.addMessage = function(messageData) {
    const message = {
        sender: messageData.sender,
        senderId: messageData.senderId,
        content: messageData.content,
        messageType: messageData.messageType || 'text',
        attachments: messageData.attachments || [],
        bookingReference: messageData.bookingReference || null
    };
    
    this.messages.push(message);
    this.stats.totalMessages += 1;
    
    if (messageData.sender === 'user') {
        this.stats.userMessages += 1;
    } else if (messageData.sender === 'agent') {
        this.stats.agentMessages += 1;
    }
    
    this.updatedAt = new Date();
    return this.save();
};

// Mark messages as read
chatSchema.methods.markAsRead = function(sender) {
    const now = new Date();
    
    this.messages.forEach(message => {
        if (message.sender !== sender && message.status !== 'read') {
            message.status = 'read';
            message.readAt = now;
        }
    });
    
    return this.save();
};

// Close chat
chatSchema.methods.closeChat = function() {
    this.status = 'closed';
    this.closedAt = new Date();
    return this.save();
};

// Resolve chat
chatSchema.methods.resolveChat = function() {
    this.status = 'resolved';
    this.resolvedAt = new Date();
    return this.save();
};

// Assign agent
chatSchema.methods.assignAgent = function(agentId, agentName) {
    this.agent.assigned = true;
    this.agent.agentId = agentId;
    this.agent.agentName = agentName;
    this.agent.assignedAt = new Date();
    return this.save();
};

// Get unread message count
chatSchema.methods.getUnreadCount = function(forUser = true) {
    const sender = forUser ? 'user' : 'agent';
    return this.messages.filter(message => 
        message.sender !== sender && message.status !== 'read'
    ).length;
};

// Get last message
chatSchema.methods.getLastMessage = function() {
    if (this.messages.length === 0) return null;
    return this.messages[this.messages.length - 1];
};

// Calculate average response time
chatSchema.methods.calculateAverageResponseTime = function() {
    let totalResponseTime = 0;
    let responseCount = 0;
    
    for (let i = 1; i < this.messages.length; i++) {
        const currentMessage = this.messages[i];
        const previousMessage = this.messages[i - 1];
        
        if (currentMessage.sender !== previousMessage.sender) {
            const responseTime = currentMessage.createdAt - previousMessage.createdAt;
            totalResponseTime += responseTime;
            responseCount += 1;
        }
    }
    
    this.stats.averageResponseTime = responseCount > 0 ? totalResponseTime / responseCount : 0;
    return this.stats.averageResponseTime;
};

module.exports = mongoose.model('Chat', chatSchema); 