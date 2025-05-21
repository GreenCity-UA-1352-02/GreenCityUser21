package greencity.service;

import greencity.dto.notification.NotificationEvent;

/**
 * Interface for notification service. Defines contract for processing
 * notification events.
 */
public interface NotificationService {
    /**
     * Processes a notification event.
     *
     * @param event The notification event
     */
    void processNotification(NotificationEvent event);
}
