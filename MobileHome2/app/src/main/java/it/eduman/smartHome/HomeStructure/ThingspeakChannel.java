package it.eduman.smartHome.HomeStructure;


public class ThingspeakChannel {
    private String device;
    private String measureType;
    private String feed;

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getMeasureType() {
        return measureType;
    }

    public void setMeasureType(String measureType) {
        this.measureType = measureType;
    }

    public String getFeed() {
        return feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }
}
