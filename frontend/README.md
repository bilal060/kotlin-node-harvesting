# Device Sync Dashboard Frontend

A modern React-based dashboard for monitoring and managing device data synchronization. Built with Next.js, Tailwind CSS, and React Query.

## Features

### 📊 Real-time Dashboard
- **Live Statistics**: View total devices, active devices, and data counts
- **Health Monitoring**: Real-time connection status to backend
- **Data Distribution**: Visual charts showing data types breakdown
- **Auto-refresh**: Automatic data updates every 30 seconds

### 📱 Device Management
- **Device List**: View all connected devices
- **Device Details**: Comprehensive device information and statistics
- **Status Control**: Activate/deactivate devices
- **Manual Sync**: Trigger manual synchronization for specific data types

### 📈 Data Visualization
- **Pie Charts**: Data distribution across different types
- **Real-time Updates**: Live notification feeds
- **Data Tables**: Detailed view of all synchronized data
- **Responsive Design**: Works on desktop, tablet, and mobile

### 🔧 Data Types Supported
- **Contacts**: Phone contacts and address book data
- **Call Logs**: Incoming, outgoing, and missed calls
- **Notifications**: Real-time app notifications
- **Messages**: SMS and text messages
- **Email Accounts**: Email account configurations
- **WhatsApp**: WhatsApp message data (when available)

## Tech Stack

- **Framework**: Next.js 14
- **Styling**: Tailwind CSS
- **State Management**: React Query (TanStack Query)
- **Charts**: Recharts
- **Icons**: Lucide React
- **Notifications**: React Hot Toast
- **HTTP Client**: Axios

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm or yarn
- Backend server running (see backend setup)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   # or
   yarn install
   ```

3. **Environment Configuration**
   
   Create a `.env.local` file in the frontend directory:
   ```env
   NEXT_PUBLIC_API_URL=https://kotlin-node-harvesting.onrender.com
   ```

4. **Start the development server**
   ```bash
   npm run dev
   # or
   yarn dev
   ```

5. **Open your browser**
   
   Navigate to [http://localhost:3000](http://localhost:3000)

### Production Build

```bash
# Build the application
npm run build

# Start the production server
npm start
```

## Project Structure

```
frontend/
├── components/          # Reusable UI components
│   ├── DeviceCard.js    # Individual device display
│   └── DeviceDetails.js # Detailed device view
├── lib/                 # Utility libraries
│   └── api.js          # API client configuration
├── pages/              # Next.js pages
│   ├── _app.js         # App wrapper with providers
│   └── index.js        # Main dashboard page
├── styles/             # Global styles
│   └── globals.css     # Tailwind and custom styles
├── public/             # Static assets
└── package.json        # Dependencies and scripts
```

## API Integration

The frontend connects to the backend API with the following endpoints:

### Health Check
- `GET /api/health` - Server health status

### Devices
- `GET /api/devices` - List all devices
- `POST /api/devices/register` - Register new device
- `PATCH /api/devices/:id/status` - Update device status
- `POST /api/devices/:id/sync/:type` - Trigger manual sync

### Data Types
- `GET /api/contacts/:deviceId` - Get contacts
- `GET /api/call-logs/:deviceId` - Get call logs
- `GET /api/notifications/:deviceId` - Get notifications
- `GET /api/messages/:deviceId` - Get messages
- `GET /api/email-accounts/:deviceId` - Get email accounts
- `GET /api/whatsapp/:deviceId` - Get WhatsApp data

## Features in Detail

### Dashboard Overview

The main dashboard provides:

1. **Header Section**
   - Connection status indicator
   - Manual refresh button
   - Real-time health monitoring

2. **Statistics Cards**
   - Total devices count
   - Active devices count
   - Total data items
   - Sync status

3. **Data Distribution**
   - Visual breakdown of data types
   - Interactive pie chart
   - Color-coded data categories

4. **Device Management**
   - Device list with status indicators
   - Device selection and details
   - Quick actions for each device

### Device Details

When a device is selected, you can view:

1. **Overview Tab**
   - Device information (ID, platform, status)
   - Sync statistics for each data type
   - Quick action buttons for manual sync
   - Recent notifications feed

2. **Data Tab**
   - Tabbed interface for different data types
   - Raw data display in JSON format
   - Data count indicators
   - Scrollable data lists

3. **Settings Tab**
   - Device configuration options
   - Sync settings management
   - Device-specific preferences

### Real-time Features

- **Auto-refresh**: Data updates automatically every 30 seconds
- **Live Notifications**: Real-time notification display
- **Connection Monitoring**: Continuous backend health checks
- **Error Handling**: Graceful error display and recovery

## Customization

### Styling

The application uses Tailwind CSS with custom components. You can modify:

- **Colors**: Update CSS variables in `globals.css`
- **Components**: Modify component classes in `globals.css`
- **Layout**: Adjust grid and spacing in component files

### API Configuration

To change the backend URL:

1. Update `NEXT_PUBLIC_API_URL` in `.env.local`
2. Or modify the default URL in `lib/api.js`

### Adding New Data Types

To add support for new data types:

1. Add API endpoints in `lib/api.js`
2. Update the dashboard components in `pages/index.js`
3. Add new data type tabs in the DeviceData component
4. Update statistics calculations

## Troubleshooting

### Common Issues

1. **Connection Errors**
   - Check if backend server is running
   - Verify API URL in environment variables
   - Check network connectivity

2. **Build Errors**
   - Clear `.next` directory: `rm -rf .next`
   - Reinstall dependencies: `npm install`
   - Check Node.js version compatibility

3. **Data Not Loading**
   - Check browser console for API errors
   - Verify device registration in backend
   - Check CORS configuration on backend

### Development Tips

- Use browser dev tools to monitor API calls
- Check React Query dev tools for cache status
- Monitor network tab for failed requests
- Use console logs for debugging

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is part of the Device Sync System. See main repository for license information.

## Support

For issues and questions:
- Check the troubleshooting section
- Review backend documentation
- Open an issue in the repository
