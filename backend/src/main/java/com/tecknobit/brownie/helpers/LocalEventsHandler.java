package com.tecknobit.brownie.helpers;

import com.tecknobit.brownie.services.hosts.services.HostsService;
import com.tecknobit.equinoxcore.annotations.Wrapper;

import java.util.prefs.Preferences;

import static com.tecknobit.brownie.helpers.LocalEventsHandler.LocalEvent.HOST_REBOOTED;
import static com.tecknobit.brownie.helpers.LocalEventsHandler.LocalEvent.HOST_STOPPED;

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

    private final Preferences preferences;

    private LocalEventsHandler() {
        preferences = Preferences.userRoot().node("tecknobit/brownie/backend");
    }

    public void executeLocalEventsScan() {
        if (hostRebootedEventIsRegistered()) {
            HostsService service = new HostsService();
            service.rebootHost();
        }
    }

    @Wrapper
    public void registerHostRebootedEvent() {
        registerHostEvent(HOST_REBOOTED);
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
        return preferences.getBoolean(event.eventKey, false);
    }

    public static LocalEventsHandler getInstance() {
        return LOCAL_EVENTS_HANDLER;
    }

}
