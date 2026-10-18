package com.rr.erp.controller;

import com.rr.erp.entity.EmployeeAttendance;
import com.rr.erp.service.EmployeeAttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employee-attendance")
@CrossOrigin(origins = "*")
public class EmployeeAttendanceController {

    private final EmployeeAttendanceService attendanceService;

    public EmployeeAttendanceController(EmployeeAttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // GET /api/employee-attendance?from=&to=&employeeCode=&status= — all filters optional.
    @GetMapping
    public ResponseEntity<List<EmployeeAttendance>> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String employeeCode,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(attendanceService.search(from, to, employeeCode, status));
    }

    // POST /api/employee-attendance
    @PostMapping
    public ResponseEntity<EmployeeAttendance> create(@RequestBody EmployeeAttendance record) {
        return ResponseEntity.ok(attendanceService.create(record));
    }

    // POST /api/employee-attendance/bulk — one record per employee, all saved together.
    @PostMapping("/bulk")
    public ResponseEntity<List<EmployeeAttendance>> createBulk(@RequestBody List<EmployeeAttendance> records) {
        return ResponseEntity.ok(attendanceService.createBulk(records));
    }

    // PUT /api/employee-attendance/bulk-checkout — body: [{attendanceId, checkOutTime}, ...]
    @PutMapping("/bulk-checkout")
    public ResponseEntity<List<EmployeeAttendance>> checkOutBulk(@RequestBody List<EmployeeAttendance> records) {
        return ResponseEntity.ok(attendanceService.checkOutBulk(records));
    }

    // PUT /api/employee-attendance/{attendanceId}
    @PutMapping("/{attendanceId}")
    public ResponseEntity<EmployeeAttendance> update(
            @PathVariable UUID attendanceId,
            @RequestBody EmployeeAttendance record) {

        return ResponseEntity.ok(attendanceService.update(attendanceId, record));
    }

    // DELETE /api/employee-attendance/{attendanceId}
    @DeleteMapping("/{attendanceId}")
    public ResponseEntity<String> delete(@PathVariable UUID attendanceId) {

        if (attendanceService.delete(attendanceId)) {
            return ResponseEntity.ok("Attendance record removed successfully");
        }

        return ResponseEntity.notFound().build();
    }
}
