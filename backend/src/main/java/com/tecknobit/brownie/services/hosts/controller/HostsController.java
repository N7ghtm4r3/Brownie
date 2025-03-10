package com.tecknobit.brownie.services.hosts.controller;

import com.jcraft.jsch.JSchException;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.shared.controllers.DefaultBrownieController;
import com.tecknobit.equinoxbackend.environment.services.DefaultEquinoxController;
import com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController;
import com.tecknobit.equinoxcore.annotations.RequestPath;
import org.json.JSONArray;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.browniecore.helpers.BrownieEndpoints.*;
import static com.tecknobit.browniecore.helpers.BrownieInputsValidator.INSTANCE;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.*;
import static com.tecknobit.equinoxcore.helpers.InputsValidator.DEFAULT_LANGUAGE;
import static com.tecknobit.equinoxcore.helpers.InputsValidator.WRONG_NAME_MESSAGE;
import static com.tecknobit.equinoxcore.network.EquinoxBaseEndpointsSet.BASE_EQUINOX_ENDPOINT;
import static com.tecknobit.equinoxcore.network.RequestMethod.*;
import static com.tecknobit.equinoxcore.network.ResponseStatus.FAILED;
import static com.tecknobit.equinoxcore.pagination.PaginatedResponse.*;

/**
 * The {@code BrownieSessionController} class is useful to manage all the {@link BrownieHost} operations
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxController
 * @see DefaultEquinoxController
 * @see DefaultBrownieController
 */
@RestController
@RequestMapping(value = BASE_EQUINOX_ENDPOINT + SESSIONS_KEY + "/{" + IDENTIFIER_KEY + "}/" + HOSTS_KEY)
public class HostsController extends DefaultBrownieController {

    /**
     * {@code WRONG_HOST_ADDRESS_MESSAGE} the key of the message sent to the clients when a host address value is not
     * valid
     */
    public static final String WRONG_HOST_ADDRESS_MESSAGE = "wrong_host_address_key";

    /**
     * {@code WRONG_SSH_CREDENTIALS_MESSAGE} the key of the message sent to the clients when the SSH credentials are not
     * valid
     */
    public static final String WRONG_SSH_CREDENTIALS_MESSAGE = "ssh_credentials_are_not_valid_key";

    /**
     * Endpoint used to retrieve the hosts of a session
     *
     * @param sessionId The identifier of the session
     * @param keywords  The keywords used to filter the results
     * @param statuses  The statuses used to filter the results
     * @param language  The language of the user who sent the request
     * @param page      The page requested
     * @param pageSize  The size of the items to insert in the page
     * @param <T>       the type of the response
     * @return the response as {@link T}
     */
    @GetMapping
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts", method = GET)
    public <T> T getHosts(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @RequestParam(name = KEYWORDS_KEY, defaultValue = "", required = false) Set<String> keywords,
            @RequestParam(
                    name = STATUSES_KEY,
                    defaultValue = "[ONLINE, OFFLINE, REBOOTING]",
                    required = false
            ) JSONArray statuses,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language,
            @RequestParam(name = PAGE_KEY, defaultValue = DEFAULT_PAGE_HEADER_VALUE, required = false) int page,
            @RequestParam(name = PAGE_SIZE_KEY, defaultValue = DEFAULT_PAGE_SIZE_HEADER_VALUE, required = false) int pageSize
    ) {
        setSessionLocale(language);
        if (!sessionExists(sessionId))
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        return (T) successResponse(hostsService.getHosts(sessionId, keywords, statuses, page, pageSize));
    }

    /**
     * Endpoint used to retrieve the current status of the specified hosts
     *
     * @param sessionId The identifier of the session
     * @param hosts The hosts to retrieve their current status
     * @param language The language of the user who sent the request
     *
     * @return the response as {@link T}
     *
     * @param <T> the type of the response
     */
    @GetMapping(
            path = "/" + STATUS_KEY
    )
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts/status", method = GET)
    public <T> T getHostsStatus(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @RequestParam(name = HOSTS_KEY, defaultValue = "[]") JSONArray hosts,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language
    ) {
        setSessionLocale(language);
        if (!sessionExists(sessionId))
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        return (T) successResponse(hostsService.getHostsStatus(hosts));
    }

