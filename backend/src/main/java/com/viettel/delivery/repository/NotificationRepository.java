package com.viettel.delivery.repository;

import com.viettel.delivery.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(value = """
            SELECT n FROM Notification n
            WHERE n.user.id = :userId AND n.isDeleted = false
            ORDER BY n.createdDate DESC
            """,
            countQuery = """
                    SELECT COUNT(n) FROM Notification n
                    WHERE n.user.id = :userId AND n.isDeleted = false
                    """)
    Page<Notification> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT COUNT(n) FROM Notification n
            WHERE n.user.id = :userId AND n.isRead = false AND n.isDeleted = false
            """)
    long countUnread(@Param("userId") Long userId);

    Optional<Notification> findByIdAndUserIdAndIsDeletedFalse(Long id, Long userId);

    @Modifying
    @Query("""
            UPDATE Notification n SET n.isRead = true, n.readAt = :now
            WHERE n.user.id = :userId AND n.isRead = false
            """)
    int markAllRead(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
