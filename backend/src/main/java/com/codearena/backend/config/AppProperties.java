package com.codearena.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;

/**
 * Centralized configuration properties for the entire application
 * Replaces all hard-coded values with configurable properties
 */
@Configuration
@ConfigurationProperties(prefix = "app")
@Data
@Validated
public class AppProperties {

    private String name = "CodeArena";
    private String version = "1.0.0";
    private String frontendUrl = "http://localhost:5173";

    private final Room room = new Room();
    private final Practice practice = new Practice();
    private final Submission submission = new Submission();
    private final CodeExecution codeExecution = new CodeExecution();
    private final Question question = new Question();
    private final Chat chat = new Chat();
    private final Email email = new Email();
    private final Ai ai = new Ai();
    private final Validation validation = new Validation();
    private final Security security = new Security();

    @Data
    public static class Room {
        @Min(100000)
        private int codeMin = 100000;

        @Max(999999)
        private int codeMax = 999999;

        @Min(1)
        @Max(100)
        private int codeGenerationMaxAttempts = 10;

        @Min(1)
        @Max(50)
        private int maxQuestions = 20;

        @Min(1)
        private int minQuestions = 1;

        @Min(5)
        @Max(300)
        private int maxDuration = 180; // minutes

        @Min(1)
        private int minDuration = 5;

        @Min(1)
        private int defaultDuration = 30;

        private long expiryCheckInterval = 60000; // milliseconds
    }

    @Data
    public static class Practice {
        @NotBlank
        private String sessionPrefix = "PRACTICE-";

        @Min(1)
        @Max(100)
        private int maxQuestions = 50;

        @Min(1)
        private int minQuestions = 1;

        @Min(1)
        @Max(360)
        private int maxTimeMinutes = 180;

        @Min(1)
        private int minTimeMinutes = 1;

        @NotBlank
        private String defaultDifficulty = "MEDIUM";
    }

    @Data
    public static class Submission {
        @Min(100)
        @Max(50000)
        private int maxCodeLength = 10000;

        @Min(1)
        @Max(10)
        private int maxAttemptsPerQuestion = 3;

        @Min(5)
        @Max(120)
        private int timeoutSeconds = 30;
    }

    @Data
    public static class CodeExecution {
        @NotBlank
        private String pistonUrl = "https://emkc.org/api/v2/piston/execute";

        @Min(1)
        @Max(60)
        private int timeoutSeconds = 10;

        @Min(64)
        @Max(1024)
        private int maxMemoryMb = 256;

        private final LanguageVersions languageVersions = new LanguageVersions();

        @Data
        public static class LanguageVersions {
            private String python = "3.10.0";
            private String java = "15.0.2";
            private String cpp = "10.2.0";
            private String c = "10.2.0";
            private String javascript = "18.15.0";
        }
    }

    @Data
    public static class Question {
        @Min(10)
        @Max(500)
        private int maxTitleLength = 200;

        @Min(100)
        @Max(10000)
        private int maxDescriptionLength = 5000;

        @Min(1)
        @Max(1000)
        private int maxPoints = 100;

        @Min(1)
        private int minPoints = 1;

        @Min(1)
        private int defaultPoints = 10;

        private final Mcq mcq = new Mcq();
        private final Coding coding = new Coding();

        @Data
        public static class Mcq {
            @Min(2)
            @Max(10)
            private int maxOptions = 6;

            @Min(2)
            private int minOptions = 2;

            @Min(10)
            @Max(1000)
            private int maxOptionLength = 500;
        }

        @Data
        public static class Coding {
            @Min(1)
            @Max(100)
            private int maxTestCases = 20;

            @Min(1)
            private int minTestCases = 1;

            // FIXED: Changed @Min/@Max to use long values or use @DecimalMin/@DecimalMax for doubles
            @DecimalMin("0.1")
            @DecimalMax("60.0")
            private double maxTimeLimit = 10.0;

            @Min(64)
            @Max(2048)
            private int maxMemoryLimit = 512;
        }
    }

    @Data
    public static class Chat {
        @Min(1)
        @Max(5000)
        private int messageMaxLength = 1000;

        @Min(1)
        private int messageMinLength = 1;

        @Min(1)
        @Max(30)
        private int cleanupDays = 1;

        private String cleanupCron = "0 0 2 * * ?";
    }

    @Data
    public static class Email {
        @Min(1)
        @Max(168)
        private int invitationExpiryHours = 24;

        private boolean useHtml = true;
    }

    @Data
    public static class Ai {
        @NotBlank
        private String ollamaUrl = "http://localhost:11434";

        @NotBlank
        private String ollamaModel = "llama3";

        @Min(5)
        @Max(120)
        private int timeoutSeconds = 30;

        @Min(1)
        @Max(10)
        private int maxRetries = 3;

        private boolean fallbackEnabled = true;
    }

    @Data
    public static class Validation {
        @NotBlank
        private String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        @Min(6)
        @Max(128)
        private int passwordMinLength = 6;

        @Min(3)
        @Max(50)
        private int usernameMinLength = 3;

        @Min(3)
        @Max(50)
        private int usernameMaxLength = 50;
    }

    @Data
    public static class Security {
        private boolean rateLimitEnabled = true;

        @Min(10)
        @Max(1000)
        private int rateLimitMaxRequests = 100;

        @Min(1)
        @Max(60)
        private int rateLimitWindowMinutes = 1;
    }
}