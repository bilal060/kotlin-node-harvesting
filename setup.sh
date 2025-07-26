#!/bin/bash

# Device Synchronization System Setup Script
# This script helps set up the complete system

set -e

echo "🚀 Device Synchronization System Setup"
echo "======================================"

# Check if required tools are installed
check_dependencies() {
    echo "📋 Checking dependencies..."
    
    # Check Node.js
    if ! command -v node &> /dev/null; then
        echo "❌ Node.js is not installed. Please install Node.js 16+ first."
        exit 1
    fi
    
    # Check npm
    if ! command -v npm &> /dev/null; then
        echo "❌ npm is not installed. Please install npm first."
        exit 1
    fi
    
    # Check MongoDB
    if ! command -v mongod &> /dev/null; then
        echo "⚠️  MongoDB is not installed. Please install MongoDB 4.4+ first."
        echo "   Visit: https://docs.mongodb.com/manual/installation/"
    fi
    
    # Check Flutter (optional)
    if ! command -v flutter &> /dev/null; then
        echo "⚠️  Flutter is not installed. Install Flutter 3.0+ to build the mobile app."
        echo "   Visit: https://flutter.dev/docs/get-started/install"
    fi
    
    echo "✅ Dependencies check completed"
}

# Setup Backend
setup_backend() {
    echo "🔧 Setting up Backend..."
    cd Backend
    
    # Install dependencies
    echo "📦 Installing backend dependencies..."
    npm install
    
    # Create .env file if it doesn't exist
    if [ ! -f .env ]; then
        echo "📝 Creating .env file..."
        cat > .env << EOL
PORT=3000
MONGODB_URI=mongodb://localhost:27017/device_sync
JWT_SECRET=$(openssl rand -base64 32)
NODE_ENV=development
EOL
        echo "✅ Created .env file with default configuration"
    fi
    
    cd ..
    echo "✅ Backend setup completed"
}

# Setup Frontend
setup_frontend() {
    echo "🎨 Setting up Frontend..."
    cd frontend
    
    # Install dependencies
    echo "📦 Installing frontend dependencies..."
    npm install
    
    # Create .env.local file if it doesn't exist
    if [ ! -f .env.local ]; then
        echo "📝 Creating .env.local file..."
        echo "NEXT_PUBLIC_API_URL=http://localhost:3000" > .env.local
        echo "✅ Created .env.local file"
    fi
    
    cd ..
    echo "✅ Frontend setup completed"
}

# Setup Mobile App
setup_mobile() {
    echo "📱 Setting up Mobile App..."
    
    if command -v flutter &> /dev/null; then
        cd App
        
        # Get Flutter dependencies
        echo "📦 Getting Flutter dependencies..."
        flutter pub get
        
        echo "✅ Mobile app setup completed"
        cd ..
    else
        echo "⚠️  Skipping mobile app setup (Flutter not installed)"
        echo "   Install Flutter and run 'flutter pub get' in the App directory"
    fi
}

# Start MongoDB
start_mongodb() {
    echo "🗄️  Starting MongoDB..."
    
    if command -v mongod &> /dev/null; then
        # Check if MongoDB is already running
        if pgrep mongod > /dev/null; then
            echo "✅ MongoDB is already running"
        else
            echo "🚀 Starting MongoDB..."
            mongod --fork --logpath /var/log/mongodb/mongod.log --dbpath /var/lib/mongodb
            echo "✅ MongoDB started"
        fi
    else
        echo "⚠️  MongoDB not found. Please start MongoDB manually."
    fi
}

# Display startup instructions
show_instructions() {
    echo ""
    echo "🎉 Setup completed successfully!"
    echo "================================"
    echo ""
    echo "To start the system:"
    echo ""
    echo "1. Start the Backend (Terminal 1):"
    echo "   cd Backend"
    echo "   npm run dev"
    echo "   # Backend will be available at http://localhost:3000"
    echo ""
    echo "2. Start the Frontend (Terminal 2):"
    echo "   cd frontend"
    echo "   npm run dev"
    echo "   # Frontend will be available at http://localhost:3001"
    echo ""
    echo "3. Run the Mobile App (Terminal 3):"
    echo "   cd App"
    echo "   flutter run"
    echo "   # Connect an Android device or start an emulator"
    echo ""
    echo "📚 Documentation:"
    echo "   - Main README: ./README.md"
    echo "   - Backend API: ./Backend/README.md"
    echo "   - Frontend: ./frontend/README.md"
    echo "   - Mobile App: ./App/README.md"
    echo ""
    echo "🔧 Configuration:"
    echo "   - Backend config: ./Backend/.env"
    echo "   - Frontend config: ./frontend/.env.local"
    echo "   - Mobile API endpoint: ./App/lib/utils/constants.dart"
    echo ""
    echo "Happy coding! 🚀"
}

# Main setup function
main() {
    check_dependencies
    echo ""
    
    setup_backend
    echo ""
    
    setup_frontend
    echo ""
    
    setup_mobile
    echo ""
    
    start_mongodb
    echo ""
    
    show_instructions
}

# Run main function
main
