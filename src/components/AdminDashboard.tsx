import { useState, useEffect } from 'react';
import { Shield, Users, BarChart3, Settings, Trash2, Edit2, UserPlus, RefreshCw } from 'lucide-react';
import { api } from '../lib/api';

interface User {
  id: string;
  email: string;
  fullName: string;
  role: 'admin' | 'executive' | 'secretary' | 'user';
  createdAt: string;
}

interface Statistics {
  totalUsers: number;
  totalMeetings: number;
  completedMeetings: number;
  pendingMeetings: number;
  scheduledMeetings: number;
  averageOptimizationScore: number;
  activeExecutives: number;
  activeSecretaries: number;
  activeAdmins: number;
}

export default function AdminDashboard() {
  const [stats, setStats] = useState<Statistics>({
    totalUsers: 0,
    totalMeetings: 0,
    completedMeetings: 0,
    pendingMeetings: 0,
    scheduledMeetings: 0,
    averageOptimizationScore: 0,
    activeExecutives: 0,
    activeSecretaries: 0,
    activeAdmins: 0
  });
  
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [showRoleModal, setShowRoleModal] = useState(false);
  const [newRole, setNewRole] = useState<string>('');

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      setLoading(true);
      const [statisticsData, usersData] = await Promise.all([
        api.getSystemStatistics(),
        api.getAllUsersForAdmin()
      ]);
      
      setStats(statisticsData);
      setUsers(usersData);
    } catch (error: any) {
      console.error('Error loading admin data:', error);
      alert('Failed to load admin data: ' + error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleUpdateRole() {
    if (!selectedUser || !newRole) return;
    
    try {
      await api.updateUserRole(selectedUser.id, newRole);
      alert('✅ User role updated successfully!');
      setShowRoleModal(false);
      setSelectedUser(null);
      setNewRole('');
      await loadData();
    } catch (error: any) {
      console.error('Error updating role:', error);
      alert('Failed to update role: ' + error.message);
    }
  }

  async function handleDeleteUser(userId: string, userName: string) {
    if (!confirm(`Are you sure you want to delete user "${userName}"? This action cannot be undone.`)) {
      return;
    }
    
    try {
      await api.deleteUser(userId);
      alert('✅ User deleted successfully!');
      await loadData();
    } catch (error: any) {
      console.error('Error deleting user:', error);
      alert('Failed to delete user: ' + error.message);
    }
  }

  function openRoleModal(user: User) {
    setSelectedUser(user);
    setNewRole(user.role);
    setShowRoleModal(true);
  }

  function getRoleBadgeColor(role: string) {
    switch (role) {
      case 'admin': return 'bg-purple-100 text-purple-800';
      case 'executive': return 'bg-blue-100 text-blue-800';
      case 'secretary': return 'bg-green-100 text-green-800';
      default: return 'bg-slate-100 text-slate-800';
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-slate-600">Loading admin dashboard...</div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-gradient-to-r from-purple-500 to-purple-600 text-white rounded-xl p-6 shadow-lg">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Shield className="w-8 h-8" />
            <div>
              <h2 className="text-2xl font-bold">Admin Dashboard</h2>
              <p className="text-purple-100">System administration and monitoring</p>
            </div>
          </div>
          <button
            onClick={loadData}
            className="bg-white/20 hover:bg-white/30 px-4 py-2 rounded-lg flex items-center gap-2 transition-colors"
          >
            <RefreshCw className="w-4 h-4" />
            Refresh
          </button>
        </div>
      </div>

      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white rounded-lg shadow-sm border border-slate-200 p-6">
          <div className="flex items-center gap-3">
            <Users className="w-8 h-8 text-blue-600" />
            <div>
              <div className="text-sm text-slate-600">Total Users</div>
              <div className="text-2xl font-bold text-slate-900">{stats.totalUsers}</div>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm border border-slate-200 p-6">
          <div className="flex items-center gap-3">
            <BarChart3 className="w-8 h-8 text-green-600" />
            <div>
              <div className="text-sm text-slate-600">Total Meetings</div>
              <div className="text-2xl font-bold text-slate-900">{stats.totalMeetings}</div>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm border border-slate-200 p-6">
          <div className="flex items-center gap-3">
            <Settings className="w-8 h-8 text-orange-600" />
            <div>
              <div className="text-sm text-slate-600">Pending</div>
              <div className="text-2xl font-bold text-slate-900">{stats.pendingMeetings}</div>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm border border-slate-200 p-6">
          <div className="flex items-center gap-3">
            <Shield className="w-8 h-8 text-purple-600" />
            <div>
              <div className="text-sm text-slate-600">Completed</div>
              <div className="text-2xl font-bold text-slate-900">{stats.completedMeetings}</div>
            </div>
          </div>
        </div>
      </div>

      {/* Role Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="text-sm text-blue-600 font-medium">Executives</div>
          <div className="text-2xl font-bold text-blue-900">{stats.activeExecutives}</div>
        </div>
        <div className="bg-green-50 border border-green-200 rounded-lg p-4">
          <div className="text-sm text-green-600 font-medium">Secretaries</div>
          <div className="text-2xl font-bold text-green-900">{stats.activeSecretaries}</div>
        </div>
        <div className="bg-purple-50 border border-purple-200 rounded-lg p-4">
          <div className="text-sm text-purple-600 font-medium">Admins</div>
          <div className="text-2xl font-bold text-purple-900">{stats.activeAdmins}</div>
        </div>
      </div>

      {/* User Management */}
      <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-xl font-bold text-slate-900">User Management</h3>
          <div className="text-sm text-slate-600">Total: {users.length} users</div>
        </div>
        
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-slate-200">
                <th className="text-left py-3 px-4 text-sm font-semibold text-slate-700">Name</th>
                <th className="text-left py-3 px-4 text-sm font-semibold text-slate-700">Email</th>
                <th className="text-left py-3 px-4 text-sm font-semibold text-slate-700">Role</th>
                <th className="text-left py-3 px-4 text-sm font-semibold text-slate-700">Created</th>
                <th className="text-right py-3 px-4 text-sm font-semibold text-slate-700">Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id} className="border-b border-slate-100 hover:bg-slate-50">
                  <td className="py-3 px-4 text-sm text-slate-900">{user.fullName}</td>
                  <td className="py-3 px-4 text-sm text-slate-600">{user.email}</td>
                  <td className="py-3 px-4">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getRoleBadgeColor(user.role)}`}>
                      {user.role.toUpperCase()}
                    </span>
                  </td>
                  <td className="py-3 px-4 text-sm text-slate-600">
                    {new Date(user.createdAt).toLocaleDateString()}
                  </td>
                  <td className="py-3 px-4">
                    <div className="flex items-center justify-end gap-2">
                      <button
                        onClick={() => openRoleModal(user)}
                        className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                        title="Edit Role"
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>
                      {user.role !== 'admin' && (
                        <button
                          onClick={() => handleDeleteUser(user.id, user.fullName)}
                          className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                          title="Delete User"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Role Update Modal */}
      {showRoleModal && selectedUser && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 max-w-md w-full mx-4">
            <h3 className="text-xl font-bold text-slate-900 mb-4">Update User Role</h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">User</label>
                <div className="text-slate-900 font-medium">{selectedUser.fullName}</div>
                <div className="text-sm text-slate-600">{selectedUser.email}</div>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">New Role</label>
                <select
                  value={newRole}
                  onChange={(e) => setNewRole(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                >
                  <option value="executive">Executive</option>
                  <option value="secretary">Secretary</option>
                  <option value="admin">Admin</option>
                  <option value="user">User</option>
                </select>
              </div>
              <div className="flex gap-3 pt-4">
                <button
                  onClick={() => {
                    setShowRoleModal(false);
                    setSelectedUser(null);
                    setNewRole('');
                  }}
                  className="flex-1 px-4 py-2 border border-slate-300 text-slate-700 rounded-lg hover:bg-slate-50 transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={handleUpdateRole}
                  className="flex-1 px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
                >
                  Update Role
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
