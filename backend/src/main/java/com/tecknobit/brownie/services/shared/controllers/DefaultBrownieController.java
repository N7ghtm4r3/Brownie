package com.tecknobit.brownie.services.shared.controllers;

import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.services.HostsService;
import com.tecknobit.brownie.services.session.entity.BrownieSession;
import com.tecknobit.brownie.services.session.service.BrownieSessionsService;
import com.tecknobit.equinoxbackend.environment.services.DefaultEquinoxController;
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

@RestController
public class DefaultBrownieController extends DefaultEquinoxController {

    public static final String SOMETHING_WENT_WRONG_MESSAGE = "something_went_wrong_key";

    @Autowired
    protected BrownieSessionsService sessionsService;

    @Autowired
    protected HostsService hostsService;

    @Autowired
    @FutureEquinoxApi(
            releaseVersion = "1.0.9",
            additionalNotes = "This will be replace the current translating system with the Mantis library"
    )
    protected MessageSource messageSource;

    protected BrownieSession currentBrownieSession;

    protected boolean sessionExists(String sessionId) {
        currentBrownieSession = sessionsService.getBrownieSession(sessionId);
        return currentBrownieSession != null;
    }

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
