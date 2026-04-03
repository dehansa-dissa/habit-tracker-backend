package com.habittracker.backend.controller;

import com.habittracker.backend.dto.HabitRequest;
import com.habittracker.backend.dto.HabitResponse;
import com.habittracker.backend.model.HabitCompletion;
import com.habittracker.backend.service.HabitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HabitController {

    private final HabitService habitService;

    private String getEmail(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email == null) email = jwt.getSubject();
        return email;
    }

    @PostMapping
    public ResponseEntity<HabitResponse> createHabit(
            @RequestBody HabitRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(habitService.createHabit(request, getEmail(jwt)));
    }

    @GetMapping
    public ResponseEntity<List<HabitResponse>> getHabits(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(habitService.getUserHabits(getEmail(jwt)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        habitService.deleteHabit(id, getEmail(jwt));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<HabitResponse> markComplete(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(habitService.markComplete(id, getEmail(jwt)));
    }

    @GetMapping("/{id}/completions")
    public ResponseEntity<List<HabitCompletion>> getCompletions(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(habitService.getCompletions(id, getEmail(jwt)));
    }
}