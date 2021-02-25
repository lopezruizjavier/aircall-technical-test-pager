import com.github.javafaker.Faker;
import org.aircall.pager.escalation.adapter.EscalationPolicyAdapter;
import org.aircall.pager.escalation.model.EscalationLevel;
import org.aircall.pager.notification.adapter.EmailAdapter;
import org.aircall.pager.notification.adapter.SMSAdapter;
import org.aircall.pager.notification.model.EmailTarget;
import org.aircall.pager.notification.model.SMSTarget;
import org.aircall.pager.notification.model.Target;
import org.aircall.pager.pager.db.AlertServiceDB;
import org.aircall.pager.pager.db.MonitoredServiceDB;
import org.aircall.pager.pager.model.Alert;
import org.aircall.pager.pager.model.AlertStatus;
import org.aircall.pager.pager.model.MonitoredService;
import org.aircall.pager.pager.model.ServiceStatus;
import org.aircall.pager.pager.service.impl.PagerServiceImpl;
import org.aircall.pager.timer.TimerAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

public class PagerServiceImplTest {
    // Mocks
    private AlertServiceDB alertServiceDB;
    private MonitoredServiceDB monitoredServiceDB;
    private EscalationPolicyAdapter escalationPolicyAdapter;
    private EmailAdapter emailAdapter;
    private SMSAdapter smsAdapter;
    private TimerAdapter timerAdapter;

    // Class to test
    private PagerServiceImpl pagerService;

    @BeforeEach
    public void setup() {
        alertServiceDB = mock(AlertServiceDB.class);
        monitoredServiceDB = mock(MonitoredServiceDB.class);

        escalationPolicyAdapter = mock(EscalationPolicyAdapter.class);
        emailAdapter = mock(EmailAdapter.class);
        smsAdapter = mock(SMSAdapter.class);
        timerAdapter = mock(TimerAdapter.class);

        pagerService = new PagerServiceImpl(alertServiceDB, monitoredServiceDB, escalationPolicyAdapter, timerAdapter);
    }

    /**
     * Given a Monitored Service in a Healthy State,
     * when the Pager receives an Alert related to this Monitored Service,
     * then the Monitored Service becomes Unhealthy,
     * the Pager notifies all targets of the first level of the escalation policy,
     * and sets a 15-minutes acknowledgement delay
     */
    @Test
    public void testReceiveAlert() {
        Alert alert = getFakeAlert();
        EscalationLevel escalationLevel = new EscalationLevel(getFakeTargets(), false);

        when(monitoredServiceDB.lockByIdentifier(alert.getMonitoredService().getIdentifier())).thenReturn(Optional.of(alert.getMonitoredService()));
        when(escalationPolicyAdapter.getLevel(alert.getMonitoredService(), 1)).thenReturn(Optional.of(escalationLevel));

        pagerService.receiveAlert(alert);

        verify(monitoredServiceDB).save(
                argThat(monitoredServiceDB -> monitoredServiceDB.getIdentifier().equals(alert.getMonitoredService().getIdentifier())
                        && monitoredServiceDB.getServiceStatus().equals(ServiceStatus.UNHEALTHY))
        );

        verify(alertServiceDB).save(
                argThat(alertDB -> alertDB.getMessage().equals(alert.getMessage()) && alert.getCurrentLevel() == 1)
        );

        escalationLevel.getTargets().stream().filter(
                target -> target instanceof EmailTarget).forEach(
                (target) -> verify(emailAdapter).sendAlert((EmailTarget) target, alert));

        escalationLevel.getTargets().stream().filter(
                target -> target instanceof SMSTarget).forEach(
                (target) -> verify(smsAdapter).sendAlert((SMSTarget) target, alert));

        verify(timerAdapter).setTimeout(alert.getMonitoredService(), PagerServiceImpl.ACKNOWLEDGE_TIMEOUT);
    }

    /**
     * Given a Monitored Service in an Unhealthy State,
     * the corresponding Alert is not Acknowledged
     * and the last level has not been notified,
     * when the Pager receives the Acknowledgement Timeout,
     * then the Pager notifies all targets of the next level of the escalation policy
     * and sets a 15-minutes acknowledgement delay.
     */
    @Test
    public void testReceiveAcknowledgementTimeout() {
        Alert alert = getFakeAlert();
        EscalationLevel escalationLevel = new EscalationLevel(getFakeTargets(), false);

        alert.getMonitoredService().setServiceStatus(ServiceStatus.UNHEALTHY);
        alert.setAlertStatus(AlertStatus.UNRESOLVED);

        when(monitoredServiceDB.lockByIdentifier(alert.getMonitoredService().getIdentifier())).thenReturn(Optional.of(alert.getMonitoredService()));
        when(escalationPolicyAdapter.getLevel(alert.getMonitoredService(), 2)).thenReturn(Optional.of(escalationLevel));

        pagerService.receiveAcknowledgementTimeout(alert);

        verify(alertServiceDB).save(
                argThat(alertDB -> alertDB.getMonitoredService().getIdentifier().equals(alert.getMonitoredService().getIdentifier())
                        && alert.getCurrentLevel() == 2)
        );

        escalationLevel.getTargets().stream().filter(
                target -> target instanceof EmailTarget).forEach(
                (target) -> verify(emailAdapter).sendAlert((EmailTarget) target, alert));

        escalationLevel.getTargets().stream().filter(
                target -> target instanceof SMSTarget).forEach(
                (target) -> verify(smsAdapter).sendAlert((SMSTarget) target, alert));

        verify(timerAdapter).setTimeout(alert.getMonitoredService(), PagerServiceImpl.ACKNOWLEDGE_TIMEOUT);
    }

