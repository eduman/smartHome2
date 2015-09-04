package it.eduman.smartHome.user;


public class UserPresence {

    private String user;
    private boolean isPresent = false;

    public UserPresence (String user){
        this.user = user;
    }

    public UserPresence (String user, boolean isPresent){
        this.user = user;
        this.isPresent = isPresent;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public void setIsPresent(boolean isPresent) {
        this.isPresent = isPresent;
    }
}
