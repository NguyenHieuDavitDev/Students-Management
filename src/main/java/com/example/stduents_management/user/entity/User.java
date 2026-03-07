package com.example.stduents_management.user.entity;

import com.example.stduents_management.lecturer.entity.Lecturer;
import com.example.stduents_management.role.entity.Role;
import com.example.stduents_management.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(nullable = false, unique = true, columnDefinition = "NVARCHAR(100)")
    private String username;

    @Column(nullable = false, unique = true, columnDefinition = "NVARCHAR(150)")
    private String email;

    // dùng để đăng nhập
    @Column(nullable = false)
    private String password;

    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /** Liên kết 1-1: User (1) -------- (1) Student. Khóa ngoại student_id trên bảng users. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", unique = true, columnDefinition = "uniqueidentifier")
    private Student student;

    /** Liên kết 1-1: User (1) -------- (1) Lecturer. Khóa ngoại lecturer_id trên bảng users. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", unique = true, columnDefinition = "uniqueidentifier")
    private Lecturer lecturer;
}
