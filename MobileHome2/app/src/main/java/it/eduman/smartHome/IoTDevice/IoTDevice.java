package it.eduman.smartHome.IoTDevice;

import java.util.ArrayList;
import java.util.List;

public class IoTDevice {
    public boolean configured = false;
    public String ip = "";
    public String subnet = "";
    public String gateway = "";
    public String port = "";
    public String description = "";
    public String type = "";
    public boolean isError = false;
    public List<Function> functions = new ArrayList<>();

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getPort() { return port; }

    public void setPort(String port) { this.port = port; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isError() {
        return isError;
    }

    public void setIsError(boolean isError) {
        this.isError = isError;
    }

    public List<Function> getFunctions() {
        return functions;
    }

    public void setFunctions(List<Function> functions) {
        this.functions = functions;
    }
}
