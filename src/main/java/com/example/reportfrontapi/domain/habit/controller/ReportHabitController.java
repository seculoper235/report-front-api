package com.example.reportfrontapi.domain.habit.controller;

import com.example.reportfrontapi.domain.habit.application.ReportHabitRequest;
import com.example.reportfrontapi.domain.habit.application.ReportHabitResponse;
import com.example.reportfrontapi.domain.habit.application.ReportHabitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class ReportHabitController {
    private final ReportHabitService reportHabitService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportHabitResponse create(@RequestBody ReportHabitRequest request) {
        return reportHabitService.create(request);
    }

    @GetMapping
    public List<ReportHabitResponse> findAll() {
        return reportHabitService.findAll();
    }

    @GetMapping("/{id}")
    public ReportHabitResponse findById(@PathVariable Long id) {
        return reportHabitService.findById(id);
    }

    @PutMapping("/{id}")
    public ReportHabitResponse update(@PathVariable Long id, @RequestBody ReportHabitRequest request) {
        return reportHabitService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        reportHabitService.delete(id);
    }
}
