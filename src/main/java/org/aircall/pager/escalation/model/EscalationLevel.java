package org.aircall.pager.escalation.model;

import org.aircall.pager.notification.model.Target;

import java.util.Set;

public class EscalationLevel {
    private final Set<Target> targets;
    private final boolean isLastLevel;

    public EscalationLevel(Set<Target> targets, boolean lastLevel) {
        this.targets = targets;
        this.isLastLevel = lastLevel;
    }

    public Set<Target> getTargets() {
        return targets;
    }

    public boolean isLastLevel() {
        return isLastLevel;
    }
}
