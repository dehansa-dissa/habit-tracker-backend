package com.habittracker.backend.service;

import com.habittracker.backend.dto.HabitRequest;
import com.habittracker.backend.dto.HabitResponse;
import com.habittracker.backend.model.Habit;
import com.habittracker.backend.model.HabitCompletion;
import com.habittracker.backend.repository.HabitCompletionRepository;
import com.habittracker.backend.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;
    private final HabitCompletionRepository completionRepository;

    public HabitResponse createHabit(HabitRequest request, String userEmail) {
        Habit habit = new Habit();
        habit.setName(request.getName());
        habit.setDescription(request.getDescription());
        habit.setUserEmail(userEmail); // from JWT
        Habit saved = habitRepository.save(habit);
        return mapToResponse(saved);
    }

    public List<HabitResponse> getUserHabits(String userEmail) {
        return habitRepository.findByUserEmail(userEmail)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteHabit(Long id, String userEmail) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Habit not found"));
        if (!habit.getUserEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }
        habitRepository.delete(habit);
    }

    public HabitResponse markComplete(Long habitId, String userEmail) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));
        if (!habit.getUserEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }
        LocalDate today = LocalDate.now();
        if (!completionRepository.existsByHabitIdAndDate(habitId, today)) {
            HabitCompletion completion = new HabitCompletion();
            completion.setHabit(habit);
            completion.setDate(today);
            completion.setCompleted(true);
            completionRepository.save(completion);
        }
        return mapToResponse(habit);
    }

    public List<HabitCompletion> getCompletions(Long habitId, String userEmail) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));
        if (!habit.getUserEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }
        return completionRepository.findByHabitId(habitId);
    }

    private HabitResponse mapToResponse(Habit habit) {
        HabitResponse response = new HabitResponse();
        response.setId(habit.getId());
        response.setName(habit.getName());
        response.setDescription(habit.getDescription());
        response.setCreatedAt(habit.getCreatedAt());

        List<HabitCompletion> completions = completionRepository.findByHabitId(habit.getId());
        long totalDays = habit.getCreatedAt().toLocalDate()
                .datesUntil(LocalDate.now().plusDays(1)).count();
        long completedDays = completions.size();

        response.setCompletionPercentage(totalDays > 0 ? (completedDays * 100.0 / totalDays) : 0);
        response.setCompletedToday(completionRepository.existsByHabitIdAndDate(habit.getId(), LocalDate.now()));
        response.setStreak(calculateStreak(completions));
        return response;
    }

    private int calculateStreak(List<HabitCompletion> completions) {
        int streak = 0;
        LocalDate check = LocalDate.now();
        while (true) {
            LocalDate finalCheck = check;
            boolean found = completions.stream()
                    .anyMatch(c -> c.getDate().equals(finalCheck));
            if (!found) break;
            streak++;
            check = check.minusDays(1);
        }
        return streak;
    }
}