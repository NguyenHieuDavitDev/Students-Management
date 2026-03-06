package com.example.stduents_management.schedule.service;

import com.example.stduents_management.classsection.entity.ClassSection;
import com.example.stduents_management.classsection.entity.ClassSectionStatus;
import com.example.stduents_management.classsection.repository.ClassSectionRepository;
import com.example.stduents_management.lecturer.entity.Lecturer;
import com.example.stduents_management.lecturer.repository.LecturerRepository;
import com.example.stduents_management.lecturercourseclass.entity.LecturerCourseClass;
import com.example.stduents_management.lecturercourseclass.repository.LecturerCourseClassRepository;
import com.example.stduents_management.room.entity.Room;
import com.example.stduents_management.room.repository.RoomRepository;
import com.example.stduents_management.schedule.dto.AutoScheduleRequest;
import com.example.stduents_management.schedule.dto.AutoScheduleResult;
import com.example.stduents_management.schedule.dto.ScheduleRequest;
import com.example.stduents_management.schedule.dto.ScheduleResponse;
import com.example.stduents_management.schedule.entity.*;
import com.example.stduents_management.schedule.repository.ScheduleRepository;
import com.example.stduents_management.semester.entity.Semester;
import com.example.stduents_management.semester.repository.SemesterRepository;
import com.example.stduents_management.timeslot.entity.TimeSlot;
import com.example.stduents_management.timeslot.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository repository;
    private final SemesterRepository semesterRepository;
    private final ClassSectionRepository classSectionRepository;
    private final LecturerRepository lecturerRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final LecturerCourseClassRepository lecturerCourseClassRepository;

    public Page<ScheduleResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dayOfWeek").and(Sort.by("timeSlot.slotCode")));
        Page<Schedule> data =
                (keyword == null || keyword.isBlank())
                        ? repository.findAll(pageable)
                        : repository.search(keyword.trim(), pageable);
        return data.map(this::toResponse);
    }

    public ScheduleResponse getById(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lịch học"));
    }

    @Transactional
    public void create(ScheduleRequest req) {
        Schedule s = new Schedule();
        s.setSemester(semesterRepository.findById(req.getSemesterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học kỳ")));
        s.setClassSection(classSectionRepository.findById(req.getClassSectionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần")));
        s.setLecturer(lecturerRepository.findById(req.getLecturerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên")));
        s.setRoom(roomRepository.findById(req.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng")));
        s.setTimeSlot(timeSlotRepository.findById(req.getTimeSlotId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khung giờ")));
        setCommon(s, req);
        repository.save(s);
    }

    @Transactional
    public void update(UUID id, ScheduleRequest req) {
        Schedule s = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lịch học"));
        s.setSemester(semesterRepository.findById(req.getSemesterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học kỳ")));
        s.setClassSection(classSectionRepository.findById(req.getClassSectionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần")));
        s.setLecturer(lecturerRepository.findById(req.getLecturerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên")));
        s.setRoom(roomRepository.findById(req.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng")));
        s.setTimeSlot(timeSlotRepository.findById(req.getTimeSlotId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khung giờ")));
        setCommon(s, req);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lịch học");
        }
        repository.deleteById(id);
    }

    /**
     * Phân lịch tự động bằng thuật toán tham lam: với mỗi cặp (lớp học phần, giảng viên) từ phân công,
     * gán lần lượt các buổi (lý thuyết/thực hành) vào ô (thứ, khung giờ, phòng) đầu tiên còn trống
     * (không trùng giảng viên, phòng, lớp).
     */
    @Transactional
    public AutoScheduleResult generateAutoSchedule(AutoScheduleRequest req) {
        Semester semester = semesterRepository.findById(req.getSemesterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học kỳ"));
        int startWeek = req.getStartWeek() != null ? req.getStartWeek() : 1;
        int endWeek = req.getEndWeek() != null ? req.getEndWeek() : 15;
        if (startWeek > endWeek) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tuần bắt đầu phải <= tuần kết thúc");
        }

        if (Boolean.TRUE.equals(req.getClearExisting())) {
            List<Schedule> existing = repository.findBySemester_Id(semester.getId());
            repository.deleteAll(existing);
        }

        Set<String> lecturerBusy = new HashSet<>();
        Set<String> roomBusy = new HashSet<>();
        Set<String> classSectionBusy = new HashSet<>();
        for (Schedule s : repository.findBySemester_Id(semester.getId())) {
            int day = s.getDayOfWeek();
            int slotId = s.getTimeSlot().getId();
            String key = day + "_" + slotId;
            lecturerBusy.add(key + "_" + s.getLecturer().getLecturerId());
            roomBusy.add(key + "_" + s.getRoom().getRoomId());
            classSectionBusy.add(key + "_" + s.getClassSection().getId());
        }

        List<Room> rooms = roomRepository.findByIsActiveTrue();
        List<TimeSlot> timeSlots = timeSlotRepository.findByIsActiveTrueOrderBySlotCode();
        if (rooms.isEmpty() || timeSlots.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cần có ít nhất một phòng và một khung giờ đang hoạt động.");
        }

        List<int[]> slotOrder = new ArrayList<>();
        for (int day = 2; day <= 6; day++) {
            for (TimeSlot ts : timeSlots) {
                slotOrder.add(new int[]{day, ts.getId()});
            }
        }

        List<LecturerCourseClass> assignments = lecturerCourseClassRepository
                .findByClassSection_Semester_IdOrderByClassSection_ClassCode(semester.getId());
        List<ClassSection> openSections = classSectionRepository.findBySemester_IdAndStatus(semester.getId(), ClassSectionStatus.OPEN);
        Set<Long> openSectionIds = new HashSet<>();
        for (ClassSection cs : openSections) openSectionIds.add(cs.getId());

        // Nếu người dùng chọn một số lớp cụ thể thì chỉ phân lịch cho các lớp đó
        Set<Long> selectedSectionIds = new HashSet<>();
        if (req.getClassSectionIds() != null) {
            selectedSectionIds.addAll(req.getClassSectionIds());
        }

        List<ScheduleTask> tasks = new ArrayList<>();
        for (LecturerCourseClass lcc : assignments) {
            Long csId = lcc.getClassSection().getId();
            if (!openSectionIds.contains(csId)) continue;
            if (!selectedSectionIds.isEmpty() && !selectedSectionIds.contains(csId)) continue;

            ClassSection cs = lcc.getClassSection();
            Lecturer lec = lcc.getLecturer();
            Integer lh = cs.getCourse() != null ? cs.getCourse().getLectureHours() : null;
            Integer ph = cs.getCourse() != null ? cs.getCourse().getPracticeHours() : null;
            int theorySlots = (lh != null && lh > 0) ? Math.min(4, (lh + 1) / 2) : 2;
            int practiceSlots = (ph != null && ph > 0) ? Math.min(2, (ph + 1) / 2) : 1;
            for (int t = 0; t < theorySlots; t++) {
                tasks.add(new ScheduleTask(cs, lec, SessionType.THEORY));
            }
            for (int p = 0; p < practiceSlots; p++) {
                tasks.add(new ScheduleTask(cs, lec, SessionType.PRACTICE));
            }
        }

        int createdCount = 0;
        int skippedCount = 0;
        for (ScheduleTask task : tasks) {
            boolean placed = false;
            for (int[] daySlot : slotOrder) {
                int day = daySlot[0];
                int timeSlotId = daySlot[1];
                String key = day + "_" + timeSlotId;
                if (lecturerBusy.contains(key + "_" + task.lecturer.getLecturerId())) continue;
                if (classSectionBusy.contains(key + "_" + task.classSection.getId())) continue;
                for (Room room : rooms) {
                    if (roomBusy.contains(key + "_" + room.getRoomId())) continue;
                    TimeSlot slot = timeSlotRepository.findById(timeSlotId).orElse(null);
                    if (slot == null) continue;
                    Schedule s = new Schedule();
                    s.setSemester(semester);
                    s.setClassSection(task.classSection);
                    s.setLecturer(task.lecturer);
                    s.setRoom(room);
                    s.setTimeSlot(slot);
                    s.setDayOfWeek(day);
                    s.setStartWeek(startWeek);
                    s.setEndWeek(endWeek);
                    s.setWeekPattern(WeekPattern.ALL);
                    s.setSessionType(task.sessionType);
                    s.setScheduleType(ScheduleType.NORMAL);
                    s.setStatus(ScheduleStatus.ACTIVE);
                    repository.save(s);
                    lecturerBusy.add(key + "_" + task.lecturer.getLecturerId());
                    roomBusy.add(key + "_" + room.getRoomId());
                    classSectionBusy.add(key + "_" + task.classSection.getId());
                    createdCount++;
                    placed = true;
                    break;
                }
                if (placed) break;
            }
            if (!placed) skippedCount++;
        }

        String message = String.format("Phân lịch xong: tạo %d buổi, không xếp được %d buổi (thiếu ô trống).",
                createdCount, skippedCount);
        return new AutoScheduleResult(createdCount, skippedCount, message);
    }

    private static class ScheduleTask {
        final ClassSection classSection;
        final Lecturer lecturer;
        final SessionType sessionType;

        ScheduleTask(ClassSection classSection, Lecturer lecturer, SessionType sessionType) {
            this.classSection = classSection;
            this.lecturer = lecturer;
            this.sessionType = sessionType;
        }
    }

    public List<ScheduleResponse> getForPrint() {
        return repository.findAll(Sort.by("semester.code").and(Sort.by("dayOfWeek")).and(Sort.by("timeSlot.slotCode")))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void importExcel(MultipartFile file) throws Exception {
        DataFormatter formatter = new DataFormatter(Locale.getDefault());
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String semesterCode = readString(row, 0, formatter);
                String classCode = readString(row, 1, formatter);
                String lecturerCode = readString(row, 2, formatter);
                String roomCode = readString(row, 3, formatter);
                String slotCode = readString(row, 4, formatter);
                if (semesterCode == null || classCode == null || lecturerCode == null || roomCode == null || slotCode == null) continue;

                final int rowNum = i + 1;
                Semester sem = semesterRepository.findByCodeIgnoreCase(semesterCode.trim())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dòng " + rowNum + ": Không tìm thấy học kỳ " + semesterCode));
                ClassSection cs = classSectionRepository.findByClassCodeIgnoreCase(classCode.trim())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dòng " + rowNum + ": Không tìm thấy lớp " + classCode));
                Lecturer lec = lecturerRepository.findByLecturerCodeIgnoreCase(lecturerCode.trim())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dòng " + rowNum + ": Không tìm thấy GV " + lecturerCode));
                Room room = roomRepository.findByRoomCodeIgnoreCase(roomCode.trim())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dòng " + rowNum + ": Không tìm thấy phòng " + roomCode));
                TimeSlot slot = timeSlotRepository.findBySlotCodeIgnoreCase(slotCode.trim())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dòng " + rowNum + ": Không tìm thấy khung giờ " + slotCode));

                Integer dayOfWeek = readInt(row, 5, formatter);
                Integer startWeek = readInt(row, 6, formatter);
                Integer endWeek = readInt(row, 7, formatter);
                if (dayOfWeek == null || dayOfWeek < 2 || dayOfWeek > 8) dayOfWeek = 2;
                if (startWeek == null) startWeek = 1;
                if (endWeek == null) endWeek = 15;

                WeekPattern wp = readEnum(row, 8, formatter, WeekPattern.class, WeekPattern.ALL);
                SessionType st = readEnum(row, 9, formatter, SessionType.class, SessionType.THEORY);
                ScheduleType sct = readEnum(row, 10, formatter, ScheduleType.class, ScheduleType.NORMAL);
                ScheduleStatus ss = readEnum(row, 11, formatter, ScheduleStatus.class, ScheduleStatus.ACTIVE);
                String note = readString(row, 12, formatter);

                Schedule s = new Schedule();
                s.setSemester(sem);
                s.setClassSection(cs);
                s.setLecturer(lec);
                s.setRoom(room);
                s.setTimeSlot(slot);
                s.setDayOfWeek(dayOfWeek);
                s.setStartWeek(startWeek);
                s.setEndWeek(endWeek);
                s.setWeekPattern(wp);
                s.setSessionType(st);
                s.setScheduleType(sct);
                s.setStatus(ss);
                s.setNote(note != null ? (note.length() > 255 ? note.substring(0, 255) : note) : null);
                repository.save(s);
            }
        }
    }

    public byte[] exportExcel() throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Schedules");
            Row header = sheet.createRow(0);
            String[] headers = {"Semester Code", "Class Code", "Lecturer Code", "Room Code", "Slot Code", "Day", "Start Week", "End Week", "Week Pattern", "Session Type", "Schedule Type", "Status", "Note"};
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);

            List<Schedule> list = repository.findAll(Sort.by("semester.code").and(Sort.by("dayOfWeek")).and(Sort.by("timeSlot.slotCode")));
            int rowNum = 1;
            for (Schedule s : list) {
                Row row = sheet.createRow(rowNum++);
                ScheduleResponse r = toResponse(s);
                row.createCell(0).setCellValue(nullToEmpty(r.semesterCode()));
                row.createCell(1).setCellValue(nullToEmpty(r.classSectionCode()));
                row.createCell(2).setCellValue(nullToEmpty(r.lecturerCode()));
                row.createCell(3).setCellValue(nullToEmpty(r.roomCode()));
                row.createCell(4).setCellValue(nullToEmpty(r.timeSlotCode()));
                row.createCell(5).setCellValue(r.dayOfWeek() != null ? r.dayOfWeek() : 0);
                row.createCell(6).setCellValue(r.startWeek() != null ? r.startWeek() : 0);
                row.createCell(7).setCellValue(r.endWeek() != null ? r.endWeek() : 0);
                row.createCell(8).setCellValue(r.weekPattern() != null ? r.weekPattern().name() : "");
                row.createCell(9).setCellValue(r.sessionType() != null ? r.sessionType().name() : "");
                row.createCell(10).setCellValue(r.scheduleType() != null ? r.scheduleType().name() : "");
                row.createCell(11).setCellValue(r.status() != null ? r.status().name() : "");
                row.createCell(12).setCellValue(nullToEmpty(r.note()));
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void setCommon(Schedule s, ScheduleRequest req) {
        s.setDayOfWeek(req.getDayOfWeek());
        s.setStartWeek(req.getStartWeek());
        s.setEndWeek(req.getEndWeek());
        s.setWeekPattern(req.getWeekPattern() != null ? req.getWeekPattern() : WeekPattern.ALL);
        s.setSessionType(req.getSessionType() != null ? req.getSessionType() : SessionType.THEORY);
        s.setScheduleType(req.getScheduleType() != null ? req.getScheduleType() : ScheduleType.NORMAL);
        s.setStatus(req.getStatus() != null ? req.getStatus() : ScheduleStatus.ACTIVE);
        s.setNote(req.getNote() != null && req.getNote().length() > 255 ? req.getNote().substring(0, 255) : req.getNote());
    }

    private ScheduleResponse toResponse(Schedule s) {
        Semester sem = s.getSemester();
        ClassSection cs = s.getClassSection();
        Lecturer lec = s.getLecturer();
        Room room = s.getRoom();
        TimeSlot slot = s.getTimeSlot();
        return new ScheduleResponse(
                s.getId(),
                sem != null ? sem.getId() : null,
                sem != null ? sem.getCode() : null,
                sem != null ? sem.getName() : null,
                cs != null ? cs.getId() : null,
                cs != null ? cs.getClassCode() : null,
                cs != null ? cs.getClassName() : null,
                cs != null && cs.getCourse() != null ? cs.getCourse().getCourseCode() : null,
                cs != null && cs.getCourse() != null ? cs.getCourse().getCourseName() : null,
                lec != null ? lec.getLecturerId() : null,
                lec != null ? lec.getLecturerCode() : null,
                lec != null ? lec.getFullName() : null,
                room != null ? room.getRoomId() : null,
                room != null ? room.getRoomCode() : null,
                room != null ? room.getRoomName() : null,
                slot != null ? slot.getId() : null,
                slot != null ? slot.getSlotCode() : null,
                slot != null ? slot.getStartTime() : null,
                slot != null ? slot.getEndTime() : null,
                s.getDayOfWeek(),
                s.getStartWeek(),
                s.getEndWeek(),
                s.getWeekPattern(),
                s.getSessionType(),
                s.getScheduleType(),
                s.getStatus(),
                s.getNote(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }

    private static String readString(Row row, int cellIndex, DataFormatter formatter) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        String v = formatter.formatCellValue(cell);
        return v != null ? v.trim() : null;
    }

    private static Integer readInt(Row row, int cellIndex, DataFormatter formatter) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) return (int) cell.getNumericCellValue();
        String v = formatter.formatCellValue(cell);
        if (v == null || v.isBlank()) return null;
        try { return Integer.parseInt(v.trim()); } catch (NumberFormatException e) { return null; }
    }

    private static <E extends Enum<E>> E readEnum(Row row, int cellIndex, DataFormatter formatter, Class<E> enumClass, E defaultValue) {
        String v = readString(row, cellIndex, formatter);
        if (v == null || v.isBlank()) return defaultValue;
        try { return Enum.valueOf(enumClass, v.trim().toUpperCase()); } catch (Exception e) { return defaultValue; }
    }

    private static String nullToEmpty(String s) { return s != null ? s : ""; }
}
