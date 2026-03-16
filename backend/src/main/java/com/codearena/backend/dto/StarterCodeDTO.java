package com.codearena.backend.dto;

import com.codearena.backend.utils.constant.Language;
import lombok.Data;

@Data
public class StarterCodeDTO {
    private String id;
    private String language;
    private String version;
    private String codeTemplate;

}