    /**
     * Endpoint used to register a new host
     *
     * @param sessionId The identifier of the session
     * @param language The language of the user who sent the request
     * @param payload The payload of the request
     *                 <pre>
     *                      {@code
     *                              {
     *                                  "name" : "the name of the host" -> [String],
     *                                  "host_address" : "the address of the host" -> [String]
     *                                  -- OPTIONAL --
     *                                  "ssh_user" : "the user of the SSH connection" -> [String],
     *                                  "ssh_password" : "the password of the SSH connection" -> [String]
     *                              }
     *                      }
     *                 </pre>
     *
     * @return the response as {@link String}
     */
    @PostMapping
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts", method = POST)
    public String registerHost(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language,
            @RequestBody Map<String, Object> payload
    ) {
        setSessionLocale(language);
        if (!sessionExists(sessionId))
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        loadJsonHelper(payload);
        String hostName = jsonHelper.getString(NAME_KEY);
        String hostAddress = jsonHelper.getString(HOST_ADDRESS_KEY);
        String requiredFormDataAreValid = validateHostPayload(hostName, hostAddress);
        if (requiredFormDataAreValid != null)
            return requiredFormDataAreValid;
        String sshUser = jsonHelper.getString(SSH_USER_KEY);
        String sshPassword = jsonHelper.getString(SSH_PASSWORD_KEY);
        if (!SSHCredentialsAreValid(sshUser, sshPassword))
            return failedResponse(WRONG_SSH_CREDENTIALS_MESSAGE);
        try {
            hostsService.registerHost(generateIdentifier(), hostName, hostAddress, sshUser, sshPassword, sessionId);
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
        return successResponse();
    }

    /**
     * Endpoint used to get an existing host
     *
     * @param sessionId The identifier of the session
     * @param hostId The identifier of the host
     * @param language The language of the user who sent the request
     *
     * @return the response as {@link T}
     *
     * @param <T> the type of the response
     */
    @GetMapping(
            path = "/{" + HOST_IDENTIFIER_KEY + "}"
    )
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts/{host_id}", method = GET)
    public <T> T getHost(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language
    ) {
        setSessionLocale(language);
        BrownieHost brownieHost = hostsService.getBrownieHost(sessionId, hostId);
        if (brownieHost == null)
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        return (T) successResponse(brownieHost);
    }

    /**
     * Endpoint used to edit an existing host
     *
     * @param sessionId The identifier of the session
     * @param hostId The identifier of the host
     * @param language The language of the user who sent the request
     * @param payload The payload of the request
     *                 <pre>
     *                      {@code
     *                              {
     *                                  "name" : "the name of the host" -> [String],
     *                                  "host_address" : "the address of the host" -> [String]
     *                                  -- OPTIONAL --
     *                                  "ssh_user" : "the user of the SSH connection" -> [String],
     *                                  "ssh_password" : "the password of the SSH connection" -> [String]
     *                              }
     *                      }
     *                 </pre>
     *
     * @return the response as {@link String}
     */
    @PatchMapping(
            path = "/{" + HOST_IDENTIFIER_KEY + "}"
    )
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts/{host_id}", method = PATCH)
    public String editHost(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language,
            @RequestBody Map<String, Object> payload
    ) {
        setSessionLocale(language);
        if (!hostsService.hostBelongsToSession(sessionId, hostId))
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        loadJsonHelper(payload);
        String hostName = jsonHelper.getString(NAME_KEY);
        String hostAddress = jsonHelper.getString(HOST_ADDRESS_KEY);
        String requiredFormDataAreValid = validateHostPayload(hostName, hostAddress);
        if (requiredFormDataAreValid != null)
            return requiredFormDataAreValid;
        String sshUser = jsonHelper.getString(SSH_USER_KEY);
        String sshPassword = jsonHelper.getString(SSH_PASSWORD_KEY);
        if (!SSHCredentialsAreValid(sshUser, sshPassword))
            return failedResponse(WRONG_SSH_CREDENTIALS_MESSAGE);
        try {
            hostsService.editHost(hostId, hostAddress, hostName, sshUser, sshPassword);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
        return successResponse();
    }

    /**
     * Method to validate the payload for the {@link #registerHost(String, String, Map)} and
     * {@link #editHost(String, String, String, Map)} requests
     *
     * @param hostName The name of the host
     * @param hostAddress The address of the host
     *
     * @return the error key if the payload is not valid as {@link String}, null otherwise
     */
    private String validateHostPayload(String hostName, String hostAddress) {
        if (!INSTANCE.isItemNameValid(hostName))
            return failedResponse(WRONG_NAME_MESSAGE);
        if (!INSTANCE.isHostAddressValid(hostAddress))
            return failedResponse(WRONG_HOST_ADDRESS_MESSAGE);
        return null;
    }

    /**
     * Method to validate the payload for the SSH credentials
     *
     * @param sshUser The user of the SSH connection
     * @param sshPassword The password of the SSH connection
     *
     * @return whether the credentials are valid as {@code boolean}
     */
    private boolean SSHCredentialsAreValid(String sshUser, String sshPassword) {
        boolean sshUserFilled = sshUser != null && !sshUser.isEmpty();
        boolean sshPasswordFilled = sshPassword != null && !sshPassword.isEmpty();
        return sshUserFilled && sshPasswordFilled || !sshUserFilled && !sshPasswordFilled;
    }

    /**
     * Endpoint used to start a remote host
     *
     * @param sessionId The identifier of the session
     * @param hostId The identifier of the host
     * @param language The language of the user who sent the request
     *
     * @apiNote Check how to enable the WoL on your remote machine reading the related
     * <a href="https://github.com/N7ghtm4r3/Brownie/blob/main/documd/WoL.md">documentation</a>
     *
     * @implNote This method to wake up a remote host has different factors that can be not properly make work this feature,
     * but collecting feedbacks or issues it gradually will be properly integrated
     *
     * @return the response as {@link String}
     */
    @PatchMapping(
            path = "/{" + HOST_IDENTIFIER_KEY + "}" + START_ENDPOINT
    )
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts/{host_id}/start", method = PATCH)
    public String startHost(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!host.isRemoteHost() || !host.isOffline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        try {
            hostsService.startHost(host);
            return successResponse();
        } catch (Exception e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        }
    }

    /**
     * Endpoint used to reboot a host
     *
     * @param sessionId The identifier of the session
     * @param hostId The identifier of the host
     * @param language The language of the user who sent the request
     *
     * @return the response as {@link String}
     */
    @PatchMapping(
            path = "/{" + HOST_IDENTIFIER_KEY + "}" + REBOOT_ENDPOINT
    )
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts/{host_id}/reboot", method = PATCH)
    public String rebootHost(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!host.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        try {
            hostsService.rebootHost(host);
            return successResponse();
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return plainResponse(FAILED, e.getMessage());
        }
    }

    /**
     * Endpoint used to stop a host
     *
     * @param sessionId The identifier of the session
     * @param hostId The identifier of the host
     * @param language The language of the user who sent the request
     *
     * @return the response as {@link String}
     */
    @PatchMapping(
            path = "/{" + HOST_IDENTIFIER_KEY + "}" + STOP_ENDPOINT
    )
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts/{host_id}/stop", method = PATCH)
    public String stopHost(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!host.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        try {
            hostsService.stopHost(host);
            return successResponse();
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return plainResponse(FAILED, e.getMessage());
        }
    }

    /**
     * Endpoint used to retrieve an overview of a host
     *
     * @param sessionId The identifier of the session
     * @param hostId The identifier of the host
     * @param language The language of the user who sent the request
     *
     * @return the response as {@link T}
     *
     * @param <T> the type of the response
     */
    @GetMapping(
            path = "/{" + HOST_IDENTIFIER_KEY + "}" + OVERVIEW_ENDPOINT
    )
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts/{host_id}/overview", method = GET)
    public <T> T getHostOverview(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        try {
            return (T) successResponse(hostsService.getHostOverview(host));
        } catch (Exception e) {
            return (T) plainResponse(FAILED, e.getMessage());
        }
    }

    /**
     * Endpoint used to unregister a host from the session
     *
     * @param sessionId The identifier of the session
     * @param hostId The identifier of the host
     * @param language The language of the user who sent the request
     *
     * @return the response as {@link String}
     */
    @DeleteMapping(
            path = "/{" + HOST_IDENTIFIER_KEY + "}"
    )
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts/{host_id}", method = DELETE)
    public String unregisterHost(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        hostsService.unregisterHost(hostId);
        return successResponse();
    }

}
