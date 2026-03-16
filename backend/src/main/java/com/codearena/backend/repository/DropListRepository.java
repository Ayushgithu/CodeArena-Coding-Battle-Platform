package com.codearena.backend.repository;

import com.codearena.backend.entity.DropList;
import com.codearena.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DropListRepository extends JpaRepository<DropList,String> {
    List<DropList> findByLabelKey(String labelKey);

    boolean existsByLabelKeyAndOptionValue(String labelKey, String optionValue);
}
