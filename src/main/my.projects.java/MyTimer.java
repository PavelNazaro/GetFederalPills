package main.my.projects.java;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import static main.my.projects.java.Main.newLoggerValue;

public class MyTimer {

    public static final int ONE_SECOND_PERIOD = 1000;
    public static final int ONE_MINUTE_PERIOD = 60 * ONE_SECOND_PERIOD;
    public static final int ONE_HOUR_PERIOD = 60 * ONE_MINUTE_PERIOD;

    public MyTimer(MyBot bot) {
        Timer timer = new Timer();

        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);
        startTime.add(Calendar.MINUTE, 1);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Calendar currentTime = Calendar.getInstance();
                System.out.println(new Date());

                if (isBetween9to11(currentTime) || isAt00MinuteEveryHour(currentTime)){
                    System.out.println("Task executed!");
                    bot.getDataFromJsonFile();

                    JsonParser jsonParser = new JsonParser();
                    try {
                        jsonParser.proceedJsonParserHourlyTimer(bot);
                    } catch (Exception e) {
                        String s = "MyTimer Error 1: " + e.getMessage();
                        System.out.println(s);
                        newLoggerValue(s, Level.WARNING);
                    }
                }
            }
        };

        timer.scheduleAtFixedRate(task, startTime.getTime(), ONE_MINUTE_PERIOD);
    }

    private static boolean isBetween9to11(Calendar currentTime) {
        return currentTime.get(Calendar.HOUR) == 9 ||
                currentTime.get(Calendar.HOUR) == 10;
    }

    private static boolean isAt00MinuteEveryHour(Calendar currentTime) {
        return currentTime.get(Calendar.MINUTE) == 0 &&
                currentTime.get(Calendar.SECOND) == 0;
    }
}