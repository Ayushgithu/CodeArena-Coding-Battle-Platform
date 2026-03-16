package com.codearena.backend.service;

import com.codearena.backend.dto.AIReviewRequest;
import com.codearena.backend.dto.AIReviewResponse;

public interface AIService {
    AIReviewResponse getAIReview(AIReviewRequest request);
}
