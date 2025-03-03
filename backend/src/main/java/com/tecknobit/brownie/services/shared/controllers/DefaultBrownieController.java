package com.tecknobit.brownie.services.shared.controllers;

import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.services.HostsService;
import com.tecknobit.brownie.services.session.entity.BrownieSession;
import com.tecknobit.brownie.services.session.service.BrownieSessionsService;
import com.tecknobit.equinoxbackend.environment.services.DefaultEquinoxController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultBrownieController extends DefaultEquinoxController {

    public static final String SOMETHING_WENT_WRONG_MESSAGE = "something_went_wrong_message";

    @Autowired
    protected BrownieSessionsService sessionsService;

    @Autowired
    protected HostsService hostsService;

    protected BrownieSession currentBrownieSession;

    protected boolean sessionExists(String sessionId) {
        currentBrownieSession = sessionsService.getBrownieSession(sessionId);
        return currentBrownieSession != null;
    }

    protected BrownieHost getBrownieHostIfAllowed(String sessionId, String hostId) {
        return hostsService.getBrownieHost(sessionId, hostId);
    }

}
