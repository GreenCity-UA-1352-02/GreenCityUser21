package greencity.service.formatter;

import greencity.enums.NotificationType;
import java.time.LocalDateTime;

/**
 * Interface for formatting notification messages.
 */
public interface NotificationMessageFormatter {
    /**
     * Formats a notification message.
     *
     * @param type            Notification type
     * @param notificationKey Notification key
     * @param articleTitle    Article title
     * @param objectType      Object type (optional, can be null)
     * @param timestamp       Timestamp
     * @return Formatted message
     */
    String formatMessage(NotificationType type, String notificationKey, String articleTitle,
        String objectType, LocalDateTime timestamp);

    /**
     * Formats a date and time according to requirements.
     *
     * @param dateTime The date and time to format
     * @return Formatted date and time string
     */
    String formatDateTime(LocalDateTime dateTime);
}
