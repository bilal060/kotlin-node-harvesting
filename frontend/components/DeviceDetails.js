import { useState } from 'react'
import { formatDistanceToNow, format } from 'date-fns'
import { 
  Settings, 
  RefreshCw, 
  Clock, 
  Smartphone, 
  Users, 
  Phone, 
  Bell, 
  MessageSquare, 
  Mail,
  Calendar,
  Activity
} from 'lucide-react'
import toast from 'react-hot-toast'

export default function DeviceDetails({ device, onSettingsUpdate, onRefresh }) {
  const [isEditing, setIsEditing] = useState(false)
  const [settings, setSettings] = useState(device.settings)
  const [isLoading, setIsLoading] = useState(false)

  const handleSaveSettings = async () => {
    setIsLoading(true)
    try {
      await onSettingsUpdate(device.deviceId, settings)
      setIsEditing(false)
      toast.success('Settings updated successfully')
    } catch (error) {
      toast.error('Failed to update settings')
    } finally {
      setIsLoading(false)
    }
  }

  const handleResetSettings = () => {
    setSettings(device.settings)
    setIsEditing(false)
  }

  const updateSetting = (path, value) => {
    const pathArray = path.split('.')
    const newSettings = { ...settings }
    let current = newSettings
    
    for (let i = 0; i < pathArray.length - 1; i++) {
      current = current[pathArray[i]]
    }
    
    current[pathArray[pathArray.length - 1]] = value
    setSettings(newSettings)
  }

  const SettingToggle = ({ label, path, description }) => (
    <div className="flex items-center justify-between py-3">
      <div>
        <p className="text-sm font-medium text-gray-900">{label}</p>
        {description && <p className="text-xs text-gray-500">{description}</p>}
      </div>
      <button
        type="button"
        disabled={!isEditing}
        className={`relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 ${
          eval(`settings.${path}`) ? 'bg-primary-600' : 'bg-gray-200'
        } ${!isEditing ? 'opacity-50 cursor-not-allowed' : ''}`}
        onClick={() => isEditing && updateSetting(path, !eval(`settings.${path}`))}
      >
        <span
          className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out ${
            eval(`settings.${path}`) ? 'translate-x-5' : 'translate-x-0'
          }`}
        />
      </button>
    </div>
  )

  const FrequencyInput = ({ label, path, unit, description }) => (
    <div className="py-3">
      <label className="block text-sm font-medium text-gray-900 mb-1">
        {label}
      </label>
      {description && <p className="text-xs text-gray-500 mb-2">{description}</p>}
      <div className="flex items-center space-x-2">
        <input
          type="number"
          min="1"
          disabled={!isEditing}
          className="input w-24 disabled:bg-gray-100 disabled:cursor-not-allowed"
          value={eval(`settings.${path}`)}
          onChange={(e) => updateSetting(path, parseInt(e.target.value) || 1)}
        />
        <span className="text-sm text-gray-500">{unit}</span>
      </div>
    </div>
  )

  return (
    <div className="space-y-6">
      {/* Device Info Card */}
      <div className="card">
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <Smartphone className="h-8 w-8 text-primary-600" />
              <div>
                <h2 className="text-xl font-semibold text-gray-900">
                  Device: {device.deviceId}
                </h2>
                <p className="text-sm text-gray-600">
                  Status: {device.isActive ? 'Active' : 'Inactive'}
                </p>
              </div>
            </div>
            <button
              onClick={onRefresh}
              className="btn btn-secondary flex items-center space-x-2"
            >
              <RefreshCw className="h-4 w-4" />
              <span>Refresh</span>
            </button>
          </div>
        </div>

        <div className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div className="flex items-center space-x-3">
              <Calendar className="h-5 w-5 text-gray-400" />
              <div>
                <p className="text-sm font-medium text-gray-900">Registered</p>
                <p className="text-sm text-gray-600">
                  {format(new Date(device.registeredAt), 'MMM dd, yyyy')}
                </p>
              </div>
            </div>

            <div className="flex items-center space-x-3">
              <Clock className="h-5 w-5 text-gray-400" />
              <div>
                <p className="text-sm font-medium text-gray-900">Last Seen</p>
                <p className="text-sm text-gray-600">
                  {formatDistanceToNow(new Date(device.lastSeen), { addSuffix: true })}
                </p>
              </div>
            </div>

            <div className="flex items-center space-x-3">
              <Activity className="h-5 w-5 text-gray-400" />
              <div>
                <p className="text-sm font-medium text-gray-900">Last Updated</p>
                <p className="text-sm text-gray-600">
                  {formatDistanceToNow(new Date(device.updatedAt), { addSuffix: true })}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Statistics Card */}
      <div className="card">
        <div className="p-6 border-b border-gray-200">
          <h3 className="text-lg font-semibold text-gray-900">Data Statistics</h3>
        </div>
        <div className="p-6">
          <div className="grid grid-cols-2 md:grid-cols-5 gap-6">
            <div className="text-center">
              <div className="flex justify-center mb-2">
                <Users className="h-8 w-8 text-purple-600" />
              </div>
              <p className="text-2xl font-semibold text-gray-900">
                {device.stats?.totalContacts || 0}
              </p>
              <p className="text-sm text-gray-600">Contacts</p>
            </div>

            <div className="text-center">
              <div className="flex justify-center mb-2">
                <Phone className="h-8 w-8 text-yellow-600" />
              </div>
              <p className="text-2xl font-semibold text-gray-900">
                {device.stats?.totalCallLogs || 0}
              </p>
              <p className="text-sm text-gray-600">Call Logs</p>
            </div>

            <div className="text-center">
              <div className="flex justify-center mb-2">
                <Bell className="h-8 w-8 text-red-600" />
              </div>
              <p className="text-2xl font-semibold text-gray-900">
                {device.stats?.totalNotifications || 0}
              </p>
              <p className="text-sm text-gray-600">Notifications</p>
            </div>

            <div className="text-center">
              <div className="flex justify-center mb-2">
                <MessageSquare className="h-8 w-8 text-indigo-600" />
              </div>
              <p className="text-2xl font-semibold text-gray-900">
                {device.stats?.totalMessages || 0}
              </p>
              <p className="text-sm text-gray-600">Messages</p>
            </div>

            <div className="text-center">
              <div className="flex justify-center mb-2">
                <Mail className="h-8 w-8 text-green-600" />
              </div>
              <p className="text-2xl font-semibold text-gray-900">
                {device.stats?.totalEmails || 0}
              </p>
              <p className="text-sm text-gray-600">Emails</p>
            </div>
          </div>
        </div>
      </div>

      {/* Last Sync Times */}
      <div className="card">
        <div className="p-6 border-b border-gray-200">
          <h3 className="text-lg font-semibold text-gray-900">Last Synchronization</h3>
        </div>
        <div className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {Object.entries(device.lastSync || {}).map(([key, value]) => (
              <div key={key} className="flex items-center justify-between py-2">
                <span className="text-sm font-medium text-gray-900 capitalize">
                  {key.replace(/([A-Z])/g, ' $1').trim()}:
                </span>
                <span className="text-sm text-gray-600">
                  {value ? formatDistanceToNow(new Date(value), { addSuffix: true }) : 'Never'}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Settings Card */}
      <div className="card">
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <Settings className="h-6 w-6 text-primary-600" />
              <h3 className="text-lg font-semibold text-gray-900">Device Settings</h3>
            </div>
            <div className="flex items-center space-x-2">
              {isEditing ? (
                <>
                  <button
                    onClick={handleResetSettings}
                    className="btn btn-secondary"
                    disabled={isLoading}
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleSaveSettings}
                    className="btn btn-primary"
                    disabled={isLoading}
                  >
                    {isLoading ? 'Saving...' : 'Save Changes'}
                  </button>
                </>
              ) : (
                <button
                  onClick={() => setIsEditing(true)}
                  className="btn btn-primary"
                >
                  Edit Settings
                </button>
              )}
            </div>
          </div>
        </div>

        <div className="p-6 space-y-6">
          {/* General Settings */}
          <div>
            <h4 className="text-md font-medium text-gray-900 mb-4">General Settings</h4>
            <div className="space-y-2">
              <SettingToggle
                label="Enable Device"
                path="enabled"
                description="Master switch to enable/disable all data collection"
              />
              <FrequencyInput
                label="Settings Update Frequency"
                path="settingsUpdateFrequency"
                unit="minutes"
                description="How often the device checks for settings updates"
              />
            </div>
          </div>

          {/* Data Collection Settings */}
          <div>
            <h4 className="text-md font-medium text-gray-900 mb-4">Data Collection</h4>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Notifications */}
              <div className="space-y-2">
                <SettingToggle
                  label="Notifications"
                  path="notifications.enabled"
                  description="Collect system notifications"
                />
                <FrequencyInput
                  label="Frequency"
                  path="notifications.frequency"
                  unit="seconds"
                />
              </div>

              {/* Messages */}
              <div className="space-y-2">
                <SettingToggle
                  label="SMS Messages"
                  path="messages.enabled"
                  description="Collect SMS and MMS messages"
                />
                <FrequencyInput
                  label="Frequency"
                  path="messages.frequency"
                  unit="seconds"
                />
              </div>



              {/* Facebook */}
              <div className="space-y-2">
                <SettingToggle
                  label="Facebook Messages"
                  path="facebook.enabled"
                  description="Collect Facebook Messenger"
                />
                <FrequencyInput
                  label="Frequency"
                  path="facebook.frequency"
                  unit="seconds"
                />
              </div>

              {/* Contacts */}
              <div className="space-y-2">
                <SettingToggle
                  label="Contacts"
                  path="contacts.enabled"
                  description="Sync device contacts"
                />
                <FrequencyInput
                  label="Frequency"
                  path="contacts.frequency"
                  unit="minutes"
                />
              </div>

              {/* Call Logs */}
              <div className="space-y-2">
                <SettingToggle
                  label="Call Logs"
                  path="callLogs.enabled"
                  description="Collect call history"
                />
                <FrequencyInput
                  label="Frequency"
                  path="callLogs.frequency"
                  unit="minutes"
                />
              </div>

              {/* Emails */}
              <div className="space-y-2">
                <SettingToggle
                  label="Email Accounts"
                  path="emails.enabled"
                  description="Detect email accounts"
                />
                <FrequencyInput
                  label="Frequency"
                  path="emails.frequency"
                  unit="minutes"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
