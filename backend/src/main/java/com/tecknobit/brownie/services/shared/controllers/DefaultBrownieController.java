package com.tecknobit.brownie.services.shared.controllers;

import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.services.HostsService;
import com.tecknobit.brownie.services.session.entity.BrownieSession;
import com.tecknobit.brownie.services.session.service.BrownieSessionsService;
import com.tecknobit.equinoxbackend.environment.services.DefaultEquinoxController;
import com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * The {@code DefaultBrownieController} class is useful to give the base behavior of the <b>Brownie's controllers</b>
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxController
 * @see DefaultEquinoxController
 */
@RestController
public class DefaultBrownieController extends DefaultEquinoxController {

    /**
     * {@code SOMETHING_WENT_WRONG_MESSAGE} the key of the message sent to the clients when a remote SSH connection failed
     */
    public static final String SOMETHING_WENT_WRONG_MESSAGE = "something_went_wrong";

    /**
     * {@code sessionsService} the support service used to manage the sessions data
     */
    protected final BrownieSessionsService sessionsService;

    /**
     * {@code hostsService} the support service used to manage the hosts data
     */
    protected final HostsService hostsService;

    /**
     * {@code currentBrownieSession} the current Brownie's session used in the request
     */
    protected BrownieSession currentBrownieSession;

    /**
     * Constructor used to init the controller
     *
     * @param sessionsService The support service used to manage the sessions data
     * @param hostsService    The support service used to manage the hosts data
     */
    @Autowired
    public DefaultBrownieController(BrownieSessionsService sessionsService, HostsService hostsService) {
        this.sessionsService = sessionsService;
        this.hostsService = hostsService;
    }

    /**
     * Method to check whether a session exists by its id
     *
     * @param sessionId The identifier of the session to check
     * @return whether the session exists as {@code boolean}
     */
    protected boolean sessionExists(String sessionId) {
        currentBrownieSession = sessionsService.getBrownieSession(sessionId);
        return currentBrownieSession != null;
    }

    /**
     * Method to retrieve a {@link BrownieHost} checking the identifier of the session and its identifier
     *
     * @param sessionId The identifier of the session owner of the host
     * @param hostId The identifier of the host to retrieve
     *
     * @return if the host owned by the session and exists as {@link BrownieHost}, null otherwise
     */
    protected BrownieHost getBrownieHostIfAllowed(String sessionId, String hostId) {
        return hostsService.getBrownieHost(sessionId, hostId);
    }

}
