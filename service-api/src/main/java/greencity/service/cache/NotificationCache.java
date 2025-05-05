package greencity.service.cache;

import greencity.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Interface for caching notification data.
 */
public interface NotificationCache {
    /**
     * Caches user name by user ID.
     *
     * @param userId   User ID
     * @param userName User name
     */
    void cacheUserName(Long userId, String userName);

    /**
     * Caches article title by article ID.
     *
     * @param articleId    Article ID
     * @param articleTitle Article title
     */
    void cacheArticleTitle(Long articleId, String articleTitle);

    /**
     * Adds an actor to a notification.
     *
     * @param type         Notification type
     * @param articleId    Article ID
     * @param targetUserId Target user ID
     * @param actorId      Actor ID
     * @param timestamp    Timestamp
     */
    void addNotificationActor(NotificationType type, Long articleId, Long targetUserId,
        Long actorId, LocalDateTime timestamp);

    /**
     * Gets cached user name.
     *
     * @param userId User ID
     * @return User name
     */
    String getUserName(Long userId);

    /**
     * Gets cached article title.
     *
     * @param articleId Article ID
     * @return Article title
     */
    String getArticleTitle(Long articleId);

    /**
     * Gets actors for a notification key.
     *
     * @param notificationKey Notification key
     * @return Map of actor IDs to timestamps
     */
    Map<Long, LocalDateTime> getActorsForNotification(String notificationKey);

    /**
     * Generates a notification key.
     *
     * @param type         Notification type
     * @param articleId    Article ID
     * @param targetUserId Target user ID
     * @return Notification key
     */
    String generateNotificationKey(NotificationType type, Long articleId, Long targetUserId);
}
