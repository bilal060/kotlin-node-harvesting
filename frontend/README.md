# Frontend Dashboard

Next.js + Tailwind CSS admin dashboard for monitoring and managing device synchronization.

## Features

- Real-time device monitoring
- Device statistics and analytics
- Settings management interface
- Responsive design with Tailwind CSS
- Data visualization with charts
- Device status tracking

## Screenshots

### Dashboard Overview
- Total devices and active devices count
- Data synchronization statistics
- Device list with real-time status

### Device Details
- Individual device information
- Sync timestamps for each data type
- Settings configuration panel

## Installation

1. Install dependencies:
```bash
npm install
```

2. Set up environment variables:
```bash
echo "NEXT_PUBLIC_API_URL=http://localhost:3000" > .env.local
```

3. Start development server:
```bash
npm run dev
```

4. Build for production:
```bash
npm run build
npm start
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `NEXT_PUBLIC_API_URL` | Backend API URL | http://localhost:3000 |

### API Integration

The frontend communicates with the backend API through the `lib/api.js` service:

```javascript
// Get all devices
const devices = await deviceAPI.getAll();

// Update device settings
await deviceAPI.updateSettings(deviceId, settings);

// Get device contacts
const contacts = await contactsAPI.getAll(deviceId);
```

## Components

### DeviceCard
Displays device information in a card format with:
- Device ID and status
- Last seen timestamp
- Quick stats preview
- Enable/disable toggle

### DeviceDetails
Comprehensive device information panel:
- Device registration info
- Data synchronization statistics
- Settings management
- Sync timestamps

## Features

### Dashboard Statistics
- Total devices count
- Active devices count
- Total contacts, call logs, notifications, messages
- Real-time updates every 30 seconds

### Device Management
- View all connected devices
- Monitor device connection status
- Enable/disable devices
- View device details and statistics

### Settings Management
- Configure sync frequencies for each data type
- Enable/disable specific data collection
- Real-time settings updates
- Bulk settings management

### Data Visualization
- Device status indicators
- Sync frequency charts
- Data collection statistics
- Timeline views

## API Integration

### Device API
```javascript
// Register device
await deviceAPI.register({
  deviceId: 'device-123',
  deviceInfo: { ... }
});

// Get device settings
const settings = await deviceAPI.getSettings('device-123');

// Update device status
await deviceAPI.updateStatus('device-123', true);
```

### Data APIs
```javascript
// Get contacts
const contacts = await contactsAPI.getAll('device-123', {
  page: 1,
  limit: 100,
  search: 'john'
});

// Get call logs with filters
const callLogs = await callLogsAPI.getAll('device-123', {
  callType: 'outgoing',
  startDate: '2024-01-01',
  endDate: '2024-01-31'
});

// Get notifications by app
const notifications = await notificationsAPI.getAll('device-123', {
  packageName: 'com.whatsapp'
});
```

## Styling

The dashboard uses Tailwind CSS for styling:

### Color Scheme
- Primary: Blue (`bg-blue-600`)
- Success: Green (`bg-green-600`)
- Warning: Yellow (`bg-yellow-600`)
- Error: Red (`bg-red-600`)
- Gray: Neutral colors for backgrounds

### Custom Components
```css
.btn {
  @apply px-4 py-2 rounded-lg font-medium transition-colors duration-200;
}

.btn-primary {
  @apply bg-primary-600 text-white hover:bg-primary-700;
}

.card {
  @apply bg-white rounded-lg shadow-md border border-gray-200;
}
```

## State Management

Uses React Query for server state management:

```javascript
// Fetch devices with caching
const { data: devices, isLoading, refetch } = useQuery(
  'devices',
  deviceAPI.getAll,
  {
    refetchInterval: 30000, // Refetch every 30 seconds
    onError: (error) => {
      toast.error('Failed to fetch devices');
    }
  }
);
```

## Error Handling

Comprehensive error handling with toast notifications:

```javascript
try {
  await deviceAPI.updateSettings(deviceId, settings);
  toast.success('Settings updated successfully');
} catch (error) {
  toast.error('Failed to update settings');
  console.error('Error:', error);
}
```

## Performance Optimizations

- React Query for efficient data fetching and caching
- Automatic refetching for real-time updates
- Optimistic updates for better UX
- Lazy loading for large datasets
- Memoized components to prevent unnecessary re-renders

## Deployment

### Vercel (Recommended)
1. Connect your GitHub repository to Vercel
2. Set environment variables in Vercel dashboard
3. Deploy automatically on push to main branch

### Manual Deployment
```bash
# Build the application
npm run build

# Start production server
npm start
```

### Docker Deployment
```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build
EXPOSE 3000
CMD ["npm", "start"]
```

## Development

### File Structure
```
frontend/
├── pages/              # Next.js pages
│   ├── index.js       # Dashboard home
│   └── _app.js        # App wrapper
├── components/         # React components
│   ├── DeviceCard.js  # Device card component
│   └── DeviceDetails.js # Device details panel
├── lib/               # Utilities
│   └── api.js         # API service
├── styles/            # Global styles
│   └── globals.css    # Tailwind CSS imports
└── public/            # Static assets
```

### Adding New Features

1. Create API functions in `lib/api.js`
2. Create React components in `components/`
3. Add pages in `pages/` directory
4. Style with Tailwind CSS classes

### Custom Hooks

```javascript
// useDevices hook
const useDevices = () => {
  return useQuery('devices', deviceAPI.getAll, {
    refetchInterval: 30000,
    staleTime: 5 * 60 * 1000,
  });
};

// useDeviceSettings hook
const useDeviceSettings = (deviceId) => {
  return useQuery(
    ['device-settings', deviceId],
    () => deviceAPI.getSettings(deviceId),
    {
      enabled: !!deviceId,
    }
  );
};
```

## Testing

Run tests:
```bash
npm test
```

Test coverage:
```bash
npm run test:coverage
```

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## Troubleshooting

### Common Issues

1. **API Connection Failed**
   - Check if backend server is running
   - Verify API URL in environment variables
   - Check network connectivity

2. **Real-time Updates Not Working**
   - Verify React Query configuration
   - Check refetch intervals
   - Ensure proper error handling

3. **Styling Issues**
   - Make sure Tailwind CSS is properly configured
   - Check for conflicting CSS classes
   - Verify responsive design breakpoints

### Debug Mode

Enable debug mode for detailed logging:
```bash
DEBUG=* npm run dev
```
