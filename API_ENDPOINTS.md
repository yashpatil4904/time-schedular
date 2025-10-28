# 🔌 ChronoSync API Endpoints Reference

## Base URL
```
http://localhost:8080/api
```

---

## 🔐 Authentication

All endpoints except `/auth/register` and `/auth/login` require JWT authentication.

### Headers Required:
```
Authorization: Bearer {your_jwt_token}
Content-Type: application/json
```

---

## 📚 Authentication Endpoints

### 1. Register New User
```http
POST /api/auth/register
```

**Request Body:**
```json
{
  "email": "executive@example.com",
  "password": "SecurePass123",
  "fullName": "John Doe",
  "role": "EXECUTIVE"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR...",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "email": "executive@example.com",
    "fullName": "John Doe",
    "role": "EXECUTIVE"
  }
}
```

---

### 2. Login
```http
POST /api/auth/login
```

**Request Body:**
```json
{
  "email": "executive@example.com",
  "password": "SecurePass123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR...",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "email": "executive@example.com",
    "fullName": "John Doe",
    "role": "EXECUTIVE"
  }
}
```

---

## 📅 Meeting Endpoints (Executive)

### 1. Create Meeting
```http
POST /api/meetings
```

**Request Body:**
```json
{
  "title": "Team Standup",
  "description": "Daily team synchronization meeting",
  "priority": 8,
  "durationMinutes": 30,
  "deadline": "2025-10-25T10:00:00"
}
```

**Response:**
```json
{
  "id": "456e7890-e89b-12d3-a456-426614174001",
  "title": "Team Standup",
  "description": "Daily team synchronization meeting",
  "priority": 8,
  "durationMinutes": 30,
  "deadline": "2025-10-25T10:00:00",
  "status": "PENDING",
  "createdAt": "2025-10-20T10:00:00"
}
```

---

### 2. Get All My Meetings
```http
GET /api/meetings
```

**Response:**
```json
[
  {
    "id": "456e7890-e89b-12d3-a456-426614174001",
    "title": "Team Standup",
    "status": "PENDING",
    ...
  },
  {
    "id": "789e0123-e89b-12d3-a456-426614174002",
    "title": "Client Review",
    "status": "SCHEDULED",
    ...
  }
]
```

---

### 3. Get Pending Meetings
```http
GET /api/meetings/pending
```

**Response:**
```json
[
  {
    "id": "456e7890-e89b-12d3-a456-426614174001",
    "title": "Team Standup",
    "status": "PENDING",
    "priority": 8,
    ...
  }
]
```

---

### 4. Update Meeting
```http
PUT /api/meetings/{meetingId}
```

**Request Body:**
```json
{
  "title": "Updated Team Standup",
  "priority": 9,
  "durationMinutes": 45
}
```

**Response:** Updated meeting object

---

### 5. Delete Meeting
```http
DELETE /api/meetings/{meetingId}
```

**Response:** 204 No Content

---

### 6. Mark Meeting as Completed ✨ NEW
```http
POST /api/meetings/{meetingId}/complete
```

**Response:**
```json
{
  "id": "456e7890-e89b-12d3-a456-426614174001",
  "title": "Team Standup",
  "status": "COMPLETED",
  ...
}
```

---

## ⏰ Availability Endpoints

### 1. Set Availability
```http
POST /api/availability
```

**Request Body:**
```json
{
  "startTime": "2025-10-21T09:00:00",
  "endTime": "2025-10-21T17:00:00"
}
```

**Response:**
```json
{
  "id": "abc12345-e89b-12d3-a456-426614174003",
  "startTime": "2025-10-21T09:00:00",
  "endTime": "2025-10-21T17:00:00",
  "createdAt": "2025-10-20T10:00:00"
}
```

---

### 2. Get My Availability
```http
GET /api/availability
```

