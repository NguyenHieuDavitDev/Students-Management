package com.example.stduents_management.schedule.service;

import com.example.stduents_management.classsection.entity.ClassSection;
import com.example.stduents_management.classsection.entity.ClassSectionStatus;
import com.example.stduents_management.classsection.repository.ClassSectionRepository;
import com.example.stduents_management.course.entity.Course;
import com.example.stduents_management.lecturer.entity.Lecturer;
import com.example.stduents_management.lecturer.repository.LecturerRepository;
import com.example.stduents_management.lecturercourseclass.entity.LecturerCourseClass;
import com.example.stduents_management.lecturercourseclass.repository.LecturerCourseClassRepository;
import com.example.stduents_management.room.entity.Room;
import com.example.stduents_management.room.repository.RoomRepository;
import com.example.stduents_management.schedule.dto.AutoScheduleRequest;
import com.example.stduents_management.schedule.dto.AutoScheduleResult;
import com.example.stduents_management.schedule.dto.ScheduleCalendarEventResponse;
import com.example.stduents_management.schedule.dto.ScheduleCalendarMetaResponse;
import com.example.stduents_management.schedule.dto.ScheduleRequest;
import com.example.stduents_management.schedule.dto.ScheduleResponse;
import com.example.stduents_management.schedule.entity.*;
import com.example.stduents_management.schedule.repository.ScheduleRepository;
import com.example.stduents_management.scheduleoverride.repository.ScheduleOverrideRepository;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private final ScheduleOverrideRepository scheduleOverrideRepository;

    @Transactional(readOnly = true)
    public Page<ScheduleResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dayOfWeek").and(Sort.by("timeSlot.slotCode")));
        Page<Schedule> data =
                (keyword == null || keyword.isBlank())
                        ? repository.findAll(pageable)
                        : repository.search(keyword.trim(), pageable);
        return data.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ScheduleResponse getById(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lịch học"));
    }

    @Transactional
    public UUID create(ScheduleRequest req) {
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
        assertClassSectionMatchesSemester(s.getClassSection(), s.getSemester());
        setCommon(s, req);
        validateWeekRange(s.getStartWeek(), s.getEndWeek());
        return repository.save(s).getId();
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
        assertClassSectionMatchesSemester(s.getClassSection(), s.getSemester());
        setCommon(s, req);
        validateWeekRange(s.getStartWeek(), s.getEndWeek());
        repository.save(s);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lịch học");
        }
        scheduleOverrideRepository.deleteByScheduleId(id);
        repository.deleteById(id);
    }

    /**
     * Phân lịch tự động theo phân công GV – lớp học phần.
     * Số buổi học lấy theo <strong>tín chỉ</strong> học phần (mặc định 5 buổi/1 TC; 1→5, 2→10, 3→15, 4→20… tối đa 60).
     * Khung tuần: {@code startWeek} và tùy chọn {@code endWeek}. Trong khung, mỗi dòng lịch = cùng thứ + tiết mỗi tuần từ tuần A→B;
     * nếu số buổi &gt; số tuần trong khung → thêm các dòng khác (thứ/tiết khác) cho đủ buổi.
     * Kiểm tra trùng theo <strong>khoảng tuần</strong> (GV / phòng / lớp HP không được chồng lấn cùng thứ+tiết).
     */
    @Transactional
    public AutoScheduleResult generateAutoSchedule(AutoScheduleRequest req) {
        Semester semester = semesterRepository.findById(req.getSemesterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học kỳ"));
        int startWeek = req.getStartWeek() != null ? req.getStartWeek() : 1;
        if (startWeek < 1 || startWeek > 53) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tuần bắt đầu phải từ 1 đến 53");
        }
        Integer reqEndWeek = req.getEndWeek();
        if (reqEndWeek != null && (reqEndWeek < 1 || reqEndWeek > 53)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tuần kết thúc phải từ 1 đến 53");
        }
        if (reqEndWeek != null && reqEndWeek < startWeek) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tuần kết thúc phải ≥ tuần bắt đầu");
        }
        List<Integer> allowedIds = req.getAllowedTimeSlotIds();
        if (allowedIds == null || allowedIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chọn ít nhất một tiết (khung giờ) được phép xếp lịch");
        }

        if (Boolean.TRUE.equals(req.getClearExisting())) {
            scheduleOverrideRepository.deleteByScheduleSemesterId(semester.getId());
            List<Schedule> existing = repository.findBySemester_Id(semester.getId());
            repository.deleteAll(existing);
        }

        List<Schedule> allInSemester = new ArrayList<>(repository.findBySemesterIdWithDetails(semester.getId()));

        List<Room> rooms = roomRepository.findByIsActiveTrue();
        List<TimeSlot> activeSlots = timeSlotRepository.findByIsActiveTrueOrderBySlotCode();
        if (rooms.isEmpty() || activeSlots.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cần có ít nhất một phòng và một khung giờ đang hoạt động.");
        }
        Map<Integer, TimeSlot> timeSlotById = new HashMap<>();
        for (TimeSlot ts : activeSlots) {
            timeSlotById.put(ts.getId(), ts);
        }
        LinkedHashSet<Integer> allowedUnique = new LinkedHashSet<>(allowedIds);
        for (Integer sid : allowedUnique) {
            if (!timeSlotById.containsKey(sid)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Khung giờ id=" + sid + " không tồn tại hoặc không đang hoạt động.");
            }
        }

        List<int[]> candidateOrder = new ArrayList<>();
        for (int day = 2; day <= 6; day++) {
            for (Integer sid : allowedUnique) {
                candidateOrder.add(new int[]{day, sid});
            }
        }

        List<LecturerCourseClass> assignments = lecturerCourseClassRepository
                .findWithSectionAndCourseAndLecturerBySemesterId(semester.getId());
        List<ClassSection> openSections = classSectionRepository.findBySemester_IdAndStatus(semester.getId(), ClassSectionStatus.OPEN);
        Set<Long> openSectionIds = new HashSet<>();
        for (ClassSection cs : openSections) {
            openSectionIds.add(cs.getId());
        }

        Set<Long> selectedSectionIds = new HashSet<>();
        if (req.getClassSectionIds() != null) {
            selectedSectionIds.addAll(req.getClassSectionIds());
        }

        Map<Long, LecturerCourseClass> oneAssignmentPerSection = new LinkedHashMap<>();
        for (LecturerCourseClass lcc : assignments) {
            Long csId = lcc.getClassSection().getId();
            if (!openSectionIds.contains(csId)) {
                continue;
            }
            if (!selectedSectionIds.isEmpty() && !selectedSectionIds.contains(csId)) {
                continue;
            }
            oneAssignmentPerSection.putIfAbsent(csId, lcc);
        }

        int createdCount = 0;
        int skippedSections = 0;
        for (LecturerCourseClass lcc : oneAssignmentPerSection.values()) {
            ClassSection cs = lcc.getClassSection();
            Lecturer lec = lcc.getLecturer();
            Integer credits = cs.getCourse() != null ? cs.getCourse().getCredits() : null;
            int totalSessions = totalSessionsForCredits(credits);

            int windowEnd;
            if (reqEndWeek != null) {
                windowEnd = reqEndWeek;
            } else {
                windowEnd = startWeek + totalSessions - 1;
                if (windowEnd > 53) {
                    skippedSections++;
                    continue;
                }
            }
            int windowWeeks = windowEnd - startWeek + 1;
            if (windowWeeks < 1) {
                skippedSections++;
                continue;
            }

            int remaining = totalSessions;
            List<Schedule> trial = new ArrayList<>(allInSemester);
            List<Schedule> planned = new ArrayList<>();
            boolean sectionOk = true;
            while (remaining > 0) {
                int chunkWeeks = Math.min(remaining, windowWeeks);
                int rowStart = startWeek;
                int rowEnd = rowStart + chunkWeeks - 1;
                if (rowEnd > windowEnd) {
                    sectionOk = false;
                    break;
                }
                Schedule row = buildAutoScheduleRowIfFree(
                        semester, cs, lec, rooms, timeSlotById, candidateOrder,
                        rowStart, rowEnd, trial);
                if (row == null) {
                    sectionOk = false;
                    break;
                }
                planned.add(row);
                trial.add(row);
                remaining -= chunkWeeks;
            }
            if (sectionOk) {
                for (Schedule s : planned) {
                    repository.save(s);
                    allInSemester.add(s);
                }
                createdCount += planned.size();
            } else {
                skippedSections++;
            }
        }

        String message = String.format(
                "Phân lịch xong: tạo %d dòng lịch; %d lớp học phần bỏ qua hoặc xếp không đủ (hết ô trống, khung tuần quá ngắn so với số buổi theo tín chỉ). "
                        + "Số buổi ≈ 5 × tín chỉ (tối đa 60); có thể nhiều dòng/lớp nếu buổi &gt; số tuần trong [tuần bắt đầu, tuần kết thúc].",
                createdCount, skippedSections);
        return new AutoScheduleResult(createdCount, skippedSections, message);
    }

    /**
     * Tạo một dòng lịch (chưa lưu DB) nếu tìm được ô trống; dùng danh sách {@code against} để kiểm tra chồng lấn tuần.
     */
    private Schedule buildAutoScheduleRowIfFree(
            Semester semester,
            ClassSection cs,
            Lecturer lec,
            List<Room> rooms,
            Map<Integer, TimeSlot> timeSlotById,
            List<int[]> candidateOrder,
            int rowStart,
            int rowEnd,
            List<Schedule> against
    ) {
        for (int[] daySlot : candidateOrder) {
            int day = daySlot[0];
            int timeSlotId = daySlot[1];
            TimeSlot slot = timeSlotById.get(timeSlotId);
            if (slot == null) {
                continue;
            }
            for (Room room : rooms) {
                if (!canPlaceRecurringSlot(lec, cs, room, day, timeSlotId, rowStart, rowEnd, against)) {
                    continue;
                }
                Schedule s = new Schedule();
                s.setSemester(semester);
                s.setClassSection(cs);
                s.setLecturer(lec);
                s.setRoom(room);
                s.setTimeSlot(slot);
                s.setDayOfWeek(day);
                s.setStartWeek(rowStart);
                s.setEndWeek(rowEnd);
                s.setWeekPattern(WeekPattern.ALL);
                s.setSessionType(SessionType.THEORY);
                s.setScheduleType(ScheduleType.NORMAL);
                s.setStatus(ScheduleStatus.ACTIVE);
                return s;
            }
        }
        return null;
    }

    private static boolean weeksOverlap(int aStart, int aEnd, int bStart, int bEnd) {
        return aStart <= bEnd && bStart <= aEnd;
    }

    private static boolean canPlaceRecurringSlot(
            Lecturer lec,
            ClassSection cs,
            Room room,
            int dayOfWeek,
            int timeSlotId,
            int newStartWeek,
            int newEndWeek,
            List<Schedule> schedules
    ) {
        UUID lecturerId = lec.getLecturerId();
        Long roomId = room.getRoomId();
        Long sectionId = cs.getId();
        for (Schedule x : schedules) {
            if (x.getDayOfWeek() == null || x.getTimeSlot() == null) {
                continue;
            }
            if (!Objects.equals(x.getDayOfWeek(), dayOfWeek)
                    || !Objects.equals(x.getTimeSlot().getId(), timeSlotId)) {
                continue;
            }
            int xsw = x.getStartWeek();
            int xew = x.getEndWeek();
            if (!weeksOverlap(newStartWeek, newEndWeek, xsw, xew)) {
                continue;
            }
            if (lecturerId != null && lecturerId.equals(x.getLecturer().getLecturerId())) {
                return false;
            }
            if (roomId != null && roomId.equals(x.getRoom().getRoomId())) {
                return false;
            }
            if (sectionId != null && sectionId.equals(x.getClassSection().getId())) {
                return false;
            }
        }
        return true;
    }

    @Transactional(readOnly = true)
    public List<ScheduleCalendarEventResponse> listCalendarEvents(Long semesterId, LocalDate from, LocalDate to) {
        Semester sem = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học kỳ"));
        LocalDate anchorMonday = semesterAnchorMonday(sem);
        List<Schedule> list = repository.findBySemesterIdWithDetails(semesterId);
        List<ScheduleCalendarEventResponse> out = new ArrayList<>();
        for (Schedule s : list) {
            TimeSlot slot = s.getTimeSlot();
            if (slot == null || slot.getStartTime() == null || slot.getEndTime() == null) {
                continue;
            }
            Integer dow = s.getDayOfWeek();
            if (dow == null) {
                continue;
            }
            Integer swBox = s.getStartWeek();
            Integer ewBox = s.getEndWeek();
            if (swBox == null || ewBox == null || swBox > ewBox) {
                continue;
            }
            int sw = swBox;
            int ew = ewBox;
            for (int w = sw; w <= ew; w++) {
                LocalDate d = dateForSemesterWeekAndDay(anchorMonday, w, dow);
                if (from != null && d.isBefore(from)) {
                    continue;
                }
                if (to != null && d.isAfter(to)) {
                    continue;
                }
                LocalDateTime start = LocalDateTime.of(d, slot.getStartTime());
                LocalDateTime end = LocalDateTime.of(d, slot.getEndTime());
                if (!end.isAfter(start)) {
                    end = end.plusDays(1);
                }
                String eid = s.getId() + "-" + w;
                Map<String, Object> props = calendarExtendedProps(s, d);
                out.add(new ScheduleCalendarEventResponse(
                        eid,
                        buildEventTitle(s, d),
                        start,
                        end,
                        colorForScheduleId(s.getId()),
                        props
                ));
            }
        }
        return out;
    }

    @Transactional(readOnly = true)
    public ScheduleCalendarMetaResponse getCalendarMeta() {
        List<ScheduleCalendarMetaResponse.SemesterRow> semesters = semesterRepository
                .findAll(Sort.by(Sort.Direction.DESC, "startDate"))
                .stream()
                .map(s -> new ScheduleCalendarMetaResponse.SemesterRow(
                        s.getId(),
                        s.getCode(),
                        s.getName(),
                        s.getStartDate() != null ? s.getStartDate().toString() : null,
                        semesterAnchorMonday(s).toString()))
                .toList();
        List<ScheduleCalendarMetaResponse.ClassSectionRow> classSections = classSectionRepository
                .findAll(Sort.by("classCode"))
                .stream()
                .map(cs -> new ScheduleCalendarMetaResponse.ClassSectionRow(
                        cs.getId(),
                        cs.getClassCode(),
                        cs.getClassName(),
                        cs.getSemester() != null ? cs.getSemester().getId() : null))
                .toList();
        List<ScheduleCalendarMetaResponse.LecturerRow> lecturers = lecturerRepository
                .findAll(Sort.by("lecturerCode"))
                .stream()
                .map(l -> new ScheduleCalendarMetaResponse.LecturerRow(
                        l.getLecturerId().toString(),
                        l.getLecturerCode(),
                        l.getFullName()))
                .toList();
        List<ScheduleCalendarMetaResponse.RoomRow> rooms = roomRepository
                .findAll(Sort.by("roomCode"))
                .stream()
                .map(r -> new ScheduleCalendarMetaResponse.RoomRow(
                        r.getRoomId(),
                        r.getRoomCode(),
                        r.getRoomName()))
                .toList();
        List<ScheduleCalendarMetaResponse.TimeSlotRow> timeSlots = timeSlotRepository
                .findAll(Sort.by("slotCode"))
                .stream()
                .filter(ts -> ts.getIsActive() == null || Boolean.TRUE.equals(ts.getIsActive()))
                .map(ts -> new ScheduleCalendarMetaResponse.TimeSlotRow(
                        ts.getId(),
                        ts.getSlotCode(),
                        ts.getStartTime() != null ? ts.getStartTime().toString() : null,
                        ts.getEndTime() != null ? ts.getEndTime().toString() : null,
                        ts.getPeriodStart(),
                        ts.getPeriodEnd()))
                .toList();
        return new ScheduleCalendarMetaResponse(
                semesters,
                classSections,
                lecturers,
                rooms,
                timeSlots,
                Arrays.stream(WeekPattern.values()).map(Enum::name).toList(),
                Arrays.stream(SessionType.values()).map(Enum::name).toList(),
                Arrays.stream(ScheduleType.values()).map(Enum::name).toList(),
                Arrays.stream(ScheduleStatus.values()).map(Enum::name).toList());
    }

    /**
     * Quy ước: ~5 buổi lý thuyết / 1 tín chỉ (chuẩn 1→5, 2→10, 3→15); tín chỉ lớn hơn nhân tiếp, tối đa 60 buổi.
     */
    private static int totalSessionsForCredits(Integer credits) {
        if (credits == null || credits < 1) {
            return 15;
        }
        return Math.min(60, credits * 5);
    }

    private static final Pattern ACADEMIC_YEAR_FOUR_DIGIT = Pattern.compile("(19|20)\\d{2}");

    
    private static LocalDate resolveSemesterCalendarStart(Semester sem) {
        if (sem.getStartDate() != null) {
            return sem.getStartDate();
        }
        String ay = sem.getAcademicYear();
        if (ay != null && !ay.isBlank()) {
            Matcher m = ACADEMIC_YEAR_FOUR_DIGIT.matcher(ay);
            if (m.find()) {
                try {
                    int y = Integer.parseInt(m.group());
                    return LocalDate.of(y, 9, 1);
                } catch (Exception ignored) {
                    // fall through
                }
            }
        }
        return LocalDate.now();
    }

    private static LocalDate semesterAnchorMonday(Semester sem) {
        return resolveSemesterCalendarStart(sem).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private static LocalDate dateForSemesterWeekAndDay(LocalDate week1Monday, int weekNumber, int dayModel) {
        int mondayBasedOffset = (dayModel == 8) ? 6 : (dayModel - 2);
        return week1Monday.plusWeeks(weekNumber - 1L).plusDays(mondayBasedOffset);
    }

    /** Tiêu đề một dòng (tooltip / tóm tắt) — luôn có ít nhất mã lớp HP / mã môn. */
    private static String buildEventTitle(Schedule s, LocalDate occurrenceDate) {
        String courseLine = buildCourseDisplayLine(s);
        if (courseLine == null || courseLine.isBlank()) {
            ClassSection cs = s.getClassSection();
            if (cs != null && cs.getClassCode() != null && !cs.getClassCode().isBlank()) {
                courseLine = cs.getClassCode().trim();
            } else {
                courseLine = "Lớp học phần";
            }
        }
        String day = s.getDayOfWeek() != null ? vietnameseDayOfWeek(s.getDayOfWeek()) : "";
        String slot = formatSlotBrief(s.getTimeSlot());
        String room = s.getRoom() != null && s.getRoom().getRoomCode() != null ? s.getRoom().getRoomCode() : "";
        String lec = s.getLecturer() != null && s.getLecturer().getFullName() != null ? s.getLecturer().getFullName() : "";
        String dateS = occurrenceDate != null ? occurrenceDate.toString() : "";
        StringBuilder sb = new StringBuilder();
        appendPart(sb, courseLine);
        appendPart(sb, day);
        appendPart(sb, dateS);
        appendPart(sb, slot);
        appendPart(sb, room);
        appendPart(sb, lec);
        return sb.length() > 0 ? sb.toString() : "Lịch học";
    }

    private static void appendPart(StringBuilder sb, String part) {
        if (part == null || part.isBlank()) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(" · ");
        }
        sb.append(part.trim());
    }

    /** Tên học phần + số tín chỉ (hoặc mã lớp HP). */
    private static String buildCourseDisplayLine(Schedule s) {
        ClassSection cs = s.getClassSection();
        if (cs == null) {
            return "";
        }
        Course c = cs.getCourse();
        String name = (c != null && c.getCourseName() != null) ? c.getCourseName().trim() : "";
        String code = (c != null && c.getCourseCode() != null) ? c.getCourseCode().trim() : "";
        Integer cred = c != null ? c.getCredits() : null;
        String credPart = cred != null ? " (" + cred + " TC)" : "";
        if (!name.isEmpty()) {
            return name + credPart;
        }
        if (!code.isEmpty()) {
            return code + credPart;
        }
        String cc = cs.getClassCode() != null ? cs.getClassCode().trim() : "";
        return cc + credPart;
    }

    private static String vietnameseDayOfWeek(int modelDow) {
        return switch (modelDow) {
            case 2 -> "Thứ 2";
            case 3 -> "Thứ 3";
            case 4 -> "Thứ 4";
            case 5 -> "Thứ 5";
            case 6 -> "Thứ 6";
            case 7 -> "Thứ 7";
            case 8 -> "Chủ nhật";
            default -> "Thứ " + modelDow;
        };
    }

    /** Mã khung giờ + khoảng giờ (rút :00 giây nếu có). */
    private static String formatSlotBrief(TimeSlot slot) {
        if (slot == null) {
            return "";
        }
        String code = slot.getSlotCode() != null ? slot.getSlotCode().trim() : "";
        if (slot.getStartTime() == null || slot.getEndTime() == null) {
            return code;
        }
        String a = trimSeconds(slot.getStartTime().toString());
        String b = trimSeconds(slot.getEndTime().toString());
        String range = a + "–" + b;
        return code.isEmpty() ? range : (code + " " + range);
    }

    private static String trimSeconds(String time) {
        if (time != null && time.length() >= 8 && time.endsWith(":00")) {
            return time.substring(0, time.length() - 3);
        }
        return time != null ? time : "";
    }

    private static Map<String, Object> calendarExtendedProps(Schedule s, LocalDate occurrenceDate) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("scheduleId", s.getId().toString());
        m.put("semesterId", s.getSemester() != null ? s.getSemester().getId() : null);
        ClassSection cs = s.getClassSection();
        Course c = cs != null ? cs.getCourse() : null;
        m.put("classSectionId", cs != null ? cs.getId() : null);
        if (cs != null && cs.getClassCode() != null && !cs.getClassCode().isBlank()) {
            m.put("classSectionCode", cs.getClassCode().trim());
        }
        if (c != null && c.getCourseCode() != null && !c.getCourseCode().isBlank()) {
            m.put("courseCode", c.getCourseCode().trim());
        }
        if (c != null && c.getCourseName() != null && !c.getCourseName().isBlank()) {
            m.put("courseName", c.getCourseName().trim());
        }
        if (c != null && c.getCredits() != null) {
            m.put("credits", c.getCredits());
        }
        m.put("lecturerId", s.getLecturer() != null ? s.getLecturer().getLecturerId().toString() : null);
        m.put("roomId", s.getRoom() != null ? s.getRoom().getRoomId() : null);
        m.put("timeSlotId", s.getTimeSlot() != null ? s.getTimeSlot().getId() : null);
        m.put("periodStart", s.getTimeSlot() != null ? s.getTimeSlot().getPeriodStart() : null);
        m.put("periodEnd", s.getTimeSlot() != null ? s.getTimeSlot().getPeriodEnd() : null);
        m.put("roomCode", s.getRoom() != null ? s.getRoom().getRoomCode() : null);
        m.put("dayOfWeek", s.getDayOfWeek());
        m.put("startWeek", s.getStartWeek());
        m.put("endWeek", s.getEndWeek());
        m.put("weekPattern", s.getWeekPattern() != null ? s.getWeekPattern().name() : null);
        m.put("sessionType", s.getSessionType() != null ? s.getSessionType().name() : null);
        m.put("scheduleType", s.getScheduleType() != null ? s.getScheduleType().name() : null);
        m.put("status", s.getStatus() != null ? s.getStatus().name() : null);
        m.put("note", s.getNote());

        String courseDisplay = buildCourseDisplayLine(s);
        if (!courseDisplay.isBlank()) {
            m.put("courseDisplay", courseDisplay);
        }
        String slotDisplay = formatSlotBrief(s.getTimeSlot());
        if (!slotDisplay.isBlank()) {
            m.put("slotDisplay", slotDisplay);
        }
        if (s.getDayOfWeek() != null) {
            m.put("dayDisplay", vietnameseDayOfWeek(s.getDayOfWeek()));
        }
        if (occurrenceDate != null) {
            m.put("instanceDate", occurrenceDate.toString());
        }
        Room room = s.getRoom();
        if (room != null) {
            String rc = room.getRoomCode() != null ? room.getRoomCode().trim() : "";
            String rn = room.getRoomName() != null ? room.getRoomName().trim() : "";
            String roomDisplay = rc;
            if (!rn.isEmpty() && !rn.equals(rc)) {
                roomDisplay = rc.isEmpty() ? rn : (rc + " — " + rn);
            }
            if (!roomDisplay.isBlank()) {
                m.put("roomDisplay", roomDisplay);
            }
        }
        if (s.getLecturer() != null && s.getLecturer().getFullName() != null
                && !s.getLecturer().getFullName().isBlank()) {
            m.put("lecturerDisplay", s.getLecturer().getFullName().trim());
        }
        return m;
    }

    private static String colorForScheduleId(UUID id) {
        int h = Math.floorMod(id.hashCode(), 0xffffff);
        return String.format("#%06x", h);
    }

    @Transactional(readOnly = true)
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
                ClassSection cs = classSectionRepository
                        .findByClassCodeIgnoreCaseAndSemester_Id(classCode.trim(), sem.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Dòng " + rowNum + ": Không tìm thấy lớp " + classCode + " trong học kỳ " + semesterCode));
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
                if (startWeek > endWeek) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Dòng " + rowNum + ": Tuần bắt đầu phải <= tuần kết thúc");
                }
                validateWeekRange(startWeek, endWeek);

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

    @Transactional(readOnly = true)
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

    private static void assertClassSectionMatchesSemester(ClassSection cs, Semester semester) {
        if (cs == null || semester == null) return;
        if (cs.getSemester() == null || !cs.getSemester().getId().equals(semester.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lớp học phần không thuộc học kỳ đã chọn");
        }
    }

    private static void validateWeekRange(Integer startWeek, Integer endWeek) {
        if (startWeek != null && (startWeek < 1 || startWeek > 53)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tuần bắt đầu phải từ 1 đến 53");
        }
        if (endWeek != null && (endWeek < 1 || endWeek > 53)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tuần kết thúc phải từ 1 đến 53");
        }
        if (startWeek != null && endWeek != null && startWeek > endWeek) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tuần bắt đầu phải <= tuần kết thúc");
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
