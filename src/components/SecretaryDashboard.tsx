import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { api } from '../lib/api';
import { Users, Calendar, Clock, CheckCircle, Settings } from 'lucide-react';
import SecretaryScheduleManager from './SecretaryScheduleManager';

interface User {
  id: string;
  email: string;
  full_name: string;
  role: string;
}

interface Meeting {
  id: string;
  title: string;
  description: string;
  priority: number;
  duration_minutes: number;
  deadline: string;
  status: string;
}

interface Schedule {
  id: string;
  meeting_id: string;
  scheduled_start: string;
  scheduled_end: string;
  optimization_score: number;
}

export default function SecretaryDashboard() {
  const { user } = useAuth();
  const [executives, setExecutives] = useState<User[]>([]);
  const [selectedExecutive, setSelectedExecutive] = useState<string>('');
  const [executiveMeetings, setExecutiveMeetings] = useState<Meeting[]>([]);
  const [executiveSchedules, setExecutiveSchedules] = useState<Schedule[]>([]);
  const [loading, setLoading] = useState(false);
  const [showScheduleManager, setShowScheduleManager] = useState(false);

  useEffect(() => {
    loadExecutives();
  }, []);

  useEffect(() => {
    if (selectedExecutive) {
      loadExecutiveMeetings();
    }
  }, [selectedExecutive]);

  async function loadExecutives() {
    try {
      setLoading(true);
      const executivesData = await api.getExecutives();
      setExecutives(executivesData);
    } catch (error) {
      console.error('Error loading executives:', error);
    } finally {
      setLoading(false);
    }
  }

  async function loadExecutiveMeetings() {
    if (!selectedExecutive) return;
    
    try {
      setLoading(true);
      const [meetings, schedules] = await Promise.all([
        api.getMeetings(selectedExecutive),
        api.getSchedules(selectedExecutive)
      ]);
      setExecutiveMeetings(meetings);
      setExecutiveSchedules(schedules || []);
    } catch (error) {
      console.error('Error loading executive meetings:', error);
    } finally {
      setLoading(false);
    }
  }

  async function handleOptimizeForExecutive() {
    if (!selectedExecutive) return;

    try {
      setLoading(true);
      console.log('Optimizing schedule for executive:', selectedExecutive);
      const result = await api.optimizeSchedule(selectedExecutive);
      console.log('Executive optimization result:', result);
      
      const numScheduled = result.scheduledMeetings?.length || 0;
      const score = result.optimizationScore || 0;
      
      alert(`✨ Schedule optimized for executive! ${numScheduled} meetings scheduled. Score: ${score.toFixed(2)}`);
      await loadExecutiveMeetings();
    } catch (error: any) {
      console.error('Optimization error:', error);
      alert(error.message || 'Failed to optimize schedule');
    } finally {
      setLoading(false);
    }
  }

  async function markMeetingAsDone(meetingId: string) {
    if (!selectedExecutive) return;

    try {
      await api.markMeetingCompleted(meetingId, selectedExecutive);
      alert('✅ Meeting marked as completed!');
      await loadExecutiveMeetings();
    } catch (error: any) {
      console.error('Error marking meeting as done:', error);
      alert(error.message || 'Failed to mark meeting as completed');
    }
  }

  const pendingMeetings = executiveMeetings.filter(m => m.status === 'pending');
  // Sort scheduled meetings by optimization score (highest first)
  const scheduledMeetings = executiveMeetings
    .filter(m => m.status === 'scheduled')
    .sort((a, b) => {
      const scheduleA = executiveSchedules.find(s => s.meeting_id === a.id);
      const scheduleB = executiveSchedules.find(s => s.meeting_id === b.id);
      const scoreA = scheduleA?.optimization_score || 0;
      const scoreB = scheduleB?.optimization_score || 0;
      // Sort descending (highest score first)
      return scoreB - scoreA;
    });

  // Show custom schedule manager if enabled
  if (showScheduleManager && selectedExecutive) {
    return (
      <SecretaryScheduleManager 
        executiveId={selectedExecutive}
        onBack={() => setShowScheduleManager(false)}
      />
    );
  }

  return (
    <div className="space-y-6">
      <div className="bg-gradient-to-r from-green-500 to-green-600 text-white rounded-xl p-6 shadow-lg">
        <div className="flex items-center gap-3">
          <Users className="w-8 h-8" />
          <div>
            <h2 className="text-2xl font-bold">Secretary Dashboard</h2>
            <p className="text-green-100">Manage executive schedules</p>
          </div>
        </div>
      </div>

      {/* Executive Selector */}
      <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
        <label className="block text-sm font-medium text-slate-700 mb-2">
          Select Executive to Manage
        </label>
        <select
          value={selectedExecutive}
          onChange={(e) => setSelectedExecutive(e.target.value)}
          className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
        >
          <option value="">-- Select an executive --</option>
          {executives.map(exec => (
            <option key={exec.id} value={exec.id}>
              {exec.full_name} ({exec.email})
            </option>
          ))}
        </select>
      </div>

      {selectedExecutive && (
        <>
          {/* Action Buttons */}
          <div className="flex gap-4">
            <button
              onClick={handleOptimizeForExecutive}
              disabled={loading || pendingMeetings.length === 0}
              className="flex items-center gap-2 bg-orange-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-orange-700 transition-colors disabled:opacity-50 shadow-sm"
            >
              <Calendar className="w-5 h-5" />
              Optimize Executive's Schedule
              {pendingMeetings.length > 0 && (
                <span className="bg-orange-700 px-2 py-0.5 rounded-full text-xs">
                  {pendingMeetings.length}
                </span>
              )}
            </button>
            
            <button
              onClick={() => setShowScheduleManager(true)}
              disabled={pendingMeetings.length === 0}
              className="flex items-center gap-2 bg-purple-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-purple-700 transition-colors disabled:opacity-50 shadow-sm"
            >
              <Settings className="w-5 h-5" />
              Custom Schedule Manager
              {pendingMeetings.length > 0 && (
                <span className="bg-purple-700 px-2 py-0.5 rounded-full text-xs">
                  {pendingMeetings.length}
                </span>
              )}
            </button>
          </div>

          {/* Executive's Meetings */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
              <h3 className="text-lg font-bold text-slate-900 mb-4">
                Pending Meetings ({pendingMeetings.length})
              </h3>
              {pendingMeetings.length === 0 ? (
                <p className="text-slate-600 text-center py-8">No pending meetings</p>
              ) : (
                <div className="space-y-3">
                  {pendingMeetings.map(meeting => (
                    <div key={meeting.id} className="border border-slate-200 rounded-lg p-4">
                      <h4 className="font-semibold text-slate-900">{meeting.title}</h4>
                      <div className="flex items-center gap-4 mt-2 text-xs text-slate-500">
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
              <h3 className="text-lg font-bold text-slate-900 mb-4">
                Scheduled Meetings ({scheduledMeetings.length})
              </h3>
              {scheduledMeetings.length === 0 ? (
                <p className="text-slate-600 text-center py-8">No scheduled meetings</p>
              ) : (
                <div className="space-y-3">
                  {scheduledMeetings.map(meeting => (
                    <div key={meeting.id} className="border border-green-200 rounded-lg p-4 bg-green-50">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <CheckCircle className="w-4 h-4 text-green-600" />
                          <h4 className="font-semibold text-slate-900">{meeting.title}</h4>
                        </div>
                        <button
                          onClick={() => markMeetingAsDone(meeting.id)}
                          className="text-xs bg-green-600 text-white px-3 py-1 rounded hover:bg-green-700 transition-colors"
                        >
                          Mark as Done
                        </button>
                      </div>
                      <div className="flex items-center gap-4 mt-2 text-xs text-slate-500">
                        <span>Priority: {meeting.priority}/10</span>
                        <span>{meeting.duration_minutes}m</span>
                        {executiveSchedules.find(s => s.meeting_id === meeting.id) && (
                          <span className="font-semibold text-green-700">
                            Score: {executiveSchedules.find(s => s.meeting_id === meeting.id)?.optimization_score.toFixed(2)}
                          </span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
}

