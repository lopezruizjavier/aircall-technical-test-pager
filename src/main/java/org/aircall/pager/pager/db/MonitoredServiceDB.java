package org.aircall.pager.pager.db;

import org.aircall.pager.pager.model.MonitoredService;

import java.util.Optional;

public interface MonitoredServiceDB {
    Optional<MonitoredService> lockByIdentifier(String identifier);

    void save(MonitoredService monitoredService);
}
