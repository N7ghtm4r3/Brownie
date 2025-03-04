package com.tecknobit.brownie.helpers;

import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.services.HostsService;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.prefs.Preferences;

import static com.tecknobit.brownie.helpers.LocalEventsHandler.LocalEvent.HOST_SUSPENDED;
import static com.tecknobit.browniecore.ConstantsKt.HOST_IDENTIFIER_KEY;
import static com.tecknobit.browniecore.ConstantsKt.SESSION_IDENTIFIER_KEY;

@Component
public class LocalEventsHandler {

    public enum LocalEvent {

        HOST_SUSPENDED("host_suspended");

        private final String eventKey;

        LocalEvent(String eventKey) {
            this.eventKey = eventKey;
        }

    }

    private static final LocalEventsHandler LOCAL_EVENTS_HANDLER = new LocalEventsHandler();

    private final Preferences preferences = Preferences.systemRoot().node("tecknobit/brownie/local_events");

    @Autowired
    private HostsService hostsService;

    @EventListener(ApplicationReadyEvent.class)
    public void executeLocalEventsScan() throws Exception {
        if (hostSuspendedEventIsRegistered()) {
            String sessionId = preferences.get(SESSION_IDENTIFIER_KEY, "");
            String hostId = preferences.get(HOST_IDENTIFIER_KEY, "");
            BrownieHost host = hostsService.getBrownieHost(sessionId, hostId);
            if (host == null)
                throw new IllegalAccessException("The host trying to restart its services is null");
            hostsService.restartHost(host);
            unregisterHostSuspendedEvent();
        }
    }

    @Wrapper
    public void registerHostSuspendedEvent(BrownieHost host) {
        registerHostEvent(HOST_SUSPENDED);
        preferences.put(SESSION_IDENTIFIER_KEY, host.getSession().getId());
        preferences.put(HOST_IDENTIFIER_KEY, host.getId());
    }

    private void registerHostEvent(LocalEvent event) {
        preferences.put(event.eventKey, event.name());
    }

    @Wrapper
    public void unregisterHostSuspendedEvent() {
        unregisterLocalEvent(HOST_SUSPENDED);
        preferences.remove(SESSION_IDENTIFIER_KEY);
        preferences.remove(HOST_IDENTIFIER_KEY);
    }

    private void unregisterLocalEvent(LocalEvent event) {
        preferences.remove(event.eventKey);
    }

    @Wrapper
    public boolean hostSuspendedEventIsRegistered() {
        return hasLocalEventRegistered(HOST_SUSPENDED);
    }

    private boolean hasLocalEventRegistered(LocalEvent event) {
        return preferences.get(event.eventKey, null) != null;
    }

    public static LocalEventsHandler getInstance() {
        return LOCAL_EVENTS_HANDLER;
    }

}
