package com.codearena.backend.utils.constant;

/**
 * Centralized error messages
 * Provides consistency across the entire application
 */
public final class ErrorMessages {

    private ErrorMessages() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ==================== Authentication Errors ====================
    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String AUTHENTICATION_REQUIRED = "Authentication required";
    public static final String UNAUTHORIZED_ACCESS = "You don't have permission to access this resource";
    public static final String TOKEN_EXPIRED = "Authentication token has expired";
    public static final String INVALID_TOKEN = "Invalid authentication token";

    // ==================== User Errors ====================
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists, please login";
    public static final String USER_ALREADY_EXISTS = "User already exists";
    public static final String USERNAME_REQUIRED = "Username is required";
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String PASSWORD_REQUIRED = "Password is required";
    public static final String NAME_REQUIRED = "Name is required";
    public static final String INVALID_EMAIL_FORMAT = "Invalid email format";
    public static final String PASSWORD_TOO_SHORT = "Password must be at least %d characters";
    public static final String PASSWORD_MISMATCH = "Passwords do not match";
    public static final String OLD_PASSWORD_INCORRECT = "Current password is incorrect";
    public static final String NEW_PASSWORD_SAME = "New password must be different from current password";

    // ==================== OTP Errors ====================
    public static final String OTP_REQUIRED = "OTP is required";
    public static final String INVALID_OTP = "Invalid OTP";
    public static final String OTP_EXPIRED = "OTP has expired";
    public static final String OTP_SEND_FAILED = "Failed to send OTP. Please try again later";
    public static final String OTP_RATE_LIMIT = "Please wait %d minutes before requesting another OTP";

    // ==================== Room Errors ====================
    public static final String ROOM_NOT_FOUND = "Room not found with code: %s";
    public static final String ROOM_ALREADY_FULL = "Room is already full";
    public static final String ROOM_EXPIRED = "Room has expired";
    public static final String ROOM_NOT_ACTIVE = "Room is not active. Current status: %s";
    public static final String ROOM_ALREADY_STARTED = "Match has already started";
    public static final String ROOM_WAITING_FOR_PLAYER = "Cannot start room – waiting for another player to join";
    public static final String ROOM_NOT_CREATOR = "Only the room creator can perform this action";
    public static final String ROOM_CANNOT_JOIN_OWN = "You cannot join your own room. Please wait for another player";
    public static final String ROOM_NOT_PARTICIPANT = "You are not a participant in this room";
    public static final String ROOM_CODE_GENERATION_FAILED = "Failed to generate unique room code. Please try again";
    public static final String ROOM_INVALID_STATUS = "Room is no longer accepting players. Status: %s";

    // ==================== Question Errors ====================
    public static final String QUESTION_NOT_FOUND = "Question not found with id: %s";
    public static final String NO_QUESTIONS_ASSIGNED = "No questions assigned to this room";
    public static final String NO_QUESTIONS_AVAILABLE = "No questions available for difficulty: %s";
    public static final String QUESTION_TYPE_REQUIRED = "Question type is required";
    public static final String INVALID_QUESTION_TYPE = "Invalid question type: %s";
    public static final String QUESTION_TITLE_REQUIRED = "Question title is required";
    public static final String INVALID_DIFFICULTY = "Invalid difficulty value: %s";

    // ==================== Submission Errors ====================
    public static final String ALREADY_SUBMITTED = "You have already submitted an answer for this match";
    public static final String SUBMISSION_REQUIRED = "Submission data is required";
    public static final String CODING_ANSWERS_REQUIRED = "Coding answers are required";
    public static final String MCQ_ANSWERS_REQUIRED = "MCQ answers are required";
    public static final String SOURCE_CODE_REQUIRED = "Source code is required";
    public static final String LANGUAGE_REQUIRED = "Programming language is required";
    public static final String SELECTED_OPTION_REQUIRED = "Selected option is required";
    public static final String CODE_TOO_LONG = "Source code cannot exceed %d characters";
    public static final String INVALID_OPTION = "Selected option does not belong to the assigned question";

    // ==================== Practice Session Errors ====================
    public static final String SESSION_NOT_FOUND = "Practice session not found";
    public static final String SESSION_ALREADY_COMPLETED = "Session is already completed";
    public static final String SESSION_EXPIRED = "Session has expired";
    public static final String SESSION_NO_PERMISSION = "You don't have permission to access this session";
    public static final String SESSION_INVALID_QUESTION_TYPE = "Question type must be CODING or MCQ";

