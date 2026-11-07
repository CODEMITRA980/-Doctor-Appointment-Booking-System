package com.luminacare.common.constants;

/**
 * Defines standard cache names used across the system to prevent magic strings.
 * Includes versioning to allow safe invalidation on schema changes.
 */
public final class CacheNames {
    private CacheNames() {
        // Prevent instantiation
    }

    public static final String DOCTORS = "doctors:v1";
    public static final String DOCTORS_ALL = "doctors_all:v1";
    public static final String PATIENTS = "patients:v1";
    public static final String APPOINTMENTS = "appointments:v1";
    public static final String SCHEDULES = "schedules:v1";
    public static final String PRESCRIPTIONS = "prescriptions:v1";
    public static final String REFERENCE_DATA = "reference_data:v1";
}
