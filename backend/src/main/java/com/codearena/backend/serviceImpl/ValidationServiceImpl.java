package com.codearena.backend.serviceImpl;

import com.codearena.backend.config.AppProperties;
import com.codearena.backend.dto.*;
import com.codearena.backend.exception.BadRequestException;
import com.codearena.backend.service.ValidationService;
import com.codearena.backend.utils.constant.Language;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Centralized validation service implementation
 * Replaces scattered validation logic with consistent, reusable validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationServiceImpl implements ValidationService {

    private final AppProperties appProperties;
    private Pattern emailPattern;

    @Override
    public void validateEmail(String email, String fieldName) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException(fieldName + " is required");
        }

        if (emailPattern == null) {
            emailPattern = Pattern.compile(appProperties.getValidation().getEmailPattern());
        }

        if (!emailPattern.matcher(email).matches()) {
            throw new BadRequestException("Invalid " + fieldName + " format");
        }
    }

    @Override
    public void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BadRequestException("Password is required");
        }

        int minLength = appProperties.getValidation().getPasswordMinLength();
        if (password.length() < minLength) {
            throw new BadRequestException("Password must be at least " + minLength + " characters");
        }
    }

    @Override
    public void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new BadRequestException("Username is required");
        }

        int minLength = appProperties.getValidation().getUsernameMinLength();
        int maxLength = appProperties.getValidation().getUsernameMaxLength();

        if (username.length() < minLength) {
            throw new BadRequestException("Username must be at least " + minLength + " characters");
        }

        if (username.length() > maxLength) {
            throw new BadRequestException("Username cannot exceed " + maxLength + " characters");
        }
    }

    @Override
    public void validateRoomRequest(RoomRequestDTO request) {
        if (request == null) {
            throw new BadRequestException("Room request cannot be null");
        }

        // Validate number of questions
        int minQuestions = appProperties.getRoom().getMinQuestions();
        int maxQuestions = appProperties.getRoom().getMaxQuestions();

        if (request.getNoOfQuestions() < minQuestions) {
            throw new BadRequestException("Number of questions must be at least " + minQuestions);
        }

        if (request.getNoOfQuestions() > maxQuestions) {
            throw new BadRequestException("Number of questions cannot exceed " + maxQuestions);
        }

        // Validate duration
        int minDuration = appProperties.getRoom().getMinDuration();
        int maxDuration = appProperties.getRoom().getMaxDuration();

        if (request.getDuration() < minDuration) {
            throw new BadRequestException("Duration must be at least " + minDuration + " minutes");
        }

        if (request.getDuration() > maxDuration) {
            throw new BadRequestException("Duration cannot exceed " + maxDuration + " minutes");
        }

        // Validate question type
        if (request.getQuestionType() == null || request.getQuestionType().isBlank()) {
            throw new BadRequestException("Question type is required");
        }
    }

    @Override
    public void validatePracticeRequest(PracticeMatchRequestDTO request) {
        if (request == null) {
            throw new BadRequestException("Practice request cannot be null");
        }

        // Validate question type
        if (request.getQuestionType() == null || request.getQuestionType().isBlank()) {
            throw new BadRequestException("Question type is required");
        }

        String questionType = request.getQuestionType().toUpperCase();
        if (!questionType.equals("CODING") && !questionType.equals("MCQ")) {
            throw new BadRequestException("Question type must be CODING or MCQ");
        }

        // Validate max questions
        int minQuestions = appProperties.getPractice().getMinQuestions();
        int maxQuestions = appProperties.getPractice().getMaxQuestions();

        if (request.getMaxQuestions() < minQuestions) {
            throw new BadRequestException("At least " + minQuestions + " question is required");
        }

        if (request.getMaxQuestions() > maxQuestions) {
            throw new BadRequestException("Maximum " + maxQuestions + " questions allowed per session");
        }

        // Validate time limit
        int minTime = appProperties.getPractice().getMinTimeMinutes();
        int maxTime = appProperties.getPractice().getMaxTimeMinutes();

        if (request.getTimeLimitMinutes() < minTime) {
            throw new BadRequestException("Time limit must be at least " + minTime + " minute");
        }

        if (request.getTimeLimitMinutes() > maxTime) {
            throw new BadRequestException("Time limit cannot exceed " + maxTime + " minutes");
        }
    }

    @Override
    public void validateChatMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new BadRequestException("Message content cannot be empty");
        }

        int maxLength = appProperties.getChat().getMessageMaxLength();
        if (message.length() > maxLength) {
            throw new BadRequestException("Message content too long (max " + maxLength + " characters)");
        }
    }

    @Override
    public void validateCodingQuestion(CodingQuestionDTO dto) {
        if (dto == null) {
            throw new BadRequestException("Question data cannot be null");
        }

        // Validate title
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BadRequestException("Question title is required");
        }

        int maxTitleLength = appProperties.getQuestion().getMaxTitleLength();
        if (dto.getTitle().length() > maxTitleLength) {
            throw new BadRequestException("Title cannot exceed " + maxTitleLength + " characters");
        }

        // Validate description
        if (dto.getDescription() != null) {
            int maxDescLength = appProperties.getQuestion().getMaxDescriptionLength();
            if (dto.getDescription().length() > maxDescLength) {
                throw new BadRequestException("Description cannot exceed " + maxDescLength + " characters");
            }
        }

        // Validate points
        int minPoints = appProperties.getQuestion().getMinPoints();
        int maxPoints = appProperties.getQuestion().getMaxPoints();

        if (dto.getPoints() < minPoints || dto.getPoints() > maxPoints) {
            throw new BadRequestException("Points must be between " + minPoints + " and " + maxPoints);
        }

        // Validate test cases
        if (dto.getTestCases() != null) {
            int minTestCases = appProperties.getQuestion().getCoding().getMinTestCases();
            int maxTestCases = appProperties.getQuestion().getCoding().getMaxTestCases();

            if (dto.getTestCases().size() < minTestCases) {
                throw new BadRequestException("At least " + minTestCases + " test case is required");
            }

            if (dto.getTestCases().size() > maxTestCases) {
                throw new BadRequestException("Cannot exceed " + maxTestCases + " test cases");
            }

            // Validate individual test cases
            dto.getTestCases().forEach(this::validateTestCase);
        }
    }

    @Override
    public void validateMcqQuestion(McqQuestionJsonDTO dto) {
        if (dto == null) {
            throw new BadRequestException("Question data cannot be null");
        }

        // Validate title
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BadRequestException("Question title is required");
        }

        int maxTitleLength = appProperties.getQuestion().getMaxTitleLength();
        if (dto.getTitle().length() > maxTitleLength) {
            throw new BadRequestException("Title cannot exceed " + maxTitleLength + " characters");
        }

        // Validate options
        if (dto.getOptions() == null || dto.getOptions().isEmpty()) {
            throw new BadRequestException("At least one option is required");
        }

        int minOptions = appProperties.getQuestion().getMcq().getMinOptions();
        int maxOptions = appProperties.getQuestion().getMcq().getMaxOptions();

        if (dto.getOptions().size() < minOptions) {
            throw new BadRequestException("At least " + minOptions + " options are required");
        }

        if (dto.getOptions().size() > maxOptions) {
            throw new BadRequestException("Cannot exceed " + maxOptions + " options");
        }

        // Validate at least one correct answer
        long correctCount = dto.getOptions().stream().filter(McqOptionJsonDTO::isCorrect).count();
        if (correctCount == 0) {
            throw new BadRequestException("At least one option must be marked as correct");
        }

        // Validate option lengths
        int maxOptionLength = appProperties.getQuestion().getMcq().getMaxOptionLength();
        for (McqOptionJsonDTO option : dto.getOptions()) {
            if (option.getOptionText() == null || option.getOptionText().isBlank()) {
                throw new BadRequestException("Option text cannot be empty");
            }
            if (option.getOptionText().length() > maxOptionLength) {
                throw new BadRequestException("Option text cannot exceed " + maxOptionLength + " characters");
            }
        }

        // Validate time limit
        if (dto.getTimeLimit() <= 0) {
            throw new BadRequestException("Time limit must be positive");
        }
    }

    @Override
    public void validateSubmissionRequest(SubmissionRequestDTO request) {
        if (request == null) {
            throw new BadRequestException("Submission request cannot be null");
        }

        if (request.getRoomCode() == 0) {
            throw new BadRequestException("Room code is required");
        }

        if (request.getQuestionType() == null || request.getQuestionType().isBlank()) {
            throw new BadRequestException("Question type is required");
        }

        String questionType = normalizeQuestionType(request.getQuestionType());

        if ("CODING".equals(questionType)) {
            validateCodingSubmission(request);
        } else if ("MCQ".equals(questionType)) {
            validateMcqSubmission(request);
        } else {
            throw new BadRequestException("Invalid question type: " + request.getQuestionType());
        }
    }

    @Override
    public void validateRoomInvitation(RoomInvitationDTO invitation) {
        if (invitation == null) {
            throw new BadRequestException("Invitation data cannot be null");
        }

        validateEmail(invitation.getRecipientEmail(), "Recipient email");

        if (invitation.getRoomCode() < 100000 || invitation.getRoomCode() > 999999) {
            throw new BadRequestException("Invalid room code");
        }
    }

    @Override
    public void validateSignupRequest(SignupRequestDTO request) {
        if (request == null) {
            throw new BadRequestException("Signup request cannot be null");
        }

        validateEmail(request.getEmail(), "Email");
        validatePassword(request.getPassword());

        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Name is required");
        }

        if (request.getName().length() > 100) {
            throw new BadRequestException("Name cannot exceed 100 characters");
        }
    }

    @Override
    public void validateChangePassword(ChangePasswordDTO dto) {
        if (dto == null) {
            throw new BadRequestException("Change password request cannot be null");
        }

        if (dto.getOldPassword() == null || dto.getOldPassword().isBlank()) {
            throw new BadRequestException("Current password is required");
        }

        if (dto.getNewPassword() == null || dto.getNewPassword().isBlank()) {
            throw new BadRequestException("New password is required");
        }

        validatePassword(dto.getNewPassword());

        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }
    }

    @Override
    public void validateResetPassword(ResetPasswordDTO dto) {
        if (dto == null) {
            throw new BadRequestException("Reset password request cannot be null");
        }

        validateEmail(dto.getEmail(), "Email");
        validatePassword(dto.getNewPassword());

        if (dto.getOtp() == null || dto.getOtp().isBlank()) {
            throw new BadRequestException("OTP is required");
        }

        if (dto.getOtp().length() != 6 || !dto.getOtp().matches("\\d{6}")) {
            throw new BadRequestException("OTP must be 6 digits");
        }
    }

    @Override
    public void validateLoginRequest(LoginRequestDTO request) {
        if (request == null) {
            throw new BadRequestException("Login request cannot be null");
        }

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new BadRequestException("Username/Email is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }
    }

    @Override
    public void validateTestCase(TestCaseJsonDTO testCase) {
        if (testCase == null) {
            throw new BadRequestException("Test case cannot be null");
        }

        if (testCase.getInputData() == null) {
            throw new BadRequestException("Input data is required");
        }

        if (testCase.getExpectedOutput() == null) {
            throw new BadRequestException("Expected output is required");
        }

        if (testCase.getInputData().length() > 5000) {
            throw new BadRequestException("Input data too long (max 5000 characters)");
        }

        if (testCase.getExpectedOutput().length() > 5000) {
            throw new BadRequestException("Expected output too long (max 5000 characters)");
        }
    }

    @Override
    public void validateStarterCode(StarterCodeDTO starterCode) {
        if (starterCode == null) {
            throw new BadRequestException("Starter code cannot be null");
        }

        if (starterCode.getLanguage() == null || starterCode.getLanguage().isBlank()) {
            throw new BadRequestException("Language is required");
        }

        // Validate language enum
        try {
            Language.valueOf(starterCode.getLanguage().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid language: " + starterCode.getLanguage());
        }

        if (starterCode.getCodeTemplate() == null) {
            throw new BadRequestException("Code template is required");
        }

        int maxCodeLength = appProperties.getSubmission().getMaxCodeLength();
        if (starterCode.getCodeTemplate().length() > maxCodeLength) {
            throw new BadRequestException("Code template too long (max " + maxCodeLength + " characters)");
        }
    }

//    @Override
//    public void validateCodingLanguage(CodingLanguageDTO language) {
//        if (language == null) {
//            throw new BadRequestException("Coding language cannot be null");
//        }
//
//        if (language.getName() == null || language.getName().isBlank()) {
//            throw new BadRequestException("Language name is required");
//        }
//
//        if (language.getSlug() == null || language.getSlug().isBlank()) {
//            throw new BadRequestException("Language slug is required");
//        }
//
//        if (language.getName().length() > 50) {
//            throw new BadRequestException("Language name too long (max 50 characters)");
//        }
//
//        if (language.getSlug().length() > 20) {
//            throw new BadRequestException("Language slug too long (max 20 characters)");
//        }
//
//        if (language.getVersion() != null && language.getVersion().length() > 20) {
//            throw new BadRequestException("Version too long (max 20 characters)");
//        }
//    }

    @Override
    public void validateDropList(DropListDTO dropList) {
        if (dropList == null) {
            throw new BadRequestException("Drop list item cannot be null");
        }

        if (dropList.getLabelKey() == null || dropList.getLabelKey().isBlank()) {
            throw new BadRequestException("Label key is required");
        }

        if (dropList.getOptionValue() == null || dropList.getOptionValue().isBlank()) {
            throw new BadRequestException("Option value is required");
        }

        if (dropList.getLabelKey().length() > 100) {
            throw new BadRequestException("Label key too long (max 100 characters)");
        }

        if (dropList.getOptionValue().length() > 200) {
            throw new BadRequestException("Option value too long (max 200 characters)");
        }
    }

    @Override
    public void validateUserProfile(UserDetailsDTO userProfile) {
        if (userProfile == null) {
            throw new BadRequestException("User profile cannot be null");
        }

        if (userProfile.getName() != null && userProfile.getName().length() > 100) {
            throw new BadRequestException("Name too long (max 100 characters)");
        }

        if (userProfile.getTotalWin() < 0) {
            throw new BadRequestException("Total wins cannot be negative");
        }

        if (userProfile.getTotalLoss() < 0) {
            throw new BadRequestException("Total losses cannot be negative");
        }

        if (userProfile.getTotalBattle() < 0) {
            throw new BadRequestException("Total battles cannot be negative");
        }

        if (userProfile.getHighestStreak() < 0) {
            throw new BadRequestException("Highest streak cannot be negative");
        }

        if (userProfile.getUserRank() < 0) {
            throw new BadRequestException("User rank cannot be negative");
        }
    }

    @Override
    public void validateAIReviewRequest(AIReviewRequest request) {
        if (request == null) {
            throw new BadRequestException("AI review request cannot be null");
        }

        if (request.getProblemStatement() == null || request.getProblemStatement().isBlank()) {
            throw new BadRequestException("Problem statement is required");
        }

        if (request.getLanguage() == null || request.getLanguage().isBlank()) {
            throw new BadRequestException("Programming language is required");
        }

        if (request.getSourceCode() == null || request.getSourceCode().isBlank()) {
            throw new BadRequestException("Source code is required");
        }

        int maxCodeLength = appProperties.getSubmission().getMaxCodeLength();
        if (request.getSourceCode().length() > maxCodeLength) {
            throw new BadRequestException("Source code too long (max " + maxCodeLength + " characters)");
        }

        if (request.getProblemStatement().length() > 5000) {
            throw new BadRequestException("Problem statement too long (max 5000 characters)");
        }
    }

    @Override
    public void validateCodeExecutionRequest(CodeExecutionDTO request) {
        if (request == null) {
            throw new BadRequestException("Code execution request cannot be null");
        }

        if (request.getLanguage() == null || request.getLanguage().isBlank()) {
            throw new BadRequestException("Programming language is required");
        }

        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new BadRequestException("Source code is required");
        }

        int maxCodeLength = appProperties.getSubmission().getMaxCodeLength();
        if (request.getCode().length() > maxCodeLength) {
            throw new BadRequestException("Source code too long (max " + maxCodeLength + " characters)");
        }


    }

    // ========== PRIVATE HELPER METHODS ==========

    private void validateCodingSubmission(SubmissionRequestDTO request) {
        if (request.getCodingAnswers() == null || request.getCodingAnswers().isEmpty()) {
            throw new BadRequestException("Coding answers are required");
        }

        for (CodingAnswerDTO answer : request.getCodingAnswers()) {
            if (answer.getQuestionId() == null || answer.getQuestionId().isBlank()) {
                throw new BadRequestException("Question ID is required");
            }

            if (answer.getLanguage() == null || answer.getLanguage().isBlank()) {
                throw new BadRequestException("Programming language is required");
            }

            if (answer.getSourceCode() == null || answer.getSourceCode().isBlank()) {
                throw new BadRequestException("Source code is required");
            }

            int maxCodeLength = appProperties.getSubmission().getMaxCodeLength();
            if (answer.getSourceCode().length() > maxCodeLength) {
                throw new BadRequestException("Source code cannot exceed " + maxCodeLength + " characters");
            }
        }
    }

    private void validateMcqSubmission(SubmissionRequestDTO request) {
        if (request.getMcqAnswers() == null || request.getMcqAnswers().isEmpty()) {
            throw new BadRequestException("MCQ answers are required");
        }

        for (McqAnswerDTO answer : request.getMcqAnswers()) {
            if (answer.getQuestionId() == null || answer.getQuestionId().isBlank()) {
                throw new BadRequestException("Question ID is required");
            }

            if (answer.getSelectedOptionId() == null || answer.getSelectedOptionId().isBlank()) {
                throw new BadRequestException("Selected option is required");
            }
        }
    }

    private String normalizeQuestionType(String questionType) {
        if (questionType == null) {
            return null;
        }

        String normalized = questionType.toUpperCase();
        if (normalized.contains("CODING")) return "CODING";
        if (normalized.contains("MCQ")) return "MCQ";

        return normalized;
    }
}