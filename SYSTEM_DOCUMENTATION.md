# ChronoSync - Meeting Scheduler System Documentation

## 📋 Overview

ChronoSync is an intelligent meeting scheduling system that uses a **Weighted Priority Algorithm** to automatically optimize schedules based on:
- **Priority** (1-10 scale, where 10 is highest)
- **Deadline** (how soon the meeting must happen)
- **Duration** (length of the meeting in minutes)

---

## 🎯 How the Optimization Schedule Works

### Step 1: User Creates Meetings
- Executives create meeting requests with:
  - Title and description
  - Priority (1-10)
  - Duration (in minutes)
  - Deadline (must complete by this time)
  
### Step 2: User Sets Availability Slots
- Users define when they're available for meetings
- Example: Monday 9 AM - 5 PM, Tuesday 10 AM - 3 PM, etc.

### Step 3: System Optimizes Schedule
When you click "Optimize Schedule", the system:

1. **Fetches all PENDING meetings** for the user
2. **Gets the user's availability slots**
3. **Runs the Weighted Priority Algorithm**:
   - **Priority Score** (40% weight): Higher priority meetings scheduled first
   - **Deadline Score** (40% weight): Urgent meetings get priority
   - **Duration Score** (20% weight): Shorter meetings preferred to fit more
   
4. **Places meetings in available time slots**:
   - Ensures no overlaps
   - Respects deadlines
   - Maximizes the number of meetings that can be scheduled
   
5. **Updates meeting status** from PENDING → SCHEDULED
6. **Creates Schedule records** with exact start/end times
7. **Sends notifications** to the user

### Scheduling Algorithm Details

```
Weighted Score = (Priority/10 × 0.4) + (Deadline Urgency × 0.4) + (Duration Factor × 0.2)
```

- **Priority Score**: Normalized to 0-1 (priority 10 = score of 1.0)
- **Deadline Score**: How close the deadline is (closer = higher score)
- **Duration Score**: Shorter meetings score higher (easier to fit)

Meetings are sorted by this weighted score and placed in the earliest available slots that respect all constraints.

---

## 👥 User Roles & Permissions

### 1. **Executive** 
**Role**: EXECUTIVE

**Can do**:
- ✅ Create meeting requests
- ✅ Update their own meetings
- ✅ Delete their own meetings
- ✅ **Mark meetings as COMPLETED** (click "Done" button)
- ✅ View their optimized schedule
- ✅ Set their availability
- ✅ View productivity statistics
- ✅ Receive notifications

**API Endpoints**:
```
POST   /api/meetings              - Create new meeting
GET    /api/meetings              - Get all my meetings
PUT    /api/meetings/{id}         - Update meeting
DELETE /api/meetings/{id}         - Delete meeting
POST   /api/meetings/{id}/complete - Mark as COMPLETED ✨
GET    /api/schedules             - View my schedule
POST   /api/schedules/optimize    - Optimize my schedule
POST   /api/availability          - Set availability
```

---

### 2. **Secretary / Personal Assistant**
**Role**: SECRETARY

**Can do**:
- ✅ Manage schedules on behalf of executives
- ✅ Create meetings for executives
- ✅ Resolve scheduling conflicts
- ✅ View executive's schedules
- ✅ Set availability for executives
- ✅ Optimize schedules for executives

**Special Features**:
- Can manage MULTIPLE executives
- Assists relationship table tracks which executives they support
- Can perform all executive functions on behalf of their assigned executives

**API Endpoints**:
```
POST   /api/secretary/meetings/executive/{executiveId}    - Create meeting for executive
GET    /api/secretary/schedules/executive/{executiveId}   - View executive's schedule
POST   /api/secretary/schedules/optimize/{executiveId}    - Optimize executive's schedule
GET    /api/secretary/executives                           - Get list of assigned executives
```

---

### 3. **Administrator**
**Role**: ADMIN

**Can do**:
- ✅ Full system control
- ✅ Manage all user accounts
- ✅ Assign and change user roles
- ✅ View system-wide statistics
- ✅ Monitor system performance
- ✅ Generate reports
- ✅ Access all meetings and schedules (read-only for monitoring)

**API Endpoints**:
```
GET    /api/admin/users                    - Get all users
POST   /api/admin/users                    - Create new user
PUT    /api/admin/users/{id}/role          - Change user role
DELETE /api/admin/users/{id}               - Delete user
GET    /api/admin/statistics               - System-wide statistics
GET    /api/admin/reports                  - Generate reports
POST   /api/admin/users/{id}/permissions   - Manage permissions
```

---

## 🔄 Meeting Status Flow

```
PENDING → SCHEDULED → COMPLETED
   ↓          ↓
CANCELLED  CANCELLED
```

1. **PENDING**: Meeting created, awaiting scheduling
2. **SCHEDULED**: Placed in calendar by optimization algorithm
3. **COMPLETED**: User clicked "Mark as Done" ✅
4. **CANCELLED**: Meeting was deleted/cancelled

---

## 🚀 How to Use the System

### For Executives:

#### 1. Create Meetings
```json
POST /api/meetings
{
  "title": "Team Standup",
  "description": "Daily team sync",
  "priority": 8,
  "durationMinutes": 30,
  "deadline": "2025-10-25T10:00:00"
}
```

#### 2. Set Availability
```json
POST /api/availability
{
  "startTime": "2025-10-21T09:00:00",
  "endTime": "2025-10-21T17:00:00"
}
```

#### 3. Optimize Schedule
```http
POST /api/schedules/optimize
```

