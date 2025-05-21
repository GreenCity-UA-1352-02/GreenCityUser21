package greencity.service.formatter;

import greencity.enums.NotificationType;
import greencity.service.cache.NotificationCache;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Implementation of NotificationMessageFormatter interface.
 */
@Component
@RequiredArgsConstructor
public class NotificationMessageFormatterImpl implements NotificationMessageFormatter {
    private final NotificationCache notificationCache;

    @Override
    public String formatMessage(NotificationType type, String notificationKey, String articleTitle,
        String objectType, LocalDateTime timestamp) {
        Map<Long, LocalDateTime> actors = notificationCache.getActorsForNotification(notificationKey);
        if (actors == null || actors.isEmpty()) {
            return "You have a new notification.";
        }

        Long[] actorIds = actors.keySet().toArray(new Long[0]);
        java.util.Arrays.sort(actorIds, (a, b) -> actors.get(b).compareTo(actors.get(a)));

        String actorsText = formatActorsText(actorIds);

        String truncatedTitle = truncateTitle(articleTitle);

        String timeText = formatDateTime(actors.get(actorIds[0]));

        return switch (type) {
            case COMMENT_CREATED -> actorsText + " commented on your news \"" + truncatedTitle + "\". " + timeText;
            case ARTICLE_LIKED -> actorsText + " " + (actorIds.length > 1 ? "like" : "likes")
                + " your news \"" + truncatedTitle + "\". " + timeText;
            case COMMENT_REPLIED -> actorsText + " replied to your comment on the "
                + objectType.toLowerCase() + " \"" + truncatedTitle + "\". " + timeText;
            case COMMENT_LIKED -> actorsText + " " + (actorIds.length > 1 ? "like" : "likes")
                + " your comment to the " + objectType.toLowerCase()
                + " \"" + truncatedTitle + "\". " + timeText;
            default -> "You have a new notification.";
        };
    }

    /**
     * Formats actors text based on number of actors.
     *
     * @param actorIds Array of actor IDs
     * @return Formatted actors text
     */
    private String formatActorsText(Long[] actorIds) {
        if (actorIds.length == 1) {
            return notificationCache.getUserName(actorIds[0]);
        } else if (actorIds.length == 2) {
            return notificationCache.getUserName(actorIds[0]) + " and "
                + notificationCache.getUserName(actorIds[1]);
        } else {
            return notificationCache.getUserName(actorIds[0]) + ", "
                + notificationCache.getUserName(actorIds[1]) + " and other users";
        }
    }

    /**
     * Truncates title if it's too long.
     *
     * @param title Title to truncate
     * @return Truncated title
     */
    private String truncateTitle(String title) {
        if (title == null) {
            return "Unknown";
        }
        return (title.length() > 30) ? title.substring(0, 27) + "..." : title;
    }

    @Override
    public String formatDateTime(LocalDateTime dateTime) {
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
