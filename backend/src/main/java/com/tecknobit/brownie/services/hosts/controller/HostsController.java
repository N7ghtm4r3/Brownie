package com.tecknobit.brownie.services.hosts.controller;

import com.jcraft.jsch.JSchException;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.services.HostsService;
import com.tecknobit.brownie.services.shared.controllers.DefaultBrownieController;
import com.tecknobit.equinoxcore.network.ResponseStatus;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.browniecore.helpers.BrownieEndpoints.*;
import static com.tecknobit.browniecore.helpers.BrownieInputsValidator.INSTANCE;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.IDENTIFIER_KEY;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.NAME_KEY;
import static com.tecknobit.equinoxcore.helpers.InputsValidator.WRONG_NAME_MESSAGE;
import static com.tecknobit.equinoxcore.network.EquinoxBaseEndpointsSet.BASE_EQUINOX_ENDPOINT;
import static com.tecknobit.equinoxcore.network.Requester.RESPONSE_DATA_KEY;
import static com.tecknobit.equinoxcore.network.Requester.RESPONSE_STATUS_KEY;
import static com.tecknobit.equinoxcore.network.ResponseStatus.FAILED;
import static com.tecknobit.equinoxcore.pagination.PaginatedResponse.*;

@RestController
@RequestMapping(value = BASE_EQUINOX_ENDPOINT + SESSIONS_KEY + "/{" + IDENTIFIER_KEY + "}/" + HOSTS_KEY)
public class HostsController extends DefaultBrownieController {

    public static final String WRONG_HOST_ADDRESS_MESSAGE = "wrong_host_address_key";

    public static final String WRONG_SSH_CREDENTIALS_MESSAGE = "ssh_credentials_are_not_valid_key";

    public static final String SOMETHING_WENT_WRONG_MESSAGE = "something_went_wrong_message";

    @Autowired
    private HostsService hostsService;

    @GetMapping
    public <T> T getHosts(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @RequestParam(name = KEYWORDS_KEY, defaultValue = "", required = false) Set<String> keywords,
            @RequestParam(
                    name = STATUSES_KEY,
                    defaultValue = "ONLINE, OFFLINE, REBOOTING",
                    required = false
            ) List<String> statuses,
            @RequestParam(name = PAGE_KEY, defaultValue = DEFAULT_PAGE_HEADER_VALUE, required = false) int page,
            @RequestParam(name = PAGE_SIZE_KEY, defaultValue = DEFAULT_PAGE_SIZE_HEADER_VALUE, required = false) int pageSize
    ) {
        if (!sessionExists(sessionId))
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        return (T) successResponse(hostsService.getHosts(keywords, statuses, page, pageSize));
    }

    @PostMapping
    public String registerHost(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @RequestBody Map<String, Object> payload
    ) {
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

    @PatchMapping(
            path = "/{" + HOST_IDENTIFIER_KEY + "}"
    )
    public String editHost(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @RequestBody Map<String, Object> payload
    ) {
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
            hostsService.editHost(hostId, hostName, hostAddress, sshUser, sshPassword);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
        return successResponse();
    }

    private String validateHostPayload(String hostName, String hostAddress) {
        if (!INSTANCE.isItemNameValid(hostName))
            return failedResponse(WRONG_NAME_MESSAGE);
        if (!INSTANCE.isHostAddressValid(hostAddress))
            return failedResponse(WRONG_HOST_ADDRESS_MESSAGE);
        return null;
    }

    private boolean SSHCredentialsAreValid(String sshUser, String sshPassword) {
        boolean sshUserFilled = sshUser != null && !sshUser.isEmpty();
        boolean sshPasswordFilled = sshPassword != null && !sshPassword.isEmpty();
        return sshUserFilled && sshPasswordFilled || !sshUserFilled && !sshPasswordFilled;
    }

    @PatchMapping(
            path = "/{" + HOST_IDENTIFIER_KEY + "}" + START_HOST_ENDPOINT
    )
    // TODO: 01/03/2025 ADD THE REFERENCE TO WoL documentation
    public String startHost(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId
    ) {
        BrownieHost brownieHost = getBrownieHostIfAllowed(sessionId, hostId);
        if (brownieHost == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!brownieHost.isRemoteHost() || !brownieHost.isOffline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        try {
            hostsService.startHost(brownieHost);
            return successResponse();
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: 01/03/2025 CUSTOMIZE THE ERROR
            return failedResponse("gg");
        }
    }

    @PatchMapping(
            path = "/{" + HOST_IDENTIFIER_KEY + "}" + REBOOT_HOST_ENDPOINT
    )
    public String rebootHost(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId
    ) {
        BrownieHost brownieHost = getBrownieHostIfAllowed(sessionId, hostId);
        if (brownieHost == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!brownieHost.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        try {
            hostsService.rebootHost(brownieHost);
            return successResponse();
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return plainResponse(FAILED, e.getMessage());
        }
    }

    @PatchMapping(
            path = "/{" + HOST_IDENTIFIER_KEY + "}" + STOP_HOST_ENDPOINT
    )
    public String stopHost(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId
    ) {
        BrownieHost brownieHost = getBrownieHostIfAllowed(sessionId, hostId);
        if (brownieHost == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!brownieHost.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        try {
            hostsService.stopHost(brownieHost);
            return successResponse();
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return plainResponse(FAILED, e.getMessage());
        }
    }

    @GetMapping(
            path = "/{" + HOST_IDENTIFIER_KEY + "}"
    )
    public <T> T getHostOverview(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId
    ) {
        BrownieHost brownieHost = getBrownieHostIfAllowed(sessionId, hostId);
        if (brownieHost == null)
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!brownieHost.isOnline())
            return (T) failedResponse(WRONG_PROCEDURE_MESSAGE);
        try {
            return (T) successResponse(hostsService.getHostOverView(brownieHost));
        } catch (JSchException e) {
            return (T) failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return (T) plainResponse(FAILED, e.getMessage());
        }
    }

    private BrownieHost getBrownieHostIfAllowed(String sessionId, String hostId) {
        return hostsService.getBrownieHost(sessionId, hostId);
    }

    /**
     * Method to assemble the payload for a response
     *
     * @param status  The response code value
     * @param message The message to send as response
     * @return the payload for a response as {@link String}
     */
    @Deprecated(since = "TO REMOVE")
    private String plainResponse(ResponseStatus status, String message) {
        return new JSONObject()
                .put(RESPONSE_STATUS_KEY, status)
                .put(RESPONSE_DATA_KEY, message).toString();
    }

}
