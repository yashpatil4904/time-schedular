# 🎯 Meeting Scheduler Backend - Demo Guide

## 📋 Prototype Requirements Implemented

✅ **Login/Register with proper authorization**  
✅ **Admin, Executive, Secretary roles defined separately**  
✅ **Meeting creation with Priority, Duration, Deadline**  
✅ **Greedy algorithm for schedule optimization**  
✅ **Weighted scoring: Priority(50%), Deadline(30%), Duration(20%)**  
✅ **Secretary and Executive treated as 'user' only**  
✅ **User availability specification**  
✅ **Online meetings only**  
✅ **Single user system**  
✅ **Dashboard notifications**  

## 🚀 Demo Flow

### **1. Login/Register**
```bash
# Register a new user
POST http://localhost:8080/api/auth/register
{
  "email": "admin@demo.com",
  "password": "password123",
  "fullName": "Demo Admin",
  "role": "ADMIN"
}

# Login
POST http://localhost:8080/api/auth/login
{
  "email": "admin@demo.com",
  "password": "password123"
}
```

### **2. Set Availability**
```bash
# Set available time slots
POST http://localhost:8080/api/availability/user/1
{
  "startTime": "2024-01-20T09:00:00",
  "endTime": "2024-01-20T17:00:00"
}

POST http://localhost:8080/api/availability/user/1
{
  "startTime": "2024-01-21T09:00:00",
  "endTime": "2024-01-21T17:00:00"
}
```

### **3. Create Meetings**
```bash
# Create high priority meeting
POST http://localhost:8080/api/meetings/user/1
{
  "title": "Urgent Client Meeting",
  "description": "Important client discussion",
  "priority": 9,
  "durationMinutes": 60,
  "deadline": "2024-01-21T15:00:00"
}

# Create medium priority meeting
POST http://localhost:8080/api/meetings/user/1
{
  "title": "Team Standup",
  "description": "Daily team sync",
  "priority": 5,
  "durationMinutes": 30,
  "deadline": "2024-01-21T18:00:00"
}

# Create low priority meeting
POST http://localhost:8080/api/meetings/user/1
{
  "title": "Training Session",
  "description": "Optional training",
  "priority": 3,
  "durationMinutes": 120,
  "deadline": "2024-01-22T17:00:00"
}
```

### **4. Optimize Schedule**
```bash
# Run optimization algorithm
POST http://localhost:8080/api/schedules/optimize/user/1

# Response will show:
# - Which meetings were scheduled
# - Start/end times for each meeting
# - Optimization scores
# - Overall schedule score
```

### **5. Check Dashboard Notifications**
```bash
# Get all notifications
GET http://localhost:8080/api/notifications/user/1

# Get unread notifications
GET http://localhost:8080/api/notifications/unread/user/1

# Get unread count
GET http://localhost:8080/api/notifications/count/unread/user/1
```

### **6. View Optimized Schedule**
```bash
# Get scheduled meetings
GET http://localhost:8080/api/schedules/user/1
```

## 🧠 Algorithm Details

### **Greedy Optimization Algorithm**
1. **Sorts meetings** by priority score (highest first)
2. **For each meeting**, finds best available slot
3. **Considers**:
   - Priority weight: 50%
   - Deadline urgency: 30% 
   - Duration preference: 20%
4. **Avoids conflicts** with already scheduled meetings
5. **Uses 15-minute increments** for optimal scheduling

### **Scoring Formula**
```
Meeting Score = (Priority/10 * 0.5) + (DeadlineUrgency * 0.3) + (DurationScore * 0.2)
```

## 📊 Expected Demo Results

### **Input:**
- **Availability**: 9 AM - 5 PM (Jan 20-21)
- **Meeting 1**: Urgent Client (Priority 9, 60min, deadline 3 PM)
- **Meeting 2**: Team Standup (Priority 5, 30min, deadline 6 PM)
- **Meeting 3**: Training (Priority 3, 120min, deadline next day)

### **Expected Output:**
1. **Urgent Client Meeting**: Scheduled first (highest priority)
2. **Team Standup**: Scheduled second (medium priority)
3. **Training Session**: Scheduled if time permits (lowest priority)

### **Notifications Generated:**
- "New meeting created: Urgent Client Meeting"
- "New meeting created: Team Standup"  
- "New meeting created: Training Session"
- "Schedule optimized! 3 meetings scheduled."

## 🛠️ Quick Start

1. **Start the application:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **Test endpoints** using Postman or curl

3. **Follow the demo flow** above

## 📝 API Endpoints Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login user |
| GET | `/api/meetings/user/{userId}` | Get user meetings |
| POST | `/api/meetings/user/{userId}` | Create meeting |
| GET | `/api/availability/user/{userId}` | Get user availability |
| POST | `/api/availability/user/{userId}` | Set availability |
| POST | `/api/schedules/optimize/user/{userId}` | Optimize schedule |
| GET | `/api/notifications/user/{userId}` | Get notifications |
| GET | `/api/schedules/user/{userId}` | Get optimized schedule |

---

**Ready for Demo! 🎉**



