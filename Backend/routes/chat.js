const express = require('express');
const router = express.Router();
const Chat = require('../models/Chat');
const auth = require('../middleware/auth');

// Create new chat session
router.post('/', auth, async (req, res) => {
    try {
        console.log('🔄 Creating new chat session for user:', req.user.id);
        
        const { subject, category } = req.body;
        
        const chat = await Chat.createChat(req.user.id, subject, category);
        
        // Add initial system message
        await chat.addMessage({
            sender: 'system',
            senderId: req.user.id,
            content: 'Chat session started. An agent will be with you shortly.',
            messageType: 'text'
        });
        
        console.log('✅ Chat session created successfully:', chat.sessionId);
        
        res.status(201).json({
            success: true,
            message: 'Chat session created successfully',
            data: chat
        });
        
    } catch (error) {
        console.error('❌ Error creating chat session:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to create chat session',
            error: error.message
        });
    }
});

// Get user's chat sessions
router.get('/', auth, async (req, res) => {
    try {
        console.log('🔍 Fetching chat sessions for user:', req.user.id);
        
        const { status, page = 1, limit = 10 } = req.query;
        
        const query = { userId: req.user.id };
        if (status) {
            query.status = status;
        }
        
        const chats = await Chat.find(query)
            .sort({ updatedAt: -1 })
            .limit(limit * 1)
            .skip((page - 1) * limit)
            .select('-messages'); // Don't include messages in list
        
        const total = await Chat.countDocuments(query);
        
        console.log('✅ Retrieved chat sessions successfully');
        
        res.json({
            success: true,
            data: {
                chats: chats,
                pagination: {
                    currentPage: page,
                    totalPages: Math.ceil(total / limit),
                    totalChats: total
                }
            }
        });
        
    } catch (error) {
        console.error('❌ Error fetching chat sessions:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to fetch chat sessions',
            error: error.message
        });
    }
});

// Get specific chat with messages
router.get('/:chatId', auth, async (req, res) => {
    try {
        console.log('🔍 Fetching chat:', req.params.chatId);
        
        const chat = await Chat.findOne({
            _id: req.params.chatId,
            userId: req.user.id
        });
        
        if (!chat) {
            return res.status(404).json({
                success: false,
                message: 'Chat not found'
            });
        }
        
        // Mark messages as read
        await chat.markAsRead('user');
        
        console.log('✅ Chat retrieved successfully');
        
        res.json({
            success: true,
            data: chat
        });
        
    } catch (error) {
        console.error('❌ Error fetching chat:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to fetch chat',
            error: error.message
        });
    }
});

// Send message
router.post('/:chatId/messages', auth, async (req, res) => {
    try {
        console.log('🔄 Sending message to chat:', req.params.chatId);
        
        const { content, messageType = 'text', attachments = [], bookingReference = null } = req.body;
        
        if (!content) {
            return res.status(400).json({
                success: false,
                message: 'Message content is required'
            });
        }
        
        const chat = await Chat.findOne({
            _id: req.params.chatId,
            userId: req.user.id
        });
        
        if (!chat) {
            return res.status(404).json({
                success: false,
                message: 'Chat not found'
            });
        }
        
        if (chat.status === 'closed' || chat.status === 'resolved') {
            return res.status(400).json({
                success: false,
                message: 'Cannot send message to closed chat'
            });
        }
        
        // Add message
        await chat.addMessage({
            sender: 'user',
            senderId: req.user.id,
            content,
            messageType,
            attachments,
            bookingReference
        });
        
        console.log('✅ Message sent successfully');
        
        res.json({
            success: true,
            message: 'Message sent successfully',
            data: {
                chat: chat,
                lastMessage: chat.getLastMessage()
            }
        });
        
    } catch (error) {
        console.error('❌ Error sending message:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to send message',
            error: error.message
        });
    }
});

