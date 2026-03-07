package com.example.stduents_management.permission.repository;

import com.example.stduents_management.permission.entity.RolePermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    boolean existsByRole_IdAndPermission_Id(UUID roleId, UUID permissionId);

    Optional<RolePermission> findByRole_IdAndPermission_Id(UUID roleId, UUID permissionId);

    @Query("""
            SELECT rp FROM RolePermission rp
            JOIN FETCH rp.role r
            JOIN FETCH rp.permission p
            WHERE (:keyword IS NULL OR :keyword = ''
                OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<RolePermission> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT rp FROM RolePermission rp JOIN FETCH rp.role r JOIN FETCH rp.permission p ORDER BY r.name, p.code")
    List<RolePermission> findAllForPrint();

    @Query("SELECT rp FROM RolePermission rp JOIN rp.role r JOIN rp.permission p ORDER BY r.name, p.code")
    Page<RolePermission> findAllOrderByRoleNameAndPermissionCode(Pageable pageable);
}
