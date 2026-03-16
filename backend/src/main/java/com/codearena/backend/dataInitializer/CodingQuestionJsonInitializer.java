package com.codearena.backend.dataInitializer;

import com.codearena.backend.dto.CodingQuestionJsonDTO;
import com.codearena.backend.entity.CodingQuestion;
import com.codearena.backend.entity.CodingQuestionTags;
import com.codearena.backend.entity.StarterCode;
import com.codearena.backend.entity.TestCase;
import com.codearena.backend.repository.CodingQuestionRepository;
import com.codearena.backend.repository.CodingQuestionTagsRepository;
import com.codearena.backend.repository.StarterCodeRepository;
import com.codearena.backend.repository.TestCaseRepository;
import com.codearena.backend.utils.constant.Difficulty;
import com.codearena.backend.utils.constant.Language;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CodingQuestionJsonInitializer implements CommandLineRunner {

    private final CodingQuestionRepository codingQuestionRepository;
    private final TestCaseRepository testCaseRepository;
    private final CodingQuestionTagsRepository codingQuestionTagsRepository;
    private final StarterCodeRepository starterCodeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(String... args) throws Exception {
        if (codingQuestionRepository.count() > 0) {
            System.out.println("✅ Coding questions already initialized.");
            return;
        }

        InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("data/coding_questions.json");

        if (inputStream == null) {
            System.err.println("❌ coding_questions.json not found in resources/data/");
            return;
        }

        List<CodingQuestionJsonDTO> questionList =
                Arrays.asList(objectMapper.readValue(inputStream, CodingQuestionJsonDTO[].class));

        int successCount = 0;

        for (CodingQuestionJsonDTO dto : questionList) {
            try {
                // ✅ Step 1: Save main question
                CodingQuestion question = new CodingQuestion();
                question.setTitle(dto.getTitle());
                question.setDescription(dto.getDescription());
                question.setInputFormat(dto.getInputFormat());
                question.setOutputFormat(dto.getOutputFormat());
                question.setConstraints(dto.getConstraints());

                // ✅ Handle difficulty safely
                try {
                    question.setDifficulty(dto.getDifficulty());
                } catch (Exception e) {
                    System.err.println("⚠️ Invalid difficulty for question: " + dto.getTitle());
                    question.setDifficulty(Difficulty.EASY);
                }

                question.setPoints(dto.getPoints());
                question.setTimeLimit(dto.getTimeLimit());
                question.setMemoryLimit(dto.getMemoryLimit());

                CodingQuestion savedQuestion = codingQuestionRepository.save(question);

                // ✅ Step 2: Tags
                if (dto.getTags() != null && !dto.getTags().isEmpty()) {
                    List<CodingQuestionTags> tags = dto.getTags().stream().map(tagName -> {
                        CodingQuestionTags tag = new CodingQuestionTags();
                        tag.setCodingQuestion(savedQuestion);
                        tag.setName(tagName);
                        return tag;
                    }).toList();
                codingQuestionTagsRepository.saveAll(tags);
            }
            if (dto.getStarterCodes()!=null && !dto.getStarterCodes().isEmpty()){
                List<StarterCode> starterCodes = dto.getStarterCodes().stream().map(starterCode->{
                    StarterCode code = new StarterCode();
                    code.setCodingQuestion(savedQuestion);
                    code.setCodeTemplate(starterCode.getCodeTemplate());
                    code.setLanguage(Language.valueOf(starterCode.getLanguage().toUpperCase()));
                    code.setVersion(starterCode.getVersion());
                    return code;
                }).toList();
                starterCodeRepository.saveAll(starterCodes);
            }


                // ✅ Step 3: Test Cases
                if (dto.getTestCases() != null && !dto.getTestCases().isEmpty()) {
                    List<TestCase> testCases = dto.getTestCases().stream().map(tcDto -> {
                        TestCase tc = new TestCase();
                        tc.setCodingQuestion(savedQuestion);
                        tc.setInputData(tcDto.getInputData());
                        tc.setExpectedOutput(tcDto.getExpectedOutput());
                        tc.setSample(tcDto.isSample());
                        tc.setOrderIndex(tcDto.getOrderIndex());
                        tc.setExplanation(tcDto.getExplanation());
                        return tc;
                    }).toList();

                    testCaseRepository.saveAll(testCases);
                }

                successCount++;

            } catch (Exception e) {
                System.err.println("❌ Failed to save question: " + dto.getTitle());
                e.printStackTrace();
            }
        }

        System.out.println("✅ " + successCount + " coding questions loaded successfully!");
    }
}
