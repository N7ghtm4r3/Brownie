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

/**
 * The {@code LocalEventsHandler} component class is used to handle the local events registered during the runtime of
 * the backend instance on the same physical machine where is running. These events are useful to indicate that for example
 * the host has been stopped or has been rebooted so is needed to restart the auto run services etc...
 *
 * @author N7ghtm4r3 - Tecknobit
 */
@Component
public class LocalEventsHandler {

    /**
     * {@code LocalEvent} list of available local events handled
     */
    public enum LocalEvent {

        /**
         * {@code HOST_SUSPENDED} this event is registered when the host has been stopped or rebooted to allow the handling
         * of the start of the auto run services
         */
        HOST_SUSPENDED("host_suspended");

        /**
         * {@code eventKey} the key of the event used during the registration in the local {@link Preferences}
         */
        private final String eventKey;

        /**
         * Constructor to instantiate the event
         *
         * @param eventKey The key of the event used during the registration in the local {@link Preferences}
         */
        LocalEvent(String eventKey) {
            this.eventKey = eventKey;
        }

    }

    /**
     * {@code LOCAL_EVENTS_HANDLER} the singleton instance of the local events handler
     */
    private static final LocalEventsHandler LOCAL_EVENTS_HANDLER = new LocalEventsHandler();

    /**
     * {@code preferences} the local preferences used to store the local events to register
     *
     * @apiNote If you run the backend on Windows you have to run as administrator to correctly launch the backend
     */
    private final Preferences preferences = Preferences.systemRoot().node("tecknobit/brownie/local_events");

    /**
     * {@code hostsService} the service used to handle the hosts between controller and related repository
     */
    @Autowired
    private HostsService hostsService;

    /**
     * Event listener which wait the complete startup of the backend to perform the scan of the local events currently
     * registered and to handle
     *
     * @throws Exception when an error during the scan and the related handling occurs
     */
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

    /**
     * Method used to register the {@link LocalEvent#HOST_SUSPENDED} event
     *
     * @param host The host which have been suspended
     */
    @Wrapper
    public void registerHostSuspendedEvent(BrownieHost host) {
        registerLocalEvent(HOST_SUSPENDED);
        preferences.put(SESSION_IDENTIFIER_KEY, host.getSession().getId());
        preferences.put(HOST_IDENTIFIER_KEY, host.getId());
    }

    /**
     * Method used to register a local event
     *
     * @param event The local event to register
     */
    private void registerLocalEvent(LocalEvent event) {
        preferences.put(event.eventKey, event.name());
    }

    /**
     * Method used to unregister the previously registered {@link LocalEvent#HOST_SUSPENDED} event and the additional
     * information registered
     */
    @Wrapper
    public void unregisterHostSuspendedEvent() {
        unregisterLocalEvent(HOST_SUSPENDED);
        preferences.remove(SESSION_IDENTIFIER_KEY);
        preferences.remove(HOST_IDENTIFIER_KEY);
    }

    /**
     * Method used to unregister a local event
     * @param event The local event to unregister
     */
    private void unregisterLocalEvent(LocalEvent event) {
        preferences.remove(event.eventKey);
    }

    /**
     * Method used to check whether the {@link LocalEvent#HOST_SUSPENDED} event is registered
     *
     * @return whether the {@link LocalEvent#HOST_SUSPENDED} event is registered as {@code boolean}
     */
    @Wrapper
    public boolean hostSuspendedEventIsRegistered() {
        return hasLocalEventRegistered(HOST_SUSPENDED);
    }

    /**
     * Method used to check whether a {@link LocalEvent} is registered
     *
     * @return whether a {@link LocalEvent} is registered as {@code boolean}
     */
    private boolean hasLocalEventRegistered(LocalEvent event) {
        return preferences.get(event.eventKey, null) != null;
    }

    /**
     * Method used to obtain an instance of the handler
     *
     * @return an instance of the handler as {@link LocalEventsHandler}
     */
    public static LocalEventsHandler getInstance() {
        return LOCAL_EVENTS_HANDLER;
    }

}
