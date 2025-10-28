import { useState, useEffect } from 'react';
import { Shield, Users, BarChart3, Settings } from 'lucide-react';

export default function AdminDashboard() {
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalMeetings: 0,
    completedMeetings: 0,
    pendingMeetings: 0
  });

  useEffect(() => {
    loadStatistics();
  }, []);

  async function loadStatistics() {
    // Placeholder - needs backend endpoint /api/admin/statistics
    console.log('Load statistics - needs backend endpoint');
  }

  return (
    <div className="space-y-6">
      <div className="bg-gradient-to-r from-purple-500 to-purple-600 text-white rounded-xl p-6 shadow-lg">
        <div className="flex items-center gap-3">
          <Shield className="w-8 h-8" />
          <div>
            <h2 className="text-2xl font-bold">Admin Dashboard</h2>
            <p className="text-purple-100">System administration and monitoring</p>
          </div>
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

      {/* Admin Features */}
      <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
        <h3 className="text-xl font-bold text-slate-900 mb-4">System Management</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <button className="p-4 border-2 border-slate-200 rounded-lg hover:border-purple-300 hover:bg-purple-50 transition-colors text-left">
            <Users className="w-6 h-6 text-purple-600 mb-2" />
            <div className="font-semibold text-slate-900">Manage Users</div>
            <div className="text-sm text-slate-600">View and edit user accounts</div>
          </button>

          <button className="p-4 border-2 border-slate-200 rounded-lg hover:border-purple-300 hover:bg-purple-50 transition-colors text-left">
            <BarChart3 className="w-6 h-6 text-purple-600 mb-2" />
            <div className="font-semibold text-slate-900">View Reports</div>
            <div className="text-sm text-slate-600">System analytics and insights</div>
          </button>

          <button className="p-4 border-2 border-slate-200 rounded-lg hover:border-purple-300 hover:bg-purple-50 transition-colors text-left">
            <Settings className="w-6 h-6 text-purple-600 mb-2" />
            <div className="font-semibold text-slate-900">System Settings</div>
            <div className="text-sm text-slate-600">Configure system preferences</div>
          </button>
        </div>
      </div>

      <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
        <p className="text-sm text-yellow-800">
          🚧 <strong>Admin features coming soon:</strong> User management, role assignment, system reports, and performance monitoring.
        </p>
      </div>
    </div>
  );
}



