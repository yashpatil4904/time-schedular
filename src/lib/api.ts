// Backend API Service Layer
const API_BASE_URL = 'http://localhost:8080/api';

interface Meeting {
  title: string;
  description: string;
  priority: number;
  durationMinutes: number;
  deadline: string;
}

interface Availability {
  startTime: string;
  endTime: string;
}

export const api = {
  // ========== MEETINGS ==========
  async getMeetings(userId: string) {
    const response = await fetch(`${API_BASE_URL}/meetings/user/${userId}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });
    if (!response.ok) throw new Error('Failed to fetch meetings');
    return response.json();
  },

  async getPendingMeetings(userId: string) {
    const response = await fetch(`${API_BASE_URL}/meetings/pending/user/${userId}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });
    if (!response.ok) throw new Error('Failed to fetch pending meetings');
    return response.json();
  },

  async createMeeting(userId: string, meeting: Meeting) {
    const response = await fetch(`${API_BASE_URL}/meetings/user/${userId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(meeting)
    });
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Failed to create meeting');
    }
    return response.json();
  },

  async updateMeeting(meetingId: string, userId: string, meeting: Partial<Meeting>) {
    const response = await fetch(`${API_BASE_URL}/meetings/${meetingId}/user/${userId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(meeting)
    });
    if (!response.ok) throw new Error('Failed to update meeting');
    return response.json();
  },

  async deleteMeeting(meetingId: string, userId: string) {
    const response = await fetch(`${API_BASE_URL}/meetings/${meetingId}/user/${userId}`, {
      method: 'DELETE'
    });
    if (!response.ok) throw new Error('Failed to delete meeting');
    return response.json();
  },

  async markMeetingCompleted(meetingId: string, userId: string) {
    const response = await fetch(`${API_BASE_URL}/meetings/${meetingId}/complete/user/${userId}`, {
      method: 'POST'
    });
    if (!response.ok) throw new Error('Failed to mark meeting as completed');
    return response.json();
  },

  async createMeetingWithParticipants(userId: string, meetingData: any) {
    const response = await fetch(`${API_BASE_URL}/meetings/with-participants/user/${userId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(meetingData)
    });
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Failed to create meeting with participants');
    }
    return response.json();
  },

  // ========== SCHEDULES ==========
  async getSchedules(userId: string) {
    const response = await fetch(`${API_BASE_URL}/schedules/user/${userId}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });
    if (!response.ok) throw new Error('Failed to fetch schedules');
    return response.json();
  },

  async optimizeSchedule(userId: string) {
    const response = await fetch(`${API_BASE_URL}/schedules/optimize/user/${userId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    });
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Failed to optimize schedule');
    }
    return response.json();
  },

  // ========== AVAILABILITY ==========
  async getAvailability(userId: string) {
    const response = await fetch(`${API_BASE_URL}/availability/user/${userId}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });
    if (!response.ok) throw new Error('Failed to fetch availability');
    return response.json();
  },

  async createAvailability(userId: string, availability: Availability) {
    const response = await fetch(`${API_BASE_URL}/availability/user/${userId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(availability)
    });
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Failed to create availability');
    }
    return response.json();
  },

  async deleteAvailability(availabilityId: string, userId: string) {
    const response = await fetch(`${API_BASE_URL}/availability/${availabilityId}/user/${userId}`, {
      method: 'DELETE'
    });
    if (!response.ok) throw new Error('Failed to delete availability');
    return response.json();
  },

  // ========== EXECUTIVE MANAGEMENT ==========
  async getExecutives() {
    const response = await fetch(`${API_BASE_URL}/executives`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });
    if (!response.ok) throw new Error('Failed to fetch executives');
    return response.json();
  },

  async getAllUsers() {
    const response = await fetch(`${API_BASE_URL}/executives/all`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });
    if (!response.ok) throw new Error('Failed to fetch all users');
    return response.json();
  },

  async getExecutivesBySecretary(secretaryId: string) {
    const response = await fetch(`${API_BASE_URL}/executives/secretary/${secretaryId}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });
    if (!response.ok) throw new Error('Failed to fetch executives for secretary');
    return response.json();
  },

  async assignSecretaryToExecutive(executiveId: string, secretaryId: string) {
    const response = await fetch(`${API_BASE_URL}/executives/${executiveId}/assign-secretary/${secretaryId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    });
    if (!response.ok) throw new Error('Failed to assign secretary to executive');
    return response.json();
  },

  // ========== NOTIFICATIONS ==========
  async getNotifications(userId: string) {
    const response = await fetch(`${API_BASE_URL}/notifications/user/${userId}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });
    if (!response.ok) throw new Error('Failed to fetch notifications');
    return response.json();
  },

  async markNotificationAsRead(notificationId: string, userId: string) {
    const response = await fetch(`${API_BASE_URL}/notifications/${notificationId}/mark-read/user/${userId}`, {
      method: 'POST'
    });
    if (!response.ok) throw new Error('Failed to mark notification as read');
    return response.json();
  },

  // ========== CUSTOM SCHEDULING ==========
  async createCustomSchedule(executiveId: string, meetingId: string, startTime: string, endTime: string) {
    const response = await fetch(`${API_BASE_URL}/schedules/custom/user/${executiveId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ meetingId, startTime, endTime })
    });
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Failed to create custom schedule');
    }
    return response.json();
  },

  async updateMeetingSchedule(executiveId: string, meetingId: string, startTime: string, endTime: string) {
    const response = await fetch(`${API_BASE_URL}/schedules/update/user/${executiveId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ meetingId, startTime, endTime })
    });
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Failed to update meeting schedule');
    }
    return response.json();
  }
};

