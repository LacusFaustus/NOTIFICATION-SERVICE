package com.notificationservice.repository;

import com.notificationservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    List<User> findByActiveTrue();

    List<User> findByPlatformAndActiveTrue(User.Platform platform);

    @Query("SELECT u FROM User u WHERE u.pushToken IS NOT NULL AND u.active = true")
    List<User> findUsersWithPushTokens();

    Optional<User> findByDeviceId(String deviceId);

    List<User> findByPushTokenNotNullAndActiveTrue();

    @Query("SELECT u FROM User u WHERE u.platform = :platform AND u.pushToken IS NOT NULL AND u.active = true")
    List<User> findUsersWithPushTokensByPlatform(@Param("platform") User.Platform platform);

    @Query("SELECT u FROM User u WHERE u.email IS NOT NULL AND u.active = true")
    List<User> findUsersWithEmail();

    @Query("SELECT u FROM User u WHERE u.lastSeenAt >= :since AND u.active = true")
    List<User> findActiveUsersSince(@Param("since") LocalDateTime since);

    @Modifying
    @Query("UPDATE User u SET u.pushToken = :pushToken, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    int updatePushToken(@Param("userId") String userId, @Param("pushToken") String pushToken);

    @Modifying
    @Query("UPDATE User u SET u.lastSeenAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    int updateLastSeen(@Param("userId") String userId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true AND u.pushToken IS NOT NULL")
    long countUsersWithPushEnabled();

    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true AND u.email IS NOT NULL")
    long countUsersWithEmailEnabled();

    List<User> findByLastSeenAtBeforeAndActiveTrue(LocalDateTime date);

    @Query("SELECT u.platform, COUNT(u) FROM User u WHERE u.active = true GROUP BY u.platform")
    List<Object[]> countUsersByPlatform();
}
