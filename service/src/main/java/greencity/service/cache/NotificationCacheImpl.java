package greencity.service.cache;

import greencity.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Implementation of NotificationCache interface.
 */
@Component
public class NotificationCacheImpl implements NotificationCache {
    private final Map<String, Map<Long, LocalDateTime>> recentNotifications = new ConcurrentHashMap<>();
    private final Map<Long, String> userNameCache = new ConcurrentHashMap<>();
    private final Map<Long, String> articleTitleCache = new ConcurrentHashMap<>();

    @Override
    public void cacheUserName(Long userId, String userName) {
        userNameCache.put(userId, userName);
    }

    @Override
    public void cacheArticleTitle(Long articleId, String articleTitle) {
        articleTitleCache.put(articleId, articleTitle != null ? articleTitle : "Unknown");
    }

    @Override
    public void addNotificationActor(NotificationType type, Long articleId, Long targetUserId,
        Long actorId, LocalDateTime timestamp) {
        String notificationKey = generateNotificationKey(type, articleId, targetUserId);
        Map<Long, LocalDateTime> actors = recentNotifications.computeIfAbsent(
            notificationKey, k -> new HashMap<>());
        actors.put(actorId, timestamp);
    }

    @Override
    public String getUserName(Long userId) {
        return userNameCache.get(userId);
    }

    @Override
    public String getArticleTitle(Long articleId) {
        return articleTitleCache.get(articleId);
    }

    @Override
    public Map<Long, LocalDateTime> getActorsForNotification(String notificationKey) {
        return recentNotifications.get(notificationKey);
    }

    @Override
    public String generateNotificationKey(NotificationType type, Long articleId, Long targetUserId) {
        return type.name() + "_" + articleId + "_" + targetUserId;
    }
}
