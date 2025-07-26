import { useState, useEffect } from 'react'
import { useQuery } from 'react-query'
import { deviceAPI } from '../lib/api'
import DeviceCard from '../components/DeviceCard'
import DeviceDetails from '../components/DeviceDetails'
import { Smartphone, Users, Phone, Bell, MessageSquare, Mail, Activity } from 'lucide-react'
import toast from 'react-hot-toast'

export default function Home() {
  const [selectedDevice, setSelectedDevice] = useState(null)
  const [stats, setStats] = useState({
    totalDevices: 0,
    activeDevices: 0,
    totalContacts: 0,
    totalCallLogs: 0,
    totalNotifications: 0,
    totalMessages: 0
  })

  const { data: devices, isLoading, error, refetch } = useQuery(
    'devices',
    deviceAPI.getAll,
    {
      refetchInterval: 30000, // Refetch every 30 seconds
      onSuccess: (response) => {
        const devicesData = response.data
        const totalStats = devicesData.reduce((acc, device) => {
          return {
            totalDevices: acc.totalDevices + 1,
            activeDevices: acc.activeDevices + (device.isActive ? 1 : 0),
            totalContacts: acc.totalContacts + (device.stats?.totalContacts || 0),
            totalCallLogs: acc.totalCallLogs + (device.stats?.totalCallLogs || 0),
            totalNotifications: acc.totalNotifications + (device.stats?.totalNotifications || 0),
            totalMessages: acc.totalMessages + (device.stats?.totalMessages || 0)
          }
        }, {
          totalDevices: 0,
          activeDevices: 0,
          totalContacts: 0,
          totalCallLogs: 0,
          totalNotifications: 0,
          totalMessages: 0
        })
        setStats(totalStats)
      },
      onError: (error) => {
        toast.error('Failed to fetch devices')
        console.error('Error fetching devices:', error)
      }
    }
  )

  const handleDeviceStatusChange = async (deviceId, isActive) => {
    try {
      await deviceAPI.updateStatus(deviceId, isActive)
      toast.success(`Device ${isActive ? 'activated' : 'deactivated'} successfully`)
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

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary-600"></div>
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
            className="btn btn-primary"
          >
            Retry
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Device Synchronization Dashboard</h1>
        <p className="text-gray-600">Monitor and manage device data synchronization</p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-6 mb-8">
        <div className="card p-6">
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

        <div className="card p-6">
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

        <div className="card p-6">
          <div className="flex items-center">
            <div className="p-2 bg-purple-100 rounded-lg">
              <Users className="h-6 w-6 text-purple-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Contacts</p>
              <p className="text-2xl font-semibold text-gray-900">{stats.totalContacts.toLocaleString()}</p>
            </div>
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center">
            <div className="p-2 bg-yellow-100 rounded-lg">
              <Phone className="h-6 w-6 text-yellow-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Call Logs</p>
              <p className="text-2xl font-semibold text-gray-900">{stats.totalCallLogs.toLocaleString()}</p>
            </div>
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center">
            <div className="p-2 bg-red-100 rounded-lg">
              <Bell className="h-6 w-6 text-red-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Notifications</p>
              <p className="text-2xl font-semibold text-gray-900">{stats.totalNotifications.toLocaleString()}</p>
            </div>
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center">
            <div className="p-2 bg-indigo-100 rounded-lg">
              <MessageSquare className="h-6 w-6 text-indigo-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Messages</p>
              <p className="text-2xl font-semibold text-gray-900">{stats.totalMessages.toLocaleString()}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Devices List */}
        <div className="lg:col-span-1">
          <div className="card">
            <div className="p-6 border-b border-gray-200">
              <h2 className="text-xl font-semibold text-gray-900">Connected Devices</h2>
              <p className="text-sm text-gray-600">Click on a device to view details</p>
            </div>
            <div className="divide-y divide-gray-200">
              {devices?.data?.length === 0 ? (
                <div className="p-6 text-center text-gray-500">
                  <Smartphone className="h-12 w-12 mx-auto mb-4 text-gray-300" />
                  <p>No devices connected</p>
                </div>
              ) : (
                devices?.data?.map((device) => (
                  <DeviceCard
                    key={device._id}
                    device={device}
                    isSelected={selectedDevice?._id === device._id}
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
            <DeviceDetails
              device={selectedDevice}
              onSettingsUpdate={handleSettingsUpdate}
              onRefresh={() => refetch()}
            />
          ) : (
            <div className="card p-12 text-center">
              <Smartphone className="h-16 w-16 mx-auto mb-4 text-gray-300" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">Select a Device</h3>
              <p className="text-gray-600">Choose a device from the list to view its details and manage settings.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
