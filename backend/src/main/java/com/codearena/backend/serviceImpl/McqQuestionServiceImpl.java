package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.McqOptionJsonDTO;
import com.codearena.backend.dto.McqQuestionJsonDTO;
import com.codearena.backend.dto.McqQuestionResponseDTO;
import com.codearena.backend.entity.McqQuestion;
import com.codearena.backend.entity.McqQuestionOption;
import com.codearena.backend.entity.McqQuestionTag;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.repository.McqQuestionOptionRepository;
import com.codearena.backend.repository.McqQuestionRepository;
import com.codearena.backend.repository.McqQuestionTagsRepository;
import com.codearena.backend.service.McqQuestionService;
import com.codearena.backend.utils.constant.Difficulty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class McqQuestionServiceImpl implements McqQuestionService {

    private final McqQuestionRepository mcqQuestionRepository;
    private final McqQuestionOptionRepository mcqQuestionOptionRepository;
    private final McqQuestionTagsRepository mcqQuestionTagsRepository;

    @Override
    public McqQuestionResponseDTO create(McqQuestionJsonDTO dto) {
        McqQuestion q = new McqQuestion();
        q.setTitle(dto.getTitle());
        q.setDescription(dto.getDescription());
        q.setDifficulty(dto.getDifficulty());
        q.setPoints(dto.getPoints());
        q.setTimeLimit(dto.getTimeLimit());
        McqQuestion saved = mcqQuestionRepository.save(q);

        // Save options
        if (dto.getOptions() != null) {
            List<McqQuestionOption> opts = dto.getOptions().stream()
                    .map(o -> {
                        McqQuestionOption opt = new McqQuestionOption();
                        opt.setOptionText(o.getOptionText());
                        opt.setCorrect(o.isCorrect());
                        opt.setMcqQuestion(saved);
                        return opt;
                    }).collect(Collectors.toList());
            mcqQuestionOptionRepository.saveAll(opts);
        }

        // Save tags
        if (dto.getTags() != null) {
            List<McqQuestionTag> tags = dto.getTags().stream()
                    .map(t -> McqQuestionTag.builder()
                            .tagName(t)
                            .mcqQuestion(saved)
                            .build())
                    .collect(Collectors.toList());
            mcqQuestionTagsRepository.saveAll(tags);
        }

        return mapToDTO(saved);
    }

    @Override
    public McqQuestionResponseDTO update(String id, McqQuestionJsonDTO dto) {
        McqQuestion q = mcqQuestionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MCQ not found"));
        q.setTitle(dto.getTitle());
        q.setDescription(dto.getDescription());
        q.setDifficulty(dto.getDifficulty());
        q.setPoints(dto.getPoints());
        q.setTimeLimit(dto.getTimeLimit());
        return mapToDTO(mcqQuestionRepository.save(q));
    }

    @Override
    @Transactional
    public void delete(String id) {
        McqQuestion question = mcqQuestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MCQ Question not found with id: " + id));

        // Delete dependent options first
        mcqQuestionOptionRepository.deleteByMcqQuestionId(id);

        // Then delete the question itself
        mcqQuestionRepository.delete(question);
    }

    @Override
    public McqQuestionResponseDTO getById(String id) {
        McqQuestion q = mcqQuestionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MCQ not found"));
        return mapToDTO(q);
    }

    @Override
    public List<McqQuestionResponseDTO> getAll() {
        return mcqQuestionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<McqQuestionResponseDTO> getByDifficultyAndCount(String difficulty, int count) {
        Difficulty diff = Difficulty.valueOf(difficulty.toUpperCase());
        // ✅ Fetch all questions for this difficulty
        List<McqQuestion> allQuestions = mcqQuestionRepository.findByDifficulty(diff);

        if (allQuestions.isEmpty()) {
            throw new RuntimeException("No questions found for difficulty: " + difficulty);
        }

        // ✅ Shuffle list for randomness
        Collections.shuffle(allQuestions, new Random());

        // ✅ Limit to requested count (or less if not enough)
        List<McqQuestion> selected = allQuestions.stream()
                .limit(count)
                .collect(Collectors.toList());

        return selected.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<McqQuestionResponseDTO> getByDifficultyAndTopicAndCount(String difficulty, String topic, int count) {
        Difficulty diff = Difficulty.valueOf(difficulty.toUpperCase());
        List<McqQuestion> questions = mcqQuestionRepository.findByDifficultyAndTopic(diff, topic);

        if (questions.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.shuffle(questions);

        return questions.stream()
                .limit(count)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ✅ Helper method
    public McqQuestionResponseDTO mapToDTO(McqQuestion q) {
        McqQuestionResponseDTO dto = new McqQuestionResponseDTO();
        dto.setId(q.getId());
        dto.setTitle(q.getTitle());
        dto.setDescription(q.getDescription());
        dto.setDifficulty(q.getDifficulty());
        dto.setPoints(q.getPoints());
        dto.setTimeLimit(q.getTimeLimit());
//        dto.setCreatedAt(q.getCreatedAt());
        dto.setCreatedBy(q.getCreatedBy());

        // Options
        List<McqOptionJsonDTO> options = mcqQuestionOptionRepository
                .findByMcqQuestionId(q.getId())
                .stream()
                .map(o -> {
                    McqOptionJsonDTO opt = new McqOptionJsonDTO();
                    opt.setId(o.getId());
                    opt.setOptionText(o.getOptionText());
                    opt.setCorrect(o.isCorrect());
                    return opt;
                })
                .collect(Collectors.toList());
        dto.setOptions(options);

        // Tags
        List<String> tags = mcqQuestionTagsRepository.findByMcqQuestionId(q.getId())
                .stream()
                .map(McqQuestionTag::getTagName)
                .collect(Collectors.toList());
        dto.setTags(tags);

        return dto;
    }

    @Override
    public List<McqQuestionResponseDTO> getMixedDifficultyQuestions(int totalQuestions) {
        if (totalQuestions <= 0) {
            throw new IllegalArgumentException("Question count must be greater than 0");
        }
        if (totalQuestions > 50) {
            throw new IllegalArgumentException("Cannot fetch more than 10 questions");
        }

        // Fetch all questions from DB
        List<McqQuestion> allQuestions = mcqQuestionRepository.findAll();

        // Group by difficulty
        List<McqQuestion> easy = allQuestions.stream()
                .filter(q -> q.getDifficulty() == Difficulty.EASY)
                .collect(Collectors.toList());
        List<McqQuestion> medium = allQuestions.stream()
                .filter(q -> q.getDifficulty() == Difficulty.MEDIUM)
                .collect(Collectors.toList());
        List<McqQuestion> hard = allQuestions.stream()
                .filter(q -> q.getDifficulty() == Difficulty.HARD)
                .collect(Collectors.toList());

        // Combine all and shuffle for randomness
        List<McqQuestion> combined = new ArrayList<>();
        combined.addAll(easy);
        combined.addAll(medium);
        combined.addAll(hard);
        Collections.shuffle(combined, new Random());

        // Take only the requested count
        List<McqQuestion> selected = combined.stream()
                .limit(totalQuestions)
                .collect(Collectors.toList());

        return selected.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

}
