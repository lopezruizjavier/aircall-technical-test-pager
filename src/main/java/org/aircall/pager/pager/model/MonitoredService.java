package org.aircall.pager.pager.model;

public class MonitoredService {
    private final String identifier;

    private ServiceStatus serviceStatus = ServiceStatus.HEALTHY;

    public MonitoredService(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public ServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(ServiceStatus serviceStatus) {
        this.serviceStatus = serviceStatus;
    }
}
