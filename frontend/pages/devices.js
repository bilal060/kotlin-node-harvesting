import { useState, useEffect } from 'react'
import { useQuery } from 'react-query'
import Link from 'next/link'
import { deviceAPI, healthAPI } from '../lib/api'
import { 
  Smartphone, 
  Users, 
  Phone, 
  Bell, 
  MessageSquare, 
  Mail, 
  Activity, 
  Wifi, 
  WifiOff, 
  RefreshCw, 
  Database, 
  Settings, 
  Calendar,
  Clock,
  Info,
  AlertCircle,
  CheckCircle,
  XCircle,
  Eye,
  EyeOff,
  Download,
  Trash2,
  Edit,
  Power,
  PowerOff,
  Search
} from 'lucide-react'
import toast from 'react-hot-toast'

export default function Devices() {
  const [selectedDevice, setSelectedDevice] = useState(null)
  const [showSensitiveData, setShowSensitiveData] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')
  const [filterStatus, setFilterStatus] = useState('all') // all, active, inactive
  const [itemsPerPage, setItemsPerPage] = useState(100)
  const [currentPage, setCurrentPage] = useState(1)
  const [dateFilter, setDateFilter] = useState('all') // all, today, yesterday, last7days, last30days

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

  // Devices data with server-side filtering and pagination
  const { data: devices, isLoading, error, refetch } = useQuery(
    ['devices', searchTerm, dateFilter, filterStatus, currentPage, itemsPerPage],
    () => deviceAPI.getAll({
      page: currentPage,
      limit: itemsPerPage,
      search: searchTerm,
      dateFilter,
      filterStatus
    }),
    {
      refetchInterval: 30000,
      onError: (error) => {
        toast.error('Failed to fetch devices')
        console.error('Error fetching devices:', error)
      }
    }
  )

  const getCurrentDevices = () => {
    return devices?.data || []
  }

  const getCurrentPagination = () => {
    return devices?.pagination || { current: 1, pages: 1, total: 0, limit: itemsPerPage }
  }

  const handleDeviceStatusChange = async (deviceId, isActive) => {
    try {
      await deviceAPI.updateStatus(deviceId, { isActive })
      toast.success(`Device ${isActive ? 'activated' : 'deactivated'} successfully`)
      refetch()
    } catch (error) {
      toast.error('Failed to update device status')
      console.error('Error updating device status:', error)
    }
  }

  const handleDeleteDevice = async (deviceId) => {
    if (!confirm('Are you sure you want to delete this device? This action cannot be undone.')) {
      return
    }

    try {
      await deviceAPI.delete(deviceId)
      toast.success('Device deleted successfully')
      if (selectedDevice?.deviceId === deviceId) {
        setSelectedDevice(null)
      }
      refetch()
    } catch (error) {
      toast.error('Failed to delete device')
      console.error('Error deleting device:', error)
    }
  }

  const exportDeviceData = (device) => {
    const deviceData = {
      deviceId: device.deviceId,
      deviceName: device.deviceName,
      model: device.model,
      manufacturer: device.manufacturer,
      isActive: device.isActive,
      lastSync: device.lastSync,
      createdAt: device.createdAt,
      updatedAt: device.updatedAt,
      stats: device.stats
    }

    const csvContent = convertToCSV([deviceData])
    const blob = new Blob([csvContent], { type: 'text/csv' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `device_${device.deviceId}_${new Date().toISOString().split('T')[0]}.csv`
    a.click()
    window.URL.revokeObjectURL(url)
    toast.success(`Exported device data for ${device.deviceName || device.deviceId}`)
  }

  const convertToCSV = (data) => {
    if (!data.length) return ''
    
    const headers = Object.keys(data[0])
    const csvRows = [headers.join(',')]
    
    data.forEach(item => {
      const values = headers.map(header => {
        const value = item[header]
        if (typeof value === 'object') {
          return `"${JSON.stringify(value).replace(/"/g, '""')}"`
        }
        return typeof value === 'string' ? `"${value.replace(/"/g, '""')}"` : value
      })
      csvRows.push(values.join(','))
    })
    
    return csvRows.join('\n')
  }

  const formatDate = (timestamp) => {
    if (!timestamp) return 'Never'
    return new Date(timestamp).toLocaleString()
  }

  const getStatusColor = (isActive) => {
    return isActive ? 'text-green-600' : 'text-red-600'
  }

  const getStatusIcon = (isActive) => {
    return isActive ? CheckCircle : XCircle
  }

  const getLastSyncStatus = (lastSync) => {
    if (!lastSync) return { status: 'Never', color: 'text-red-600' }
    
    const now = new Date()
    const lastSyncDate = new Date(lastSync)
    const diffHours = (now - lastSyncDate) / (1000 * 60 * 60)
    
    if (diffHours < 1) return { status: 'Recent', color: 'text-green-600' }
    if (diffHours < 24) return { status: 'Today', color: 'text-yellow-600' }
    if (diffHours < 168) return { status: 'This week', color: 'text-orange-600' }
    return { status: 'Old', color: 'text-red-600' }
  }

  const renderDeviceCard = (device) => {
    const StatusIcon = getStatusIcon(device.isActive)
    const lastSyncInfo = getLastSyncStatus(device.lastSync)

    return (
      <div
        key={device.deviceId}
        className={`bg-white rounded-lg shadow-sm border-2 cursor-pointer transition-all hover:shadow-md ${
          selectedDevice?.deviceId === device.deviceId
            ? 'border-blue-500 bg-blue-50'
            : 'border-gray-200 hover:border-gray-300'
        }`}
        onClick={() => setSelectedDevice(device)}
      >
        <div className="p-6">
          <div className="flex items-start justify-between">
            <div className="flex items-center space-x-3">
              <div className="p-2 bg-blue-100 rounded-lg">
                <Smartphone className="h-6 w-6 text-blue-600" />
              </div>
              <div>
                <h3 className="text-lg font-semibold text-gray-900">
                  {device.deviceName || 'Unknown Device'}
                </h3>
                <p className="text-sm text-gray-500">{device.deviceId}</p>
                <p className="text-sm text-gray-400">
                  {device.manufacturer} {device.model}
                </p>
              </div>
            </div>
            <div className="flex items-center space-x-2">
              <StatusIcon className={`h-5 w-5 ${getStatusColor(device.isActive)}`} />
              <span className={`text-sm font-medium ${getStatusColor(device.isActive)}`}>
                {device.isActive ? 'Active' : 'Inactive'}
              </span>
            </div>
          </div>

          <div className="mt-4 grid grid-cols-2 gap-4">
            <div className="text-center">
              <p className="text-xs text-gray-500">Last Sync</p>
              <p className={`text-sm font-medium ${lastSyncInfo.color}`}>
                {lastSyncInfo.status}
              </p>
            </div>
            <div className="text-center">
              <p className="text-xs text-gray-500">Total Data</p>
              <p className="text-sm font-medium text-gray-900">
                {Object.values(device.stats || {}).reduce((sum, val) => sum + (val || 0), 0)}
              </p>
            </div>
          </div>

          <div className="mt-4 flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <Calendar className="h-4 w-4 text-gray-400" />
              <span className="text-xs text-gray-500">
                Created: {formatDate(device.createdAt)}
              </span>
            </div>
            <div className="flex items-center space-x-1">
              <button
                onClick={(e) => {
                  e.stopPropagation()
                  handleDeviceStatusChange(device.deviceId, !device.isActive)
                }}
                className="p-1 text-gray-400 hover:text-gray-600"
                title={device.isActive ? 'Deactivate' : 'Activate'}
              >
                {device.isActive ? <PowerOff className="h-4 w-4" /> : <Power className="h-4 w-4" />}
              </button>
              <button
                onClick={(e) => {
                  e.stopPropagation()
                  exportDeviceData(device)
                }}
                className="p-1 text-gray-400 hover:text-gray-600"
                title="Export Data"
              >
                <Download className="h-4 w-4" />
              </button>
              <button
                onClick={(e) => {
                  e.stopPropagation()
                  handleDeleteDevice(device.deviceId)
                }}
                className="p-1 text-gray-400 hover:text-red-600"
                title="Delete Device"
              >
                <Trash2 className="h-4 w-4" />
              </button>
            </div>
          </div>
        </div>
      </div>
    )
  }

  const renderDeviceDetails = (device) => {
    const StatusIcon = getStatusIcon(device.isActive)
    const lastSyncInfo = getLastSyncStatus(device.lastSync)

    return (
      <div className="bg-white rounded-lg shadow-sm border">
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-semibold text-gray-900">Device Details</h2>
            <div className="flex items-center space-x-2">
              <button
                onClick={() => setShowSensitiveData(!showSensitiveData)}
                className="flex items-center px-3 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
              >
                {showSensitiveData ? <EyeOff className="h-4 w-4 mr-2" /> : <Eye className="h-4 w-4 mr-2" />}
                {showSensitiveData ? 'Hide' : 'Show'} Sensitive Data
              </button>
              <button
                onClick={() => setSelectedDevice(null)}
                className="p-2 text-gray-400 hover:text-gray-600"
              >
                <XCircle className="h-5 w-5" />
              </button>
            </div>
          </div>
        </div>

        <div className="p-6">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Basic Information */}
            <div>
              <h3 className="text-lg font-medium text-gray-900 mb-4">Basic Information</h3>
              <div className="space-y-4">
                <div>
                  <label className="text-sm font-medium text-gray-500">Device Name</label>
                  <p className="text-sm text-gray-900">{device.deviceName || 'Not set'}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Device ID</label>
                  <p className="text-sm text-gray-900 font-mono">{device.deviceId}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Manufacturer</label>
                  <p className="text-sm text-gray-900">{device.manufacturer || 'Unknown'}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Model</label>
                  <p className="text-sm text-gray-900">{device.model || 'Unknown'}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Status</label>
                  <div className="flex items-center space-x-2">
                    <StatusIcon className={`h-5 w-5 ${getStatusColor(device.isActive)}`} />
                    <span className={`text-sm font-medium ${getStatusColor(device.isActive)}`}>
                      {device.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Sync Information */}
            <div>
              <h3 className="text-lg font-medium text-gray-900 mb-4">Sync Information</h3>
              <div className="space-y-4">
                <div>
                  <label className="text-sm font-medium text-gray-500">Last Sync</label>
                  <div className="flex items-center space-x-2">
                    <Clock className="h-4 w-4 text-gray-400" />
                    <p className={`text-sm font-medium ${lastSyncInfo.color}`}>
                      {formatDate(device.lastSync)}
                    </p>
                  </div>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Created</label>
                  <p className="text-sm text-gray-900">{formatDate(device.createdAt)}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Last Updated</label>
                  <p className="text-sm text-gray-900">{formatDate(device.updatedAt)}</p>
                </div>
              </div>
            </div>
          </div>

          {/* Data Statistics */}
          <div className="mt-8">
            <h3 className="text-lg font-medium text-gray-900 mb-4">Data Statistics</h3>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
              <div className="bg-gray-50 rounded-lg p-4 text-center">
                <Users className="h-6 w-6 text-blue-600 mx-auto mb-2" />
                <p className="text-2xl font-semibold text-gray-900">{device.stats?.totalContacts || 0}</p>
                <p className="text-xs text-gray-500">Contacts</p>
              </div>
              <div className="bg-gray-50 rounded-lg p-4 text-center">
                <Phone className="h-6 w-6 text-green-600 mx-auto mb-2" />
                <p className="text-2xl font-semibold text-gray-900">{device.stats?.totalCallLogs || 0}</p>
                <p className="text-xs text-gray-500">Call Logs</p>
              </div>
              <div className="bg-gray-50 rounded-lg p-4 text-center">
                <MessageSquare className="h-6 w-6 text-purple-600 mx-auto mb-2" />
                <p className="text-2xl font-semibold text-gray-900">{device.stats?.totalMessages || 0}</p>
                <p className="text-xs text-gray-500">Messages</p>
              </div>
              <div className="bg-gray-50 rounded-lg p-4 text-center">
                <Bell className="h-6 w-6 text-red-600 mx-auto mb-2" />
                <p className="text-2xl font-semibold text-gray-900">{device.stats?.totalNotifications || 0}</p>
                <p className="text-xs text-gray-500">Notifications</p>
              </div>
              <div className="bg-gray-50 rounded-lg p-4 text-center">
                <Mail className="h-6 w-6 text-orange-600 mx-auto mb-2" />
                <p className="text-2xl font-semibold text-gray-900">{device.stats?.totalEmails || 0}</p>
                <p className="text-xs text-gray-500">Emails</p>
              </div>
              <div className="bg-gray-50 rounded-lg p-4 text-center">
                <Database className="h-6 w-6 text-indigo-600 mx-auto mb-2" />
                <p className="text-2xl font-semibold text-gray-900">
                  {Object.values(device.stats || {}).reduce((sum, val) => sum + (val || 0), 0)}
                </p>
                <p className="text-xs text-gray-500">Total</p>
              </div>
            </div>
          </div>

          {/* Actions */}
          <div className="mt-8 flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <button
                onClick={() => handleDeviceStatusChange(device.deviceId, !device.isActive)}
                className={`flex items-center px-4 py-2 rounded-md text-sm font-medium ${
                  device.isActive
                    ? 'bg-red-600 text-white hover:bg-red-700'
                    : 'bg-green-600 text-white hover:bg-green-700'
                }`}
              >
                {device.isActive ? <PowerOff className="h-4 w-4 mr-2" /> : <Power className="h-4 w-4 mr-2" />}
                {device.isActive ? 'Deactivate' : 'Activate'}
              </button>
              <a
                href={`/data-viewer?device=${device.deviceId}`}
                className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm font-medium"
              >
                <Database className="h-4 w-4 mr-2" />
                View Data
              </a>
            </div>
            <button
              onClick={() => exportDeviceData(device)}
              className="flex items-center px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              <Download className="h-4 w-4 mr-2" />
              Export Data
            </button>
          </div>
        </div>
      </div>
    )
  }

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <RefreshCw className="mx-auto h-8 w-8 text-gray-400 animate-spin" />
          <p className="mt-2 text-sm text-gray-500">Loading devices...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <AlertCircle className="mx-auto h-12 w-12 text-red-400" />
          <h1 className="mt-4 text-lg font-medium text-gray-900">Error Loading Devices</h1>
          <p className="mt-2 text-sm text-gray-500">Failed to connect to the server</p>
          <button
            onClick={() => refetch()}
            className="mt-4 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
          >
            Retry
          </button>
        </div>
      </div>
    )
  }

  const currentDevices = getCurrentDevices()
  const currentPagination = getCurrentPagination()

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b">
        <div className="container mx-auto px-4 py-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Device Management</h1>
              <p className="text-gray-600 mt-1">Manage and monitor all connected devices</p>
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
                href="/"
                className="flex items-center space-x-2 bg-gray-600 text-white px-4 py-2 rounded-lg hover:bg-gray-700"
              >
                <Activity className="h-4 w-4" />
                <span>Dashboard</span>
              </Link>
              <button
                onClick={() => refetch()}
                className="flex items-center space-x-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
              >
                <RefreshCw className="h-4 w-4" />
                <span>Refresh</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        {/* Filters and Search */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div className="flex items-center space-x-4">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search devices by name, ID, model, manufacturer..."
                  value={searchTerm}
                  onChange={(e) => {
                    setSearchTerm(e.target.value)
                    setCurrentPage(1)
                  }}
                  className="pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
              <select
                value={filterStatus}
                onChange={(e) => {
                  setFilterStatus(e.target.value)
                  setCurrentPage(1)
                }}
                className="border border-gray-300 rounded-md px-3 py-2 text-sm"
              >
                <option value="all">All Status</option>
                <option value="active">Active Only</option>
                <option value="inactive">Inactive Only</option>
              </select>
              <select
                value={dateFilter}
                onChange={(e) => {
                  setDateFilter(e.target.value)
                  setCurrentPage(1)
                }}
                className="border border-gray-300 rounded-md px-3 py-2 text-sm"
              >
                <option value="all">All Time</option>
                <option value="today">Today</option>
                <option value="yesterday">Yesterday</option>
                <option value="last7days">Last 7 Days</option>
                <option value="last30days">Last 30 Days</option>
              </select>
              <select
                value={itemsPerPage}
                onChange={(e) => {
                  setItemsPerPage(Number(e.target.value))
                  setCurrentPage(1)
                }}
                className="border border-gray-300 rounded-md px-3 py-2 text-sm"
              >
                <option value={50}>50 per page</option>
                <option value={100}>100 per page</option>
                <option value={200}>200 per page</option>
                <option value={500}>500 per page</option>
              </select>
            </div>
            <div className="text-sm text-gray-500">
              {currentPagination.total} devices
            </div>
          </div>
        </div>

        {/* Content */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Device List */}
          <div className={`${selectedDevice ? 'lg:col-span-1' : 'lg:col-span-3'}`}>
            <div className="bg-white rounded-lg shadow-sm p-6">
              <h2 className="text-lg font-medium text-gray-900 mb-4">All Devices</h2>
              {currentDevices.length === 0 ? (
                <div className="text-center py-8">
                  <Smartphone className="mx-auto h-12 w-12 text-gray-400" />
                  <h3 className="mt-2 text-sm font-medium text-gray-900">No devices found</h3>
                  <p className="mt-1 text-sm text-gray-500">
                    {searchTerm || filterStatus !== 'all' || dateFilter !== 'all' ? 'Try adjusting your filters.' : 'No devices are currently registered.'}
                  </p>
                </div>
              ) : (
                <div className="space-y-4">
                  {currentDevices.map(renderDeviceCard)}
                </div>
              )}
            </div>

            {/* Pagination */}
            {currentPagination.pages > 1 && (
              <div className="mt-6 flex items-center justify-between">
                <div className="text-sm text-gray-700">
                  Showing {((currentPagination.current - 1) * currentPagination.limit) + 1} to {Math.min(currentPagination.current * currentPagination.limit, currentPagination.total)} of {currentPagination.total} results
                </div>
                <div className="flex space-x-2">
                  <button
                    onClick={() => setCurrentPage(Math.max(1, currentPagination.current - 1))}
                    disabled={currentPagination.current === 1}
                    className="px-3 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    Previous
                  </button>
                  <span className="px-3 py-2 text-sm text-gray-700">
                    Page {currentPagination.current} of {currentPagination.pages}
                  </span>
                  <button
                    onClick={() => setCurrentPage(Math.min(currentPagination.pages, currentPagination.current + 1))}
                    disabled={currentPagination.current === currentPagination.pages}
                    className="px-3 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    Next
                  </button>
                </div>
              </div>
            )}
          </div>

          {/* Device Details */}
          {selectedDevice && (
            <div className="lg:col-span-2">
              {renderDeviceDetails(selectedDevice)}
            </div>
          )}
        </div>
      </div>
    </div>
  )
} 