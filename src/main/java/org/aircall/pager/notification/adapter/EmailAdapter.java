package org.aircall.pager.notification.adapter;

import org.aircall.pager.notification.model.EmailTarget;
import org.aircall.pager.pager.model.Alert;

public interface EmailAdapter {
    void sendAlert(EmailTarget email, Alert alert);
}