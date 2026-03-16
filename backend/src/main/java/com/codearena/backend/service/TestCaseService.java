package com.codearena.backend.service;

import com.codearena.backend.dto.TestCaseJsonDTO;
import com.codearena.backend.dto.TestCaseJsonDTO;

import java.util.List;

public interface TestCaseService {
    TestCaseJsonDTO create(String questionId, TestCaseJsonDTO dto);
    TestCaseJsonDTO update(String id, TestCaseJsonDTO dto);
    void delete(String id);
    List<TestCaseJsonDTO> getByQuestionId(String questionId);
}
