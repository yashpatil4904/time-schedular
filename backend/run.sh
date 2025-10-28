#!/bin/bash
echo "Starting ChronoSync Backend..."

# Check if target directory exists
if [ ! -d "target/classes" ]; then
    echo "ERROR: Backend not compiled. Need Maven to compile."
    echo "Please install Maven or use IntelliJ IDEA."
    exit 1
fi

# Get all JAR files from target/dependency
CLASSPATH="target/classes"
if [ -d "target/dependency" ]; then
    for jar in target/dependency/*.jar; do
        CLASSPATH="$CLASSPATH:$jar"
    done
fi

echo "Starting with classpath..."
java -cp "$CLASSPATH" com.meetingscheduler.MeetingSchedulerApplication



