package jiho.whereru.org.ignitednewapplication.Util;

public class AlarmItem {
    private String date,name,title,latitude,longtitude,time,content;

    public AlarmItem() {
    }

    public AlarmItem(String date, String name, String title, String latitude, String longtitude, String time, String content) {
        this.date = date;
        this.name = name;
        this.title = title;
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.time = time;
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
