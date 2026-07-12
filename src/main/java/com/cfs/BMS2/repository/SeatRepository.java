package com.cfs.BMS2.repository;

import com.cfs.BMS2.entity.Seat;
import com.cfs.BMS2.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByScreenId(Long screenId);

}
