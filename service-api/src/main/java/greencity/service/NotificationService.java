package greencity.service;

import greencity.dto.notification.NotificationEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final TelegramBotService telegramBotService;

    private final Map<String, Map<Long, LocalDateTime>> recentNotifications = new ConcurrentHashMap<>();

    private final Map<Long, String> userNameCache = new ConcurrentHashMap<>();

    private final Map<Long, String> articleTitleCache = new ConcurrentHashMap<>();

    /**
     * Listens for notification events from Kafka.
     *
     * @param event The notification event
     */
    @KafkaListener(topics = {"notifications.greencity", "notifications.user"},
        groupId = "${spring.kafka.consumer.group-id:notification-service-group}")
    public void processNotification(NotificationEvent event) {
        log.info("Received notification event: {}", event);
        System.out.println("\n\n====== NOTIFICATION RECEIVED ======");
        System.out.println("Event Type: " + event.getEventType());
        System.out.println("Target User: " + event.getTargetUserId());
        System.out.println("Source: " + event.getSource());
        System.out.println("Payload: " + event.getPayload());
        System.out.println("Timestamp: " + event.getTimestamp());
        System.out.println("===================================\n");

        try {
            switch (event.getEventType()) {
                case "COMMENT_CREATED" -> processCommentNotification(event);
                case "ARTICLE_LIKED" -> processLikeNotification(event);
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Processes a comment notification.
     *
     * @param event The notification event
     */
    @SuppressWarnings("unchecked")
    private void processCommentNotification(NotificationEvent event) {
        Map<String, Object> payload = event.getPayload();
        Long targetUserId = event.getTargetUserId();
        Long actorId = ((Number) payload.get("actorId")).longValue();
        String actorName = (String) payload.get("actorName");
        Long articleId = ((Number) payload.get("articleId")).longValue();
        String articleTitle = (String) payload.get("articleTitle");

        userNameCache.put(actorId, actorName);
        articleTitleCache.put(articleId, articleTitle);

        String notificationKey = "COMMENT_" + articleId + "_" + targetUserId;

        Map<Long, LocalDateTime> actors = recentNotifications.computeIfAbsent(
            notificationKey, k -> new HashMap<>());

        actors.put(actorId, event.getTimestamp());

        String message = formatCommentNotification(notificationKey, articleTitle);

        boolean sent = telegramBotService.sendNotificationToUser(targetUserId, message);
        if (sent) {
            log.info("Comment notification sent to user {}", targetUserId);
        }
    }

    /**
     * Processes a like notification.
     *
     * @param event The notification event
     */
    @SuppressWarnings("unchecked")
    private void processLikeNotification(NotificationEvent event) {
        Map<String, Object> payload = event.getPayload();
        Long targetUserId = event.getTargetUserId();
        Long actorId = ((Number) payload.get("actorId")).longValue();
        String actorName = (String) payload.get("actorName");
        Long articleId = ((Number) payload.get("articleId")).longValue();
        String articleTitle = (String) payload.get("articleTitle");

        userNameCache.put(actorId, actorName);
        articleTitleCache.put(articleId, articleTitle);

        String notificationKey = "LIKE_" + articleId + "_" + targetUserId;

        Map<Long, LocalDateTime> actors = recentNotifications.computeIfAbsent(
            notificationKey, k -> new HashMap<>());

        actors.put(actorId, event.getTimestamp());

        String message = formatLikeNotification(notificationKey, articleTitle);

        boolean sent = telegramBotService.sendNotificationToUser(targetUserId, message);
        if (sent) {
            log.info("Like notification sent to user {}", targetUserId);
        }
    }

    /**
     * Formats a comment notification message.
     *
     * @param notificationKey The key for the notification
     * @param articleTitle    The article title
     * @return Formatted message
     */
    private String formatCommentNotification(String notificationKey, String articleTitle) {
        Map<Long, LocalDateTime> actors = recentNotifications.get(notificationKey);

        Long[] actorIds = actors.keySet().toArray(new Long[0]);
        java.util.Arrays.sort(actorIds, (a, b) -> actors.get(b).compareTo(actors.get(a)));

        String actorsText;
        if (actorIds.length == 1) {
            actorsText = userNameCache.get(actorIds[0]);
        } else if (actorIds.length == 2) {
            actorsText = userNameCache.get(actorIds[0]) + " and "
                + userNameCache.get(actorIds[1]);
        } else {
            actorsText = userNameCache.get(actorIds[0]) + ", "
                + userNameCache.get(actorIds[1]) + " and other users";
        }

        LocalDateTime timestamp = actors.get(actorIds[0]);
        String timeText = formatDateTime(timestamp);

        String truncatedTitle = articleTitle.length() > 30 ? articleTitle.substring(0, 27) + "..." : articleTitle;

        String verb = "commented on";
        return actorsText + " " + verb + " your news \"" + truncatedTitle + "\". " + timeText;
    }

    /**
     * Formats a like notification message.
     *
     * @param notificationKey The key for the notification
     * @param articleTitle    The article title
     * @return Formatted message
     */
    private String formatLikeNotification(String notificationKey, String articleTitle) {
        Map<Long, LocalDateTime> actors = recentNotifications.get(notificationKey);

        Long[] actorIds = actors.keySet().toArray(new Long[0]);
        java.util.Arrays.sort(actorIds, (a, b) -> actors.get(b).compareTo(actors.get(a)));

        String actorsText;
        if (actorIds.length == 1) {
            actorsText = userNameCache.get(actorIds[0]);
        } else if (actorIds.length == 2) {
            actorsText = userNameCache.get(actorIds[0]) + " and "
                + userNameCache.get(actorIds[1]);
        } else {
            actorsText = userNameCache.get(actorIds[0]) + ", "
                + userNameCache.get(actorIds[1]) + " and other users";
        }

        LocalDateTime timestamp = actors.get(actorIds[0]);
        String timeText = formatDateTime(timestamp);

        String truncatedTitle = articleTitle.length() > 30 ? articleTitle.substring(0, 27) + "..." : articleTitle;

        String verb = actorIds.length > 1 ? "like" : "likes";
        return actorsText + " " + verb + " your news \"" + truncatedTitle + "\". " + timeText;
    }

    /**
     * Formats a date and time according to requirements.
     *
     * @param dateTime The date and time to format
     * @return Formatted date and time string
     */
    private String formatDateTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);

        String timeStr = dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"));

        if (dateTime.toLocalDate().equals(now.toLocalDate())) {
            return "Today " + timeStr;
        } else if (dateTime.toLocalDate().equals(yesterday.toLocalDate())) {
            return "Yesterday " + timeStr;
        } else {
            return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " " + timeStr;
        }
    }
}
