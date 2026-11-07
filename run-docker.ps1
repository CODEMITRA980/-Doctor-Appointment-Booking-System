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
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to build common-libs. Exiting."
    exit $LASTEXITCODE
}
cd ..

foreach ($service in $services) {
    Write-Host "Building $service..."
    cd $service
    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to build $service. Exiting."
        exit $LASTEXITCODE
    }
    cd ..
}

Write-Host "Building Docker images..."
docker compose build

Write-Host "Starting all services with Docker Compose..."
docker compose up -d

Write-Host "All services started successfully in Docker!"
