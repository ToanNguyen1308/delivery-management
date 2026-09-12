package com.viettel.delivery.repository;

import com.viettel.delivery.entity.RoleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleGroupRepository extends JpaRepository<RoleGroup, Long> {

    Optional<RoleGroup> findByRoleGroupCodeAndIsDeletedFalse(String roleGroupCode);

    Optional<RoleGroup> findByIdAndIsDeletedFalse(Long id);

    boolean existsByRoleGroupCodeAndIsDeletedFalse(String roleGroupCode);

    List<RoleGroup> findAllByIsDeletedFalseOrderByRoleGroupCodeAsc();

    @Query("""
            SELECT DISTINCT rg FROM RoleGroup rg
            LEFT JOIN FETCH rg.functions
            WHERE rg.isDeleted = false
            ORDER BY rg.roleGroupCode
            """)
    List<RoleGroup> findAllWithFunctions();

    @Query("""
            SELECT DISTINCT rg FROM RoleGroup rg
            LEFT JOIN FETCH rg.functions
            WHERE rg.id = :id AND rg.isDeleted = false
            """)
    Optional<RoleGroup> findByIdWithFunctions(@Param("id") Long id);

    Set<RoleGroup> findAllByIdInAndIsDeletedFalse(Set<Long> ids);

    Set<RoleGroup> findAllByRoleGroupCodeInAndIsDeletedFalse(Set<String> codes);
}
