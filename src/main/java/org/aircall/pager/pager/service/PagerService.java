package org.aircall.pager.pager.service;

import org.aircall.pager.pager.model.Alert;

public interface PagerService {
    void receiveAlert(Alert alert);

    void receiveAcknowledgementTimeout(Alert alert);

    void receiveAlertAcknowledgement(Alert alert);

    void receiveHealthyStatus(String monitoredServiceID);
}
