package com.tecknobit.brownie.helpers;

import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.services.HostsService;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.prefs.Preferences;

import static com.tecknobit.brownie.helpers.LocalEventsHandler.LocalEvent.HOST_REBOOTED;
import static com.tecknobit.brownie.helpers.LocalEventsHandler.LocalEvent.HOST_STOPPED;
import static com.tecknobit.browniecore.ConstantsKt.HOST_IDENTIFIER_KEY;
import static com.tecknobit.browniecore.ConstantsKt.SESSION_IDENTIFIER_KEY;

@Component
public class LocalEventsHandler {

    public enum LocalEvent {

        HOST_REBOOTED("host_rebooted"),

        HOST_STOPPED("host_stopped");

        private final String eventKey;

        LocalEvent(String eventKey) {
            this.eventKey = eventKey;
        }

    }

    private static final LocalEventsHandler LOCAL_EVENTS_HANDLER = new LocalEventsHandler();

    private final Preferences preferences = Preferences.userRoot().node("tecknobit/brownie/backend");

    @Autowired
    private HostsService hostsService;

    @EventListener(ApplicationReadyEvent.class)
    public void executeLocalEventsScan() throws Exception {
        if (hostRebootedEventIsRegistered()) {
            System.out.println("STO RIAVVIANDO I SERVIZI");
            String sessionId = preferences.get(SESSION_IDENTIFIER_KEY, "");
            System.out.println("Session id:" + sessionId);
            String hostId = preferences.get(HOST_IDENTIFIER_KEY, "");
            System.out.println("Host id:" + hostId);
            BrownieHost host = hostsService.getBrownieHost(sessionId, hostId);
            if (host == null)
                throw new IllegalAccessException("The host trying to restart its services is null");
            hostsService.restartHost(host);
            System.out.println("Host " + host.getId());
            unregisterHostRebootedEvent();
        }
    }

    @Wrapper
    public void registerHostRebootedEvent(String sessionId, String hostId) {
        System.out.println("HO REGISTRATO!!!");
        registerHostEvent(HOST_REBOOTED);
        preferences.put(SESSION_IDENTIFIER_KEY, sessionId);
        preferences.put(HOST_IDENTIFIER_KEY, hostId);
    }

    @Wrapper
    public void registerHostStoppedEvent() {
        registerHostEvent(HOST_STOPPED);
    }

    private void registerHostEvent(LocalEvent event) {
        preferences.put(event.eventKey, event.name());
    }

    @Wrapper
    public void unregisterHostRebootedEvent() {
        unregisterLocalEvent(HOST_REBOOTED);
        preferences.remove(SESSION_IDENTIFIER_KEY);
        preferences.remove(HOST_IDENTIFIER_KEY);
    }

    @Wrapper
    public void unregisterHostStoppedEvent() {
        unregisterLocalEvent(HOST_STOPPED);
    }

    private void unregisterLocalEvent(LocalEvent event) {
        preferences.remove(event.eventKey);
    }

    @Wrapper
    public boolean hostRebootedEventIsRegistered() {
        return hasLocalEventRegistered(HOST_REBOOTED);
    }

    @Wrapper
    public boolean hostStoppedEventIsRegistered() {
        return hasLocalEventRegistered(HOST_STOPPED);
    }

    private boolean hasLocalEventRegistered(LocalEvent event) {
        return preferences.get(event.eventKey, null) != null;
    }

    public static LocalEventsHandler getInstance() {
        return LOCAL_EVENTS_HANDLER;
    }

}
