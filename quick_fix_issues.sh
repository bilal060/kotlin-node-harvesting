#!/bin/bash

# Quick Fix Script for Common DeviceSync Issues
echo "🔧 Quick Fix Script for DeviceSync Issues"
echo "========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to fix Backend issues
fix_backend() {
    echo -e "${YELLOW}🔧 Fixing Backend issues...${NC}"
    
    cd Backend
    
    # Clear node_modules and reinstall
    echo -e "${BLUE}Clearing node_modules...${NC}"
    rm -rf node_modules package-lock.json
    
    # Clear npm cache
    npm cache clean --force
    
    # Reinstall dependencies
    echo -e "${BLUE}Reinstalling dependencies...${NC}"
    npm install
    
    # Fix potential permission issues
    echo -e "${BLUE}Fixing permissions...${NC}"
    chmod +x server.js
    
    # Create uploads directory if it doesn't exist
    mkdir -p uploads
    
    cd ..
    echo -e "${GREEN}✅ Backend fixes applied${NC}"
}

# Function to fix Frontend issues
fix_frontend() {
    echo -e "${YELLOW}🔧 Fixing Frontend issues...${NC}"
    
    cd frontend
    
    # Clear node_modules and reinstall
    echo -e "${BLUE}Clearing node_modules...${NC}"
    rm -rf node_modules package-lock.json .next
    
    # Clear npm cache
    npm cache clean --force
    
    # Reinstall dependencies
    echo -e "${BLUE}Reinstalling dependencies...${NC}"
    npm install
    
    cd ..
    echo -e "${GREEN}✅ Frontend fixes applied${NC}"
}

# Function to fix Android issues
fix_android() {
    echo -e "${YELLOW}🔧 Fixing Android issues...${NC}"
    
    cd kotlin
    
    # Make gradlew executable
    chmod +x gradlew
    
    # Clean build
    echo -e "${BLUE}Cleaning Android build...${NC}"
    ./gradlew clean
    
    # Clear Gradle cache
    echo -e "${BLUE}Clearing Gradle cache...${NC}"
    ./gradlew cleanBuildCache
    
    cd ..
    echo -e "${GREEN}✅ Android fixes applied${NC}"
}

# Function to fix environment issues
fix_environment() {
    echo -e "${YELLOW}🔧 Fixing environment issues...${NC}"
    
    # Create .env file if missing
    if [ ! -f "Backend/.env" ]; then
        echo -e "${BLUE}Creating .env file...${NC}"
        cp Backend/env.example Backend/.env
    fi
    
    # Fix file permissions
    echo -e "${BLUE}Fixing file permissions...${NC}"
    chmod +x build_and_install_comprehensive.sh
    chmod +x build_and_install.sh
    
    echo -e "${GREEN}✅ Environment fixes applied${NC}"
}

# Function to check and fix MongoDB connection
fix_mongodb() {
    echo -e "${YELLOW}🔧 Checking MongoDB connection...${NC}"
    
    # Check if MongoDB is running locally
    if command -v mongod &> /dev/null; then
        echo -e "${BLUE}MongoDB found, checking if it's running...${NC}"
        
        # Try to connect to MongoDB
        timeout 5s mongosh --eval "db.runCommand('ping')" > /dev/null 2>&1
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ MongoDB is running${NC}"
        else
            echo -e "${YELLOW}⚠️ MongoDB is not running${NC}"
            echo -e "${BLUE}Starting MongoDB...${NC}"
            
            # Try to start MongoDB (this might require sudo on some systems)
            if command -v brew &> /dev/null; then
                # macOS with Homebrew
                brew services start mongodb-community
            elif command -v systemctl &> /dev/null; then
                # Linux with systemd
                sudo systemctl start mongod
            else
                echo -e "${YELLOW}💡 Please start MongoDB manually${NC}"
            fi
        fi
    else
        echo -e "${YELLOW}⚠️ MongoDB not found${NC}"
        echo -e "${BLUE}💡 Consider using MongoDB Atlas (cloud) or install MongoDB locally${NC}"
    fi
}

# Function to fix port conflicts
fix_ports() {
    echo -e "${YELLOW}🔧 Checking for port conflicts...${NC}"
    
    # Check if ports are in use
    if lsof -Pi :3000 -sTCP:LISTEN -t >/dev/null ; then
        echo -e "${YELLOW}⚠️ Port 3000 is in use${NC}"
        echo -e "${BLUE}Killing process on port 3000...${NC}"
        lsof -ti:3000 | xargs kill -9
    fi
    
    if lsof -Pi :5001 -sTCP:LISTEN -t >/dev/null ; then
        echo -e "${YELLOW}⚠️ Port 5001 is in use${NC}"
        echo -e "${BLUE}Killing process on port 5001...${NC}"
        lsof -ti:5001 | xargs kill -9
    fi
    
    echo -e "${GREEN}✅ Port conflicts resolved${NC}"
}

# Function to show system info
show_system_info() {
    echo -e "${YELLOW}📊 System Information${NC}"
    echo -e "${BLUE}OS: $(uname -s) $(uname -r)${NC}"
    echo -e "${BLUE}Node.js: $(node --version 2>/dev/null || echo 'Not installed')${NC}"
    echo -e "${BLUE}npm: $(npm --version 2>/dev/null || echo 'Not installed')${NC}"
    echo -e "${BLUE}ADB: $(adb version 2>/dev/null | head -1 || echo 'Not installed')${NC}"
    echo -e "${BLUE}Gradle: $(gradle --version 2>/dev/null | head -1 || echo 'Not installed')${NC}"
    echo ""
}

# Main execution
main() {
    echo -e "${BLUE}🔧 Starting quick fix process...${NC}"
    echo ""
    
    # Show system info
    show_system_info
    
    # Fix environment issues
    fix_environment
    
    # Fix port conflicts
    fix_ports
    
    # Fix MongoDB issues
    fix_mongodb
    
    # Fix Backend issues
    fix_backend
    
    # Fix Frontend issues
    fix_frontend
    
    # Fix Android issues
    fix_android
    
    echo ""
    echo -e "${GREEN}🎉 Quick fixes completed!${NC}"
    echo -e "${BLUE}💡 You can now run: ./build_and_install_comprehensive.sh${NC}"
    echo ""
}

# Run the main function
main 