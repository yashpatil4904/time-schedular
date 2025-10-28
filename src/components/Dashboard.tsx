import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { authService } from '../lib/auth';
import { api } from '../lib/api'; // USING BACKEND API NOW
import { Calendar, Clock, Plus, LogOut, Bell, CheckCircle } from 'lucide-react';
import CreateMeetingModal from './CreateMeetingModal';
import AvailabilityModal from './AvailabilityModal';
import SecretaryDashboard from './SecretaryDashboard';
import AdminDashboard from './AdminDashboard';

interface Meeting {
  id: string;
  title: string;
  description: string;
  priority: number;
  duration_minutes: number;
  deadline: string;
  status: string;
  created_at: string;
}

interface Schedule {
  id: string;
  meeting_id: string;
  scheduled_start: string;
  scheduled_end: string;
  optimization_score: number;
  meeting?: Meeting;
}

interface Notification {
  id: string;
  message: string;
  type: string;
  isRead: boolean;
  createdAt: string;
}

export default function Dashboard() {
  const { user, setUser } = useAuth();
  const [meetings, setMeetings] = useState<Meeting[]>([]);
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [showCreateMeeting, setShowCreateMeeting] = useState(false);
  const [showAvailability, setShowAvailability] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, [user]);

  async function loadData() {
    if (!user) return;

    try {
      // ✅ NOW USING BACKEND API
      const [meetingsData, schedulesData, notificationsData] = await Promise.all([
        api.getMeetings(user.id),
        api.getSchedules(user.id),
        api.getNotifications(user.id)
      ]);

      setMeetings(meetingsData || []);
      setSchedules(schedulesData || []);
      // Filter out read notifications
      const unreadNotifications = (notificationsData || []).filter(notif => !notif.isRead);
      setNotifications(unreadNotifications);
    } catch (error) {
      console.error('Error loading data:', error);
      alert('Failed to load data. Make sure backend is running on http://localhost:8080');
    } finally {
      setLoading(false);
    }
  }

  async function handleLogout() {
    await authService.logout();
    setUser(null);
  }

  async function markNotificationAsRead(notificationId: string) {
    if (!user) return;
    
    try {
      await api.markNotificationAsRead(notificationId, user.id);
      // Remove the notification from the list (it's now marked as read)
      setNotifications(prev => prev.filter(notif => notif.id !== notificationId));
    } catch (error) {
      console.error('Error marking notification as read:', error);
    }
  }

  async function handleOptimizeSchedule() {
    if (!user) return;

    try {
      console.log('Starting optimization for user:', user.id);
      // ✅ BACKEND RUNS THE WEIGHTED ALGORITHM!
      const result = await api.optimizeSchedule(user.id);
      console.log('Optimization result:', result);

      const numScheduled = result.scheduledMeetings?.length || 0;
      const score = result.optimizationScore || 0;

      console.log('Scheduled meetings:', numScheduled, 'Score:', score);

      const newNotification: Notification = {
        id: crypto.randomUUID(),
        message: `✨ Successfully scheduled ${numScheduled} meeting(s)! Optimization score: ${score.toFixed(2)}`,
        timestamp: new Date().toISOString(),
      };
      setNotifications(prev => [newNotification, ...prev]);

      // Reload data from backend
      await loadData();
    } catch (error: any) {
      console.error('Error optimizing schedule:', error);
      alert(error.message || 'Failed to optimize schedule. Make sure you have set availability.');
    }
  }

  async function handleMarkAsCompleted(meetingId: string) {
    if (!user) return;

    try {
      // ✅ USING BACKEND ENDPOINT
      await api.markMeetingCompleted(meetingId, user.id);

      // Add notification
      const newNotification: Notification = {
        id: crypto.randomUUID(),
        message: '✅ Meeting marked as completed!',
        timestamp: new Date().toISOString(),
      };
      setNotifications(prev => [newNotification, ...prev]);

      // Reload data from backend
      await loadData();
    } catch (error: any) {
      console.error('Error marking meeting as completed:', error);
      alert(error.message || 'Failed to mark meeting as completed');
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        <div className="text-xl text-slate-600">Loading...</div>
      </div>
    );
  }

  const pendingMeetings = meetings.filter(m => m.status === 'pending');
  const scheduledMeetings = meetings.filter(m => m.status === 'scheduled');
  const completedMeetings = meetings.filter(m => m.status === 'completed');

  return (
    <div className="min-h-screen bg-slate-50">
      <nav className="bg-white shadow-sm border-b border-slate-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="bg-blue-600 p-2 rounded-lg">
                <Calendar className="w-6 h-6 text-white" />
              </div>
              <div>
                <h1 className="text-xl font-bold text-slate-900">ChronoSync - Meeting Scheduler</h1>
                <div className="flex items-center gap-3">
                  <p className="text-sm text-slate-600">Welcome, {user?.full_name}</p>
                  <span className={`text-xs px-2 py-1 rounded-full font-medium ${
                    user?.role === 'EXECUTIVE' ? 'bg-blue-100 text-blue-700' :
                    user?.role === 'SECRETARY' ? 'bg-green-100 text-green-700' :
                    user?.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' :
                    'bg-gray-100 text-gray-700'
                  }`}>
                    {user?.role || 'USER'}
                  </span>
                </div>
              </div>
            </div>
            <button
              onClick={handleLogout}
              className="flex items-center gap-2 px-4 py-2 text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
            >
              <LogOut className="w-4 h-4" />
              Logout
            </button>
          </div>
        </div>
      </nav>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* ROLE-BASED DASHBOARD RENDERING */}
        {user?.role === 'secretary' && <SecretaryDashboard />}
        {user?.role === 'admin' && <AdminDashboard />}
        
        {/* EXECUTIVE DASHBOARD (default) */}
        {(user?.role === 'executive' || user?.role === 'user' || !user?.role) && (
          <>
        {notifications.length > 0 && (
          <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
            <div className="flex items-start gap-3">
              <Bell className="w-5 h-5 text-blue-600 mt-0.5" />
              <div className="flex-1">
                <h3 className="font-semibold text-blue-900 mb-2">Notifications</h3>
                {notifications.map(notif => (
                  <div 
                    key={notif.id} 
                    className="border border-slate-200 rounded-lg p-3 mb-2 cursor-pointer hover:shadow-sm transition-shadow bg-blue-50 border-blue-200"
                    onClick={() => markNotificationAsRead(notif.id)}
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <p className="text-slate-900 text-sm">{notif.message}</p>
                        <p className="text-xs text-slate-500 mt-1">
                          {new Date(notif.createdAt).toLocaleString()}
                        </p>
                      </div>
                      <div className="flex items-center gap-2">
                        <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            markNotificationAsRead(notif.id);
                          }}
                          className="text-xs text-red-600 hover:text-red-800 px-2 py-1 rounded hover:bg-red-100"
                        >
                          Clear
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Statistics Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
          <div className="bg-white rounded-lg shadow-sm border border-slate-200 p-4">
            <div className="text-sm text-slate-600">Total Meetings</div>
            <div className="text-2xl font-bold text-slate-900 mt-1">{meetings.length}</div>
          </div>
          <div className="bg-yellow-50 rounded-lg shadow-sm border border-yellow-200 p-4">
            <div className="text-sm text-yellow-700">Pending</div>
            <div className="text-2xl font-bold text-yellow-900 mt-1">{pendingMeetings.length}</div>
          </div>
          <div className="bg-green-50 rounded-lg shadow-sm border border-green-200 p-4">
            <div className="text-sm text-green-700">Scheduled</div>
            <div className="text-2xl font-bold text-green-900 mt-1">{scheduledMeetings.length}</div>
          </div>
          <div className="bg-blue-50 rounded-lg shadow-sm border border-blue-200 p-4">
            <div className="text-sm text-blue-700">Completed</div>
            <div className="text-2xl font-bold text-blue-900 mt-1">{completedMeetings.length}</div>
          </div>
        </div>

        <div className="flex gap-4 mb-8">
          <button
            onClick={() => setShowCreateMeeting(true)}
            className="flex items-center gap-2 bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700 transition-colors shadow-sm"
          >
            <Plus className="w-5 h-5" />
            Create Meeting
          </button>
          <button
            onClick={() => setShowAvailability(true)}
            className="flex items-center gap-2 bg-green-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-green-700 transition-colors shadow-sm"
          >
            <Clock className="w-5 h-5" />
            Set Availability
          </button>
          <button
            onClick={handleOptimizeSchedule}
            disabled={pendingMeetings.length === 0}
            className="flex items-center gap-2 bg-orange-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-orange-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed shadow-sm"
            title={pendingMeetings.length === 0 ? 'No pending meetings to optimize' : 'Run weighted priority algorithm'}
          >
            <Calendar className="w-5 h-5" />
            🎯 Optimize Schedule
            {pendingMeetings.length > 0 && (
              <span className="bg-orange-700 px-2 py-0.5 rounded-full text-xs">
                {pendingMeetings.length}
              </span>
            )}
          </button>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
            <h2 className="text-xl font-bold text-slate-900 mb-4">Pending Meetings</h2>
            {pendingMeetings.length === 0 ? (
              <p className="text-slate-600 text-center py-8">No pending meetings</p>
            ) : (
              <div className="space-y-3">
                {pendingMeetings.map(meeting => (
                  <div key={meeting.id} className="border border-slate-200 rounded-lg p-4 hover:border-blue-300 transition-colors">
                    <h3 className="font-semibold text-slate-900">{meeting.title}</h3>
                    <p className="text-sm text-slate-600 mt-1">{meeting.description}</p>
                    <div className="flex items-center gap-4 mt-3 text-xs text-slate-500">
                      <span>Priority: {meeting.priority}/10</span>
                      <span>Duration: {meeting.duration_minutes}m</span>
                      <span>Deadline: {new Date(meeting.deadline).toLocaleDateString()}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
            <h2 className="text-xl font-bold text-slate-900 mb-4">
              Scheduled Meetings
              {scheduledMeetings.length > 0 && (
                <span className="ml-2 text-sm font-normal text-slate-600">
                  ({scheduledMeetings.length})
                </span>
              )}
            </h2>
            {scheduledMeetings.length === 0 ? (
              <p className="text-slate-600 text-center py-8">No scheduled meetings</p>
            ) : (
              <div className="space-y-3">
                {scheduledMeetings.map(meeting => {
                  const schedule = schedules.find(s => s.meeting_id === meeting.id);
                  return (
                    <div key={meeting.id} className="border border-green-200 rounded-lg p-4 bg-green-50">
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <h3 className="font-semibold text-slate-900">{meeting.title}</h3>
                          {schedule && (
                            <div className="flex items-center gap-2 mt-2 text-sm text-slate-700">
                              <Clock className="w-4 h-4" />
                              <span>
                                {new Date(schedule.scheduled_start).toLocaleString()} - {new Date(schedule.scheduled_end).toLocaleTimeString()}
                              </span>
                            </div>
                          )}
                          <div className="flex items-center gap-4 mt-2 text-xs text-slate-500">
                            <span>Priority: {meeting.priority}/10</span>
                            <span>Duration: {meeting.duration_minutes}m</span>
                            {schedule && <span>Score: {schedule.optimization_score.toFixed(2)}</span>}
                          </div>
                        </div>
                        <button
                          onClick={() => handleMarkAsCompleted(meeting.id)}
                          className="flex items-center gap-1 bg-blue-600 text-white px-3 py-1.5 rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors ml-3"
                        >
                          <CheckCircle className="w-4 h-4" />
                          Mark Done
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>

        {/* Completed Meetings Section */}
        {completedMeetings.length > 0 && (
          <div className="mt-6 bg-white rounded-xl shadow-sm border border-slate-200 p-6">
            <h2 className="text-xl font-bold text-slate-900 mb-4">
              ✅ Completed Meetings
              <span className="ml-2 text-sm font-normal text-slate-600">
                ({completedMeetings.length})
              </span>
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
              {completedMeetings.map(meeting => (
                <div key={meeting.id} className="border border-blue-200 rounded-lg p-4 bg-blue-50">
                  <div className="flex items-start gap-2">
                    <CheckCircle className="w-5 h-5 text-blue-600 mt-0.5 flex-shrink-0" />
                    <div className="flex-1">
                      <h3 className="font-semibold text-slate-900">{meeting.title}</h3>
                      <p className="text-xs text-slate-600 mt-1 line-clamp-2">{meeting.description}</p>
                      <div className="flex items-center gap-3 mt-2 text-xs text-slate-500">
                        <span>Priority: {meeting.priority}/10</span>
                        <span>{meeting.duration_minutes}m</span>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
        </>
        )}
      </div>

      {showCreateMeeting && (
        <CreateMeetingModal
          onClose={() => setShowCreateMeeting(false)}
          onSuccess={loadData}
        />
      )}

      {showAvailability && (
        <AvailabilityModal
          onClose={() => setShowAvailability(false)}
          onSuccess={loadData}
        />
      )}
    </div>
  );
}
