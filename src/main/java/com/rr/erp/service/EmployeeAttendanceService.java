package com.rr.erp.service;

import com.rr.erp.entity.EmployeeAttendance;
import com.rr.erp.repository.EmployeeAttendanceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class EmployeeAttendanceService {

    private static final Set<String> STATUSES = Set.of("PRESENT", "ABSENT", "HALF_DAY", "ON_LEAVE");

    // A working day shorter than this many hours counts as a half day.
    private static final long HALF_DAY_BELOW_HOURS = 5;

    private final EmployeeAttendanceRepository attendanceRepository;

    public EmployeeAttendanceService(EmployeeAttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public EmployeeAttendance create(EmployeeAttendance record) {
        if (record.getEmployeeCode() == null || record.getEmployeeCode().isBlank()) {
            throw badRequest("Employee is required");
        }
        if (record.getAttendanceDate() == null) {
            throw badRequest("Attendance date is required");
        }
        if (record.getAttendanceDate().isAfter(LocalDate.now())) {
            throw badRequest("Attendance cannot be recorded for a future date");
        }
        normalise(record);
        validate(record);

        if (attendanceRepository.existsForEmployeeOnDate(record.getEmployeeCode(), record.getAttendanceDate())) {
            throw badRequest("Attendance is already recorded for this employee on " + record.getAttendanceDate());
        }

        try {
            attendanceRepository.create(record);
        } catch (DataIntegrityViolationException exception) {
            throw badRequest("Unknown employee, or attendance already recorded for this day");
        }
        return attendanceRepository.findById(record.getAttendanceId());
    }

    // Records a whole day's register in one go; all rows are saved or none are.
    @Transactional
    public List<EmployeeAttendance> createBulk(List<EmployeeAttendance> records) {
        if (records == null || records.isEmpty()) {
            throw badRequest("Select at least one employee");
        }
        Set<String> seen = new HashSet<>();
        for (EmployeeAttendance record : records) {
            if (record.getEmployeeCode() != null && !seen.add(record.getEmployeeCode() + "|" + record.getAttendanceDate())) {
                throw badRequest("Employee " + record.getEmployeeCode() + " appears more than once");
            }
        }
        List<EmployeeAttendance> saved = new ArrayList<>();
        for (EmployeeAttendance record : records) {
            saved.add(create(record));
        }
        return saved;
    }

    // Adds check-out times to many existing records at once (attendanceId + checkOutTime per row).
    // Present / half day is re-derived from the hours worked; all rows are saved or none are.
    @Transactional
    public List<EmployeeAttendance> checkOutBulk(List<EmployeeAttendance> records) {
        if (records == null || records.isEmpty()) {
            throw badRequest("Select at least one record");
        }
        List<EmployeeAttendance> saved = new ArrayList<>();
        for (EmployeeAttendance request : records) {
            if (request.getAttendanceId() == null || request.getCheckOutTime() == null) {
                throw badRequest("Attendance record and check-out time are required");
            }
            EmployeeAttendance existing = attendanceRepository.findById(request.getAttendanceId());
            if (existing == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance record not found");
            }
            String label = existing.getEmployeeName() != null ? existing.getEmployeeName() : existing.getEmployeeCode();
            if (!"PRESENT".equals(existing.getStatus()) && !"HALF_DAY".equals(existing.getStatus())) {
                throw badRequest(label + " has no working times to check out");
            }
            if (existing.getCheckInTime() == null) {
                throw badRequest(label + " has no check-in time");
            }
            if (!request.getCheckOutTime().isAfter(existing.getCheckInTime())) {
                throw badRequest(label + ": check-out time must be after check-in time");
            }
            existing.setCheckOutTime(request.getCheckOutTime());
            long minutes = Duration.between(existing.getCheckInTime(), existing.getCheckOutTime()).toMinutes();
            existing.setStatus(minutes < HALF_DAY_BELOW_HOURS * 60 ? "HALF_DAY" : "PRESENT");
            attendanceRepository.update(existing.getAttendanceId(), existing);
            saved.add(attendanceRepository.findById(existing.getAttendanceId()));
        }
        return saved;
    }

    public EmployeeAttendance update(UUID attendanceId, EmployeeAttendance record) {
        if (attendanceRepository.findById(attendanceId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance record not found");
        }
        normalise(record);
        validate(record);

        attendanceRepository.update(attendanceId, record);
        return attendanceRepository.findById(attendanceId);
    }

    public List<EmployeeAttendance> search(LocalDate from, LocalDate to, String employeeCode, String status) {
        return attendanceRepository.search(from, to, employeeCode, status);
    }

    public boolean delete(UUID attendanceId) {
        return attendanceRepository.delete(attendanceId) > 0;
    }

    // Absent and on-leave days have no working times, so any sent are dropped.
    private void normalise(EmployeeAttendance record) {
        if (record.getStatus() != null) {
            record.setStatus(record.getStatus().trim().toUpperCase());
        }
        if ("ABSENT".equals(record.getStatus()) || "ON_LEAVE".equals(record.getStatus())) {
            record.setCheckInTime(null);
            record.setCheckOutTime(null);
        }
    }

    private void validate(EmployeeAttendance record) {
        if (record.getStatus() == null || !STATUSES.contains(record.getStatus())) {
            throw badRequest("Status must be one of PRESENT, ABSENT, HALF_DAY, ON_LEAVE");
        }
        if (record.getCheckInTime() != null && record.getCheckOutTime() != null
                && !record.getCheckOutTime().isAfter(record.getCheckInTime())) {
            throw badRequest("Check-out time must be after check-in time");
        }
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
