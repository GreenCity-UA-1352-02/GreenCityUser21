package greencity.service;

import greencity.dto.notification.NotificationEvent;
import greencity.dto.notification.NotificationPayloadDto;
import greencity.enums.NotificationType;
import greencity.service.cache.NotificationCache;
import greencity.service.formatter.NotificationMessageFormatter;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Implementation of NotificationService interface. Processes notification
 * events from Kafka and sends them to users via Telegram. Uses separation of
 * concerns and reduced code duplication for better maintainability.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final TelegramBotService telegramBotService;
    private final NotificationCache notificationCache;
    private final NotificationMessageFormatter messageFormatter;

    /**
     * Listens for notification events from Kafka.
     *
     * @param event The notification event
     */
    @KafkaListener(topics = {"notifications.greencity", "notifications.user"},
        groupId = "${spring.kafka.consumer.group-id:notification-service-group}")
    public void processNotification(NotificationEvent event) {
        log.info("Received notification event: {}", event);
        logNotificationDetails(event);

        try {
            NotificationType type = event.getEventType();
            NotificationPayloadDto payload = event.getPayload();
            Long targetUserId = event.getTargetUserId();

            processNotificationEvent(type, payload, targetUserId, event.getTimestamp());
        } catch (Exception e) {
            log.error("Error processing notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Logs notification details for debugging.
     *
     * @param event The notification event
     */
    private void logNotificationDetails(NotificationEvent event) {
        System.out.println("\n\n====== NOTIFICATION RECEIVED ======");
        System.out.println("Event Type: " + event.getEventType());
        System.out.println("Target User: " + event.getTargetUserId());
        System.out.println("Source: " + event.getSource());
        System.out.println("Payload: " + event.getPayload());
        System.out.println("Timestamp: " + event.getTimestamp());
        System.out.println("===================================\n");
    }

    /**
     * Processes a notification event.
     *
     * @param type         Notification type
     * @param payload      Notification payload
     * @param targetUserId Target user ID
     * @param timestamp    Timestamp
     */
    private void processNotificationEvent(NotificationType type, NotificationPayloadDto payload,
        Long targetUserId, LocalDateTime timestamp) {
        // Extract data from payload
        Long actorId = payload.getActorId();
        String actorName = payload.getActorName();
        Long articleId = payload.getArticleId();
        String articleTitle = payload.getArticleTitle();
        String objectType = payload.getObjectType();

        // Cache data
        notificationCache.cacheUserName(actorId, actorName);
        notificationCache.cacheArticleTitle(articleId, articleTitle);

        // Add actor to notification
        notificationCache.addNotificationActor(type, articleId, targetUserId, actorId, timestamp);

        // Generate notification key
        String notificationKey = notificationCache.generateNotificationKey(type, articleId, targetUserId);

        // Format message
        String message = messageFormatter.formatMessage(type, notificationKey, articleTitle, objectType, timestamp);

        // Send notification
        boolean sent = telegramBotService.sendNotificationToUser(targetUserId, message);
        if (sent) {
            log.info("{} notification sent to user {}", type, targetUserId);
        }
    }
}
