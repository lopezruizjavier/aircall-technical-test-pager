package org.aircall.pager.escalation.adapter;

import org.aircall.pager.escalation.model.EscalationLevel;
import org.aircall.pager.pager.model.MonitoredService;

import java.util.Optional;

public interface EscalationPolicyAdapter {
    Optional<EscalationLevel> getLevel(MonitoredService monitoredService, int level);
}
