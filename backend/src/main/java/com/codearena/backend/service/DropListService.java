package com.codearena.backend.service;

import com.codearena.backend.dto.DropListDTO;
import com.codearena.backend.entity.DropList;

import java.util.List;

public interface DropListService {
    DropListDTO createDropList(DropListDTO dropListDTO);

    List<DropListDTO> findByLabelKey(String labelKey);
}
