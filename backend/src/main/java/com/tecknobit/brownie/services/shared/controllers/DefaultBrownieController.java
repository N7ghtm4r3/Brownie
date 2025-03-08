package com.tecknobit.brownie.services.shared.controllers;

import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.services.HostsService;
import com.tecknobit.brownie.services.session.entity.BrownieSession;
import com.tecknobit.brownie.services.session.service.BrownieSessionsService;
import com.tecknobit.equinoxbackend.environment.services.DefaultEquinoxController;
import com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController;
import com.tecknobit.equinoxcore.annotations.Assembler;
import com.tecknobit.equinoxcore.annotations.FutureEquinoxApi;
import com.tecknobit.equinoxcore.annotations.Returner;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import com.tecknobit.equinoxcore.network.ResponseStatus;
import jakarta.annotation.Nullable;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

import static com.tecknobit.equinoxcore.network.Requester.RESPONSE_DATA_KEY;
import static com.tecknobit.equinoxcore.network.Requester.RESPONSE_STATUS_KEY;
import static com.tecknobit.equinoxcore.network.ResponseStatus.FAILED;
import static com.tecknobit.equinoxcore.network.ResponseStatus.SUCCESSFUL;

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
    public static final String SOMETHING_WENT_WRONG_MESSAGE = "something_went_wrong_key";

    /**
     * {@code sessionsService} the support service used to manage the sessions data
     */
    @Autowired
    protected BrownieSessionsService sessionsService;

    /**
     * {@code hostsService} the support service used to manage the hosts data
     */
    @Autowired
    protected HostsService hostsService;

    @Autowired
    @FutureEquinoxApi(
            releaseVersion = "1.0.9",
            additionalNotes = "This will be replace the current translating system with the Mantis library"
    )
    protected MessageSource messageSource;

    /**
     * {@code currentBrownieSession} the current Brownie's session used in the request
     */
    protected BrownieSession currentBrownieSession;

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

    @FutureEquinoxApi(
            releaseVersion = "1.0.9",
            additionalNotes = "This will be replace the current translating system with the Mantis library"
    )
    protected String successResponse() {
        return plainResponse(SUCCESSFUL, getInternationalizedMessage(RESPONSE_SUCCESSFUL_MESSAGE));
    }

    @FutureEquinoxApi(
            releaseVersion = "1.0.9",
            additionalNotes = "This will be replace the current translating system with the Mantis library"
    )
    protected void setSessionLocale(String locale) {
        setSessionLocale(Locale.forLanguageTag(locale));
    }

    @FutureEquinoxApi(
            releaseVersion = "1.0.9",
            additionalNotes = "This will be replace the current translating system with the Mantis library"
    )
    protected void setSessionLocale(Locale locale) {
        LocaleContextHolder.setLocale(locale);
    }

    @FutureEquinoxApi(
            releaseVersion = "1.0.9",
            additionalNotes = "This will be replace the current translating system with the Mantis library"
    )
    @Wrapper
    @Assembler
    protected String failedResponse(String errorKey) {
        return failedResponse(errorKey, null);
    }

    @FutureEquinoxApi(
            releaseVersion = "1.0.9",
            additionalNotes = "This will be replace the current translating system with the Mantis library"
    )
    @Assembler
    protected String failedResponse(String errorKey, @Nullable Object[] args) {
        return plainResponse(FAILED, getInternationalizedMessage(errorKey, args));
    }

    @FutureEquinoxApi(
            releaseVersion = "1.0.9",
            additionalNotes = "This will be replace the current translating system with the Mantis library"
    )
    @Wrapper
    @Returner
    protected String getInternationalizedMessage(String errorKey) {
        return getInternationalizedMessage(errorKey, null);
    }

    @FutureEquinoxApi(
            releaseVersion = "1.0.9",
            additionalNotes = "This will be replace the current translating system with the Mantis library"
    )
    @Returner
    protected String getInternationalizedMessage(String errorKey, @Nullable Object[] args) {
        return messageSource.getMessage(errorKey, args, LocaleContextHolder.getLocale());
    }

    /**
     * Method to assemble the payload for a response
     *
     * @param status  The response code value
     * @param message The message to send as response
     * @return the payload for a response as {@link String}
     */
    @Deprecated(since = "TO REMOVE")
    @Assembler
    protected String plainResponse(ResponseStatus status, String message) {
        return new JSONObject()
                .put(RESPONSE_STATUS_KEY, status)
                .put(RESPONSE_DATA_KEY, message).toString();
    }

}
