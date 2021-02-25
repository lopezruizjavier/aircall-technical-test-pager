package org.aircall.pager.pager.service.impl;

import org.aircall.pager.escalation.adapter.EscalationPolicyAdapter;
import org.aircall.pager.escalation.model.EscalationLevel;
import org.aircall.pager.pager.db.AlertServiceDB;
import org.aircall.pager.pager.db.MonitoredServiceDB;
import org.aircall.pager.pager.exception.NotFoundException;
import org.aircall.pager.pager.model.Alert;
import org.aircall.pager.pager.model.AlertStatus;
import org.aircall.pager.pager.model.MonitoredService;
import org.aircall.pager.pager.model.ServiceStatus;
import org.aircall.pager.pager.service.PagerService;
import org.aircall.pager.timer.TimerAdapter;
import org.aircall.pager.util.Transactional;

public class PagerServiceImpl implements PagerService {

    public static final int ACKNOWLEDGE_TIMEOUT = 15;

    private AlertServiceDB alertServiceDB;
    private MonitoredServiceDB monitoredServiceDB;
    private EscalationPolicyAdapter escalationPolicyAdapter;
    private TimerAdapter timerAdapter;

    public PagerServiceImpl(AlertServiceDB alertServiceDB, MonitoredServiceDB monitoredServiceDB, EscalationPolicyAdapter escalationPolicyAdapter, TimerAdapter timerAdapter) {
        this.alertServiceDB = alertServiceDB;
        this.monitoredServiceDB = monitoredServiceDB;
        this.escalationPolicyAdapter = escalationPolicyAdapter;
        this.timerAdapter = timerAdapter;
    }

    @Transactional
    @Override
    public void receiveAlert(Alert alert) {
        MonitoredService monitoredService = monitoredServiceDB.lockByIdentifier(alert.getMonitoredService().getIdentifier())
                .orElseThrow(() -> new NotFoundException(MonitoredService.class, alert.getMonitoredService().getIdentifier()));

        if (monitoredService.getServiceStatus().equals(ServiceStatus.HEALTHY)) {
            monitoredService.setServiceStatus(ServiceStatus.UNHEALTHY);

            monitoredServiceDB.save(monitoredService);
            alertServiceDB.save(alert);

            executeLevel(alert);
        }
    }

    @Transactional
    @Override
    public void receiveAcknowledgementTimeout(Alert alert) {
        MonitoredService monitoredService = monitoredServiceDB.lockByIdentifier(alert.getMonitoredService().getIdentifier())
                .orElseThrow(() -> new NotFoundException(MonitoredService.class, alert.getMonitoredService().getIdentifier()));

        if (monitoredService.getServiceStatus().equals(ServiceStatus.UNHEALTHY)
                && !alert.getAlertStatus().equals(AlertStatus.ACKNOWLEDGEDMENT)) {

            alert.increaseCurrentLevel();
            alertServiceDB.save(alert);

            executeLevel(alert);
        }
    }

    @Transactional
    @Override
    public void receiveAlertAcknowledgement(Alert alert) {
        MonitoredService monitoredService = monitoredServiceDB.lockByIdentifier(alert.getMonitoredService().getIdentifier())
                .orElseThrow(() -> new NotFoundException(MonitoredService.class, alert.getMonitoredService().getIdentifier()));

        if (monitoredService.getServiceStatus().equals(ServiceStatus.UNHEALTHY)
                && alert.getAlertStatus().equals(AlertStatus.ACKNOWLEDGEDMENT)) {

            alertServiceDB.save(alert);
        }
    }

    @Transactional
    @Override
    public void receiveHealthyStatus(String monitoredServiceID) {
        MonitoredService monitoredService = monitoredServiceDB.lockByIdentifier(monitoredServiceID)
                .orElseThrow(() -> new NotFoundException(MonitoredService.class, monitoredServiceID));

        if (monitoredService.getServiceStatus().equals(ServiceStatus.UNHEALTHY)) {
            monitoredService.setServiceStatus(ServiceStatus.HEALTHY);
            monitoredServiceDB.save(monitoredService);
        }
    }

    private void executeLevel(Alert alert) {
        EscalationLevel level = escalationPolicyAdapter.getLevel(alert.getMonitoredService(), alert.getCurrentLevel())
                .orElseThrow(() -> new NotFoundException(EscalationLevel.class, String.valueOf(alert.getCurrentLevel())));

        level.getTargets().forEach(target -> target.notify(alert));

        if (!level.isLastLevel()) {
            timerAdapter.setTimeout(alert.getMonitoredService(), ACKNOWLEDGE_TIMEOUT);
        }
    }
}
