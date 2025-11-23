# Meeting Scheduler Backend - Spring Boot Implementation

A comprehensive Spring Boot backend implementation for the Meeting Scheduler system based on the UML class diagram.

## 🏗️ Architecture

This backend follows the **Spring Boot layered architecture**:

- **Entity Layer**: JPA entities mapped from UML diagram
- **Repository Layer**: Data access with Spring Data JPA
- **Service Layer**: Business logic and transactions
- **Controller Layer**: REST API endpoints
- **Security Layer**: Authentication and authorization

## 📋 Features

### Core Functionality
- ✅ User management (Executive, Secretary, Admin roles)
- ✅ Meeting management (CRUD operations)
- ✅ Schedule optimization with weighted priority algorithm
- ✅ Notification system
- ✅ Authentication and authorization
- ✅ Calendar integration support

### Advanced Features
- ✅ Conflict resolution strategies
- ✅ Meeting request approval workflow
- ✅ Role-based access control
- ✅ Real-time notifications
- ✅ Schedule optimization algorithm

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- IDE (IntelliJ IDEA, Eclipse, VS Code)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd meeting-scheduler-backend
   ```

2. **Database Setup**
   ```sql
   CREATE DATABASE meeting_scheduler;
   CREATE USER scheduler_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE meeting_scheduler TO scheduler_user;
   ```

3. **Configuration**
   Update `src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/meeting_scheduler
       username: scheduler_user
       password: your_password
   ```

4. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```

The API will be available at: `http://localhost:8080`

## 📚 API Documentation

### Authentication Endpoints
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/logout` - User logout

### Meeting Endpoints
- `GET /api/meetings` - Get all meetings
- `GET /api/meetings/{id}` - Get meeting by ID
- `POST /api/meetings` - Create new meeting
- `PUT /api/meetings/{id}` - Update meeting
- `POST /api/meetings/{id}/cancel` - Cancel meeting
- `POST /api/meetings/{id}/acknowledge` - Acknowledge meeting

### Schedule Endpoints
- `GET /api/schedules` - Get all schedules
- `POST /api/schedules` - Create new schedule
- `POST /api/schedules/optimize` - Optimize schedule
- `POST /api/schedules/{id}/resolve-conflict` - Resolve schedule conflict

### Notification Endpoints
- `GET /api/notifications/user/{userId}` - Get user notifications
- `POST /api/notifications/{id}/mark-read` - Mark notification as read

## 🏛️ Database Schema

The system uses the following main entities:

### Core Entities
- **User** (Base class for Executive, Secretary, Admin)
- **Meeting** (Meeting information and status)
- **Schedule** (Time-based schedule management)
- **Notification** (System notifications)

### Association Entities
- **Participation** (User-Meeting relationship)
- **Assists** (Secretary-Executive relationship)
- **ManageMeetingRequest** (Meeting request workflow)

### Integration Entities
- **AuthService** (Authentication management)
- **CalendarIntegration** (External calendar sync)

## 🔧 Configuration

### Environment Variables
```bash
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret_key
```

### Application Properties
```yaml
server:
  port: 8080

spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

logging:
  level:
    com.meetingscheduler: DEBUG
```

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Test Coverage
```bash
mvn jacoco:report
```

## 🚀 Deployment

### Docker Deployment
```bash
# Build image
docker build -t meeting-scheduler-backend .

# Run container
docker run -p 8080:8080 meeting-scheduler-backend
```

### Production Configuration
- Use environment-specific profiles
- Configure proper database connection pooling
- Set up monitoring and logging
- Configure security headers

## 📊 Algorithm Details

### Weighted Priority Algorithm
The schedule optimization uses a weighted scoring system:

- **Priority Weight**: 40% (1-10 scale)
- **Deadline Weight**: 40% (urgency factor)
- **Duration Weight**: 20% (meeting length factor)

### Conflict Resolution Strategies
- **RESCHEDULE_MEETING**: Move conflicting meeting to available slot
- **CANCEL_MEETING**: Cancel lower priority meeting
- **ADJUST_TIME**: Adjust meeting duration or time

## 🔒 Security

### Authentication
- JWT-based authentication
- Password encryption with BCrypt
- Role-based access control

### Authorization
- Endpoint-level security
- Role-based permissions
- Session management

## 📈 Monitoring

### Health Checks
- Spring Boot Actuator endpoints
- Database connectivity monitoring
- Application metrics

### Logging
- Structured logging with SLF4J
- Request/response logging
- Error tracking

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support, email support@meetingscheduler.com or create an issue in the repository.

---

**Built with ❤️ using Spring Boot**






