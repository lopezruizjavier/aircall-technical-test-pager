package org.aircall.pager.notification.model;

import org.aircall.pager.notification.adapter.SMSAdapter;
import org.aircall.pager.pager.model.Alert;

public class SMSTarget extends Target {
    private final String phoneNumber;
    private final SMSAdapter smsAdapter;

    public SMSTarget(SMSAdapter smsAdapter, String phoneNumber) {
        this.smsAdapter = smsAdapter;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void notify(Alert alert) {
        this.smsAdapter.sendAlert(this, alert);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
