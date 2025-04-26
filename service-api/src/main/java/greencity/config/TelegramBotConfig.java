package greencity.config;

import greencity.service.TelegramBotService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@Getter
public class TelegramBotConfig {
    @Value("${telegram.bot.username:greencity_notification_bot}")
    private String botUsername;

    @Value("${telegram.bot.token:7766119898:AAEMO-Pnnx8U67m-7mxtpzL5cbi5nr63B00}")
    private String botToken;

    /**
     * Registers the Telegram bot with the Telegram API.
     *
     * @param telegramBotService The Telegram bot service implementation
     * @return TelegramBotsApi instance
     * @throws TelegramApiException if there's an error registering the bot
     */
    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBotService telegramBotService) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(telegramBotService);
        return botsApi;
    }
}
