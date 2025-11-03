package com.notificationservice.repository;

import com.notificationservice.entity.EmailProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailProviderRepository extends JpaRepository<EmailProvider, String> {

    List<EmailProvider> findByActiveTrue();

    List<EmailProvider> findByActiveTrueOrderByPriorityAsc();

    @Query("SELECT ep FROM EmailProvider ep WHERE ep.active = true AND ep.currentUsage < ep.dailyLimit ORDER BY ep.priority ASC")
    List<EmailProvider> findAvailableProviders();

    @Query("SELECT ep FROM EmailProvider ep WHERE ep.active = true AND ep.currentUsage < ep.dailyLimit ORDER BY ep.priority ASC, ep.currentUsage ASC")
    List<EmailProvider> findBestAvailableProviders();

    Optional<EmailProvider> findByName(String name);

    @Query("SELECT ep FROM EmailProvider ep WHERE ep.lastUsed IS NOT NULL ORDER BY ep.lastUsed DESC")
    List<EmailProvider> findRecentlyUsedProviders();

    @Modifying
    @Query("UPDATE EmailProvider ep SET ep.currentUsage = 0, ep.lastReset = CURRENT_TIMESTAMP WHERE ep.lastReset < CURRENT_DATE")
    int resetDailyUsage();

    @Modifying
    @Query("UPDATE EmailProvider ep SET ep.currentUsage = ep.currentUsage + :count WHERE ep.id = :providerId")
    int incrementUsage(@Param("providerId") String providerId, @Param("count") int count);

    @Query("SELECT COUNT(ep) FROM EmailProvider ep WHERE ep.active = true AND ep.currentUsage < ep.dailyLimit")
    long countAvailableProviders();

    @Query("SELECT ep FROM EmailProvider ep WHERE ep.dailyLimit > 0 AND ep.currentUsage >= ep.dailyLimit")
    List<EmailProvider> findExhaustedProviders();
}
