package com.cfs.BMS2.repository;

import com.cfs.BMS2.entity.Screen;
import com.cfs.BMS2.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenRepository extends JpaRepository<Screen,Long> {

    List<Screen> findByTheaterId(Long theaterId);
}