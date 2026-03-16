package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.StarterCodeDTO;
import com.codearena.backend.entity.StarterCode;
import com.codearena.backend.repository.StarterCodeRepository;
import com.codearena.backend.service.StarterCodeService;
import com.codearena.backend.utils.constant.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StarterCodeServiceImpl implements StarterCodeService {

    private final StarterCodeRepository starterCodeRepository;

    public StarterCodeServiceImpl(StarterCodeRepository starterCodeRepository) {
        this.starterCodeRepository = starterCodeRepository;
    }


    @Override
    public List<StarterCodeDTO> getStarterCodesByQuestionId(String questionId) {
        List<StarterCode> starterCodes = starterCodeRepository.findByCodingQuestionId(questionId);
        return starterCodes.stream().map(this::toDTO).toList();
    }
    private StarterCodeDTO toDTO(StarterCode starterCode){
        StarterCodeDTO starterCodeDTO = new StarterCodeDTO();
        starterCodeDTO.setId(starterCode.getId());
        starterCodeDTO.setLanguage(starterCode.getLanguage().name().toLowerCase());
        starterCodeDTO.setCodeTemplate(starterCode.getCodeTemplate());
        starterCodeDTO.setVersion(starterCode.getVersion());
        return starterCodeDTO;
    }
    @Override
    public StarterCodeDTO getStarterCodeByQuestionAndLanguage(String questionId, String language) {

        // Convert language String -> ENUM
        Language langEnum;

        try {
            langEnum = Language.valueOf(language.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid language: " + language);
        }

        StarterCode code = starterCodeRepository
                .findByCodingQuestionIdAndLanguage(questionId, langEnum)
                .orElseThrow(() ->
                        new RuntimeException("Starter code not found for question " + questionId + " and language " + language)
                );

        return toDTO(code);
    }

    @Override
    public void deleteStarterCode(String id) {
        Optional<StarterCode>starterCode=starterCodeRepository.findById(id);
        if (starterCode.isPresent()){
            starterCodeRepository.deleteById(id);
        }

    }
}
