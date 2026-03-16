package com.codearena.backend.serviceImpl;

import com.codearena.backend.dto.DropListDTO;
import com.codearena.backend.entity.DropList;
import com.codearena.backend.repository.DropListRepository;
import com.codearena.backend.service.DropListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DropListServiceImpl implements DropListService {
    private final DropListRepository dropListRepository;

    public DropListServiceImpl(DropListRepository dropListRepository) {
        this.dropListRepository = dropListRepository;
    }


    @Override
    public DropListDTO createDropList(DropListDTO dropListDTO) {
        DropList dropList = new DropList(dropListDTO.getLabelKey(), dropListDTO.getOptionValue());

        return toDTO(dropListRepository.save(dropList));
    }

    @Override
    public List<DropListDTO> findByLabelKey(String labelKey) {
        List<DropList> dropLists = dropListRepository.findByLabelKey(labelKey);
        return dropLists.stream().map(this::toDTO).toList();
    }

    public DropListDTO toDTO(DropList dropList) {
        DropListDTO dropListDTO = new DropListDTO();
        dropListDTO.setId(dropList.getId());
        dropListDTO.setLabelKey(dropList.getLabelKey());
        dropListDTO.setOptionValue(dropList.getOptionValue());
        return dropListDTO;
    }

}
