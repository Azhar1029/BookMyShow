package com.cfs.BMS2.repository;

import com.cfs.BMS2.entity.Booking;
import com.cfs.BMS2.entity.City;
import com.cfs.BMS2.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);
    List<Booking> findByShowId(Long showId);

    // find all seat Ids that are already booked (or awaiting payment) for given show
    @Query("SELECT s.id FROM Booking b JOIN b.seats s WHERE b.show.id=:showId AND b.status IN ('CONFIRMED','PENDING_PAYMENT')")
    List<Long> findBookedSeatIdsByShowId(@Param("showId") Long showId);

    // bookings stuck awaiting payment past the cutoff - candidates for auto-cancellation
    List<Booking> findByStatusAndBookedAtBefore(BookingStatus status, LocalDateTime cutoff);

}