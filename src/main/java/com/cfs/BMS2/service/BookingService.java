package com.cfs.BMS2.service;


import com.cfs.BMS2.dto.BookingRequest;
import com.cfs.BMS2.entity.*;
import com.cfs.BMS2.enums.BookingStatus;
import com.cfs.BMS2.repository.BookingRepository;
import com.cfs.BMS2.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final UserService userService;
    private final ShowService showService;

    @Value("${app.booking.pending-timeout-minutes:15}")
    private int pendingTimeoutMinutes;

    @Transactional
    public Booking createBooking(BookingRequest request)
    {
        User user=userService.getUserById(request.getUserId());
        // Acquire a row lock on this Show for the rest of the transaction. Any other
        // concurrent createBooking() call for the SAME show will block here until this
        // transaction commits or rolls back, so the seat-availability check below can't
        // be raced by two requests both seeing the seat as "free".
        Show show=showService.getShowByIdForUpdate(request.getShowId());

        //check if any of the requested seat are already booked
        List<Long> alreadyBookedSeats=bookingRepository.findBookedSeatIdsByShowId(show.getId());
        for(Long seatId:request.getSeatIds())
        {
            if(alreadyBookedSeats.contains(seatId))
            {
                throw new RuntimeException("Seat with id "+seatId+" is already Booked");
            }
        }

        List<Seat> seats=seatRepository.findAllById(request.getSeatIds());
        if(seats.size()!=request.getSeatIds().size())
        {
            throw new RuntimeException("Some Seats Are Invalid");
        }

        double totalPrice=seats.size()*show.getTicketPrice();
        Booking booking=Booking.builder()
                .user(user)
                .show(show)
                .seats(seats)
                .totalPrice(totalPrice)
                .status(BookingStatus.PENDING_PAYMENT)
                .build();

        return bookingRepository.save(booking);
    }

    // Called after Razorpay signature verification succeeds (see PaymentService.verifyAndNotify).
    @Transactional
    public Booking confirmBooking(Long bookingId)
    {
        Booking booking = getBookingById(bookingId);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Cannot confirm booking " + bookingId + ": it was already cancelled");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    public Booking getBookingById(Long id)
    {
        return bookingRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Booking not found with id: "+id));

    }

    public List<Booking> getBookingByUser(Long userId)
    {
        return bookingRepository.findByUserId(userId);
    }

    @Transactional
    public Booking cancelbooking(Long bookingid)
    {
        Booking booking=getBookingById(bookingid);
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    public List<Seat> getAvailableSeats(Long showId)
    {
        Show show=showService.getShowById(showId);
        List<Seat> allSeats=seatRepository.findByScreenId(show.getScreen().getId());
        List<Long> bookingSeatIds=bookingRepository.findBookedSeatIdsByShowId(showId);
        return allSeats.stream()
                .filter(seat -> !bookingSeatIds.contains(seat.getId()))
                .toList();
    }

    // Runs every minute; cancels any PENDING_PAYMENT booking older than
    // app.booking.pending-timeout-minutes (default 15) so its seats free up
    // for other users instead of being held forever by an abandoned checkout.
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void expireStalePendingBookings()
    {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(pendingTimeoutMinutes);
        List<Booking> stale = bookingRepository.findByStatusAndBookedAtBefore(BookingStatus.PENDING_PAYMENT, cutoff);

        for (Booking booking : stale) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            log.info("Auto-cancelled stale pending-payment booking id={} (booked at {})",
                    booking.getId(), booking.getBookedAt());
        }
    }
}