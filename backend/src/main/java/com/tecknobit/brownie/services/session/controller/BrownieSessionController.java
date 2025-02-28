package com.tecknobit.brownie.services.session.controller;

import com.tecknobit.apimanager.apis.ServerProtector;
import com.tecknobit.brownie.services.session.entity.BrownieSession;
import com.tecknobit.brownie.services.shared.controllers.DefaultBrownieController;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static com.tecknobit.apimanager.apis.ServerProtector.SERVER_SECRET_KEY;
import static com.tecknobit.browniecore.ConstantsKt.JOIN_CODE_KEY;
import static com.tecknobit.browniecore.ConstantsKt.SESSIONS_KEY;
import static com.tecknobit.browniecore.helpers.BrownieEndpoints.CONNECT_ENDPOINT;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.IDENTIFIER_KEY;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.PASSWORD_KEY;
import static com.tecknobit.equinoxcore.helpers.InputsValidator.Companion;
import static com.tecknobit.equinoxcore.helpers.InputsValidator.WRONG_PASSWORD_MESSAGE;
import static com.tecknobit.equinoxcore.network.EquinoxBaseEndpointsSet.BASE_EQUINOX_ENDPOINT;

@RestController
@RequestMapping(value = BASE_EQUINOX_ENDPOINT + SESSIONS_KEY)
public class BrownieSessionController extends DefaultBrownieController {

    public static ServerProtector brownieServerProtector;

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

    @PutMapping(
            path = CONNECT_ENDPOINT
    )
    public <T> T connectToSession(
            @RequestBody Map<String, String> payload
    ) throws NoSuchAlgorithmException {
        loadJsonHelper(payload);
        if (!brownieServerProtector.serverSecretMatches(jsonHelper.getString(SERVER_SECRET_KEY)))
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        String joinCode = jsonHelper.getString(JOIN_CODE_KEY);
        String password = jsonHelper.getString(PASSWORD_KEY);
        BrownieSession session = sessionsService.connectToSession(joinCode, password);
        if (session == null)
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        return (T) successResponse(session);
    }

    @DeleteMapping(
            path = "/{" + IDENTIFIER_KEY + "}"
    )
    public String deleteSession(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @RequestBody Map<String, String> payload
    ) throws NoSuchAlgorithmException {
        loadJsonHelper(payload);
        String password = jsonHelper.getString(PASSWORD_KEY, "");
        BrownieSession session = sessionsService.getBrownieSession(sessionId, password);
        if(session == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        sessionsService.deleteSession(sessionId);
        return successResponse();
    }

}
