# Kafka Setup for Notification System

This document describes how to set up and use Kafka for the notification system in the GreenCity project.

## Prerequisites

- Docker and Docker Compose installed on your machine
- Java 21 or higher

## Getting Started

1. Start Kafka and Zookeeper using Docker Compose:

```bash
docker-compose up -d
```

This will start Zookeeper on port 2181 and Kafka on port 9092.

2. Verify that Kafka is running:

```bash
docker-compose ps
```

You should see both zookeeper and kafka containers running.

## How the Notification System Works

The notification system consists of two parts:

1. **Producer** (in GreenCityMVP21):
   - Generates notification events when users comment on or like articles
   - Sends these events to the Kafka topic "notifications.greencity"

2. **Consumer** (in GreenCityUser21):
   - Listens to the Kafka topics "notifications.greencity" and "notifications.user"
   - Processes notification events and sends them to users via Telegram

## Telegram Bot Configuration

You have two options for the Telegram bot:

### Option 1: Use the existing test bot

The default configuration includes a test bot that you can use:

```java
@Value("${telegram.bot.username:greencity_notification_bot}")
private String botUsername;

@Value("${telegram.bot.token:7766119898:AAEMO-Pnnx8U67m-7mxtpzL5cbi5nr63B00}")
private String botToken;
```

### Option 2: Create your own bot

To create your own Telegram bot:

1. Open Telegram and search for "BotFather"
2. Start a chat with BotFather and send the command `/newbot`
3. Follow the instructions to create a new bot
4. BotFather will provide you with a token for your new bot
5. Update the `TelegramBotConfig` file with your bot's username and token:

## Testing the Notification System

To test the notification system:

1. Start both GreenCityMVP21 and GreenCityUser21 applications
2. Register with the Telegram bot by sending `/start YOUR_USER_ID` to the bot
3. Create a news article in the GreenCityMVP21 application
4. Have another user comment on or like your article
5. You should receive a notification via Telegram

**Important**: For notifications to work properly, the author of the article and the user who comments or likes the article must be different users with different IDs. The system is designed to notify the author about actions performed by other users.

## Troubleshooting

If you're not receiving notifications:

1. Check that Kafka is running: `docker-compose ps`
2. Verify that you've registered with the Telegram bot
3. Check the application logs for any errors

## Stopping Kafka

To stop Kafka and Zookeeper:

```bash
docker-compose down
```

This will stop and remove the containers, but preserve the data.
