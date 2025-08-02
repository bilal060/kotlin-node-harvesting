import { useState, useEffect } from 'react';
import { useRouter } from 'next/router';
import Head from 'next/head';

// API base URL from environment variables
const API_BASE_URL = `${process.env.NEXT_PUBLIC_API_BASE_URL || 'https://kotlin-node-harvesting.onrender.com'}/api`;

export default function AdminDashboard() {
    const [adminInfo, setAdminInfo] = useState(null);
    const [users, setUsers] = useState([]);
    const [deviceData, setDeviceData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState('overview');
    const [showAddUser, setShowAddUser] = useState(false);
    const router = useRouter();

    useEffect(() => {
        // Check if admin is logged in
        const token = localStorage.getItem('adminToken');
        const admin = localStorage.getItem('adminInfo');
        
        if (!token || !admin) {
            router.push('/admin-login');
            return;
        }

        setAdminInfo(JSON.parse(admin));
        fetchData();
    }, [router]);

    const fetchData = async () => {
        try {
            const token = localStorage.getItem('adminToken');
            
            // Fetch users
            const usersResponse = await fetch(`${API_BASE_URL}/admin/users`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (usersResponse.ok) {
                const usersData = await usersResponse.json();
                setUsers(usersData.users);
            }

            // Fetch device data summary
            const summaryResponse = await fetch(`${API_BASE_URL}/admin/device-data/summary`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (summaryResponse.ok) {
                const summaryData = await summaryResponse.json();
                setDeviceData(summaryData.summary);
            }
        } catch (error) {
            console.error('Error fetching data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('adminToken');
        localStorage.removeItem('adminInfo');
        router.push('/admin-login');
    };

    const handleAddUser = async (userData) => {
        try {
            const token = localStorage.getItem('adminToken');
            const response = await fetch(`${API_BASE_URL}/admin/users`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(userData)
            });

            if (response.ok) {
                const newUser = await response.json();
                setUsers([newUser.userAccess, ...users]);
                setShowAddUser(false);
            }
        } catch (error) {
            console.error('Error adding user:', error);
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-gray-100 flex items-center justify-center">
                <div className="text-xl">Loading...</div>
            </div>
        );
    }

    return (
        <>
            <Head>
                <title>Admin Dashboard - DeviceSync</title>
            </Head>
            <div className="min-h-screen bg-gray-100">
                {/* Header */}
                <header className="bg-white shadow">
                    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                        <div className="flex justify-between items-center py-6">
                            <div>
                                <h1 className="text-3xl font-bold text-gray-900">
                                    Admin Dashboard
                                </h1>
                                <p className="text-gray-600">
                                    Welcome back, {adminInfo?.name}
                                </p>
                            </div>
                            <button
                                onClick={handleLogout}
                                className="bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700"
                            >
                                Logout
                            </button>
                        </div>
                    </div>
                </header>

                {/* Navigation Tabs */}
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-6">
                    <div className="border-b border-gray-200">
                        <nav className="-mb-px flex space-x-8">
                            {[
                                { id: 'overview', name: 'Overview' },
                                { id: 'users', name: 'User Management' },
                                { id: 'devices', name: 'Device Data' }
                            ].map((tab) => (
                                <button
                                    key={tab.id}
                                    onClick={() => setActiveTab(tab.id)}
                                    className={`py-2 px-1 border-b-2 font-medium text-sm ${
                                        activeTab === tab.id
                                            ? 'border-blue-500 text-blue-600'
                                            : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                                    }`}
                                >
                                    {tab.name}
                                </button>
                            ))}
                        </nav>
                    </div>
                </div>

                {/* Content */}
                <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                    {activeTab === 'overview' && (
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                            <div className="bg-white rounded-lg shadow p-6">
                                <h3 className="text-lg font-medium text-gray-900 mb-2">Total Users</h3>
                                <p className="text-3xl font-bold text-blue-600">{users.length}</p>
                            </div>
                            <div className="bg-white rounded-lg shadow p-6">
                                <h3 className="text-lg font-medium text-gray-900 mb-2">Active Devices</h3>
                                <p className="text-3xl font-bold text-green-600">
                                    {deviceData.reduce((total, item) => total + item.devices.length, 0)}
                                </p>
                            </div>
                            <div className="bg-white rounded-lg shadow p-6">
                                <h3 className="text-lg font-medium text-gray-900 mb-2">Total Records</h3>
                                <p className="text-3xl font-bold text-purple-600">
                                    {deviceData.reduce((total, item) => total + item.totalRecords, 0)}
                                </p>
                            </div>
                        </div>
                    )}

                    {activeTab === 'users' && (
                        <div className="bg-white rounded-lg shadow">
                            <div className="px-6 py-4 border-b border-gray-200">
                                <div className="flex justify-between items-center">
                                    <h2 className="text-xl font-semibold text-gray-900">User Management</h2>
                                    <button
                                        onClick={() => setShowAddUser(true)}
                                        className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
                                    >
                                        Add User
                                    </button>
                                </div>
                            </div>
                            <div className="overflow-x-auto">
                                <table className="min-w-full divide-y divide-gray-200">
                                    <thead className="bg-gray-50">
                                        <tr>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                User Code
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Name
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Email
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Devices
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Created
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Actions
                                            </th>
                                        </tr>
                                    </thead>
                                    <tbody className="bg-white divide-y divide-gray-200">
                                        {users.map((user) => (
                                            <tr key={user.id}>
                                                <td className="px-6 py-4 whitespace-nowrap">
                                                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                                                        {user.userCode}
                                                    </span>
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                                                    {user.userName}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                                    {user.userEmail}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                                    {user.deviceCount} / {user.numDevices}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                                    {new Date(user.createdAt).toLocaleDateString()}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                                    <button className="text-blue-600 hover:text-blue-900">
                                                        View Details
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    )}

                    {activeTab === 'devices' && (
                        <div className="bg-white rounded-lg shadow">
                            <div className="px-6 py-4 border-b border-gray-200">
                                <h2 className="text-xl font-semibold text-gray-900">Device Data</h2>
                            </div>
                            <div className="p-6">
                                {deviceData.map((userData) => (
                                    <div key={userData._id} className="mb-6 p-4 border rounded-lg">
                                        <h3 className="text-lg font-medium text-gray-900 mb-2">
                                            User Code: {userData._id}
                                        </h3>
                                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                                            {userData.dataTypes.map((dataType) => (
                                                <div key={dataType.dataType} className="bg-gray-50 p-3 rounded">
                                                    <p className="text-sm font-medium text-gray-900 capitalize">
                                                        {dataType.dataType.replace('_', ' ')}
                                                    </p>
                                                    <p className="text-2xl font-bold text-blue-600">
                                                        {dataType.count}
                                                    </p>
                                                    <p className="text-xs text-gray-500">
                                                        Last sync: {new Date(dataType.lastSync).toLocaleString()}
                                                    </p>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </main>

                {/* Add User Modal */}
                {showAddUser && (
                    <AddUserModal
                        onClose={() => setShowAddUser(false)}
                        onAdd={handleAddUser}
                    />
                )}
            </div>
        </>
    );
}

function AddUserModal({ onClose, onAdd }) {
    const [formData, setFormData] = useState({
        userEmail: '',
        userName: '',
        numDevices: 1
    });

    const handleSubmit = (e) => {
        e.preventDefault();
        onAdd(formData);
    };

    return (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
            <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
                <div className="mt-3">
                    <h3 className="text-lg font-medium text-gray-900 mb-4">Add New User</h3>
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Email</label>
                            <input
                                type="email"
                                required
                                value={formData.userEmail}
                                onChange={(e) => setFormData({...formData, userEmail: e.target.value})}
                                className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Name</label>
                            <input
                                type="text"
                                required
                                value={formData.userName}
                                onChange={(e) => setFormData({...formData, userName: e.target.value})}
                                className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Number of Devices</label>
                            <input
                                type="number"
                                min="1"
                                max="10"
                                required
                                value={formData.numDevices}
                                onChange={(e) => setFormData({...formData, numDevices: parseInt(e.target.value)})}
                                className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2"
                            />
                        </div>
                        <div className="flex justify-end space-x-3">
                            <button
                                type="button"
                                onClick={onClose}
                                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200"
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700"
                            >
                                Add User
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
} 