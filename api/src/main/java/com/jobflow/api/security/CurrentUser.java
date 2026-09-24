package com.jobflow.api.security;

import java.util.UUID;

public class CurrentUser {
    private static final ThreadLocal<UUID> CURRENT_USER_ID = new ThreadLocal<>();

    public static void set(UUID userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static UUID get() {
        UUID id = CURRENT_USER_ID.get();
        if (id == null) {
            throw new IllegalStateException("No authenticated user in context");
        }
        return id;
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
    }
}