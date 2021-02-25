package org.aircall.pager.notification.model;

import org.aircall.pager.notification.adapter.EmailAdapter;
import org.aircall.pager.pager.model.Alert;

public class EmailTarget extends Target {
    private String address;
    private EmailAdapter emailAdapter;

    public EmailTarget(EmailAdapter emailAdapter, String address) {
        this.emailAdapter = emailAdapter;
        this.address = address;
    }

    @Override
    public void notify(Alert alert) {
        this.emailAdapter.sendAlert(this, alert);
    }

    public String getAddress() {
        return this.address;
    }
}