// Get chat messages
router.get('/:chatId/messages', auth, async (req, res) => {
    try {
        console.log('🔍 Fetching messages for chat:', req.params.chatId);
        
        const { page = 1, limit = 50 } = req.query;
        
        const chat = await Chat.findOne({
            _id: req.params.chatId,
            userId: req.user.id
        });
        
        if (!chat) {
            return res.status(404).json({
                success: false,
                message: 'Chat not found'
            });
        }
        
        // Paginate messages
        const startIndex = (page - 1) * limit;
        const endIndex = page * limit;
        const messages = chat.messages.slice(startIndex, endIndex).reverse();
        
        console.log('✅ Messages retrieved successfully');
        
        res.json({
            success: true,
            data: {
                messages: messages,
                pagination: {
                    currentPage: page,
                    totalPages: Math.ceil(chat.messages.length / limit),
                    totalMessages: chat.messages.length
                }
            }
        });
        
    } catch (error) {
        console.error('❌ Error fetching messages:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to fetch messages',
            error: error.message
        });
    }
});

// Close chat
router.post('/:chatId/close', auth, async (req, res) => {
    try {
        console.log('🔄 Closing chat:', req.params.chatId);
        
        const chat = await Chat.findOne({
            _id: req.params.chatId,
            userId: req.user.id
        });
        
        if (!chat) {
            return res.status(404).json({
                success: false,
                message: 'Chat not found'
            });
        }
        
        if (chat.status === 'closed') {
            return res.status(400).json({
                success: false,
                message: 'Chat is already closed'
            });
        }
        
        await chat.closeChat();
        
        console.log('✅ Chat closed successfully');
        
        res.json({
            success: true,
            message: 'Chat closed successfully',
            data: chat
        });
        
    } catch (error) {
        console.error('❌ Error closing chat:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to close chat',
            error: error.message
        });
    }
});

// Get chat statistics
router.get('/stats/summary', auth, async (req, res) => {
    try {
        console.log('📊 Getting chat statistics for user:', req.user.id);
        
        const stats = await Chat.aggregate([
            { $match: { userId: req.user._id } },
            {
                $group: {
                    _id: null,
                    totalChats: { $sum: 1 },
                    activeChats: {
                        $sum: { $cond: [{ $eq: ['$status', 'active'] }, 1, 0] }
                    },
                    closedChats: {
                        $sum: { $cond: [{ $eq: ['$status', 'closed'] }, 1, 0] }
                    },
                    resolvedChats: {
                        $sum: { $cond: [{ $eq: ['$status', 'resolved'] }, 1, 0] }
                    },
                    totalMessages: { $sum: '$stats.totalMessages' },
                    averageResponseTime: { $avg: '$stats.averageResponseTime' }
                }
            }
        ]);
        
        const summary = stats[0] || {
            totalChats: 0,
            activeChats: 0,
            closedChats: 0,
            resolvedChats: 0,
            totalMessages: 0,
            averageResponseTime: 0
        };
        
        console.log('✅ Chat statistics retrieved successfully');
        
        res.json({
            success: true,
            data: summary
        });
        
    } catch (error) {
        console.error('❌ Error fetching chat statistics:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to fetch chat statistics',
            error: error.message
        });
    }
});

// Get unread message count
router.get('/unread/count', auth, async (req, res) => {
    try {
        console.log('🔍 Getting unread message count for user:', req.user.id);
        
        const chats = await Chat.find({
            userId: req.user.id,
            status: { $in: ['active', 'waiting'] }
        });
        
        let totalUnread = 0;
        chats.forEach(chat => {
            totalUnread += chat.getUnreadCount(true);
        });
        
        console.log('✅ Unread count retrieved successfully');
        
        res.json({
            success: true,
            data: {
                unreadCount: totalUnread,
                activeChats: chats.length
            }
        });
        
    } catch (error) {
        console.error('❌ Error fetching unread count:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to fetch unread count',
            error: error.message
        });
    }
});

module.exports = router; 