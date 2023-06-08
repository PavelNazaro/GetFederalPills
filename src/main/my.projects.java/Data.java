package main.my.projects.java;

public class Data {
    private String lastDay;
    private String[] ids;

    public Data() {
    }

    public Data(String lastDay, String[] ids) {
        this.lastDay = lastDay;
        this.ids = ids;
    }

    public String getLastDay() {
        return lastDay;
    }

    public void setLastDay(String lastDay) {
        this.lastDay = lastDay;
    }

    public String[] getIds() {
        return ids;
    }

    public void setIds(String[] ids) {
        this.ids = ids;
    }
}
