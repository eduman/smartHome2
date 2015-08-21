package it.eduman.smartHome.IoTDevice;

public class Function {

    public int pin;
    public String type;
    public String configuredAs;
    public String status;
    public String unit;
    public String rest;
    public String ws;

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfiguredAs() {
        return configuredAs;
    }

    public void setConfiguredAs(String configuredAs) {
        this.configuredAs = configuredAs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getRest() {
        return rest;
    }

    public void setRest(String rest) {
        this.rest = rest;
    }

    public String getWs() {
        return ws;
    }

    public void setWs(String ws) {
        this.ws = ws;
    }
}
