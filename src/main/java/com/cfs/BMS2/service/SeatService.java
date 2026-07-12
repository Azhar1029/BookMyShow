package com.cfs.BMS2.service;


import com.cfs.BMS2.dto.SeatRequest;
import com.cfs.BMS2.entity.Screen;
import com.cfs.BMS2.entity.Seat;
import com.cfs.BMS2.entity.Theater;
import com.cfs.BMS2.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final ScreenService screenService;

    public Seat addSeat(SeatRequest request)
    {
        Screen screen = screenService.getScreenById(request.getScreenId());
        Seat seat = Seat.builder()
                .seatNumber(request.getSeatNumber())
                .row(request.getRow())
                .col(request.getCol())
                .seatType(request.getSeatType())
                .screen(screen)
                .build();
        return seatRepository.save(seat);
    }

    public List<Seat> getSeatsByScreen(Long screenId)
    {
        return seatRepository.findByScreenId(screenId);
    }

    public Seat getSeatById(Long id)
    {
        return seatRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Seat not found with id: "+id));

    }
}