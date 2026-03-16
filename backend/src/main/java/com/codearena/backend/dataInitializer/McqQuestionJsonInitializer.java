package com.codearena.backend.dataInitializer;

import com.codearena.backend.dto.McqQuestionJsonDTO;
import com.codearena.backend.entity.McqQuestion;
import com.codearena.backend.entity.McqQuestionOption;
import com.codearena.backend.entity.McqQuestionTag;
import com.codearena.backend.repository.McqQuestionOptionRepository;
import com.codearena.backend.repository.McqQuestionRepository;
import com.codearena.backend.repository.McqQuestionTagsRepository;
import com.codearena.backend.utils.constant.Difficulty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class McqQuestionJsonInitializer implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final McqQuestionRepository mcqQuestionRepository;
    private final McqQuestionOptionRepository mcqQuestionOptionRepository;
    private final McqQuestionTagsRepository mcqQuestionTagRepository;

    @Override
    public void run(String... args) throws Exception {
        if (mcqQuestionRepository.count() > 0) {
            System.out.println("✅ MCQ questions already initialized.");
            return;
        }

        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("data/mcq-questions.json");

        if (inputStream == null) {
            System.err.println("❌ mcq-questions.json not found in resources/data/");
            return;
        }

        List<McqQuestionJsonDTO> questions =
                Arrays.asList(objectMapper.readValue(inputStream, McqQuestionJsonDTO[].class));

        int successCount = 0;

        for (McqQuestionJsonDTO dto : questions) {
            try {
                McqQuestion question = new McqQuestion();
                question.setTitle(dto.getTitle());
                question.setDescription(dto.getDescription());

                try {
                    question.setDifficulty(dto.getDifficulty());
                } catch (Exception e) {
                    question.setDifficulty(Difficulty.EASY);
                }

                question.setPoints(dto.getPoints());
                question.setTimeLimit(dto.getTimeLimit());

                McqQuestion savedQuestion = mcqQuestionRepository.save(question);

                // ✅ Save options
                List<McqQuestionOption> options = dto.getOptions().stream()
                        .map(opt -> {
                            McqQuestionOption option = new McqQuestionOption();
                            option.setOptionText(opt.getOptionText());
                            option.setCorrect(opt.isCorrect());
                            option.setMcqQuestion(savedQuestion);
                            return option;
                        })
                        .collect(Collectors.toList());
                mcqQuestionOptionRepository.saveAll(options);

                // ✅ Save tags
                if (dto.getTags() != null && !dto.getTags().isEmpty()) {
                    List<McqQuestionTag> tags = dto.getTags().stream()
                            .map(tagName -> McqQuestionTag.builder()
                                    .tagName(tagName)
                                    .mcqQuestion(savedQuestion)
                                    .build())
                            .collect(Collectors.toList());
                    mcqQuestionTagRepository.saveAll(tags);
                }

                successCount++;
            } catch (Exception e) {
                System.err.println("❌ Failed to save MCQ: " + dto.getTitle());
                e.printStackTrace();
            }
        }

        System.out.println("✅ " + successCount + " MCQ Questions Initialized Successfully!");
    }
}
