import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { api } from '../lib/api';
import { X, Trash2 } from 'lucide-react';

interface AvailabilityModalProps {
  onClose: () => void;
  onSuccess: () => void;
}

interface AvailabilitySlot {
  id: string;
  startTime: string;
  endTime: string;
}

export default function AvailabilityModal({ onClose, onSuccess }: AvailabilityModalProps) {
  const { user } = useAuth();
  const [slots, setSlots] = useState<AvailabilitySlot[]>([]);
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadAvailability();
  }, [user]);

  async function loadAvailability() {
    if (!user) return;

    try {
      // ✅ USING BACKEND API
      const data = await api.getAvailability(user.id);
      setSlots(data || []);
    } catch (error) {
      console.error('Error loading availability:', error);
    }
  }

  const handleAddSlot = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;

    setLoading(true);
    setError('');

    try {
      // ✅ USING BACKEND API WITH VALIDATION
      await api.createAvailability(user.id, {
        startTime: new Date(startTime).toISOString(),
        endTime: new Date(endTime).toISOString()
      });

      setStartTime('');
      setEndTime('');
      await loadAvailability();
      onSuccess();
    } catch (err: any) {
      setError(err.message || 'Failed to add availability');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteSlot = async (slotId: string) => {
    if (!user) return;
    
    try {
      // ✅ USING BACKEND API
      await api.deleteAvailability(slotId, user.id);
      await loadAvailability();
      onSuccess();
    } catch (err: any) {
      console.error('Error deleting slot:', err);
      alert(err.message || 'Failed to delete availability');
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between p-6 border-b border-slate-200 sticky top-0 bg-white">
          <h2 className="text-2xl font-bold text-slate-900">Set Availability</h2>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-slate-600 transition-colors"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        <div className="p-6">
          <form onSubmit={handleAddSlot} className="space-y-4 mb-6">
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
                {error}
              </div>
            )}

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label htmlFor="startTime" className="block text-sm font-medium text-slate-700 mb-2">
                  Start Time
                </label>
                <input
                  id="startTime"
                  type="datetime-local"
                  value={startTime}
                  onChange={(e) => setStartTime(e.target.value)}
                  className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  required
                />
              </div>

              <div>
                <label htmlFor="endTime" className="block text-sm font-medium text-slate-700 mb-2">
                  End Time
                </label>
                <input
                  id="endTime"
                  type="datetime-local"
                  value={endTime}
                  onChange={(e) => setEndTime(e.target.value)}
                  className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  required
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full px-4 py-2 bg-green-600 text-white rounded-lg font-medium hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Adding...' : 'Add Availability Slot'}
            </button>
          </form>

          <div className="border-t border-slate-200 pt-6">
            <h3 className="text-lg font-semibold text-slate-900 mb-4">Your Availability</h3>
            {slots.length === 0 ? (
              <p className="text-slate-600 text-center py-8">No availability slots set</p>
            ) : (
              <div className="space-y-2">
                {slots.map(slot => (
                  <div
                    key={slot.id}
                    className="flex items-center justify-between bg-slate-50 border border-slate-200 rounded-lg p-4"
                  >
                    <div>
                      <div className="text-sm font-medium text-slate-900">
                        {new Date(slot.startTime).toLocaleString()}
                      </div>
                      <div className="text-sm text-slate-600">
                        to {new Date(slot.endTime).toLocaleString()}
                      </div>
                    </div>
                    <button
                      onClick={() => handleDeleteSlot(slot.id)}
                      className="text-red-600 hover:text-red-700 p-2 hover:bg-red-50 rounded-lg transition-colors"
                    >
                      <Trash2 className="w-5 h-5" />
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="border-t border-slate-200 p-6">
          <button
            onClick={onClose}
            className="w-full px-4 py-2 border border-slate-300 text-slate-700 rounded-lg font-medium hover:bg-slate-50 transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
