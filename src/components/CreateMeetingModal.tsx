import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { api } from '../lib/api'; // USING BACKEND API
import { X, Plus, XCircle } from 'lucide-react';

interface CreateMeetingModalProps {
  onClose: () => void;
  onSuccess: () => void;
}

interface User {
  id: string;
  fullName: string;
  email: string;
  role: string;
}

export default function CreateMeetingModal({ onClose, onSuccess }: CreateMeetingModalProps) {
  const { user } = useAuth();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState(5);
  const [durationMinutes, setDurationMinutes] = useState(30);
  const [deadline, setDeadline] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  // Participants functionality
  const [availableUsers, setAvailableUsers] = useState<User[]>([]);
  const [selectedParticipants, setSelectedParticipants] = useState<User[]>([]);
  const [showUserDropdown, setShowUserDropdown] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  // Load available users on component mount
  useEffect(() => {
    loadAvailableUsers();
  }, []);

  const loadAvailableUsers = async () => {
    try {
      const users = await api.getAllUsers();
      setAvailableUsers(users);
    } catch (error) {
      console.error('Error loading users:', error);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;

    setLoading(true);
    setError('');

    try {
      // ✅ USING BACKEND API WITH VALIDATION
      if (selectedParticipants.length > 0) {
        // Create meeting with participants
        await api.createMeetingWithParticipants(user.id, {
          title,
          description,
          priority,
          durationMinutes,
          deadline: new Date(deadline).toISOString(),
          participantIds: selectedParticipants.map(p => p.id)
        });
      } else {
        // Create meeting without participants
        await api.createMeeting(user.id, {
          title,
          description,
          priority,
          durationMinutes,
          deadline: new Date(deadline).toISOString()
        });
      }

      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err.message || 'Failed to create meeting');
    } finally {
      setLoading(false);
    }
  };

  const addParticipant = (user: User) => {
    if (!selectedParticipants.find(p => p.id === user.id)) {
      setSelectedParticipants([...selectedParticipants, user]);
    }
    setSearchTerm('');
    setShowUserDropdown(false);
  };

  const removeParticipant = (userId: string) => {
    setSelectedParticipants(selectedParticipants.filter(p => p.id !== userId));
  };

  const filteredUsers = availableUsers.filter(user => 
    user.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as HTMLElement;
      if (!target.closest('.participant-dropdown')) {
        setShowUserDropdown(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-xl shadow-2xl w-full max-w-md">
        <div className="flex items-center justify-between p-6 border-b border-slate-200">
          <h2 className="text-2xl font-bold text-slate-900">Create Meeting</h2>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-slate-600 transition-colors"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          <div>
            <label htmlFor="title" className="block text-sm font-medium text-slate-700 mb-2">
              Meeting Title
            </label>
            <input
              id="title"
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Team standup"
              required
            />
          </div>

          <div>
            <label htmlFor="description" className="block text-sm font-medium text-slate-700 mb-2">
              Description
            </label>
            <textarea
              id="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Discuss project progress"
              rows={3}
            />
          </div>

          <div>
            <label htmlFor="priority" className="block text-sm font-medium text-slate-700 mb-2">
              Priority: {priority}/10
            </label>
            <input
              id="priority"
              type="range"
              min="1"
              max="10"
              value={priority}
              onChange={(e) => setPriority(parseInt(e.target.value))}
              className="w-full"
            />
            <div className="flex justify-between text-xs text-slate-500 mt-1">
              <span>Low</span>
              <span>High</span>
            </div>
          </div>

          <div>
            <label htmlFor="duration" className="block text-sm font-medium text-slate-700 mb-2">
              Duration (minutes)
            </label>
            <input
              id="duration"
              type="number"
              value={durationMinutes}
              onChange={(e) => setDurationMinutes(parseInt(e.target.value))}
              className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              min="5"
              step="5"
              required
            />
          </div>

          <div>
            <label htmlFor="deadline" className="block text-sm font-medium text-slate-700 mb-2">
              Deadline
            </label>
            <input
              id="deadline"
              type="datetime-local"
              value={deadline}
              onChange={(e) => setDeadline(e.target.value)}
              className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              required
            />
          </div>

          {/* Participants Section */}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">
              Add Participants (Optional)
            </label>
            
            {/* Selected Participants */}
            {selectedParticipants.length > 0 && (
              <div className="mb-3">
                <div className="flex flex-wrap gap-2">
                  {selectedParticipants.map(participant => (
                    <div
                      key={participant.id}
                      className="flex items-center gap-2 bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm"
                    >
                      <span>{participant.fullName}</span>
                      <button
                        type="button"
                        onClick={() => removeParticipant(participant.id)}
                        className="text-blue-600 hover:text-blue-800"
                      >
                        <XCircle className="w-4 h-4" />
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Add Participant Input */}
            <div className="relative participant-dropdown">
              <input
                type="text"
                placeholder="Search for employees..."
                value={searchTerm}
                onChange={(e) => {
                  setSearchTerm(e.target.value);
                  setShowUserDropdown(true);
                }}
                onFocus={() => setShowUserDropdown(true)}
                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
              
              {/* User Dropdown */}
              {showUserDropdown && (
                <div className="absolute z-10 w-full mt-1 bg-white border border-slate-300 rounded-lg shadow-lg max-h-48 overflow-y-auto">
                  {filteredUsers
                    .filter(user => !selectedParticipants.find(p => p.id === user.id))
                    .length > 0 ? (
                    filteredUsers
                      .filter(user => !selectedParticipants.find(p => p.id === user.id))
                      .map(user => (
                      <button
                        key={user.id}
                        type="button"
                        onClick={() => addParticipant(user)}
                        className="w-full text-left px-4 py-2 hover:bg-slate-100 flex items-center gap-3"
                      >
                        <div className="w-8 h-8 bg-slate-200 rounded-full flex items-center justify-center">
                          <span className="text-sm font-medium text-slate-600">
                            {user.fullName.charAt(0).toUpperCase()}
                          </span>
                        </div>
                        <div className="flex-1">
                          <div className="font-medium text-slate-900">{user.fullName}</div>
                          <div className="text-sm text-slate-500">{user.email}</div>
                        </div>
                        <div className="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded-full">
                          {user.role.charAt(0).toUpperCase() + user.role.slice(1)}
                        </div>
                      </button>
                    ))
                  ) : (
                    <div className="px-4 py-3 text-center text-slate-500">
                      {searchTerm ? 'No users found' : 'No users available'}
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>

          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2 border border-slate-300 text-slate-700 rounded-lg font-medium hover:bg-slate-50 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Creating...' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
