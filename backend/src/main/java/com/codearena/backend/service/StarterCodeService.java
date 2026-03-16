package com.codearena.backend.service;

import com.codearena.backend.dto.StarterCodeDTO;

import java.util.List;

public interface StarterCodeService {
    List<StarterCodeDTO> getStarterCodesByQuestionId(String questionId);

    StarterCodeDTO getStarterCodeByQuestionAndLanguage(String questionId, String language);

    void deleteStarterCode(String id);
}
