# 🚀 How to Start the Backend

## ⚠️ Problem
Maven (`mvn`) is not installed on your system, so you can't run `mvn spring-boot:run`.

---

## ✅ **BEST SOLUTION: Use IntelliJ IDEA** (Recommended - 5 minutes)

### Step 1: Download IntelliJ IDEA Community (Free)
https://www.jetbrains.com/idea/download/

### Step 2: Install and Open
1. Install IntelliJ IDEA
2. Click **Open**
3. Navigate to: `C:\Users\ASUS\OneDrive\Desktop\SE time schedular\project\backend`
4. Click **OK**

### Step 3: Wait for Indexing
- IntelliJ will automatically detect it's a Maven project
- It will download all dependencies (takes 2-3 minutes)
- Wait for progress bar at bottom to finish

### Step 4: Run the Application
1. In Project view (left side), expand: `src/main/java/com/meetingscheduler`
2. Right-click on `MeetingSchedulerApplication.java`
3. Click **"Run 'MeetingSchedule...'"** ▶️

### Step 5: Backend Started! ✅
You'll see in the console:
```
Started MeetingSchedulerApplication in 8.5 seconds (JVM running for 9.2)
Tomcat started on port(s): 8080 (http)
```

**Done!** Backend is running on `http://localhost:8080`

---

## 🔧 **Alternative: Install Maven**

### Option A: Using Winget (Windows Package Manager)
```powershell
# Open PowerShell as Administrator
winget install Apache.Maven
```

### Option B: Manual Installation
1. Download Maven: https://maven.apache.org/download.cgi
2. Extract to `C:\apache-maven-3.9.9`
3. Add to PATH:
   - Search "Environment Variables" in Windows
   - Click "Environment Variables"
   - Under "System Variables", find "Path"
   - Click "Edit" → "New"
   - Add: `C:\apache-maven-3.9.9\bin`
   - Click "OK" on all dialogs
4. **Restart your terminal**
5. Test: `mvn --version`

Then run:
```bash
cd backend
mvn spring-boot:run
```

---

## 🎯 **Quick Check**

After starting backend, open browser and go to:
```
http://localhost:8080/api/meetings/user/test-id
```

You should see a JSON response or error (not "connection refused").

---

## ✅ **Verification**

Backend is running correctly when you see:
```
2025-10-20 ... INFO ... Started MeetingSchedulerApplication
2025-10-20 ... INFO ... Tomcat started on port(s): 8080 (http)
```

**Then refresh your frontend and it will work!** 🚀

---

## 📞 **Still Having Issues?**

**Easiest solution**: Use IntelliJ IDEA Community (it's free and handles everything automatically)

The backend is **already compiled** and ready to run. You just need:
1. Maven to run it, OR
2. An IDE like IntelliJ to run it

**Recommended**: Install IntelliJ IDEA - it's the standard for Java development and will make everything easier! 💡



