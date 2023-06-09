package main.my.projects.java;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import static main.my.projects.java.Main.newLoggerValue;

public class JsonParser {
    public static final String THERE_ARE_NO_UPERIO_NOW_YOU_WILL_GET_A_MESSAGE_WHEN_UPERIO_APPEARS = "There are no Uperio now. You will get a message when Uperio appears!";
    public static final String NEW_DATE_IN_DATA = "New date in data.";
    public static final String FINDING_UPERIO = "Finding Uperio...";
    public static final String NEW_DATE_IN_DATA_FINDING_UPERIO = NEW_DATE_IN_DATA + " " + FINDING_UPERIO;
    public static final int MAX_MESSAGE_LENGTH = 4096;
    public static final String UPERIO = "Юперио, 100 мг (51,4 мг + 48,6 мг) № 56, таблетки покрытые плёночной оболочкой";

    public JsonParser() {
    }

    protected void proceedJsonParser(MyBot bot, boolean getAllData) throws Exception {
        String resultString = processData(bot, getAllData).toString();

        System.out.println("Id: " + bot.getChatId());
        sendMessageToBot(bot, FINDING_UPERIO);

        if (resultString.isEmpty() || resultString.equals(NEW_DATE_IN_DATA)) {
            newLoggerValue("No Uperio", Level.INFO);
            sendMessageToBot(bot, THERE_ARE_NO_UPERIO_NOW_YOU_WILL_GET_A_MESSAGE_WHEN_UPERIO_APPEARS);
        } else {
//            if (resultString.equals(NEW_DATE_IN_DATA)) {
//                logger.log(Level.INFO, "No Uperio");
//                sendMessageToBot(bot, THERE_ARE_NO_UPERIO_NOW_YOU_WILL_GET_A_MESSAGE_WHEN_UPERIO_APPEARS);
//            } else {
            printLongStringToBot(resultString, bot);
//            }
        }
    }

    protected void proceedJsonParserHourlyTimer(MyBot bot) throws Exception {
        String resultString = processData(bot, false).toString();
        Set<Long> idsSet = bot.getIdsSet();

        if (idsSet.isEmpty()) {
            newLoggerValue("JsonParser Error 1", Level.WARNING);
        }

        if (resultString.isEmpty()) {
            return;
        }

        if (resultString.equals(NEW_DATE_IN_DATA)) {
            for (Long id : idsSet) {
                bot.setChatId(id);
                sendMessageToBot(bot, NEW_DATE_IN_DATA_FINDING_UPERIO);
                sendMessageToBot(bot, THERE_ARE_NO_UPERIO_NOW_YOU_WILL_GET_A_MESSAGE_WHEN_UPERIO_APPEARS);
            }
        } else {
            for (Long id : idsSet) {
                bot.setChatId(id);
                printLongStringToBot(resultString, bot);
            }
        }
    }

    private static StringBuilder processData(MyBot bot, boolean getAllData) throws Exception {
        LocalDate lastDayFromJsonFile = LocalDate.parse(bot.getLastDay());

        ArrayList<FederalDTO> federalDTOS = getFederalDTOSFromJson(bot);
        if (federalDTOS.isEmpty()) {
            newLoggerValue("JsonParser Error 2", Level.WARNING);
        }

        LocalDate actualDateFromJsonWeb = LocalDateTime.parse(federalDTOS.get(0).actualDate).toLocalDate();

        if (lastDayFromJsonFile.isBefore(actualDateFromJsonWeb) || getAllData) {
            bot.writeDataToJsonFile();
            return new StringBuilder(NEW_DATE_IN_DATA).append(findUperio(federalDTOS, getAllData));
        }
        return new StringBuilder();
    }

    private static StringBuilder findUperio(ArrayList<FederalDTO> federalDTOS, boolean getAllData) {
        StringBuilder stringBuilder = new StringBuilder();
        for (FederalDTO federalDTO : federalDTOS) {
            if (federalDTO.federalCount > 0 || getAllData) {
                stringBuilder.append(federalDTO).append("\n");
            }
        }

        return stringBuilder;
    }

    private void printLongStringToBot(String str, MyBot bot) {
        int length = str.length();
        int countParts = length / MAX_MESSAGE_LENGTH;
        int j = 0;
        for (int i = 0; i <= countParts; i++) {
            if (i == countParts) {
                sendMessageToBot(bot, str.subSequence(j, length).toString());
            } else {
                sendMessageToBot(bot, str.subSequence(j, j += MAX_MESSAGE_LENGTH).toString());
            }
        }
    }

    private static ArrayList<FederalDTO> getFederalDTOSFromJson(MyBot bot) throws Exception {
        ArrayList<FederalDTO> federalDTOS = new ArrayList<>();
        JSONObject drugObject = getUperioPillFromJson(bot);
        if (drugObject.isEmpty()) {
            newLoggerValue("JsonParser Error 3", Level.WARNING);
        }

        Iterator<String> drugObjectIterator = drugObject.keys();
        while (drugObjectIterator.hasNext()) {
            JSONArray storeArray = drugObject.getJSONArray(drugObjectIterator.next());
            for (Object o : storeArray) {
                JSONObject storeObject = (JSONObject) o;

                String addressSplit = storeObject.getString("storeAddress");
                int position = addressSplit.indexOf("* На момент обращения");

                FederalDTO federal = new FederalDTO(storeObject.getString("storeName"),
                        storeObject.getString("storeDistrict"),
                        addressSplit.substring(0, position),
                        storeObject.getString("drugName"),
                        storeObject.getString("actualDate"),
                        storeObject.getInt("federalCount"));

                federalDTOS.add(federal);
            }
        }
        return federalDTOS;
    }

    private static JSONObject getUperioPillFromJson(MyBot bot) {
        JSONObject resultObject = new JSONObject();
        try {
            String dataFromJson = getDataFromJson(bot);
            if (dataFromJson.isEmpty()) {
                newLoggerValue("JsonParser Error 4", Level.WARNING);
            }

            JSONObject jsonObject = new JSONObject(dataFromJson);
            resultObject = jsonObject.getJSONObject("result").getJSONObject(UPERIO);
        } catch (JSONException e) {
            newLoggerValue("JsonParser Error 5. Connection", Level.WARNING);
        }
        return resultObject;
    }

    private static String getDataFromJson(MyBot bot) {
        String url = bot.getPathToJsonFromWeb();
        String jsonData = "";
        try {
            jsonData = readUrl(url);
        } catch (IOException e) {
            newLoggerValue("JsonParser Error 6. Connection", Level.WARNING);
        }
        return jsonData;
    }

    private static void sendMessageToBot(MyBot bot, String response) {
        bot.sendMessageToBot(response);
    }

    private static String readUrl(String urlString) throws IOException {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);
            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }
}