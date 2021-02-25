package org.aircall.pager.notification.adapter;

import org.aircall.pager.notification.model.SMSTarget;
import org.aircall.pager.pager.model.Alert;

public interface SMSAdapter {
    void sendAlert(SMSTarget sms, Alert alert);
}
