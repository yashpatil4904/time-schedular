const API_BASE_URL = 'http://localhost:8080/api';

export interface RegisterData {
  email: string;
  password: string;
  fullName: string;
  role: string;
}

export interface LoginData {
  email: string;
  password: string;
}

export const authService = {
  async register({ email, password, fullName, role }: RegisterData) {
    // ✅ USE BACKEND FOR REGISTRATION
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email,
        password,
        fullName,
        role
      })
    });

    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Registration failed');
    }

    const data = await response.json();
    
    // Store user data in localStorage for session
    localStorage.setItem('user', JSON.stringify({
      id: data.userId,
      email: data.email,
      full_name: data.fullName,
      role: data.role
    }));
    
    return data;
  },

  async login({ email, password }: LoginData) {
    // ✅ USE BACKEND FOR LOGIN
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Login failed');
    }

    const data = await response.json();
    
    // Store user data
    localStorage.setItem('user', JSON.stringify({
      id: data.userId,
      email: data.email,
      full_name: data.fullName,
      role: data.role
    }));
    
    return data;
  },

  async logout() {
    localStorage.removeItem('user');
  },

  async getCurrentUser() {
    const userStr = localStorage.getItem('user');
    if (!userStr) return null;
    
    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  },
};
