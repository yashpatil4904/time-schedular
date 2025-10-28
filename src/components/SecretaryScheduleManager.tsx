import { useState, useEffect } from 'react';
import { api } from '../lib/api';
import { Calendar, Clock, Save, ArrowLeft } from 'lucide-react';

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
  scheduled_start: string;
  scheduled_end: string;
  optimization_score: number;
  meeting?: Meeting;
}

interface SecretaryScheduleManagerProps {
  executiveId: string;
  onBack: () => void;
}

export default function SecretaryScheduleManager({ executiveId, onBack }: SecretaryScheduleManagerProps) {
  const [meetings, setMeetings] = useState<Meeting[]>([]);
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [loading, setLoading] = useState(false);
  const [draggedMeeting, setDraggedMeeting] = useState<Meeting | null>(null);
  const [customSchedules, setCustomSchedules] = useState<{ [key: string]: { start: string; end: string } }>({});

  useEffect(() => {
    loadData();
  }, [executiveId]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [meetingsData, schedulesData] = await Promise.all([
        api.getMeetings(executiveId),
        api.getSchedules(executiveId)
      ]);
      setMeetings(meetingsData || []);
      setSchedules(schedulesData || []);
      
      // Pre-populate custom schedules with existing scheduled meetings
      const existingSchedules: { [key: string]: { start: string; end: string } } = {};
      schedulesData?.forEach(schedule => {
        if (schedule.meeting) {
          existingSchedules[schedule.meeting.id] = {
            start: schedule.scheduled_start,
            end: schedule.scheduled_end
          };
        }
      });
      setCustomSchedules(existingSchedules);
    } catch (error) {
      console.error('Error loading data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDragStart = (meeting: Meeting) => {
    setDraggedMeeting(meeting);
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
  };

  const handleDrop = (e: React.DragEvent, timeSlot: string) => {
    e.preventDefault();
    if (!draggedMeeting) {
      console.log('No dragged meeting');
      return;
    }

    console.log('Dropping meeting:', draggedMeeting.title, 'at time:', timeSlot);

    const duration = draggedMeeting.duration_minutes;
    const startTime = new Date(timeSlot);
    const endTime = new Date(startTime.getTime() + duration * 60000);

    console.log('Setting schedule:', {
      start: startTime.toISOString(),
      end: endTime.toISOString()
    });

    setCustomSchedules(prev => ({
      ...prev,
      [draggedMeeting.id]: {
        start: startTime.toISOString(),
        end: endTime.toISOString()
      }
    }));

    setDraggedMeeting(null);
  };

  const saveCustomSchedule = async () => {
    try {
      setLoading(true);
      
      console.log('Saving custom schedules:', customSchedules);
      
      // Instead of creating custom schedules, we'll update the existing optimization
      // by modifying meeting schedules directly
      for (const [meetingId, schedule] of Object.entries(customSchedules)) {
        console.log('Updating schedule for meeting:', meetingId, schedule);
        await api.updateMeetingSchedule(executiveId, meetingId, schedule.start, schedule.end);
      }

      alert('✅ Custom schedule arrangement saved! Executive will see your optimized schedule.');
      await loadData();
    } catch (error: any) {
      console.error('Error saving custom schedule:', error);
      alert(error.message || 'Failed to save custom schedule');
    } finally {
      setLoading(false);
    }
  };

  const generateTimeSlots = () => {
    const slots = [];
    const start = new Date();
    start.setHours(9, 0, 0, 0); // 9 AM
    const end = new Date();
    end.setHours(17, 0, 0, 0); // 5 PM

    for (let time = new Date(start); time < end; time.setMinutes(time.getMinutes() + 30)) {
      slots.push(new Date(time).toISOString());
    }
    return slots;
  };

  const timeSlots = generateTimeSlots();
  const allMeetings = meetings; // Show all meetings, not just pending

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <button
          onClick={onBack}
          className="flex items-center gap-2 text-slate-600 hover:text-slate-800"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Dashboard
        </button>
        <h2 className="text-2xl font-bold text-slate-900">Custom Schedule Manager</h2>
      </div>

      {/* Instructions */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h3 className="font-semibold text-blue-900 mb-2">How to Use:</h3>
        <ul className="text-sm text-blue-800 space-y-1">
          <li>• Drag meetings from the left to time slots on the right</li>
          <li>• Meetings will automatically fit their duration</li>
          <li>• Click "Save Custom Schedule" to apply changes</li>
          <li>• The executive will see your custom arrangement</li>
        </ul>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* All Meetings */}
        <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
          <h3 className="text-lg font-bold text-slate-900 mb-4 flex items-center gap-2">
            <Calendar className="w-5 h-5" />
            All Meetings ({allMeetings.length})
          </h3>
          {allMeetings.length === 0 ? (
            <p className="text-slate-600 text-center py-8">No meetings found</p>
          ) : (
            <div className="space-y-3">
              {allMeetings.map(meeting => (
                <div
                  key={meeting.id}
                  draggable
                  onDragStart={() => handleDragStart(meeting)}
                  className={`border rounded-lg p-4 cursor-move hover:shadow-sm transition-shadow ${
                    meeting.status === 'scheduled' 
                      ? 'border-green-300 bg-green-50' 
                      : 'border-slate-200 bg-white'
                  }`}
                >
                  <h4 className="font-semibold text-slate-900">{meeting.title}</h4>
                  <div className="flex items-center gap-4 mt-2 text-xs text-slate-500">
                    <span>Priority: {meeting.priority}/10</span>
                    <span>Duration: {meeting.duration_minutes}m</span>
                    <span>Deadline: {new Date(meeting.deadline).toLocaleDateString()}</span>
                    <span className={`px-2 py-1 rounded text-xs ${
                      meeting.status === 'scheduled' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
                    }`}>
                      {meeting.status}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Time Slots */}
        <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
          <h3 className="text-lg font-bold text-slate-900 mb-4 flex items-center gap-2">
            <Clock className="w-5 h-5" />
            Schedule Time Slots
          </h3>
          <div className="space-y-2 max-h-96 overflow-y-auto">
            {timeSlots.map(slot => {
              const meetingId = Object.keys(customSchedules).find(id => 
                customSchedules[id].start === slot
              );
              const meeting = meetingId ? meetings.find(m => m.id === meetingId) : null;

              return (
                <div
                  key={slot}
                  onDragOver={handleDragOver}
                  onDrop={(e) => handleDrop(e, slot)}
                  className={`border-2 border-dashed rounded-lg p-3 min-h-[60px] transition-colors ${
                    meeting 
                      ? 'border-green-300 bg-green-50' 
                      : 'border-slate-300 hover:border-blue-300 hover:bg-blue-50'
                  } ${draggedMeeting ? 'border-blue-400 bg-blue-100' : ''}`}
                >
                  {meeting ? (
                    <div className="flex items-center justify-between">
                      <div>
                        <div className="font-medium text-slate-900">{meeting.title}</div>
                        <div className="text-xs text-slate-500">
                          {new Date(slot).toLocaleTimeString()} - {new Date(customSchedules[meeting.id].end).toLocaleTimeString()}
                        </div>
                      </div>
                      <button
                        onClick={() => {
                          const newSchedules = { ...customSchedules };
                          delete newSchedules[meeting.id];
                          setCustomSchedules(newSchedules);
                        }}
                        className="text-red-600 hover:text-red-800 text-xs"
                      >
                        Remove
                      </button>
                    </div>
                  ) : (
                    <div className="text-slate-500 text-sm">
                      {new Date(slot).toLocaleTimeString()} - {new Date(new Date(slot).getTime() + 30 * 60000).toLocaleTimeString()}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Save Button */}
      <div className="flex justify-end">
        <button
          onClick={saveCustomSchedule}
          disabled={loading || Object.keys(customSchedules).length === 0}
          className="flex items-center gap-2 bg-green-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <Save className="w-5 h-5" />
          {loading ? 'Saving...' : 'Save Custom Schedule'}
        </button>
      </div>
    </div>
  );
}
