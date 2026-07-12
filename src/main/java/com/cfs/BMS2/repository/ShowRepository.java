package com.cfs.BMS2.repository;

import com.cfs.BMS2.entity.Show;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByMovieId(Long movieId);
    List<Show> findByScreenId(Long screenId);
    List<Show> findByMovieIdAndShowDate(Long movieId, LocalDate showDate);
    List<Show> findByScreenIdAndShowDate(Long screenId, LocalDate showDate);

    // Acquires a DB row lock (SELECT ... FOR UPDATE) on the Show for the duration of the
    // calling transaction. Used by BookingService.createBooking so that two concurrent
    // booking requests for the same show can't both pass the "seat is free" check before
    // either one commits - the second request blocks until the first transaction finishes.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Show s WHERE s.id = :id")
    Optional<Show> findByIdForUpdate(@Param("id") Long id);

}