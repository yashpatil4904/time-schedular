/*
  # Meeting Scheduler Database Schema

  ## Overview
  This migration creates the core database schema for a meeting scheduling application
  with user authentication, meeting management, availability tracking, and optimized schedules.

  ## 1. New Tables

  ### `users` table
  - `id` (uuid, primary key) - Auto-generated user ID
  - `email` (text, unique) - User email for login
  - `password_hash` (text) - Hashed password
  - `full_name` (text) - User's full name
  - `role` (text) - User role (admin, user)
  - `created_at` (timestamptz) - Account creation timestamp

  ### `meetings` table
  - `id` (uuid, primary key) - Auto-generated meeting ID
  - `user_id` (uuid, foreign key) - Creator of the meeting
  - `title` (text) - Meeting title
  - `description` (text) - Meeting description
  - `priority` (integer) - Meeting priority (1-10, higher is more important)
  - `duration_minutes` (integer) - Meeting duration in minutes
  - `deadline` (timestamptz) - Latest time by which meeting must occur
  - `status` (text) - Meeting status (pending, scheduled, completed, cancelled)
  - `created_at` (timestamptz) - Meeting creation timestamp

  ### `availability` table
  - `id` (uuid, primary key) - Auto-generated availability ID
  - `user_id` (uuid, foreign key) - User who set availability
  - `start_time` (timestamptz) - Start of available time slot
  - `end_time` (timestamptz) - End of available time slot
  - `created_at` (timestamptz) - Record creation timestamp

  ### `schedules` table
  - `id` (uuid, primary key) - Auto-generated schedule ID
  - `meeting_id` (uuid, foreign key) - Scheduled meeting
  - `user_id` (uuid, foreign key) - User for whom meeting is scheduled
  - `scheduled_start` (timestamptz) - Scheduled start time
  - `scheduled_end` (timestamptz) - Scheduled end time
  - `optimization_score` (numeric) - Score from optimization algorithm
  - `created_at` (timestamptz) - Schedule creation timestamp

  ## 2. Security
  - Enable Row Level Security (RLS) on all tables
  - Users can only view and modify their own data
  - Admin role has broader access (for future expansion)

  ## 3. Important Notes
  - All meetings are online (no location field needed)
  - Single user system for now (simplified implementation)
  - Priority range: 1-10 (higher = more important)
  - Greedy algorithm will use weighted scoring based on priority, duration, and deadline proximity
*/

-- Create users table
CREATE TABLE IF NOT EXISTS users (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  email text UNIQUE NOT NULL,
  password_hash text NOT NULL,
  full_name text NOT NULL,
  role text NOT NULL DEFAULT 'user',
  created_at timestamptz DEFAULT now()
);

-- Create meetings table
CREATE TABLE IF NOT EXISTS meetings (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  title text NOT NULL,
  description text DEFAULT '',
  priority integer NOT NULL CHECK (priority >= 1 AND priority <= 10),
  duration_minutes integer NOT NULL CHECK (duration_minutes > 0),
  deadline timestamptz NOT NULL,
  status text NOT NULL DEFAULT 'pending',
  created_at timestamptz DEFAULT now()
);

-- Create availability table
CREATE TABLE IF NOT EXISTS availability (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  start_time timestamptz NOT NULL,
  end_time timestamptz NOT NULL,
  created_at timestamptz DEFAULT now(),
  CHECK (end_time > start_time)
);

-- Create schedules table
CREATE TABLE IF NOT EXISTS schedules (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  meeting_id uuid NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  scheduled_start timestamptz NOT NULL,
  scheduled_end timestamptz NOT NULL,
  optimization_score numeric DEFAULT 0,
  created_at timestamptz DEFAULT now(),
  CHECK (scheduled_end > scheduled_start)
);

-- Enable Row Level Security
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE meetings ENABLE ROW LEVEL SECURITY;
ALTER TABLE availability ENABLE ROW LEVEL SECURITY;
ALTER TABLE schedules ENABLE ROW LEVEL SECURITY;

-- RLS Policies for users table
CREATE POLICY "Users can view own profile"
  ON users FOR SELECT
  TO authenticated
  USING (auth.uid() = id);

CREATE POLICY "Users can update own profile"
  ON users FOR UPDATE
  TO authenticated
  USING (auth.uid() = id)
  WITH CHECK (auth.uid() = id);

-- RLS Policies for meetings table
CREATE POLICY "Users can view own meetings"
  ON meetings FOR SELECT
  TO authenticated
  USING (auth.uid() = user_id);

CREATE POLICY "Users can create own meetings"
  ON meetings FOR INSERT
  TO authenticated
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own meetings"
  ON meetings FOR UPDATE
  TO authenticated
  USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own meetings"
  ON meetings FOR DELETE
  TO authenticated
  USING (auth.uid() = user_id);

-- RLS Policies for availability table
CREATE POLICY "Users can view own availability"
  ON availability FOR SELECT
  TO authenticated
  USING (auth.uid() = user_id);

CREATE POLICY "Users can create own availability"
  ON availability FOR INSERT
  TO authenticated
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own availability"
  ON availability FOR UPDATE
  TO authenticated
  USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own availability"
  ON availability FOR DELETE
  TO authenticated
  USING (auth.uid() = user_id);

-- RLS Policies for schedules table
CREATE POLICY "Users can view own schedules"
  ON schedules FOR SELECT
  TO authenticated
  USING (auth.uid() = user_id);

CREATE POLICY "Users can create own schedules"
  ON schedules FOR INSERT
  TO authenticated
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own schedules"
  ON schedules FOR UPDATE
  TO authenticated
  USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own schedules"
  ON schedules FOR DELETE
  TO authenticated
  USING (auth.uid() = user_id);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_meetings_user_id ON meetings(user_id);
CREATE INDEX IF NOT EXISTS idx_meetings_deadline ON meetings(deadline);
CREATE INDEX IF NOT EXISTS idx_availability_user_id ON availability(user_id);
CREATE INDEX IF NOT EXISTS idx_availability_times ON availability(start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_schedules_user_id ON schedules(user_id);
CREATE INDEX IF NOT EXISTS idx_schedules_meeting_id ON schedules(meeting_id);