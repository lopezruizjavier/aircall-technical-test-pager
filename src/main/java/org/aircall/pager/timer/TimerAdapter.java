package org.aircall.pager.timer;

import org.aircall.pager.pager.model.MonitoredService;

public interface TimerAdapter {
    void setTimeout(MonitoredService monitoredService, int minutes);
}
