package org.aircall.pager.notification.model;

import org.aircall.pager.pager.model.Alert;

public abstract class Target {
    public abstract void notify(Alert alert);
}
