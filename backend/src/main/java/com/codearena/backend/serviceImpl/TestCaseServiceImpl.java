package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.TestCaseJsonDTO;
import com.codearena.backend.entity.CodingQuestion;
import com.codearena.backend.entity.TestCase;
import com.codearena.backend.exception.BadRequestException;
import com.codearena.backend.repository.CodingQuestionRepository;
import com.codearena.backend.repository.TestCaseRepository;
import com.codearena.backend.service.TestCaseService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final CodingQuestionRepository codingQuestionRepository;

    public TestCaseServiceImpl(TestCaseRepository testCaseRepository,
                           CodingQuestionRepository codingQuestionRepository) {

        this.testCaseRepository = testCaseRepository;
        this.codingQuestionRepository = codingQuestionRepository;
    }


    @Override
    @Transactional
    public TestCaseJsonDTO create(String questionId, TestCaseJsonDTO dto) {
        CodingQuestion q = codingQuestionRepository.findById(questionId)
                .orElseThrow(() -> new BadRequestException("Question not found"));
        TestCase tc = new TestCase();
        tc.setCodingQuestion(q);
        tc.setInputData(dto.getInputData());
        tc.setExpectedOutput(dto.getExpectedOutput());
        tc.setSample(dto.isSample());
        tc.setOrderIndex(dto.getOrderIndex());
        tc.setExplanation(dto.getExplanation());
        TestCase saved = testCaseRepository.save(tc);
        dto.setId(saved.getId());
        return dto;
    }

    @Override
    @Transactional
    public TestCaseJsonDTO update(String id, TestCaseJsonDTO dto) {
        TestCase tc = testCaseRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Test case not found"));
        tc.setInputData(dto.getInputData());
        tc.setExpectedOutput(dto.getExpectedOutput());
        tc.setSample(dto.isSample());
        tc.setOrderIndex(dto.getOrderIndex());
        tc.setExplanation(dto.getExplanation());
        TestCase saved = testCaseRepository.save(tc);
        dto.setId(saved.getId());
        return dto;
    }

    @Override
    public void delete(String id) {
        TestCase tc = testCaseRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Test case not found"));
        testCaseRepository.delete(tc);
    }

    @Override
    public List<TestCaseJsonDTO> getByQuestionId(String questionId) {
        return testCaseRepository.findByCodingQuestionIdOrderByOrderIndexAsc(questionId).stream().map(tc -> {
            TestCaseJsonDTO dto = new TestCaseJsonDTO();
            dto.setId(tc.getId());
            dto.setInputData(tc.getInputData());
            dto.setExpectedOutput(tc.getExpectedOutput());
            dto.setSample(tc.isSample());
            dto.setOrderIndex(tc.getOrderIndex());
            dto.setExplanation(tc.getExplanation());
            return dto;
        }).collect(Collectors.toList());
    }
}
