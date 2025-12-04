#!/bin/bash

echo "=== Environment Check ==="
echo "Java Version:"
java -version 2>&1 | head -3

echo -e "\n=== Database Connection Check ==="
# Check if MySQL is running and accessible
if mysql -h localhost -P 3306 -u yimusi -pyimusi123456 -e "SELECT 1;" oilgas_test 2>/dev/null; then
    echo "✓ MySQL connection successful"
else
    echo "✗ MySQL connection failed - check if MySQL is running and credentials are correct"
fi

echo -e "\n=== Redis Connection Check ==="
# Check if Redis is running and accessible
if redis-cli -h localhost -p 6379 -a redis123456 ping 2>/dev/null | grep -q PONG; then
    echo "✓ Redis connection successful"
else
    echo "✗ Redis connection failed - check if Redis is running and password is correct"
fi

echo -e "\n=== Port Check ==="
# Check if port 8080 is available
if lsof -i :8080 >/dev/null 2>&1; then
    echo "✗ Port 8080 is in use"
else
    echo "✓ Port 8080 is available"
fi

echo -e "\n=== Logs Directory Check ==="
if [ -d "./logs" ]; then
    echo "✓ Logs directory exists"
    ls -la ./logs/ 2>/dev/null || echo "No log files yet"
else
    echo "⚠ Logs directory doesn't exist, will be created on first log"
fi

echo -e "\n=== Running Application in Foreground ==="
echo "Starting application. Press Ctrl+C to stop."
java -jar target/springboot-app-1.0.0-SNAPSHOT.jar