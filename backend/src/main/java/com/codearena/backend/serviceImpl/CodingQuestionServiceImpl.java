package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.CodingQuestionDTO;
import com.codearena.backend.dto.StarterCodeDTO;
import com.codearena.backend.dto.TestCaseJsonDTO;
import com.codearena.backend.entity.CodingQuestion;
import com.codearena.backend.entity.CodingQuestionTags;
import com.codearena.backend.entity.StarterCode;
import com.codearena.backend.entity.TestCase;
import com.codearena.backend.exception.BadRequestException;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.repository.CodingQuestionRepository;
import com.codearena.backend.repository.CodingQuestionTagsRepository;
import com.codearena.backend.repository.StarterCodeRepository;
import com.codearena.backend.repository.TestCaseRepository;
import com.codearena.backend.service.CodingQuestionService;
//import jakarta.transaction.Transactional;
import com.codearena.backend.utils.constant.Difficulty;
import com.codearena.backend.utils.constant.Language;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional; // ✅ Not jakarta

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CodingQuestionServiceImpl implements CodingQuestionService {

    private final CodingQuestionRepository codingQuestionRepository;
    private final TestCaseRepository testCaseRepository;
    private final CodingQuestionTagsRepository codingQuestionTagsRepository;
    private final StarterCodeRepository starterCodeRepository;


    private CodingQuestionDTO toDTO(CodingQuestion question) {
        CodingQuestionDTO dto = new CodingQuestionDTO();
        dto.setId(question.getId());
        dto.setTitle(question.getTitle());
        dto.setDescription(question.getDescription());
        dto.setInputFormat(question.getInputFormat());
        dto.setOutputFormat(question.getOutputFormat());
        dto.setConstraints(question.getConstraints());
        dto.setDifficulty(question.getDifficulty());
        dto.setPoints(question.getPoints());
        dto.setTimeLimit(question.getTimeLimit());
        dto.setMemoryLimit(question.getMemoryLimit());

        // tags
        List<CodingQuestionTags> tags = codingQuestionTagsRepository.findByCodingQuestionId(question.getId());
        dto.setTags(tags.stream().map(CodingQuestionTags::getName).collect(Collectors.toList()));

        // testcases
        List<TestCase> tcs = testCaseRepository.findByCodingQuestionIdOrderByOrderIndexAsc(question.getId());
        dto.setTestCases(tcs.stream().map(tc -> {
            TestCaseJsonDTO tcDto = new TestCaseJsonDTO();
            tcDto.setId(tc.getId());
            tcDto.setInputData(tc.getInputData());
            tcDto.setExpectedOutput(tc.getExpectedOutput());
            tcDto.setSample(tc.isSample());
            tcDto.setOrderIndex(tc.getOrderIndex());
            tcDto.setExplanation(tc.getExplanation());
            return tcDto;
        }).collect(Collectors.toList()));


        // Starter Codes
        List<StarterCode> scList = starterCodeRepository.findByCodingQuestionId(question.getId());

        dto.setStarterCodes(
                scList.stream().map(sc -> {
                    StarterCodeDTO scDto = new StarterCodeDTO();
                    scDto.setId(sc.getId());
                    scDto.setLanguage(sc.getLanguage().name().toLowerCase());
                    scDto.setVersion(sc.getVersion());
                    scDto.setCodeTemplate(sc.getCodeTemplate());  // FIXED
                    return scDto;
                }).collect(Collectors.toList())
        );



        return dto;
    }

    private void updateEntityFromDTO(CodingQuestion question, CodingQuestionDTO dto) {
        question.setTitle(dto.getTitle());
        question.setDescription(dto.getDescription());
        question.setInputFormat(dto.getInputFormat());
        question.setOutputFormat(dto.getOutputFormat());
        question.setConstraints(dto.getConstraints());
        question.setDifficulty(dto.getDifficulty());
        question.setPoints(dto.getPoints());
        question.setTimeLimit(dto.getTimeLimit());
        question.setMemoryLimit(dto.getMemoryLimit());
    }

    @Override
    @Transactional
    public CodingQuestionDTO createQuestion(CodingQuestionDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BadRequestException("Title required");
        }
        CodingQuestion question = new CodingQuestion();
        updateEntityFromDTO(question, dto);
        CodingQuestion saved = codingQuestionRepository.save(question);

        // tags
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            List<CodingQuestionTags> tags = dto.getTags().stream().map(name -> {
                CodingQuestionTags t = new CodingQuestionTags();
                t.setCodingQuestion(saved);
                t.setName(name);
                return t;
            }).collect(Collectors.toList());
            codingQuestionTagsRepository.saveAll(tags);
        }

        // testcases
        if (dto.getTestCases() != null && !dto.getTestCases().isEmpty()) {
            List<TestCase> testCases = dto.getTestCases().stream().map(tcDto -> {
                TestCase tc = new TestCase();
                tc.setCodingQuestion(saved);
                tc.setInputData(tcDto.getInputData());
                tc.setExpectedOutput(tcDto.getExpectedOutput());
                tc.setSample(tcDto.isSample());
                tc.setOrderIndex(tcDto.getOrderIndex());
                tc.setExplanation(tcDto.getExplanation());
                return tc;
            }).collect(Collectors.toList());
            testCaseRepository.saveAll(testCases);
        }

        // starter codes
        if (dto.getStarterCodes() != null && !dto.getStarterCodes().isEmpty()) {
            List<StarterCode> starterCodes = dto.getStarterCodes().stream().map(scDto -> {
                StarterCode sc = new StarterCode();
                sc.setCodingQuestion(saved);
                sc.setLanguage(Language.valueOf(scDto.getLanguage().trim().toUpperCase()));
                sc.setVersion(scDto.getVersion());
                sc.setCodeTemplate(scDto.getCodeTemplate());
                return sc;
            }).collect(Collectors.toList());

            starterCodeRepository.saveAll(starterCodes);
            saved.setStarterCodes(starterCodes);

        }

        return toDTO(saved);
    }

    @Override
    @Transactional
    public CodingQuestionDTO updateQuestion(String id, CodingQuestionDTO dto) {
        CodingQuestion question = codingQuestionRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Coding question not found"));
        updateEntityFromDTO(question, dto);
        CodingQuestion updated = codingQuestionRepository.save(question);

        // replace tags
        List<CodingQuestionTags> existingTags = codingQuestionTagsRepository.findByCodingQuestionId(id);
        if (!existingTags.isEmpty()) codingQuestionTagsRepository.deleteAll(existingTags);
        if (dto.getTags() != null) {
            List<CodingQuestionTags> newTags = dto.getTags().stream().map(name -> {
                CodingQuestionTags t = new CodingQuestionTags();
                t.setCodingQuestion(updated);
                t.setName(name);
                return t;
            }).collect(Collectors.toList());
            codingQuestionTagsRepository.saveAll(newTags);
        }

        // replace testcases
        List<TestCase> prev = testCaseRepository.findByCodingQuestionIdOrderByOrderIndexAsc(id);
        if (!prev.isEmpty()) testCaseRepository.deleteAll(prev);
        if (dto.getTestCases() != null) {
            List<TestCase> newTcs = dto.getTestCases().stream().map(tcDto -> {
                TestCase tc = new TestCase();
                tc.setCodingQuestion(updated);
                tc.setInputData(tcDto.getInputData());
                tc.setExpectedOutput(tcDto.getExpectedOutput());
                tc.setSample(tcDto.isSample());
                tc.setOrderIndex(tcDto.getOrderIndex());
                tc.setExplanation(tcDto.getExplanation());
                return tc;
            }).collect(Collectors.toList());
            testCaseRepository.saveAll(newTcs);
        }

        return toDTO(updated);
    }

    @Override
    public List<CodingQuestionDTO> getAllQuestions() {
        return codingQuestionRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public CodingQuestionDTO getQuestionById(String id) {
        CodingQuestion q = codingQuestionRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Coding question not found"));
        return toDTO(q);
    }

    // ✅ Delete a coding question with all its test cases
    @Override
    @Transactional
    public void deleteQuestion(String id) {
        CodingQuestion question = codingQuestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coding Question not found with id: " + id));

        // Delete dependent test cases first
        testCaseRepository.deleteByCodingQuestionId(id);

        // Delete tags
        codingQuestionTagsRepository.deleteByCodingQuestion_Id(id);

        // Then delete the coding question
        codingQuestionRepository.delete(question);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodingQuestionDTO> getByDifficultyAndCount(String difficulty, int count) {
        Difficulty level;
        try {
            level = Difficulty.valueOf(difficulty.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid difficulty level: " + difficulty);
        }

        List<CodingQuestion> allQuestions = codingQuestionRepository.findByDifficulty(level);
        if (allQuestions.isEmpty())
            throw new BadRequestException("No questions found for difficulty: " + difficulty);

        // ✅ Shuffle and pick 'count' questions randomly
        Collections.shuffle(allQuestions);
        List<CodingQuestion> selected = allQuestions.stream()
                .limit(count)
                .toList();

        return selected.stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodingQuestionDTO> getByDifficultyAndTopicAndCount(String difficulty, String topic, int count) {
        Difficulty level;
        try {
            level = Difficulty.valueOf(difficulty.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid difficulty level: " + difficulty);
        }

        List<CodingQuestion> questions = codingQuestionRepository.findByDifficultyAndTopic(level, topic);
        if (questions.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.shuffle(questions);

        return questions.stream()
                .limit(count)
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodingQuestionDTO> getMixedQuestions(int count) {

        // Validation already added, but double-check here
        if (count <= 0) {
            throw new BadRequestException("Count must be greater than 0");
        }
        if (count > 10) {
            throw new BadRequestException("Count cannot be greater than 10");
        }

        // Ratio: 40% easy, 40% medium, 20% hard
        int easyCount = (int) Math.ceil(count * 0.40);
        int mediumCount = (int) Math.ceil(count * 0.40);
        int hardCount = count - easyCount - mediumCount;

        // Special Rule: count = 1 → only EASY
        if (count == 1) {
            easyCount = 1;
            mediumCount = 0;
            hardCount = 0;
        }

        // Fetch all questions by difficulty
        List<CodingQuestion> easy = codingQuestionRepository.findByDifficulty(Difficulty.EASY);
        List<CodingQuestion> medium = codingQuestionRepository.findByDifficulty(Difficulty.MEDIUM);
        List<CodingQuestion> hard = codingQuestionRepository.findByDifficulty(Difficulty.HARD);

        // Shuffle each difficulty for randomness
        Collections.shuffle(easy);
        Collections.shuffle(medium);
        Collections.shuffle(hard);

        // Prepare final list
        List<CodingQuestion> finalList = new ArrayList<>();

        finalList.addAll(easy.stream().limit(easyCount).toList());
        finalList.addAll(medium.stream().limit(mediumCount).toList());
        finalList.addAll(hard.stream().limit(hardCount).toList());

        // Final shuffle for full mixing
        Collections.shuffle(finalList);

        return finalList.stream()
                .map(this::toDTO) // Includes tags, test cases, starter code
                .toList();
    }









}
