package com.example.stduents_management.scheduleoverride.service;

import com.example.stduents_management.lecturer.entity.Lecturer;
import com.example.stduents_management.lecturer.repository.LecturerRepository;
import com.example.stduents_management.courseregistration.entity.CourseRegistration;
import com.example.stduents_management.courseregistration.repository.CourseRegistrationRepository;
import com.example.stduents_management.room.entity.Room;
import com.example.stduents_management.room.repository.RoomRepository;
import com.example.stduents_management.schedule.entity.Schedule;
import com.example.stduents_management.schedule.repository.ScheduleRepository;
import com.example.stduents_management.scheduleoverride.dto.ScheduleOverrideRequest;
import com.example.stduents_management.scheduleoverride.dto.ScheduleOverrideResponse;
import com.example.stduents_management.scheduleoverride.entity.OverrideStatus;
import com.example.stduents_management.scheduleoverride.entity.OverrideType;
import com.example.stduents_management.scheduleoverride.entity.ScheduleOverride;
import com.example.stduents_management.scheduleoverride.repository.ScheduleOverrideRepository;
import com.example.stduents_management.notification.entity.NotificationCategory;
import com.example.stduents_management.notification.service.NotificationService;
import com.example.stduents_management.timeslot.entity.TimeSlot;
import com.example.stduents_management.timeslot.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduleOverrideService {

    private final ScheduleOverrideRepository repository;
    private final ScheduleRepository scheduleRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final LecturerRepository lecturerRepository;
    private final CourseRegistrationRepository courseRegistrationRepository;
    private final NotificationService notificationService;

    public Page<ScheduleOverrideResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("overrideDate").descending().and(Sort.by("createdAt").descending()));
        Page<ScheduleOverride> data = (keyword == null || keyword.isBlank())
                ? repository.findAll(pageable)
                : repository.search(keyword.trim(), pageable);
        return data.map(this::toResponse);
    }

    public ScheduleOverrideResponse getById(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi thay đổi lịch"));
    }

    @Transactional(readOnly = true)
    public ScheduleOverride getEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi thay đổi lịch"));
    }

    @Transactional
    public void create(ScheduleOverrideRequest req) {
        validateRequest(req);
        Schedule schedule = scheduleRepository.findById(req.getScheduleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lịch gốc"));
        ScheduleOverride o = new ScheduleOverride();
        o.setSchedule(schedule);
        o.setOverrideDate(req.getOverrideDate());
        o.setOverrideType(req.getOverrideType());
        o.setStatus(req.getStatus() != null ? req.getStatus() : OverrideStatus.ACTIVE);
        o.setReason(req.getReason() != null && req.getReason().length() > 255 ? req.getReason().substring(0, 255) : req.getReason());
        if (req.getNewRoomId() != null) {
            o.setNewRoom(roomRepository.findById(req.getNewRoomId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng mới")));
        }
        if (req.getNewTimeSlotId() != null) {
            o.setNewTimeSlot(timeSlotRepository.findById(req.getNewTimeSlotId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khung giờ mới")));
        }
        if (req.getNewLecturerId() != null) {
            o.setNewLecturer(lecturerRepository.findById(req.getNewLecturerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên thay thế")));
        }
        repository.save(o);
        notifyScheduleChange(schedule, o);
    }

    @Transactional
    public void update(UUID id, ScheduleOverrideRequest req) {
        validateRequest(req);
        ScheduleOverride o = getEntityById(id);
        Schedule schedule = scheduleRepository.findById(req.getScheduleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lịch gốc"));
        o.setSchedule(schedule);
        o.setOverrideDate(req.getOverrideDate());
        o.setOverrideType(req.getOverrideType());
        o.setStatus(req.getStatus() != null ? req.getStatus() : OverrideStatus.ACTIVE);
        o.setReason(req.getReason() != null && req.getReason().length() > 255 ? req.getReason().substring(0, 255) : req.getReason());
        o.setNewRoom(req.getNewRoomId() != null ? roomRepository.findById(req.getNewRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng mới")) : null);
        o.setNewTimeSlot(req.getNewTimeSlotId() != null ? timeSlotRepository.findById(req.getNewTimeSlotId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khung giờ mới")) : null);
        o.setNewLecturer(req.getNewLecturerId() != null ? lecturerRepository.findById(req.getNewLecturerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên thay thế")) : null);
        notifyScheduleChange(schedule, o);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi thay đổi lịch");
        }
        repository.deleteById(id);
        notificationService.deleteBySource(NotificationCategory.SCHEDULE_CHANGE.name(), id.toString());
    }

    private void notifyScheduleChange(Schedule schedule, ScheduleOverride o) {
        if (schedule == null || schedule.getClassSection() == null || schedule.getClassSection().getId() == null) return;

        var cs = schedule.getClassSection();
        Long classSectionId = cs.getId();

        String title = NotificationCategory.SCHEDULE_CHANGE.getLabel();
        String courseName = cs.getCourse() != null ? cs.getCourse().getCourseName() : "";
        String semesterName = cs.getSemester() != null ? cs.getSemester().getName() : "";
        String classCode = cs.getClassCode();

        String overrideType = o.getOverrideType() != null ? o.getOverrideType().name() : "";
        String overrideDate = o.getOverrideDate() != null ? o.getOverrideDate().toString() : "";
        String status = o.getStatus() != null ? o.getStatus().name() : "";
        java.time.LocalDateTime scheduledAt = o.getOverrideDate() != null ? o.getOverrideDate().atStartOfDay() : null;

        String newRoomStr = o.getNewRoom() != null
                ? (o.getNewRoom().getRoomCode() + " - " + o.getNewRoom().getRoomName())
                : "";
        String newTimeSlotStr = o.getNewTimeSlot() != null ? o.getNewTimeSlot().getSlotCode() : "";
        String newLecturerStr = o.getNewLecturer() != null ? o.getNewLecturer().getFullName() : "";

        String reasonPart = o.getReason() != null && !o.getReason().isBlank()
                ? " Lý do: " + o.getReason().trim() + "."
                : "";

        boolean isCancelled = status.equalsIgnoreCase(OverrideStatus.CANCELLED.name());
        String statusPart = isCancelled ? " (đã hủy)" : "";

        String content = "Bạn có thông báo thay đổi lịch học: lớp "
                + (classCode != null ? classCode : "")
                + (courseName.isBlank() ? "" : " - " + courseName)
                + (semesterName.isBlank() ? "" : " (" + semesterName + ")")
                + ". Ngày áp dụng: " + overrideDate
                + ". Loại: " + overrideType
                + statusPart
                + ". "
                + (newRoomStr.isBlank() ? "" : "Phòng: " + newRoomStr + ". ")
                + (newTimeSlotStr.isBlank() ? "" : "Khung giờ: " + newTimeSlotStr + ". ")
                + (newLecturerStr.isBlank() ? "" : "GV thay thế: " + newLecturerStr + ". ")
                + reasonPart;

        List<CourseRegistration> regs = courseRegistrationRepository.findByClassSection_IdOrderByStudent_FullName(classSectionId);
        for (CourseRegistration cr : regs) {
            if (cr == null || cr.getStudent() == null || cr.getStudent().getUser() == null) continue;
            notificationService.upsertForUserBySource(
                    cr.getStudent().getUser().getId(),
                    NotificationCategory.SCHEDULE_CHANGE,
                    title,
                    content,
                    scheduledAt,
                    NotificationCategory.SCHEDULE_CHANGE.name(),
                    o.getOverrideId() != null ? o.getOverrideId().toString() : null
            );
        }
    }

    public List<ScheduleOverrideResponse> getForPrint() {
        return repository.findAllForPrint().stream().map(this::toResponse).toList();
    }

    @Transactional
    public void importExcel(MultipartFile file) throws Exception {
        DataFormatter formatter = new DataFormatter(Locale.getDefault());
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String scheduleIdStr = readString(row, 0, formatter);
                String overrideDateStr = readString(row, 1, formatter);
                String overrideTypeStr = readString(row, 2, formatter);
                String newRoomCode = readString(row, 3, formatter);
                String newSlotCode = readString(row, 4, formatter);
                String newLecturerCode = readString(row, 5, formatter);
                String statusStr = readString(row, 6, formatter);
                String reason = readString(row, 7, formatter);

                if (scheduleIdStr == null || overrideDateStr == null || overrideTypeStr == null) continue;

                final int rowNum = i + 1;
                UUID scheduleId;
                try {
                    scheduleId = UUID.fromString(scheduleIdStr.trim());
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dòng " + rowNum + ": Mã lịch không hợp lệ");
                }
                Schedule schedule = scheduleRepository.findById(scheduleId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dòng " + rowNum + ": Không tìm thấy lịch " + scheduleIdStr));
                LocalDate overrideDate = parseDate(overrideDateStr, rowNum);
                OverrideType overrideType = parseEnum(overrideTypeStr, OverrideType.class, OverrideType.MAKEUP);
                OverrideStatus status = parseEnum(statusStr != null ? statusStr : "ACTIVE", OverrideStatus.class, OverrideStatus.ACTIVE);

                ScheduleOverride o = new ScheduleOverride();
                o.setSchedule(schedule);
                o.setOverrideDate(overrideDate);
                o.setOverrideType(overrideType);
                o.setStatus(status);
                o.setReason(reason != null && reason.length() > 255 ? reason.substring(0, 255) : reason);

                if (newRoomCode != null && !newRoomCode.isBlank()) {
                    o.setNewRoom(roomRepository.findByRoomCodeIgnoreCase(newRoomCode.trim())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dòng " + rowNum + ": Không tìm thấy phòng " + newRoomCode)));
                }
                if (newSlotCode != null && !newSlotCode.isBlank()) {
                    o.setNewTimeSlot(timeSlotRepository.findBySlotCodeIgnoreCase(newSlotCode.trim())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dòng " + rowNum + ": Không tìm thấy khung giờ " + newSlotCode)));
                }
                if (newLecturerCode != null && !newLecturerCode.isBlank()) {
                    o.setNewLecturer(lecturerRepository.findByLecturerCodeIgnoreCase(newLecturerCode.trim())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dòng " + rowNum + ": Không tìm thấy GV " + newLecturerCode)));
                }
                repository.save(o);
            }
        }
    }

    public byte[] exportExcel() throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("ScheduleOverrides");
            Row header = sheet.createRow(0);
            String[] headers = {"Override ID", "Schedule ID", "Lịch gốc", "Ngày áp dụng", "Loại", "Phòng mới", "Khung giờ mới", "GV thay thế", "Trạng thái", "Lý do", "Duyệt lúc", "Ngày tạo"};
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);

            List<ScheduleOverride> list = repository.findAll(Sort.by("overrideDate").descending().and(Sort.by("createdAt").descending()));
            int rowNum = 1;
            for (ScheduleOverride o : list) {
                ScheduleOverrideResponse r = toResponse(o);
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.overrideId() != null ? r.overrideId().toString() : "");
                row.createCell(1).setCellValue(r.scheduleId() != null ? r.scheduleId().toString() : "");
                row.createCell(2).setCellValue(nullToEmpty(r.scheduleBrief()));
                row.createCell(3).setCellValue(r.overrideDate() != null ? r.overrideDate().toString() : "");
                row.createCell(4).setCellValue(r.overrideType() != null ? r.overrideType().name() : "");
                row.createCell(5).setCellValue(nullToEmpty(r.newRoomDisplay()));
                row.createCell(6).setCellValue(nullToEmpty(r.newTimeSlotDisplay()));
                row.createCell(7).setCellValue(nullToEmpty(r.newLecturerName()));
                row.createCell(8).setCellValue(r.status() != null ? r.status().name() : "");
                row.createCell(9).setCellValue(nullToEmpty(r.reason()));
                row.createCell(10).setCellValue(r.approvedAt() != null ? r.approvedAt().toString() : "");
                row.createCell(11).setCellValue(r.createdAt() != null ? r.createdAt().toString() : "");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void validateRequest(ScheduleOverrideRequest req) {
        if (req.getOverrideType() == OverrideType.ROOM_CHANGE && req.getNewRoomId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đổi phòng cần chọn phòng mới");
        }
        if (req.getOverrideType() == OverrideType.TIME_CHANGE && req.getNewTimeSlotId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đổi khung giờ cần chọn khung giờ mới");
        }
    }

    private ScheduleOverrideResponse toResponse(ScheduleOverride o) {
        Schedule s = o.getSchedule();
        String scheduleBrief = null;
        if (s != null) {
            String sem = s.getSemester() != null ? s.getSemester().getCode() : "";
            String cls = s.getClassSection() != null && s.getClassSection().getCourse() != null ? s.getClassSection().getCourse().getCourseCode() : (s.getClassSection() != null ? s.getClassSection().getClassCode() : "");
            String slot = s.getTimeSlot() != null ? s.getTimeSlot().getSlotCode() : "";
            scheduleBrief = sem + " - " + cls + " - " + slot;
        }
        Room nr = o.getNewRoom();
        TimeSlot nts = o.getNewTimeSlot();
        Lecturer nl = o.getNewLecturer();
        return new ScheduleOverrideResponse(
                o.getOverrideId(),
                s != null ? s.getId() : null,
                scheduleBrief,
                o.getOverrideDate(),
                o.getOverrideType(),
                nr != null ? nr.getRoomId() : null,
                nr != null ? (nr.getRoomCode() + " - " + nr.getRoomName()) : null,
                nts != null ? nts.getId() : null,
                nts != null ? nts.getSlotCode() : null,
                nl != null ? nl.getLecturerId() : null,
                nl != null ? nl.getFullName() : null,
                o.getStatus(),
                o.getReason(),
                o.getApprovedBy(),
                o.getApprovedAt(),
                o.getCreatedAt(),
                o.getUpdatedAt()
        );
    }

    private static String readString(Row row, int cellIndex, DataFormatter formatter) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        String v = formatter.formatCellValue(cell);
        return v != null ? v.trim() : null;
    }

    private static LocalDate parseDate(String s, int rowNum) {
        if (s == null || s.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dòng " + rowNum + ": Ngày áp dụng không được trống");
        try {
            return LocalDate.parse(s.trim());
        } catch (Exception e) {
            for (DateTimeFormatter f : new DateTimeFormatter[]{DateTimeFormatter.ISO_LOCAL_DATE, DateTimeFormatter.ofPattern("dd/MM/yyyy"), DateTimeFormatter.ofPattern("d/M/yyyy")}) {
                try {
                    return LocalDate.parse(s.trim(), f);
                } catch (Exception ignored) {}
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dòng " + rowNum + ": Định dạng ngày không hợp lệ");
        }
    }

    private static <E extends Enum<E>> E parseEnum(String v, Class<E> enumClass, E defaultValue) {
        if (v == null || v.isBlank()) return defaultValue;
        try {
            return Enum.valueOf(enumClass, v.trim().toUpperCase());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }
}
