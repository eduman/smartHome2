package it.eduman.smartHome.HomeStructure;

import java.util.List;

public class TelegramBot {
    private String telegramToken;
    private List<Integer> allowedUserID;

    public String getTelegramToken() {
        return telegramToken;
    }

    public void setTelegramToken(String telegramToken) {
        this.telegramToken = telegramToken;
    }

    public List<Integer> getAllowedUserID() {
        return allowedUserID;
    }

    public void setAllowedUserID(List<Integer> allowedUserID) {
        this.allowedUserID = allowedUserID;
    }
}
