package greencity.service;

import greencity.config.TelegramBotConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {
    private final TelegramBotConfig telegramBotConfig;

    // Map to store user ID to chat ID mappings
    private final Map<Long, Long> userChatMap = new ConcurrentHashMap<>();

    @Autowired
    public TelegramBotService(TelegramBotConfig telegramBotConfig) {
        this.telegramBotConfig = telegramBotConfig;
    }

    @Override
    public String getBotUsername() {
        return telegramBotConfig.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return telegramBotConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.startsWith("/start")) {
                handleStartCommand(chatId, messageText);
            }
        }
    }

    /**
     * Handles the /start command with optional user ID parameter. Format: /start
     * userId
     *
     * @param chatId  Telegram chat ID
     * @param message The full message text
     */
    private void handleStartCommand(long chatId, String message) {
        String[] parts = message.split(" ");
        if (parts.length > 1) {
            try {
                Long userId = Long.parseLong(parts[1]);
                userChatMap.put(userId, chatId);

                sendMessage(chatId,
                    "Welcome! You have successfully subscribed to notifications for the user with ID: " + userId);
                log.info("User {} subscribed to notifications via chat {}", userId, chatId);
            } catch (NumberFormatException e) {
                sendMessage(chatId, "Invalid user ID format. Use: /start userId");
            }
        } else {
            sendMessage(chatId, "To subscribe to notifications, please provide a user ID: /start userId");
        }
    }

    /**
     * Sends a notification to a user.
     *
     * @param userId  User ID
     * @param message Message text
     * @return true if message was sent, false otherwise
     */
    public boolean sendNotificationToUser(Long userId, String message) {
        Long chatId = userChatMap.get(userId);
        if (chatId != null) {
            return sendMessage(chatId, message);
        } else {
            log.warn("No Telegram chat found for user ID: {}", userId);
            return false;
        }
    }

    /**
     * Sends a message to a specific chat.
     *
     * @param chatId Telegram chat ID
     * @param text   Message text
     * @return true if message was sent, false otherwise
     */
    private boolean sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
            return true;
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram message to chat {}: {}", chatId, e.getMessage());
            return false;
        }
    }
}
