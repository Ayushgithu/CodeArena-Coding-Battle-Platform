package com.codearena.backend.service;

import com.codearena.backend.dto.*;

/**
 * Centralized validation service interface
 * Provides consistent validation across the application
 */
public interface ValidationService {

    /**
     * Validate email format
     */
    void validateEmail(String email, String fieldName);

    /**
     * Validate password
     */
    void validatePassword(String password);

    /**
     * Validate username
     */
    void validateUsername(String username);

    /**
     * Validate room creation request
     */
    void validateRoomRequest(RoomRequestDTO request);

    /**
     * Validate practice session request
     */
    void validatePracticeRequest(PracticeMatchRequestDTO request);

    /**
     * Validate chat message
     */
    void validateChatMessage(String message);

    /**
     * Validate coding question
     */
    void validateCodingQuestion(CodingQuestionDTO dto);

    /**
     * Validate MCQ question
     */
    void validateMcqQuestion(McqQuestionJsonDTO dto);

    /**
     * Validate submission request
     */
    void validateSubmissionRequest(SubmissionRequestDTO request);

    /**
     * Validate room invitation
     */
    void validateRoomInvitation(RoomInvitationDTO invitation);

    /**
     * Validate signup request
     */
    void validateSignupRequest(SignupRequestDTO request);

    /**
     * Validate change password request
     */
    void validateChangePassword(ChangePasswordDTO dto);

    /**
     * Validate reset password request
     */
    void validateResetPassword(ResetPasswordDTO dto);

    /**
     * Validate login request
     */
    void validateLoginRequest(LoginRequestDTO request);

    /**
     * Validate test case
     */
    void validateTestCase(TestCaseJsonDTO testCase);

    /**
     * Validate starter code
     */
    void validateStarterCode(StarterCodeDTO starterCode);

    /**
     * Validate coding language
     */
//    void validateCodingLanguage(CodingLanguageDTO language);

    /**
     * Validate dropdown list item
     */
    void validateDropList(DropListDTO dropList);

    /**
     * Validate user profile
     */
    void validateUserProfile(UserDetailsDTO userProfile);

    /**
     * Validate AI review request
     */
    void validateAIReviewRequest(AIReviewRequest request);

    /**
     * Validate code execution request
     */
    void validateCodeExecutionRequest(CodeExecutionDTO request);
}