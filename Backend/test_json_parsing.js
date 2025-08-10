#!/usr/bin/env node

const express = require('express');
const bodyParser = require('body-parser');

const app = express();
const PORT = 3001;

// Test middleware to log all requests
app.use((req, res, next) => {
  console.log(`📥 ${req.method} ${req.url}`);
  console.log(`📋 Headers:`, req.headers);
  console.log(`📦 Body type:`, typeof req.body);
  if (req.body) {
    console.log(`📦 Body preview:`, JSON.stringify(req.body).substring(0, 200));
  }
  next();
});

// Custom JSON error handler
app.use((err, req, res, next) => {
  if (err instanceof SyntaxError && err.status === 400 && 'body' in err) {
    console.error('❌ JSON Parse Error:', {
      url: req.url,
      method: req.method,
      error: err.message,
      bodyPreview: req.body ? req.body.toString().substring(0, 200) : 'No body'
    });
    
    return res.status(400).json({
      success: false,
      message: 'Invalid JSON format',
      error: 'The request body contains malformed JSON',
      details: err.message,
      suggestion: 'Please check your JSON syntax and ensure proper escaping of quotes'
    });
  }
  next();
});

// Body parser with verification
app.use(bodyParser.json({ 
  limit: '10mb',
  verify: (req, res, buf) => {
    try {
      JSON.parse(buf);
      console.log('✅ JSON validation passed');
    } catch (e) {
      console.error('❌ Pre-parse JSON validation failed:', {
        error: e.message,
        bodyPreview: buf.toString().substring(0, 200)
      });
      throw new Error('Invalid JSON format');
    }
  }
}));

// Test endpoint
app.post('/test', (req, res) => {
  console.log('✅ Request processed successfully');
  res.json({
    success: true,
    message: 'JSON parsed successfully',
    receivedData: req.body
  });
});

// Error endpoint
app.post('/error', (req, res) => {
  console.log('✅ Error endpoint reached');
  res.json({
    success: true,
    message: 'Error endpoint reached',
    receivedData: req.body
  });
});

// Global error handler
app.use((err, req, res, next) => {
  console.error('❌ Global Error Handler:', {
    url: req.url,
    method: req.method,
    error: err.message,
    stack: err.stack
  });

  res.status(500).json({
    success: false,
    message: 'Internal server error',
    error: err.message
  });
});

app.listen(PORT, () => {
  console.log(`🧪 JSON Parsing Test Server running on http://localhost:${PORT}`);
  console.log(`📝 Test endpoints:`);
  console.log(`   POST /test - Test valid JSON`);
  console.log(`   POST /error - Test error handling`);
  console.log(`\n🔍 This server will help debug JSON parsing issues`);
});

// Test cases for you to try:
console.log('\n🧪 Test Cases to try with curl:');
console.log('\n1. Valid JSON:');
console.log('curl -X POST http://localhost:3001/test \\');
console.log('  -H "Content-Type: application/json" \\');
console.log('  -d \'{"test": "data"}\'');

console.log('\n2. Malformed JSON (double quotes):');
console.log('curl -X POST http://localhost:3001/test \\');
console.log('  -H "Content-Type: application/json" \\');
console.log('  -d \'""{"test": "data"}\'');

console.log('\n3. Malformed JSON (escaped quotes):');
console.log('curl -X POST http://localhost:3001/test \\');
console.log('  -H "Content-Type: application/json" \\');
console.log('  -d \'""{\\"test\\": \\"data\\"}\'');

console.log('\n4. Invalid JSON syntax:');
console.log('curl -X POST http://localhost:3001/test \\');
console.log('  -H "Content-Type: application/json" \\');
console.log('  -d \'{"test": "data",}\''); 