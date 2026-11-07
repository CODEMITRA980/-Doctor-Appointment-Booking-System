$services = @(
    "config-server",
    "eureka-server",
    "api-gateway",
    "auth-service",
    "patient-service",
    "doctor-service",
    "appointment-service",
    "billing-service",
    "notification-service",
    "admin-server"
)

Write-Host "Building common-libs..."
cd common-libs
mvn clean install -DskipTests
cd ..

foreach ($service in $services) {
    Write-Host "Starting $service..."
    Start-Process powershell -ArgumentList "-NoExit -Command `"cd $service; mvn spring-boot:run`""
    Start-Sleep -Seconds 5
}

Write-Host "All services started."
