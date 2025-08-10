#!/bin/bash

# Simple script to rename notification collections with date suffix
# This script connects to MongoDB using environment variables and renames notification collections

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    print_error "Node.js is not installed. Please install Node.js first."
    exit 1
fi

# Check if the rename script exists
if [ ! -f "rename_notification_collection.js" ]; then
    print_error "rename_notification_collection.js not found in current directory."
    exit 1
fi

# Check if MONGODB_URI is set
if [ -z "$MONGODB_URI" ]; then
    print_warning "MONGODB_URI environment variable is not set."
    print_info "Please set it before running this script:"
    echo "  export MONGODB_URI=\"mongodb://localhost:27017/your_database_name\""
    echo ""
    print_info "Or create a .env file with:"
    echo "  MONGODB_URI=mongodb://localhost:27017/your_database_name"
    echo ""
    read -p "Do you want to continue anyway? (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_warning "Operation cancelled by user"
        exit 0
    fi
fi

# Function to show usage
show_usage() {
    print_header "Notification Collection Rename Tool"
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  rename                    - Rename notification collections with date suffix"
    echo "  list                      - List all notification collections"
    echo "  help                      - Show this help message"
    echo ""
    echo "Environment Variables:"
    echo "  MONGODB_URI              - MongoDB connection string (required)"
    echo ""
    echo "Examples:"
    echo "  export MONGODB_URI=\"mongodb://localhost:27017/myapp\""
    echo "  $0 rename"
    echo "  $0 list"
    echo ""
}

# Function to run rename operation
run_rename() {
    print_header "Renaming Notification Collections"
    print_info "This will rename notification collections with date suffixes and create new empty collections"
    echo ""
    
    # Ask for confirmation
    read -p "Do you want to proceed with renaming? (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_warning "Rename operation cancelled by user"
        exit 0
    fi
    
    print_info "Running rename operation..."
    node rename_notification_collection.js rename
    
    if [ $? -eq 0 ]; then
        print_info "Rename operation completed successfully!"
    else
        print_error "Rename operation failed!"
        exit 1
    fi
}

# Function to list collections
run_list() {
    print_header "Listing Notification Collections"
    print_info "Retrieving collection information..."
    echo ""
    
    node rename_notification_collection.js list
    
    if [ $? -eq 0 ]; then
        print_info "List operation completed successfully!"
    else
        print_error "List operation failed!"
        exit 1
    fi
}

# Main script logic
case "$1" in
    "rename")
        run_rename
        ;;
    "list")
        run_list
        ;;
    "help"|"-h"|"--help"|"")
        show_usage
        ;;
    *)
        print_error "Unknown command: $1"
        echo ""
        show_usage
        exit 1
        ;;
esac 