Response:
```json
{
  "scheduledMeetings": [
    {
      "meeting": { "title": "Team Standup", ... },
      "scheduledStart": "2025-10-21T09:00:00",
      "scheduledEnd": "2025-10-21T09:30:00",
      "score": 0.85
    }
  ],
  "optimizationScore": 0.85
}
```

#### 4. Mark Meeting as Done
```http
POST /api/meetings/{meetingId}/complete
```

---

### For Secretaries:

#### 1. View Assigned Executives
```http
GET /api/secretary/executives
```

#### 2. Create Meeting for Executive
```http
POST /api/secretary/meetings/executive/{executiveId}
```

#### 3. Optimize Executive's Schedule
```http
POST /api/secretary/schedules/optimize/{executiveId}
```

---

### For Administrators:

#### 1. View All Users
```http
GET /api/admin/users
```

#### 2. Change User Role
```http
PUT /api/admin/users/{userId}/role
{
  "role": "EXECUTIVE"
}
```

#### 3. Generate Statistics
```http
GET /api/admin/statistics
```

---

## 📊 Database Schema (Supabase PostgreSQL)

### Key Tables:

**users** - All users (UUID primary key)
- id (UUID)
- email, password_hash, full_name
- role (ADMIN | EXECUTIVE | SECRETARY)

**meetings** - Meeting requests
- id (UUID)
- user_id (FK to users)
- title, description
- priority (1-10)
- duration_minutes
- deadline
- status (PENDING | SCHEDULED | COMPLETED | CANCELLED)

**availability** - User availability slots
- id (UUID)
- user_id (FK to users)
- start_time, end_time

**schedules** - Optimized meeting schedules
- id (UUID)
- meeting_id (FK to meetings)
- user_id (FK to users)
- scheduled_start, scheduled_end
- optimization_score

**assists** - Secretary-Executive relationships
- id (UUID)
- assignee_id (Secretary FK)
- assigner_id (Executive FK)
- permissions, start_date, end_date

**notifications** - System notifications
- id (UUID)
- user_id (FK to users)
- type, message, is_read

---

## 🔐 Security & Authentication

- JWT-based authentication
- Role-based access control (RBAC)
- Users can only access their own data
- Secretaries can access assigned executives' data
- Admins have system-wide access

---

## 🎨 Frontend Integration

### Environment Variables (.env)
```bash
VITE_SUPABASE_URL=https://cgkoddhokpepfxaggwrp.supabase.co
VITE_SUPABASE_ANON_KEY=your_anon_key
```

### Key Features to Implement:

1. **Meeting List with Status Badges**
   - Show PENDING, SCHEDULED, COMPLETED status
   - Color-coded: Yellow (Pending), Green (Scheduled), Blue (Completed)

2. **"Mark as Done" Button**
   - Show on SCHEDULED meetings
   - Click to mark as COMPLETED
   - Updates immediately with notification

3. **Schedule Optimizer Button**
   - "Optimize My Schedule" button
   - Shows progress and result
   - Displays scheduled meetings in calendar view

4. **Role-Based UI**
   - Show/hide features based on user role
   - Secretary: Executive selector dropdown
   - Admin: System management panel

---

## 🔄 Workflow Example

### Complete Workflow for Executive:

1. **Login** → JWT token received
2. **Create 3 meetings**:
   - High priority (10), 30 min, deadline tomorrow
   - Medium priority (5), 60 min, deadline next week
   - Low priority (3), 45 min, deadline next month
   
3. **Set availability**:
   - Monday-Friday, 9 AM - 5 PM
   
4. **Click "Optimize Schedule"**:
   - System calculates weighted scores
   - High priority meeting → 9:00-9:30 AM (earliest slot)
   - Medium priority → 9:30-10:30 AM
   - Low priority → 10:30-11:15 AM
   
5. **View Schedule**:
   - See all meetings with exact times
   - Calendar view shows no conflicts
   
6. **Complete meetings**:
   - After each meeting, click "Mark as Done"
   - Status changes to COMPLETED
   - Statistics updated

---

## 📈 Statistics & Reports

Track:
- Total meetings created
- Meetings completed vs cancelled
- Average optimization score
- Schedule utilization rate
- Most productive time slots
- Meeting duration trends

---

## 🚨 Notifications

Auto-generated for:
- ✉️ Meeting created
- 📅 Schedule optimized
- ✅ Meeting completed
- ❌ Meeting cancelled
- 🔔 Schedule conflicts
- ⏰ Upcoming deadlines

---

## 🛠️ Technical Stack

**Backend**:
- Java 17 + Spring Boot 3.2.0
- Spring Data JPA + Hibernate
- PostgreSQL (Supabase)
- Lombok, JWT Authentication

**Frontend**:
- React + TypeScript + Vite
- TailwindCSS
- Supabase Client

**Database**:
- Supabase PostgreSQL
- UUID primary keys
- Row Level Security (RLS)

---

## 📝 Next Steps to Implement

1. ✅ Update all services to use UUID
2. ✅ Add "Mark as Completed" endpoint
3. ⏳ Create role-specific controllers
4. ⏳ Implement Secretary management features
5. ⏳ Add Admin dashboard endpoints
6. ⏳ Create statistics/reports service
7. ⏳ Implement calendar synchronization

---

## 💡 Tips

- Set realistic availability to avoid over-scheduling
- Higher priority numbers (8-10) for urgent meetings
- Leave buffer time between meetings
- Review optimized schedule before confirming
- Mark meetings as COMPLETED promptly for accurate statistics

---

**System Status**: ✅ Core features implemented, role-based access ready for frontend integration!



