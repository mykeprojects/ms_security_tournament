package com.uc.ms_security.repository;

import com.uc.ms_security.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    List<RolePermission> findByRoleId(Long roleId);

    @Query("SELECT rp FROM RolePermission rp JOIN FETCH rp.role")
    List<RolePermission> findAllWithRole();
}