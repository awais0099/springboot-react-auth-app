package com.project.auth_app_backend.helpers;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserHelper {

    public static UUID parseUUID(String uuid) {
        log.trace("UserHelper: Attempting to parse UUID string: '{}'", uuid);

        try {
            if (uuid == null || uuid.isBlank()) {
                log.warn("UserHelper: Parse failed - UUID string is null or blank");
                throw new IllegalArgumentException("UUID string cannot be null or empty");
            }

            UUID parsed = UUID.fromString(uuid);
            
            return parsed;

        } catch (IllegalArgumentException e) {
            log.error("UserHelper: Failed to parse UUID. Invalid format: '{}'", uuid);
            throw e;
        }
    }
}