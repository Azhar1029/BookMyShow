package com.cfs.BMS2.controller;


import com.cfs.BMS2.dto.SeatRequest;
import com.cfs.BMS2.entity.Seat;
import com.cfs.BMS2.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PostMapping
    public ResponseEntity<Seat> addSeat(@RequestBody SeatRequest request)
    {
        return ResponseEntity.ok(seatService.addSeat(request));
    }

    @GetMapping("/screen/{screenId}")
    public ResponseEntity<List<Seat>> getSeatByScreen(@PathVariable Long screenId)
    {
        return ResponseEntity.ok(seatService.getSeatsByScreen(screenId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seat> getSeatById(@PathVariable Long id)
    {
        return ResponseEntity.ok(seatService.getSeatById(id));
    }



}