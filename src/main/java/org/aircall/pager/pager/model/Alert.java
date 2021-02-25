package org.aircall.pager.pager.model;

public class Alert {
    private final String message;
    private MonitoredService monitoredService;
    private AlertStatus alertStatus = AlertStatus.UNRESOLVED;
    private int currentLevel = 1;

    public Alert(MonitoredService monitoredService, String message) {
        this.monitoredService = monitoredService;
        this.message = message;
    }

    public MonitoredService getMonitoredService() {
        return monitoredService;
    }

    public void setMonitoredService(MonitoredService monitoredService) {
        this.monitoredService = monitoredService;
    }

    public String getMessage() {
        return message;
    }

    public AlertStatus getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(AlertStatus alertStatus) {
        this.alertStatus = alertStatus;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int increaseCurrentLevel() {
        return ++currentLevel;
    }
}
