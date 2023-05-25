package main.my.projects.java;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        Properties properties = new Properties();
        File propertiesFile = new File("src/main/resources/config.properties");
        if (!propertiesFile.exists()){
            System.out.println("File NOT found!");
            return;
        }

        try(FileInputStream fileInputStream = new FileInputStream(propertiesFile)) {
            properties.load(fileInputStream);

            if (properties.isEmpty()){
                System.out.println("Properties is EMPTY!");
                return;
            }

            String botToken = properties.getProperty("botToken");
            String botUsername = properties.getProperty("botUsername");

            if (botToken.isEmpty() || botUsername.isEmpty()){
                System.out.println("Any of properties is EMPTY!");
                return;
            }

            MyBot bot = new MyBot(botToken, botUsername);

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            System.out.println("Bot started successfully!");

        } catch (IOException | TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
