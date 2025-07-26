# Environment Variables Configuration

This document explains how to configure the backend server using environment variables.

## Setup

1. **Copy the example environment file:**
   ```bash
   cp .env.example .env
   ```

2. **Edit the `.env` file with your configuration:**
   ```bash
   nano .env
   ```

## Environment Variables

### Required Variables

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `MONGODB_URI` | MongoDB connection string | `mongodb://localhost:27017/sync_data` | `mongodb+srv://user:pass@cluster.mongodb.net/sync_data` |
| `PORT` | Server port | `5001` | `3000` |

### Optional Variables

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `NODE_ENV` | Environment mode | `development` | `production` |

## MongoDB Connection Examples

### Local MongoDB
```env
MONGODB_URI=mongodb://localhost:27017/sync_data
```

### MongoDB Atlas (Cloud)
```env
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/sync_data?retryWrites=true&w=majority
```

### MongoDB with Authentication
```env
MONGODB_URI=mongodb://username:password@localhost:27017/sync_data?authSource=admin
```

### MongoDB Replica Set
```env
MONGODB_URI=mongodb://localhost:27017,localhost:27018,localhost:27019/sync_data?replicaSet=rs0
```

## Production Deployment

For production deployment, set these environment variables:

```env
NODE_ENV=production
MONGODB_URI=your_production_mongodb_uri
PORT=3000
```

## Security Notes

- Never commit `.env` files to version control
- Use strong passwords for MongoDB authentication
- Consider using MongoDB Atlas for production deployments
- Enable MongoDB authentication in production

## Troubleshooting

### Connection Issues
1. Check if MongoDB is running
2. Verify the connection string format
3. Ensure network connectivity
4. Check firewall settings

### Authentication Issues
1. Verify username and password
2. Check if the user has proper permissions
3. Ensure the authSource is correct

## Example .env File

```env
# MongoDB Configuration
MONGODB_URI=mongodb://localhost:27017/sync_data

# Server Configuration
PORT=5001

# Environment
NODE_ENV=development

# Optional: MongoDB Atlas URI (uncomment and replace with your Atlas connection string)
# MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/sync_data?retryWrites=true&w=majority
``` 