    // ==================== Chat Errors ====================
    public static final String MESSAGE_REQUIRED = "Message content is required";
    public static final String MESSAGE_EMPTY = "Message content cannot be empty";
    public static final String MESSAGE_TOO_LONG = "Message content too long (max %d characters)";
    public static final String ROOM_CODE_REQUIRED = "Room code is required";
    public static final String CHAT_PERMISSION_DENIED = "You don't have permission to view this chat history";

    // ==================== Test Case Errors ====================
    public static final String TEST_CASE_NOT_FOUND = "Test case not found with id: %s";
    public static final String NO_TEST_CASES = "No test cases found for question: %s";
    public static final String TEST_CASE_REQUIRED = "At least one test case is required";

    // ==================== Validation Errors ====================
    public static final String FIELD_REQUIRED = "%s is required";
    public static final String FIELD_TOO_SHORT = "%s must be at least %d characters";
    public static final String FIELD_TOO_LONG = "%s cannot exceed %d characters";
    public static final String FIELD_OUT_OF_RANGE = "%s must be between %d and %d";
    public static final String INVALID_FORMAT = "Invalid %s format";
    public static final String VALUE_TOO_SMALL = "%s must be at least %d";
    public static final String VALUE_TOO_LARGE = "%s cannot exceed %d";

    // ==================== Code Execution Errors ====================
    public static final String COMPILATION_ERROR = "Compilation Error";
    public static final String RUNTIME_ERROR = "Runtime Error";
    public static final String WRONG_ANSWER = "Wrong Answer";
    public static final String TIME_LIMIT_EXCEEDED = "Time Limit Exceeded";
    public static final String MEMORY_LIMIT_EXCEEDED = "Memory Limit Exceeded";
    public static final String EXECUTION_FAILED = "Code execution failed: %s";
    public static final String SYSTEM_ERROR = "System Error: %s";

    // ==================== Email Errors ====================
    public static final String EMAIL_SEND_FAILED = "Failed to send email to %s";
    public static final String EMAIL_SERVICE_UNAVAILABLE = "Email service temporarily unavailable. Please try again later";
    public static final String INVALID_RECIPIENT = "Invalid recipient email address";

    // ==================== AI Service Errors ====================
    public static final String AI_SERVICE_UNAVAILABLE = "AI service is temporarily unavailable";
    public static final String AI_REQUEST_FAILED = "AI request failed: %s";
    public static final String AI_TIMEOUT = "AI request timed out. Please try again";
    public static final String AI_RESPONSE_INVALID = "AI response could not be parsed. Please try again";
    public static final String AI_FALLBACK_ACTIVATED = "AI service unavailable, using fallback logic";

    // ==================== General Errors ====================
    public static final String RESOURCE_NOT_FOUND = "%s not found";
    public static final String RESOURCE_ALREADY_EXISTS = "%s already exists";
    public static final String OPERATION_FAILED = "Operation failed: %s";
    public static final String INVALID_REQUEST = "Invalid request: %s";
    public static final String INTERNAL_SERVER_ERROR = "An unexpected error occurred. Please try again later";
    public static final String SERVICE_UNAVAILABLE = "Service temporarily unavailable. Please try again later";
    public static final String BAD_REQUEST = "Bad request: %s";
    public static final String FORBIDDEN = "Access forbidden: %s";

    // ==================== Database Errors ====================
    public static final String DATABASE_ERROR = "Database error occurred";
    public static final String TRANSACTION_FAILED = "Transaction failed: %s";
    public static final String CONSTRAINT_VIOLATION = "Data constraint violation: %s";

    // ==================== Rate Limiting ====================
    public static final String RATE_LIMIT_EXCEEDED = "Too many requests. Please try again in %d seconds";

    // ==================== File Upload ====================
    public static final String FILE_TOO_LARGE = "File size exceeds maximum limit of %d MB";
    public static final String INVALID_FILE_TYPE = "Invalid file type. Allowed types: %s";
    public static final String FILE_UPLOAD_FAILED = "File upload failed: %s";

    /**
     * Format error message with parameters
     */
    public static String format(String message, Object... args) {
        return String.format(message, args);
    }
}