**Response:**
```json
[
  {
    "id": "abc12345-e89b-12d3-a456-426614174003",
    "startTime": "2025-10-21T09:00:00",
    "endTime": "2025-10-21T17:00:00"
  },
  {
    "id": "def67890-e89b-12d3-a456-426614174004",
    "startTime": "2025-10-22T10:00:00",
    "endTime": "2025-10-22T15:00:00"
  }
]
```

---

### 3. Delete Availability
```http
DELETE /api/availability/{availabilityId}
```

---

## 📊 Schedule Endpoints

### 1. Get My Schedule
```http
GET /api/schedules
```

**Response:**
```json
[
  {
    "id": "schedule-uuid",
    "meeting": {
      "id": "meeting-uuid",
      "title": "Team Standup",
      "priority": 8
    },
    "scheduledStart": "2025-10-21T09:00:00",
    "scheduledEnd": "2025-10-21T09:30:00",
    "optimizationScore": 0.85
  }
]
```

---

### 2. Optimize My Schedule ✨ KEY FEATURE
```http
POST /api/schedules/optimize
```

**How it works:**
1. Gets all PENDING meetings
2. Gets all availability slots
3. Runs weighted priority algorithm
4. Places meetings in optimal time slots
5. Updates meeting status to SCHEDULED
6. Returns optimized schedule

**Response:**
```json
{
  "scheduledMeetings": [
    {
      "meeting": {
        "id": "meeting-uuid-1",
        "title": "High Priority Meeting",
        "priority": 10,
        "durationMinutes": 30
      },
      "scheduledStart": "2025-10-21T09:00:00",
      "scheduledEnd": "2025-10-21T09:30:00",
      "score": 0.95
    },
    {
      "meeting": {
        "id": "meeting-uuid-2",
        "title": "Medium Priority Meeting",
        "priority": 5,
        "durationMinutes": 60
      },
      "scheduledStart": "2025-10-21T09:30:00",
      "scheduledEnd": "2025-10-21T10:30:00",
      "score": 0.75
    }
  ],
  "optimizationScore": 0.85
}
```

**Algorithm Details:**
- **Priority Weight**: 40%
- **Deadline Weight**: 40%
- **Duration Weight**: 20%

Formula:
```
Score = (Priority/10 × 0.4) + (Deadline Urgency × 0.4) + (Duration Factor × 0.2)
```

---

## 🔔 Notification Endpoints

### 1. Get My Notifications
```http
GET /api/notifications
```

**Response:**
```json
[
  {
    "id": "notif-uuid",
    "type": "MEETING_CREATED",
    "message": "New meeting created: Team Standup",
    "isRead": false,
    "createdAt": "2025-10-20T10:00:00"
  }
]
```

---

### 2. Get Unread Notifications
```http
GET /api/notifications/unread
```

---

### 3. Mark Notification as Read
```http
PUT /api/notifications/{notificationId}/read
```

---

## 👔 Secretary Endpoints

### 1. Get Assigned Executives
```http
GET /api/secretary/executives
```

**Response:**
```json
[
  {
    "id": "executive-uuid",
    "fullName": "John Doe",
    "email": "john@example.com",
    "employeeId": "emp-uuid"
  }
]
```

---

### 2. Create Meeting for Executive
```http
POST /api/secretary/meetings/executive/{executiveId}
```

**Request Body:**
```json
{
  "title": "Client Meeting",
  "description": "Quarterly review",
  "priority": 9,
  "durationMinutes": 60,
  "deadline": "2025-10-25T14:00:00"
}
```

---

### 3. View Executive's Schedule
```http
GET /api/secretary/schedules/executive/{executiveId}
```

---

### 4. Optimize Executive's Schedule
```http
POST /api/secretary/schedules/optimize/{executiveId}
```

---

### 5. Set Availability for Executive
```http
POST /api/secretary/availability/executive/{executiveId}
```

**Request Body:**
```json
{
  "startTime": "2025-10-21T09:00:00",
  "endTime": "2025-10-21T17:00:00"
}
```

---

