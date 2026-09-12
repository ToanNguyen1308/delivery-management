package com.viettel.delivery.repository;

import com.viettel.delivery.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * Nap kem nhom quyen va chuc nang de tranh N+1 khi dung nhap.
     */
    @Query("""
            SELECT DISTINCT u FROM User u
            LEFT JOIN FETCH u.roleGroups rg
            LEFT JOIN FETCH rg.functions
            WHERE u.username = :username AND u.isDeleted = false
            """)
    Optional<User> findByUsernameWithAuthorities(@Param("username") String username);

    @Query("""
            SELECT DISTINCT u FROM User u
            LEFT JOIN FETCH u.roleGroups rg
            LEFT JOIN FETCH rg.functions
            WHERE u.id = :id AND u.isDeleted = false
            """)
    Optional<User> findByIdWithAuthorities(@Param("id") Long id);

    Optional<User> findByIdAndIsDeletedFalse(Long id);

    Optional<User> findByUsernameAndIsDeletedFalse(String username);

    boolean existsByUsernameAndIsDeletedFalse(String username);

    boolean existsByEmailAndIsDeletedFalse(String email);

    boolean existsByPhoneNumberAndIsDeletedFalse(String phoneNumber);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id <> :id AND u.isDeleted = false")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.phoneNumber = :phone AND u.id <> :id AND u.isDeleted = false")
    boolean existsByPhoneNumberAndIdNot(@Param("phone") String phoneNumber, @Param("id") Long id);

    @Query("""
            SELECT DISTINCT u.id FROM User u
            JOIN u.roleGroups rg
            WHERE rg.roleGroupCode = :roleGroupCode AND u.isDeleted = false
            """)
    List<Long> findUserIdsByRoleGroupCode(@Param("roleGroupCode") String roleGroupCode);
}
