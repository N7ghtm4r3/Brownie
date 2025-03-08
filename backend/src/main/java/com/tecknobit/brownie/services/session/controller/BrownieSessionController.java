package com.tecknobit.brownie.services.session.controller;

import com.tecknobit.apimanager.apis.ServerProtector;
import com.tecknobit.brownie.services.session.entity.BrownieSession;
import com.tecknobit.brownie.services.shared.controllers.DefaultBrownieController;
import com.tecknobit.equinoxbackend.environment.services.DefaultEquinoxController;
import com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController;
import com.tecknobit.equinoxcore.annotations.RequestPath;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static com.tecknobit.apimanager.apis.ServerProtector.SERVER_SECRET_KEY;
import static com.tecknobit.browniecore.ConstantsKt.JOIN_CODE_KEY;
import static com.tecknobit.browniecore.ConstantsKt.SESSIONS_KEY;
import static com.tecknobit.browniecore.helpers.BrownieEndpoints.CONNECT_ENDPOINT;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.*;
import static com.tecknobit.equinoxcore.helpers.InputsValidator.*;
import static com.tecknobit.equinoxcore.network.EquinoxBaseEndpointsSet.BASE_EQUINOX_ENDPOINT;
import static com.tecknobit.equinoxcore.network.RequestMethod.*;

/**
 * The {@code BrownieSessionController} class is useful to manage all the {@link BrownieSession} operations
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxController
 * @see DefaultEquinoxController
 * @see DefaultBrownieController
 */
@RestController
@RequestMapping(value = BASE_EQUINOX_ENDPOINT + SESSIONS_KEY)
public class BrownieSessionController extends DefaultBrownieController {

    /**
     * {@code brownieServerProtector} instance of the {@link ServerProtector} used to protect the accesses to a private
     * backend instance
     */
    public static ServerProtector brownieServerProtector;

    /**
     * Endpoint used to create a new session
     *
     * @param payload The payload of the request
     *                 <pre>
     *                      {@code
     *                              {
     *                                  "server_secret" : "the server secret used to protect the access to a private backend instance" -> [String],
     *                                  "password" : "the password used to protect the session accesses" -> [String]
     *                              }
     *                      }
     *                 </pre>
     *
     * @return the response as {@link T}
     *
     * @param <T> the type of the response
     */
    @PostMapping
    @RequestPath(path = "/api/v1/sessions", method = POST)
    public <T> T createSession(
            @RequestBody Map<String, String> payload
    ) throws NoSuchAlgorithmException {
        loadJsonHelper(payload);
        if (!brownieServerProtector.serverSecretMatches(jsonHelper.getString(SERVER_SECRET_KEY)))
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        String password = jsonHelper.getString(PASSWORD_KEY);
        if (!Companion.isPasswordValid(password))
            return (T) failedResponse(WRONG_PASSWORD_MESSAGE);
        BrownieSession session = sessionsService.createSession(generateIdentifier(), generateIdentifier(), password);
        return (T) successResponse(session);
    }

    /**
     * Endpoint used to connect to an existing session
     *
     * @param payload The payload of the request
     *                 <pre>
     *                      {@code
     *                              {
     *                                  "server_secret" : "the server secret used to protect the access to a private backend instance" -> [String],
     *                                  "password" : "the password used to protect the session accesses" -> [String],
     *                                  "join_code" : "the join code to connect to the session" -> [String]
     *                              }
     *                      }
     *                 </pre>
     *
     * @return the response as {@link T}
     *
     * @param <T> the type of the response
     */
    @PutMapping(
            path = CONNECT_ENDPOINT
    )
    @RequestPath(path = "/api/v1/sessions/connect", method = PUT)
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

    /**
     * Endpoint used to delete an existing session
     *
     * @param sessionId The identifier of the session to delete
     * @param language The language of the user who sent the request
     * @param payload The payload of the request
     *                 <pre>
     *                      {@code
     *                              {
     *                                  "password" : "the password used to protect the session accesses" -> [String]
     *                              }
     *                      }
     *                 </pre>
     *
     * @return the response as {@link String}
     */
    @DeleteMapping(
            path = "/{" + IDENTIFIER_KEY + "}"
    )
    @RequestPath(path = "/api/v1/sessions/{session_id}", method = DELETE)
    public String deleteSession(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language,
            @RequestBody Map<String, String> payload
    ) throws NoSuchAlgorithmException {
        setSessionLocale(language);
        loadJsonHelper(payload);
        String password = jsonHelper.getString(PASSWORD_KEY, "");
        BrownieSession session = sessionsService.getBrownieSession(sessionId, password);
        if(session == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        sessionsService.deleteSession(sessionId);
        return successResponse();
    }

}
