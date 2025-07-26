import { formatDistanceToNow } from 'date-fns'
import { Smartphone, Wifi, WifiOff, Settings } from 'lucide-react'

export default function DeviceCard({ device, isSelected, onClick, onStatusChange }) {
  const getStatusColor = (isActive, lastSeen) => {
    if (!isActive) return 'bg-red-500'
    
    const lastSeenDate = new Date(lastSeen)
    const now = new Date()
    const diffMinutes = (now - lastSeenDate) / (1000 * 60)
    
    if (diffMinutes < 5) return 'bg-green-500'
    if (diffMinutes < 30) return 'bg-yellow-500'
    return 'bg-red-500'
  }

  const getStatusText = (isActive, lastSeen) => {
    if (!isActive) return 'Inactive'
    
    const lastSeenDate = new Date(lastSeen)
    const now = new Date()
    const diffMinutes = (now - lastSeenDate) / (1000 * 60)
    
    if (diffMinutes < 5) return 'Online'
    if (diffMinutes < 30) return 'Recently Active'
    return 'Offline'
  }

  return (
    <div
      className={`p-4 cursor-pointer transition-colors duration-200 hover:bg-gray-50 ${
        isSelected ? 'bg-blue-50 border-r-4 border-blue-500' : ''
      }`}
      onClick={onClick}
    >
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="flex-shrink-0">
            <div className="relative">
              <Smartphone className="h-8 w-8 text-gray-400" />
              <div
                className={`absolute -bottom-1 -right-1 w-3 h-3 rounded-full border-2 border-white ${getStatusColor(
                  device.isActive,
                  device.lastSeen
                )}`}
              />
            </div>
          </div>
          
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-gray-900 truncate">
              Device: {device.deviceId}
            </p>
            <div className="flex items-center space-x-2 mt-1">
              <span className={`inline-flex items-center text-xs ${
                device.isActive ? 'text-green-600' : 'text-red-600'
              }`}>
                {device.isActive ? (
                  <Wifi className="h-3 w-3 mr-1" />
                ) : (
                  <WifiOff className="h-3 w-3 mr-1" />
                )}
                {getStatusText(device.isActive, device.lastSeen)}
              </span>
            </div>
            <p className="text-xs text-gray-500 mt-1">
              Last seen: {formatDistanceToNow(new Date(device.lastSeen), { addSuffix: true })}
            </p>
          </div>
        </div>

        <div className="flex items-center space-x-2">
          <button
            onClick={(e) => {
              e.stopPropagation()
              onStatusChange(device.deviceId, !device.isActive)
            }}
            className={`inline-flex items-center px-2 py-1 rounded text-xs font-medium ${
              device.isActive
                ? 'bg-red-100 text-red-800 hover:bg-red-200'
                : 'bg-green-100 text-green-800 hover:bg-green-200'
            }`}
          >
            {device.isActive ? 'Disable' : 'Enable'}
          </button>
        </div>
      </div>

      {/* Stats Preview */}
      <div className="mt-3 grid grid-cols-4 gap-2 text-xs text-gray-600">
        <div className="text-center">
          <p className="font-medium text-gray-900">{device.stats?.totalContacts || 0}</p>
          <p>Contacts</p>
        </div>
        <div className="text-center">
          <p className="font-medium text-gray-900">{device.stats?.totalCallLogs || 0}</p>
          <p>Calls</p>
        </div>
        <div className="text-center">
          <p className="font-medium text-gray-900">{device.stats?.totalNotifications || 0}</p>
          <p>Notifications</p>
        </div>
        <div className="text-center">
          <p className="font-medium text-gray-900">{device.stats?.totalMessages || 0}</p>
          <p>Messages</p>
        </div>
      </div>
    </div>
  )
}
