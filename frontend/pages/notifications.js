import { useState, useEffect } from 'react';
import { useRouter } from 'next/router';
import Head from 'next/head';
import api from '../lib/api';

export default function Notifications() {
    const router = useRouter();
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [filters, setFilters] = useState({
        search: '',
        deviceId: '',
        packageName: '',
        dateFilter: 'all'
    });
    const [pagination, setPagination] = useState({
        page: 1,
        limit: 50,
        total: 0,
        totalPages: 0
    });
    const [devices, setDevices] = useState([]);
    const [selectedNotification, setSelectedNotification] = useState(null);

    useEffect(() => {
        fetchDevices();
        fetchNotifications();
    }, [pagination.page, pagination.limit, filters]);

    const fetchDevices = async () => {
        try {
            const response = await api.get('/devices');
            if (response.data.success) {
                setDevices(response.data.devices);
            }
        } catch (error) {
            console.error('Error fetching devices:', error);
        }
    };

    const fetchNotifications = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                page: pagination.page,
                limit: pagination.limit,
                ...filters
            });

            const response = await api.get(`/notifications?${params}`);
            if (response.data.success) {
                setNotifications(response.data.notifications);
                setPagination(prev => ({
                    ...prev,
                    total: response.data.total,
                    totalPages: Math.ceil(response.data.total / pagination.limit)
                }));
            }
        } catch (error) {
            setError('Failed to fetch notifications');
            console.error('Error fetching notifications:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleFilterChange = (key, value) => {
        setFilters(prev => ({ ...prev, [key]: value }));
        setPagination(prev => ({ ...prev, page: 1 }));
    };

    const handlePageChange = (newPage) => {
        setPagination(prev => ({ ...prev, page: newPage }));
    };

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleString();
    };

    const truncateText = (text, maxLength = 100) => {
        if (!text) return 'N/A';
        return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    };

    const getAppIcon = (packageName) => {
        // You can add app icon logic here
        return `https://via.placeholder.com/40/4F46E5/FFFFFF?text=${packageName.charAt(0).toUpperCase()}`;
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <Head>
                <title>Notifications - DeviceSync Admin</title>
            </Head>

            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Header */}
                <div className="mb-8">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900">Notifications</h1>
                            <p className="mt-2 text-gray-600">
                                View and manage all synced notifications from devices
                            </p>
                        </div>
                        <button
                            onClick={() => router.back()}
                            className="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
                        >
                            Back
                        </button>
                    </div>
                </div>

                {/* Filters */}
                <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
                    <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Search
                            </label>
                            <input
                                type="text"
                                placeholder="Search by title, text, app..."
                                value={filters.search}
                                onChange={(e) => handleFilterChange('search', e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Device
                            </label>
                            <select
                                value={filters.deviceId}
                                onChange={(e) => handleFilterChange('deviceId', e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                <option value="">All Devices</option>
                                {devices.map(device => (
                                    <option key={device.deviceId} value={device.deviceId}>
                                        {device.deviceName || device.deviceId}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                App Package
                            </label>
                            <input
                                type="text"
                                placeholder="e.g., com.whatsapp"
                                value={filters.packageName}
                                onChange={(e) => handleFilterChange('packageName', e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Date Filter
                            </label>
                            <select
                                value={filters.dateFilter}
                                onChange={(e) => handleFilterChange('dateFilter', e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                <option value="all">All Time</option>
                                <option value="today">Today</option>
                                <option value="week">Last 7 Days</option>
                                <option value="month">Last 30 Days</option>
                            </select>
                        </div>
                        <div className="flex items-end">
                            <button
                                onClick={fetchNotifications}
                                className="w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
                            >
                                Apply Filters
                            </button>
                        </div>
                    </div>
                </div>

                {/* Stats */}
                <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
                    <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                        <div className="text-center">
                            <div className="text-2xl font-bold text-blue-600">{pagination.total}</div>
                            <div className="text-sm text-gray-600">Total Notifications</div>
                        </div>
                        <div className="text-center">
                            <div className="text-2xl font-bold text-green-600">{devices.length}</div>
                            <div className="text-sm text-gray-600">Active Devices</div>
                        </div>
                        <div className="text-center">
                            <div className="text-2xl font-bold text-purple-600">{pagination.totalPages}</div>
                            <div className="text-sm text-gray-600">Total Pages</div>
                        </div>
                        <div className="text-center">
                            <div className="text-2xl font-bold text-orange-600">{pagination.page}</div>
                            <div className="text-sm text-gray-600">Current Page</div>
                        </div>
                    </div>
                </div>

                {/* Notifications List */}
                <div className="bg-white rounded-lg shadow-sm overflow-hidden">
                    {loading ? (
                        <div className="p-8 text-center">
                            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
                            <p className="mt-4 text-gray-600">Loading notifications...</p>
                        </div>
                    ) : error ? (
                        <div className="p-8 text-center">
                            <p className="text-red-600">{error}</p>
                            <button
                                onClick={fetchNotifications}
                                className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                            >
                                Retry
                            </button>
                        </div>
                    ) : (
                        <>
                            <div className="overflow-x-auto">
                                <table className="min-w-full divide-y divide-gray-200">
                                    <thead className="bg-gray-50">
                                        <tr>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                App
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Title
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Message
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Device
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Time
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Actions
                                            </th>
                                        </tr>
                                    </thead>
                                    <tbody className="bg-white divide-y divide-gray-200">
                                        {notifications.map((notification) => (
                                            <tr key={notification._id} className="hover:bg-gray-50">
                                                <td className="px-6 py-4 whitespace-nowrap">
                                                    <div className="flex items-center">
                                                        <div className="flex-shrink-0 h-10 w-10">
                                                            <img
                                                                className="h-10 w-10 rounded-lg"
                                                                src={getAppIcon(notification.packageName)}
                                                                alt={notification.appName}
                                                            />
                                                        </div>
                                                        <div className="ml-4">
                                                            <div className="text-sm font-medium text-gray-900">
                                                                {notification.appName}
                                                            </div>
                                                            <div className="text-sm text-gray-500">
                                                                {notification.packageName}
                                                            </div>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td className="px-6 py-4">
                                                    <div className="text-sm font-medium text-gray-900">
                                                        {truncateText(notification.title, 50)}
                                                    </div>
                                                </td>
                                                <td className="px-6 py-4">
                                                    <div className="text-sm text-gray-900">
                                                        {truncateText(notification.text, 80)}
                                                    </div>
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap">
                                                    <div className="text-sm text-gray-900">{notification.deviceId}</div>
                                                    <div className="text-sm text-gray-500">{notification.user_internal_code}</div>
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                                    {formatDate(notification.timestamp)}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                                    <button
                                                        onClick={() => setSelectedNotification(notification)}
                                                        className="text-blue-600 hover:text-blue-900"
                                                    >
                                                        View Details
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>

                            {/* Pagination */}
                            {pagination.totalPages > 1 && (
                                <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
                                    <div className="flex-1 flex justify-between sm:hidden">
                                        <button
                                            onClick={() => handlePageChange(pagination.page - 1)}
                                            disabled={pagination.page === 1}
                                            className="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
                                        >
                                            Previous
                                        </button>
                                        <button
                                            onClick={() => handlePageChange(pagination.page + 1)}
                                            disabled={pagination.page === pagination.totalPages}
                                            className="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
                                        >
                                            Next
                                        </button>
                                    </div>
                                    <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
                                        <div>
                                            <p className="text-sm text-gray-700">
                                                Showing{' '}
                                                <span className="font-medium">
                                                    {((pagination.page - 1) * pagination.limit) + 1}
                                                </span>{' '}
                                                to{' '}
                                                <span className="font-medium">
                                                    {Math.min(pagination.page * pagination.limit, pagination.total)}
                                                </span>{' '}
                                                of{' '}
                                                <span className="font-medium">{pagination.total}</span> results
                                            </p>
                                        </div>
                                        <div>
                                            <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
                                                <button
                                                    onClick={() => handlePageChange(pagination.page - 1)}
                                                    disabled={pagination.page === 1}
                                                    className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
                                                >
                                                    Previous
                                                </button>
                                                {Array.from({ length: Math.min(5, pagination.totalPages) }, (_, i) => {
                                                    const pageNum = i + 1;
                                                    return (
                                                        <button
                                                            key={pageNum}
                                                            onClick={() => handlePageChange(pageNum)}
                                                            className={`relative inline-flex items-center px-4 py-2 border text-sm font-medium ${
                                                                pageNum === pagination.page
                                                                    ? 'z-10 bg-blue-50 border-blue-500 text-blue-600'
                                                                    : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                                                            }`}
                                                        >
                                                            {pageNum}
                                                        </button>
                                                    );
                                                })}
                                                <button
                                                    onClick={() => handlePageChange(pagination.page + 1)}
                                                    disabled={pagination.page === pagination.totalPages}
                                                    className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
                                                >
                                                    Next
                                                </button>
                                            </nav>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </>
                    )}
                </div>

                {/* Notification Details Modal */}
                {selectedNotification && (
                    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
                        <div className="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white">
                            <div className="mt-3">
                                <div className="flex items-center justify-between mb-4">
                                    <h3 className="text-lg font-medium text-gray-900">Notification Details</h3>
                                    <button
                                        onClick={() => setSelectedNotification(null)}
                                        className="text-gray-400 hover:text-gray-600"
                                    >
                                        <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                                        </svg>
                                    </button>
                                </div>
                                
                                <div className="space-y-4">
                                    <div>
                                        <h4 className="text-sm font-medium text-gray-500">App Information</h4>
                                        <div className="mt-1 text-sm text-gray-900">
                                            <p><strong>App Name:</strong> {selectedNotification.appName}</p>
                                            <p><strong>Package:</strong> {selectedNotification.packageName}</p>
                                        </div>
                                    </div>
                                    
                                    <div>
                                        <h4 className="text-sm font-medium text-gray-500">Content</h4>
                                        <div className="mt-1 text-sm text-gray-900">
                                            <p><strong>Title:</strong> {selectedNotification.title}</p>
                                            <p><strong>Message:</strong> {selectedNotification.text}</p>
                                        </div>
                                    </div>
                                    
                                    <div>
                                        <h4 className="text-sm font-medium text-gray-500">Device Information</h4>
                                        <div className="mt-1 text-sm text-gray-900">
                                            <p><strong>Device ID:</strong> {selectedNotification.deviceId}</p>
                                            <p><strong>User Code:</strong> {selectedNotification.user_internal_code}</p>
                                        </div>
                                    </div>
                                    
                                    <div>
                                        <h4 className="text-sm font-medium text-gray-500">Timestamps</h4>
                                        <div className="mt-1 text-sm text-gray-900">
                                            <p><strong>Notification Time:</strong> {formatDate(selectedNotification.timestamp)}</p>
                                            <p><strong>Synced:</strong> {formatDate(selectedNotification.createdAt)}</p>
                                        </div>
                                    </div>
                                    
                                    {selectedNotification.metadata && Object.keys(selectedNotification.metadata).length > 0 && (
                                        <div>
                                            <h4 className="text-sm font-medium text-gray-500">Metadata</h4>
                                            <div className="mt-1 space-y-4">
                                                {/* Basic Info */}
                                                {selectedNotification.metadata.basicInfo && (
                                                    <div className="bg-blue-50 p-3 rounded">
                                                        <h5 className="text-xs font-semibold text-blue-800 mb-2">Basic Information</h5>
                                                        <pre className="whitespace-pre-wrap text-xs text-blue-700">
                                                            {JSON.stringify(selectedNotification.metadata.basicInfo, null, 2)}
                                                        </pre>
                                                    </div>
                                                )}

                                                {/* System Info */}
                                                {selectedNotification.metadata.systemInfo && (
                                                    <div className="bg-green-50 p-3 rounded">
                                                        <h5 className="text-xs font-semibold text-green-800 mb-2">System Information</h5>
                                                        <pre className="whitespace-pre-wrap text-xs text-green-700">
                                                            {JSON.stringify(selectedNotification.metadata.systemInfo, null, 2)}
                                                        </pre>
                                                    </div>
                                                )}

                                                {/* App Info */}
                                                {selectedNotification.metadata.appInfo && (
                                                    <div className="bg-purple-50 p-3 rounded">
                                                        <h5 className="text-xs font-semibold text-purple-800 mb-2">App Information</h5>
                                                        <pre className="whitespace-pre-wrap text-xs text-purple-700">
                                                            {JSON.stringify(selectedNotification.metadata.appInfo, null, 2)}
                                                        </pre>
                                                    </div>
                                                )}

                                                {/* Content Analysis */}
                                                {selectedNotification.metadata.contentAnalysis && (
                                                    <div className="bg-orange-50 p-3 rounded">
                                                        <h5 className="text-xs font-semibold text-orange-800 mb-2">Content Analysis</h5>
                                                        <pre className="whitespace-pre-wrap text-xs text-orange-700">
                                                            {JSON.stringify(selectedNotification.metadata.contentAnalysis, null, 2)}
                                                        </pre>
                                                    </div>
                                                )}

                                                {/* Extras */}
                                                {selectedNotification.metadata.extras && (
                                                    <div className="bg-gray-50 p-3 rounded">
                                                        <h5 className="text-xs font-semibold text-gray-800 mb-2">Notification Extras</h5>
                                                        <pre className="whitespace-pre-wrap text-xs text-gray-700 max-h-40 overflow-y-auto">
                                                            {JSON.stringify(selectedNotification.metadata.extras, null, 2)}
                                                        </pre>
                                                    </div>
                                                )}

                                                {/* Actions */}
                                                {selectedNotification.metadata.actions && (
                                                    <div className="bg-indigo-50 p-3 rounded">
                                                        <h5 className="text-xs font-semibold text-indigo-800 mb-2">Notification Actions</h5>
                                                        <pre className="whitespace-pre-wrap text-xs text-indigo-700">
                                                            {JSON.stringify(selectedNotification.metadata.actions, null, 2)}
                                                        </pre>
                                                    </div>
                                                )}

                                                {/* Raw Data */}
                                                {selectedNotification.metadata.rawData && (
                                                    <div className="bg-red-50 p-3 rounded">
                                                        <h5 className="text-xs font-semibold text-red-800 mb-2">Raw Data (Debug)</h5>
                                                        <pre className="whitespace-pre-wrap text-xs text-red-700 max-h-40 overflow-y-auto">
                                                            {JSON.stringify(selectedNotification.metadata.rawData, null, 2)}
                                                        </pre>
                                                    </div>
                                                )}

                                                {/* Complete Metadata (if not organized) */}
                                                {!selectedNotification.metadata.basicInfo && !selectedNotification.metadata.systemInfo && (
                                                    <div className="bg-gray-50 p-3 rounded">
                                                        <h5 className="text-xs font-semibold text-gray-800 mb-2">Complete Metadata</h5>
                                                        <pre className="whitespace-pre-wrap text-xs text-gray-700 max-h-60 overflow-y-auto">
                                                            {JSON.stringify(selectedNotification.metadata, null, 2)}
                                                        </pre>
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </div>
                                
                                <div className="mt-6 flex justify-end">
                                    <button
                                        onClick={() => setSelectedNotification(null)}
                                        className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700"
                                    >
                                        Close
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
} 