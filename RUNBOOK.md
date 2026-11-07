# Lumina Care — Observability & Cache Runbook

This runbook covers operational procedures for managing the AOP and Redis Caching layers in the Lumina Care architecture.

## 1. How to Clear a Specific Cache

In the event of corrupted data or an emergency schema change, caches can be cleared natively or programmatically.

### Option A: Via Redis CLI (Targeted)
To clear the `doctors:v1` cache for environment `prod` and app `doctor-service`:
```bash
redis-cli --scan --pattern "doctor-service:prod:doctors:v1::*" | xargs redis-cli del
```

### Option B: Via Spring Boot Actuator
Ensure `management.endpoint.caches.enabled=true` is set.
```bash
# View all caches
curl -X GET http://localhost:8080/actuator/caches

# Clear specific cache
curl -X DELETE "http://localhost:8080/actuator/caches?cache=doctors:v1"
```

## 2. Debugging Aspects Not Firing

If an aspect (e.g., `@Loggable` or `@Cacheable`) is not triggering:

1. **Self-Invocation Problem (Proxy Bypass)**:
   - Ensure you are NOT calling the annotated method from *within the same class*. Spring AOP proxies only intercept external calls.
   - *Fix*: Move the annotated method to a separate bean, or inject `ApplicationContext` to fetch the proxy.

2. **Bean Not Managed**:
   - Ensure the class containing the method is annotated with `@Service`, `@Component`, or `@RestController`.

3. **Check Logging Level**:
   - Set AOP logging to TRACE in `application.yml` temporarily:
     ```yaml
     logging.level.org.springframework.aop: TRACE
     logging.level.org.springframework.cache: TRACE
     ```

## 3. Disabling Features (Graceful Degradation)

If Redis or aspects are causing critical production issues, disable them via environment variables dynamically (requires Spring Cloud Config / Actuator Refresh or a reboot):

- **Disable Caching Completely**:
  Set `spring.cache.type=none`
- **Disable Execution Time Aspect**:
  Set `app.aop.execution-time.enabled=false`
- **Disable PII Masking (Debugging only)**:
  Set `app.aop.logging.mask-fields=` (blank)

## 4. Validating Multi-Tenant Isolation
If cross-tenant leakage is suspected, query Redis directly:
```bash
redis-cli
127.0.0.1:6379> KEYS *patients*
```
You should *always* see `orgId` prefixes:
`patient-service:prod:patients:v1::orgA::1`
If you see `patient-service:prod:patients:v1::1`, the `TenantGuardInterceptor` or `CacheKeyBuilder` is bypassed. Escalate immediately.
