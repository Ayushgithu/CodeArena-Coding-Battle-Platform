package com.codearena.backend.dataInitializer;


import com.codearena.backend.entity.DropList;
import com.codearena.backend.repository.DropListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DropListInitializer implements CommandLineRunner {
    @Autowired
    private DropListRepository dropListRepository;


    @Override
    public void run(String... args) {
        List<DropList> items = List.of(
                new DropList("QUESTION_TYPE", "Coding Question"),
                new DropList("QUESTION_TYPE", "MCQ Question"),
                new DropList("DIFFICULTY", "EASY"),
                new DropList("DIFFICULTY", "MEDIUM"),
                new DropList("DIFFICULTY", "HARD"),
                new DropList("DIFFICULTY", "MIXED"),
                new DropList("LANGUAGE", "C"),
                new DropList("LANGUAGE", "C++"),
                new DropList("LANGUAGE", "JAVA"),
                new DropList("LANGUAGE", "PYTHON")
        );

        for (DropList item : items) {
            boolean exists = dropListRepository.existsByLabelKeyAndOptionValue(item.getLabelKey(), item.getOptionValue());
            if (!exists) {
                dropListRepository.save(item);
            }
        }
    }
}

