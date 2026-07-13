package com.cfs.BMS2.controller;


import com.cfs.BMS2.dto.BookingRequest;
import com.cfs.BMS2.entity.Booking;
import com.cfs.BMS2.entity.Seat;
import com.cfs.BMS2.entity.User;
import com.cfs.BMS2.service.BookingService;
import com.cfs.BMS2.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequest request)
    {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id)
    {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingByUserId(@PathVariable Long userId, Authentication authentication)
    {
        // authentication.getName() is the email (see CustomUserDetailsService) - a plain
        // USER may only ever see their own booking history; an ADMIN can see anyone's.
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            User requester = userService.getUserByEmail(authentication.getName());
            if (!requester.getId().equals(userId)) {
                throw new AccessDeniedException("You can only view your own bookings");
            }
        }

        return ResponseEntity.ok(bookingService.getBookingByUser(userId));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long id)
    {
        return ResponseEntity.ok(bookingService.cancelbooking(id));
    }

    @GetMapping("/show/{showId}/available-seats")
    public ResponseEntity<List<Seat>> getAvailableSeats(@PathVariable Long showId)
    {
        return ResponseEntity.ok(bookingService.getAvailableSeats(showId));
    }


}