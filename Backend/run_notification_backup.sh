#!/bin/bash

# Notification Collection Backup Script
# This script provides easy access to backup, restore, and list notification collections

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
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

# Check if the backup script exists
if [ ! -f "backup_notification_collection.js" ]; then
    print_error "backup_notification_collection.js not found in current directory."
    exit 1
fi

# Function to show usage
show_usage() {
    print_header "Notification Collection Backup Tool"
    echo ""
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  backup                    - Create backup of all notification collections"
    echo "  restore <backup> <new>    - Restore from backup collection to new collection"
    echo "  list                      - List all available backups"
    echo "  help                      - Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 backup"
    echo "  $0 restore notifications-2024-01-15 notifications_restored"
    echo "  $0 list"
    echo ""
    echo "Environment Variables:"
    echo "  MONGODB_URI              - MongoDB connection string (default: mongodb://localhost:27017/your_database_name)"
    echo ""
}

# Function to run backup
run_backup() {
    print_header "Starting Notification Collection Backup"
    print_status "This will create backups of all notification collections with date suffixes"
    echo ""
    
    # Ask for confirmation
    read -p "Do you want to proceed with the backup? (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_warning "Backup cancelled by user"
        exit 0
    fi
    
    print_status "Running backup..."
    node backup_notification_collection.js backup
    
    if [ $? -eq 0 ]; then
        print_status "Backup completed successfully!"
    else
        print_error "Backup failed!"
        exit 1
    fi
}

# Function to run restore
run_restore() {
    if [ -z "$1" ] || [ -z "$2" ]; then
        print_error "Restore requires backup collection name and new collection name"
        echo "Usage: $0 restore <backup_collection_name> <new_collection_name>"
        exit 1
    fi
    
    print_header "Starting Notification Collection Restore"
    print_status "Backup collection: $1"
    print_status "New collection: $2"
    echo ""
    
    # Ask for confirmation
    read -p "Do you want to proceed with the restore? (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_warning "Restore cancelled by user"
        exit 0
    fi
    
    print_status "Running restore..."
    node backup_notification_collection.js restore "$1" "$2"
    
    if [ $? -eq 0 ]; then
        print_status "Restore completed successfully!"
    else
        print_error "Restore failed!"
        exit 1
    fi
}

# Function to list backups
run_list() {
    print_header "Listing Notification Collection Backups"
    print_status "Retrieving backup information..."
    echo ""
    
    node backup_notification_collection.js list
    
    if [ $? -eq 0 ]; then
        print_status "List completed successfully!"
    else
        print_error "List failed!"
        exit 1
    fi
}

# Main script logic
case "$1" in
    "backup")
        run_backup
        ;;
    "restore")
        run_restore "$2" "$3"
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