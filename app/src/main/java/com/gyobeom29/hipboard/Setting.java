package com.gyobeom29.hipboard;

public class Setting {

    private boolean pushOn;

    public Setting() {}

    public Setting(boolean pushOn) {
        this.pushOn = pushOn;
    }

    public boolean isPushOn() {
        return pushOn;
    }

    public void setPushOn(boolean pushOn) {
        this.pushOn = pushOn;
    }

    @Override
    public String toString() {
        return "Setting{" +
                "pushOn=" + pushOn +
                '}';
    }
}
