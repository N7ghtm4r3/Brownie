package com.tecknobit.brownie.services.shared.controllers;

import com.tecknobit.brownie.services.session.entity.BrownieSession;
import com.tecknobit.brownie.services.session.service.BrownieSessionsService;
import com.tecknobit.equinoxbackend.environment.services.DefaultEquinoxController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultBrownieController extends DefaultEquinoxController {

    @Autowired
    protected BrownieSessionsService sessionsService;

    protected BrownieSession currentBrownieSession;

    protected boolean sessionExists(String sessionId) {
        currentBrownieSession = sessionsService.getBrownieSession(sessionId);
        return currentBrownieSession != null;
    }

}