## 👨‍💼 Admin Endpoints

### 1. Get All Users
```http
GET /api/admin/users
```

**Response:**
```json
[
  {
    "id": "user-uuid",
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "EXECUTIVE",
    "createdAt": "2025-10-01T10:00:00"
  }
]
```

---

### 2. Create User
```http
POST /api/admin/users
```

**Request Body:**
```json
{
  "email": "newuser@example.com",
  "password": "SecurePass123",
  "fullName": "Jane Smith",
  "role": "SECRETARY"
}
```

---

### 3. Update User Role
```http
PUT /api/admin/users/{userId}/role
```

**Request Body:**
```json
{
  "role": "EXECUTIVE"
}
```

---

### 4. Delete User
```http
DELETE /api/admin/users/{userId}
```

---

### 5. Get System Statistics
```http
GET /api/admin/statistics
```

**Response:**
```json
{
  "totalUsers": 50,
  "totalMeetings": 500,
  "completedMeetings": 350,
  "pendingMeetings": 100,
  "scheduledMeetings": 50,
  "averageOptimizationScore": 0.82,
  "activeExecutives": 30,
  "activeSecretaries": 15,
  "activeAdmins": 5
}
```

---

### 6. Generate Report
```http
GET /api/admin/reports?type=weekly&userId={optional}
```

**Query Parameters:**
- `type`: daily, weekly, monthly, custom
- `userId`: (optional) specific user report
- `startDate`: (optional) for custom range
- `endDate`: (optional) for custom range

---

## 🧪 Testing with cURL

### Example: Create Meeting
```bash
curl -X POST http://localhost:8080/api/meetings \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Meeting",
    "description": "Testing API",
    "priority": 8,
    "durationMinutes": 30,
    "deadline": "2025-10-25T10:00:00"
  }'
```

### Example: Optimize Schedule
```bash
curl -X POST http://localhost:8080/api/schedules/optimize \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Example: Mark as Completed
```bash
curl -X POST http://localhost:8080/api/meetings/MEETING_UUID/complete \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 📝 Status Codes

- **200 OK**: Success
- **201 Created**: Resource created
- **204 No Content**: Success (no response body)
- **400 Bad Request**: Invalid input
- **401 Unauthorized**: Missing/invalid token
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error

---

## 🚦 Meeting Status Values

- `PENDING`: Created, awaiting scheduling
- `SCHEDULED`: Placed in optimized schedule
- `COMPLETED`: Marked as done by user
- `CANCELLED`: Deleted or cancelled

---

## 🎯 Priority Scale

- `1-3`: Low priority
- `4-6`: Medium priority
- `7-9`: High priority
- `10`: Critical/Urgent

---

## 💡 Best Practices

1. **Always set availability before optimizing**
2. **Use realistic deadlines** (not too far in future)
3. **Higher priority for urgent meetings** (8-10)
4. **Mark meetings as completed** after they finish
5. **Review optimized schedule** before confirming

---

## 🔄 Workflow Example

```bash
# 1. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass"}'

# 2. Create meetings (repeat 3 times with different data)
curl -X POST http://localhost:8080/api/meetings \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Meeting 1","priority":10,"durationMinutes":30,"deadline":"2025-10-25T10:00:00"}'

# 3. Set availability
curl -X POST http://localhost:8080/api/availability \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"startTime":"2025-10-21T09:00:00","endTime":"2025-10-21T17:00:00"}'

# 4. Optimize schedule
curl -X POST http://localhost:8080/api/schedules/optimize \
  -H "Authorization: Bearer TOKEN"

# 5. View schedule
curl -X GET http://localhost:8080/api/schedules \
  -H "Authorization: Bearer TOKEN"

# 6. Mark meeting as completed
curl -X POST http://localhost:8080/api/meetings/MEETING_ID/complete \
  -H "Authorization: Bearer TOKEN"
```

---

**Status**: ✅ All core endpoints implemented and ready to use!



