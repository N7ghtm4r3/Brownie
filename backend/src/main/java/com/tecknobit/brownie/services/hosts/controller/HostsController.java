package com.tecknobit.brownie.services.hosts.controller;

import com.tecknobit.brownie.services.hosts.service.HostsService;
import com.tecknobit.brownie.services.shared.controllers.DefaultBrownieController;
import com.tecknobit.equinoxcore.helpers.InputsValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.browniecore.helpers.BrownieInputsValidator.INSTANCE;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.IDENTIFIER_KEY;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.NAME_KEY;
import static com.tecknobit.equinoxcore.helpers.InputsValidator.WRONG_NAME_MESSAGE;
import static com.tecknobit.equinoxcore.network.EquinoxBaseEndpointsSet.BASE_EQUINOX_ENDPOINT;
import static com.tecknobit.equinoxcore.pagination.PaginatedResponse.*;

@RestController
@RequestMapping(value = BASE_EQUINOX_ENDPOINT + SESSIONS_KEY + "/{" + IDENTIFIER_KEY + "}/" + HOSTS_KEY)
public class HostsController extends DefaultBrownieController {

    public static final String WRONG_HOST_ADDRESS_MESSAGE = "wrong_host_address_key";

    public static final String WRONG_SSH_CREDENTIALS_MESSAGE = "ssh_credentials_are_not_valid_key";

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
        if (!InputsValidator.Companion.isHostValid(hostAddress))
            return failedResponse(WRONG_HOST_ADDRESS_MESSAGE);
        return null;
    }

    private boolean SSHCredentialsAreValid(String sshUser, String sshPassword) {
        boolean sshUserFilled = sshUser != null && !sshUser.isEmpty();
        boolean sshPasswordFilled = sshPassword != null && !sshPassword.isEmpty();
        return sshUserFilled && sshPasswordFilled || !sshUserFilled && !sshPasswordFilled;
    }

}
