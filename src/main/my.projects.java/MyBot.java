package main.my.projects.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static main.my.projects.java.Main.newLoggerValue;

public class MyBot extends TelegramLongPollingBot {
    public static final String LAST_DAY = "lastDay";
    public static final String IDS = "ids";
    public static final String START = "/start";
    public static final String REFRESH = "/refresh";
    public static final String GET_ALL = "/get_all";
    public static final String STOP = "/stop";
    public static final String BOT_ALREADY_STARTED = "Bot already started!";
    public static final String BOT_STARTED = "Bot started!";
    public static final String BOT_STOPPED = "Bot stopped!";
    public static final String BOT_ALREADY_STOPPED = "Bot already stopped!";
    public static final String INFO_MESSAGE = "Send command /start or /refresh to get data; /get_all to get current data; /stop to stop bot";

    private final String botToken;
    private final String botUsername;
    private final String pathToJsonFromWeb;
    private long chatId;
    private Set<Long> idsSet;
    private String lastDay;
    private final File dataJsonFile;

    public MyBot(String botToken, String botUsername, String pathToJsonFromWeb, File dataJsonFile, Logger logger) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.pathToJsonFromWeb = pathToJsonFromWeb;
        this.dataJsonFile = dataJsonFile;

        getDataFromJsonFile();

        JsonParser jsonParser = new JsonParser();
        try {
            jsonParser.proceedJsonParserHourlyTimer(this);
        } catch (Exception e) {
            newLoggerValue("MyBot Error 1: " + e.getMessage(), Level.WARNING);
        }
    }

    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            if (!message.hasText()) {
                newLoggerValue("MyBot Error 2: Message is EMPTY!", Level.WARNING);
                return;
            }

            chatId = message.getChatId();
            String text = message.getText();

            if (text.isEmpty()) {
                newLoggerValue("MyBot Error 3: Message is EMPTY!", Level.WARNING);
                return;
            }

            getDataFromJsonFile();

            JsonParser jsonParser = new JsonParser();
            switch (text) {
                case START: {
                    if (idsSet.contains(chatId)) {
                        sendMessageToBot(BOT_ALREADY_STARTED);
                    } else {
                        sendMessageToBot(BOT_STARTED);
                        idsSet.add(chatId);
                        writeDataToJsonFile();
                    }
                    proceedJsonParser(jsonParser, false);
                    return;
                }
                case REFRESH: {
                    if (!idsSet.contains(chatId)) {
                        sendMessageToBot(BOT_STARTED);
                        idsSet.add(chatId);
                        writeDataToJsonFile();
                    }
                    proceedJsonParser(jsonParser, false);
                    return;
                }
                case GET_ALL: {
                    proceedJsonParser(jsonParser, true);
                    return;
                }
                case STOP: {
                    if (idsSet.contains(chatId)) {
                        sendMessageToBot(BOT_STOPPED);
                    } else {
                        sendMessageToBot(BOT_ALREADY_STOPPED);
                    }
                    idsSet.remove(chatId);
                    writeDataToJsonFile();
                    return;
                }
                default:
                    sendMessageToBot(INFO_MESSAGE);
            }
        }
    }

    private void proceedJsonParser(JsonParser jsonParser, boolean getAllData) {
        try {
            jsonParser.proceedJsonParser(this, getAllData);
        } catch (Exception e) {
            newLoggerValue("MyBot Error 4: " + e.getMessage(), Level.WARNING);
        }
    }

    protected void getDataFromJsonFile() {
        System.out.println(dataJsonFile);
        try {
            Scanner myJson = new Scanner(dataJsonFile);
            String str = myJson.useDelimiter("\\Z").next();
            JSONObject dataJson = new JSONObject(str);
            System.out.println(dataJson);
            myJson.close();

            ObjectMapper objectMapper = new ObjectMapper();
            Data data = objectMapper.readValue(dataJson.toString(), Data.class);
            System.out.println(data.getLastDay());
            System.out.println(Arrays.toString(Arrays.stream(data.getIds()).toArray()));

            Set<Long> idsSet = new HashSet<>();
            ArrayList<String> strings = new ArrayList<>(List.of(data.getIds()));
            strings.forEach(s -> idsSet.add(Long.valueOf(s)));

            this.idsSet = idsSet;
            this.lastDay = data.getLastDay();

        } catch (IOException e) {
            newLoggerValue("MyBot Error 5: " + e.getMessage(), Level.WARNING);
        }
    }

    protected void writeDataToJsonFile() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(LAST_DAY, LocalDate.now());
        jsonObject.put(IDS, idsSet);

        try (PrintWriter out = new PrintWriter(dataJsonFile)) {
            out.write(jsonObject.toString());
        } catch (Exception e) {
            newLoggerValue("MyBot Error 6: " + e.getMessage(), Level.WARNING);
        }
    }

    protected void sendMessageToBot(String response) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(response);
        sendMessage.setChatId(chatId);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            newLoggerValue("MyBot Error 7: " + e.getMessage(), Level.WARNING);
        }
    }

    public String getBotUsername() {
        return botUsername;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getPathToJsonFromWeb() {
        return pathToJsonFromWeb;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getLastDay() {
        return lastDay;
    }

    public Set<Long> getIdsSet() {
        return idsSet;
    }
}
