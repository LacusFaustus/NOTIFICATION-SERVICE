package com.notificationservice.repository;

import com.notificationservice.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<NotificationTemplate, String> {

    // Basic CRUD operations are provided by JpaRepository

    // Find template by name
    Optional<NotificationTemplate> findByName(String name);

    // Find templates by type
    List<NotificationTemplate> findByType(String type);

    // Find active templates
    List<NotificationTemplate> findByActiveTrue();

    // Find active templates by type
    List<NotificationTemplate> findByTypeAndActiveTrue(String type);

    // Find templates by name containing (case-insensitive)
    List<NotificationTemplate> findByNameContainingIgnoreCase(String name);

    // Find templates by active status and type
    List<NotificationTemplate> findByActiveAndType(Boolean active, String type);

    // Find templates created by specific user
    List<NotificationTemplate> findByCreatedBy(String createdBy);

    // Find templates with version greater than specified
    List<NotificationTemplate> findByVersionGreaterThan(String version);

    // Custom query to find templates by type and active status with sorting
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.type = :type AND nt.active = :active ORDER BY nt.name ASC")
    List<NotificationTemplate> findByTypeAndActiveOrderByName(@Param("type") String type, @Param("active") boolean active);

    // Custom query to find templates with specific variable
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.variables LIKE %:variable%")
    List<NotificationTemplate> findByVariable(@Param("variable") String variable);

    // Count templates by type
    @Query("SELECT COUNT(nt) FROM NotificationTemplate nt WHERE nt.type = :type")
    long countByType(@Param("type") String type);

    // Count active templates by type
    @Query("SELECT COUNT(nt) FROM NotificationTemplate nt WHERE nt.type = :type AND nt.active = true")
    long countActiveByType(@Param("type") String type);

    // Find latest templates (ordered by updated date)
    @Query("SELECT nt FROM NotificationTemplate nt ORDER BY nt.updatedAt DESC")
    List<NotificationTemplate> findLatestTemplates();

    // Find templates updated after specific date
    List<NotificationTemplate> findByUpdatedAtAfter(java.time.LocalDateTime date);

    // Find templates by multiple types
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.type IN :types")
    List<NotificationTemplate> findByTypes(@Param("types") List<String> types);

    // Find templates by name pattern and type
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.name LIKE %:namePattern% AND nt.type = :type")
    List<NotificationTemplate> findByNamePatternAndType(@Param("namePattern") String namePattern, @Param("type") String type);

    // Update template active status
    @Modifying
    @Query("UPDATE NotificationTemplate nt SET nt.active = :active, nt.updatedAt = CURRENT_TIMESTAMP WHERE nt.id = :id")
    int updateActiveStatus(@Param("id") String id, @Param("active") boolean active);

    // Update template version (упрощенная версия)
    @Modifying
    @Query("UPDATE NotificationTemplate nt SET nt.version = :version, nt.updatedAt = CURRENT_TIMESTAMP WHERE nt.id = :id")
    int updateVersion(@Param("id") String id, @Param("version") String version);

    // УДАЛИТЕ этот метод - он вызывает ошибку
    // @Modifying
    // @Query("UPDATE NotificationTemplate nt SET nt.version = CONCAT(SPLIT_PART(nt.version, '.', 1), '.', (SPLIT_PART(nt.version, '.', 2)::integer + 1)::text), nt.updatedAt = CURRENT_TIMESTAMP WHERE nt.id = :id")
    // int incrementMinorVersion(@Param("id") String id);

    // Bulk update active status by type
    @Modifying
    @Query("UPDATE NotificationTemplate nt SET nt.active = :active, nt.updatedAt = CURRENT_TIMESTAMP WHERE nt.type = :type")
    int updateActiveStatusByType(@Param("type") String type, @Param("active") boolean active);

    // Find duplicate templates (same name, different ID)
    @Query("SELECT nt1 FROM NotificationTemplate nt1 WHERE EXISTS (SELECT 1 FROM NotificationTemplate nt2 WHERE nt2.name = nt1.name AND nt2.id != nt1.id)")
    List<NotificationTemplate> findDuplicateTemplates();

    // Get template usage statistics
    @Query(value = """
        SELECT 
            nt.type as templateType,
            COUNT(n.id) as usageCount
        FROM notification_templates nt
        LEFT JOIN notifications n ON nt.id = n.template_id
        WHERE nt.active = true
        GROUP BY nt.type
        ORDER BY usageCount DESC
        """, nativeQuery = true)
    List<Object[]> getTemplateUsageStatistics();

    // Find templates with no associated notifications
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.id NOT IN (SELECT DISTINCT n.templateId FROM Notification n WHERE n.templateId IS NOT NULL)")
    List<NotificationTemplate> findUnusedTemplates();

    // Search templates by multiple criteria
    @Query("""
        SELECT nt FROM NotificationTemplate nt WHERE 
        (:name IS NULL OR nt.name LIKE %:name%) AND
        (:type IS NULL OR nt.type = :type) AND
        (:active IS NULL OR nt.active = :active) AND
        (:createdBy IS NULL OR nt.createdBy = :createdBy)
        ORDER BY nt.updatedAt DESC
        """)
    List<NotificationTemplate> searchTemplates(
            @Param("name") String name,
            @Param("type") String type,
            @Param("active") Boolean active,
            @Param("createdBy") String createdBy
    );

    // Find templates that need version update (based on content changes)
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.updatedAt > nt.createdAt AND nt.version = '1.0'")
    List<NotificationTemplate> findTemplatesNeedingVersionUpdate();

    // Get template count by status
    @Query("SELECT nt.active, COUNT(nt) FROM NotificationTemplate nt GROUP BY nt.active")
    List<Object[]> countTemplatesByActiveStatus();

    // Find templates with specific content pattern
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.content LIKE %:contentPattern%")
    List<NotificationTemplate> findByContentPattern(@Param("contentPattern") String contentPattern);

    // Update template content and increment version (упрощенная версия)
    @Modifying
    @Query("""
        UPDATE NotificationTemplate nt 
        SET nt.content = :content, 
            nt.subject = :subject,
            nt.updatedAt = CURRENT_TIMESTAMP 
        WHERE nt.id = :id
        """)
    int updateTemplateContent(
            @Param("id") String id,
            @Param("content") String content,
            @Param("subject") String subject
    );

    // Find templates with specific variables (JSON array contains)
    @Query(value = """
        SELECT * FROM notification_templates nt 
        WHERE nt.variables LIKE CONCAT('%', :variable, '%')
        """, nativeQuery = true)
    List<NotificationTemplate> findByVariablesContaining(@Param("variable") String variable);
}
