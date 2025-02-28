package com.tecknobit.brownie.services.session.controller;

import com.tecknobit.apimanager.apis.ServerProtector;
import com.tecknobit.brownie.services.session.repository.BrownieSessionsService;
import com.tecknobit.equinoxbackend.environment.services.DefaultEquinoxController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static com.tecknobit.apimanager.apis.ServerProtector.SERVER_SECRET_KEY;
import static com.tecknobit.browniecore.ConstantsKt.SESSIONS_KEY;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.PASSWORD_KEY;
import static com.tecknobit.equinoxcore.helpers.InputsValidator.Companion;
import static com.tecknobit.equinoxcore.helpers.InputsValidator.WRONG_PASSWORD_MESSAGE;
import static com.tecknobit.equinoxcore.network.EquinoxBaseEndpointsSet.BASE_EQUINOX_ENDPOINT;

@RestController
@RequestMapping(value = BASE_EQUINOX_ENDPOINT + SESSIONS_KEY)
public class BrownieSessionController extends DefaultEquinoxController {

    public static ServerProtector brownieServerProtector;

    @Autowired
    private BrownieSessionsService sessionsService;

    @PostMapping
    public String createSession(
            @RequestBody Map<String, String> payload
    ) throws NoSuchAlgorithmException {
        loadJsonHelper(payload);
        if (!brownieServerProtector.serverSecretMatches(jsonHelper.getString(SERVER_SECRET_KEY)))
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        String password = jsonHelper.getString(PASSWORD_KEY);
        if (!Companion.isPasswordValid(password))
            return failedResponse(WRONG_PASSWORD_MESSAGE);
        sessionsService.createSession(generateIdentifier(), generateIdentifier(), password);
        return successResponse();
    }

}
