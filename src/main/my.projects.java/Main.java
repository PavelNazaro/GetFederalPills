package main.my.projects.java;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    public static final String CONFIG_PROPERTIES = "config.properties";
    public static final String DATA_JSON = "data.json";
    public static final String DATA_JSON_ABSOLUTE_FILE_PATH =
            "C:\\Users\\pavel\\YDmy\\Projects\\GetFederalPills\\" + DATA_JSON;

    public static void main(String[] args) {
        File dataJsonFile = findJsonFile();
        if (!dataJsonFile.exists()) {
            return;
        }

        try (InputStream configStream = Main.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES)) {
            Properties properties = new Properties();
            properties.load(configStream);
            if (properties.isEmpty()) {
                newLoggerValue("Main Error 1: Properties is EMPTY!", Level.WARNING);
                return;
            }

            String botToken = properties.getProperty("botToken");
            String botUsername = properties.getProperty("botUsername");
            String pathToJsonFromWeb = properties.getProperty("pathToJsonFromWeb");

            if (botToken.isEmpty() || botUsername.isEmpty() || pathToJsonFromWeb.isEmpty()) {
                newLoggerValue("Main Error 2: Any of properties is EMPTY!", Level.WARNING);
                return;
            }

            MyBot bot = new MyBot(botToken, botUsername, pathToJsonFromWeb, dataJsonFile, logger);

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            newLoggerValue("Bot started successfully!", Level.INFO);

            new MyTimer(bot);
        } catch (IOException | TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private static File findJsonFile() {
        try {
            File parentFile =
                    new File(MyBot.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                            .getParentFile();
            File file = new File(parentFile + File.separator + DATA_JSON);
            if (file.exists()) {
                newLoggerValue(file.getPath(), Level.INFO);
            } else {
                newLoggerValue("File not exists: " + file, Level.INFO);
                file = new File(DATA_JSON_ABSOLUTE_FILE_PATH);

                if (!file.exists()) {
                    if (file.createNewFile()) {
                        newLoggerValue("New file " + file.getName() + " created", Level.INFO);
                    } else {
                        newLoggerValue("Main Error 3: Can not create file "
                                + file.getName() + " !", Level.WARNING);
                    }
                }
            }
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static void newLoggerValue(String x, Level level) {
        System.out.println(x);
//        logger.log(level, x);
    }
}