    /**
     * Given a Monitored Service in an Unhealthy State
     * when the Pager receives the Acknowledgement
     * and later receives the Acknowledgement Timeout,
     * then the Pager doesn't notify any Target
     * and doesn't set an acknowledgement delay.
     */
    @Test
    public void testReceiveAlertAcknowledgement() {
        Alert alert = getFakeAlert();
        alert.getMonitoredService().setServiceStatus(ServiceStatus.UNHEALTHY);
        alert.setAlertStatus(AlertStatus.ACKNOWLEDGEDMENT);

        when(monitoredServiceDB.lockByIdentifier(alert.getMonitoredService().getIdentifier())).thenReturn(Optional.of(alert.getMonitoredService()));

        pagerService.receiveAlertAcknowledgement(alert);

        verify(alertServiceDB).save(
                argThat(alertDB -> alertDB.getMonitoredService().getIdentifier().equals(alert.getMonitoredService().getIdentifier())
                        && alert.getAlertStatus().equals(AlertStatus.ACKNOWLEDGEDMENT))
        );

        verify(emailAdapter, never()).sendAlert(any(EmailTarget.class), any(Alert.class));
        verify(smsAdapter, never()).sendAlert(any(SMSTarget.class), any(Alert.class));
        verify(timerAdapter, never()).setTimeout(any(MonitoredService.class), any(Integer.TYPE));
    }

    /**
     * Given a Monitored Service in an Unhealthy State,
     * when the Pager receives an Alert related to this Monitored Service,
     * then the Pager doesn’t notify any Target
     * and doesn’t set an acknowledgement delay
     */
    @Test
    public void testReceiveAlertUnhealthyState() {
        Alert alert = getFakeAlert();
        EscalationLevel escalationLevel = new EscalationLevel(getFakeTargets(), false);

        alert.getMonitoredService().setServiceStatus(ServiceStatus.UNHEALTHY);
        alert.setAlertStatus(AlertStatus.UNRESOLVED);

        when(monitoredServiceDB.lockByIdentifier(alert.getMonitoredService().getIdentifier())).thenReturn(Optional.of(alert.getMonitoredService()));
        when(escalationPolicyAdapter.getLevel(alert.getMonitoredService(), 1)).thenReturn(Optional.of(escalationLevel));

        pagerService.receiveAlert(alert);

        verify(emailAdapter, never()).sendAlert(any(EmailTarget.class), any(Alert.class));
        verify(smsAdapter, never()).sendAlert(any(SMSTarget.class), any(Alert.class));
        verify(timerAdapter, never()).setTimeout(any(MonitoredService.class), any(Integer.TYPE));
    }

    /**
     * Given a Monitored Service in an Unhealthy State,
     * when the Pager receives a Healthy event related to this Monitored Service
     * and later receives the Acknowledgement Timeout,
     * then the Monitored Service becomes Healthy,
     * the Pager doesn’t notify any Target
     * and doesn’t set an acknowledgement delay
     */
    @Test
    public void testReceiveAlertHealthyState() {
        MonitoredService monitoredService = getFakeMonitoredService();
        monitoredService.setServiceStatus(ServiceStatus.UNHEALTHY);

        when(monitoredServiceDB.lockByIdentifier(monitoredService.getIdentifier())).thenReturn(Optional.of(monitoredService));

        pagerService.receiveHealthyStatus(monitoredService.getIdentifier());

        verify(monitoredServiceDB).save(
                argThat(monitoredServiceDB -> monitoredServiceDB.getIdentifier().equals(monitoredService.getIdentifier())
                        && monitoredService.getServiceStatus().equals(ServiceStatus.HEALTHY))
        );

        verify(emailAdapter, never()).sendAlert(any(EmailTarget.class), any(Alert.class));
        verify(smsAdapter, never()).sendAlert(any(SMSTarget.class), any(Alert.class));
        verify(timerAdapter, never()).setTimeout(any(MonitoredService.class), any(Integer.TYPE));
    }

    private MonitoredService getFakeMonitoredService() {
        return new MonitoredService(Faker.instance().commerce().department());
    }

    private Alert getFakeAlert() {
        return new Alert(getFakeMonitoredService(), Faker.instance().chuckNorris().fact());
    }

    private Set<Target> getFakeTargets() {
        Set<Target> fakeTargets = new HashSet<>();
        fakeTargets.add(new SMSTarget(smsAdapter, Faker.instance().phoneNumber().cellPhone()));
        fakeTargets.add(new EmailTarget(emailAdapter, Faker.instance().internet().emailAddress()));

        return fakeTargets;
    }
}
