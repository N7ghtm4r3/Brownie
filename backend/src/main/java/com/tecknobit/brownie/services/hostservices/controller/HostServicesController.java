package com.tecknobit.brownie.services.hostservices.controller;

import com.jcraft.jsch.JSchException;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hostservices.entity.BrownieHostService;
import com.tecknobit.brownie.services.hostservices.services.HostServicesService;
import com.tecknobit.brownie.services.shared.controllers.DefaultBrownieController;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
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
import static com.tecknobit.equinoxcore.pagination.PaginatedResponse.*;

@RestController
@RequestMapping(value = BASE_EQUINOX_ENDPOINT + SESSIONS_KEY + "/{" + IDENTIFIER_KEY + "}/" + HOSTS_KEY +
        "/{" + HOST_IDENTIFIER_KEY + "}/" + SERVICES_KEY)
public class HostServicesController extends DefaultBrownieController {

    @Autowired
    private HostServicesService service;

    @PutMapping
    public String addService(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language,
            @RequestBody Map<String, Object> payload
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!host.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        loadJsonHelper(payload);
        String serviceName = jsonHelper.getString(NAME_KEY);
        if (!INSTANCE.isItemNameValid(serviceName))
            return failedResponse(WRONG_NAME_MESSAGE);
        try {
            hostsService.addService(host, serviceName, jsonHelper);
            return successResponse();
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
    }

    @GetMapping(
            path = "/{" + SERVICE_IDENTIFIER_KEY + "}"
    )
    public <T> T getService(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @PathVariable(SERVICE_IDENTIFIER_KEY) String serviceId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        BrownieHostService hostService = null;
        if (host != null && host.isOnline())
            hostService = host.getService(serviceId);
        if (hostService == null)
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        return (T) successResponse(hostService);
    }

    @PatchMapping(
            path = "/{" + SERVICE_IDENTIFIER_KEY + "}"
    )
    public String editService(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @PathVariable(SERVICE_IDENTIFIER_KEY) String serviceId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language,
            @RequestBody Map<String, Object> payload
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null || !host.hasService(serviceId))
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!host.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        loadJsonHelper(payload);
        String serviceName = jsonHelper.getString(NAME_KEY);
        if (!INSTANCE.isItemNameValid(serviceName))
            return failedResponse(WRONG_NAME_MESSAGE);
        try {
            hostsService.editService(host, serviceId, serviceName, jsonHelper);
            return successResponse();
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
    }

    @GetMapping
    public <T> T getServices(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @RequestParam(name = KEYWORDS_KEY, defaultValue = "", required = false) Set<String> keywords,
            @RequestParam(
                    name = STATUSES_KEY,
                    defaultValue = "RUNNING, STOPPED, REBOOTING",
                    required = false
            ) JSONArray statuses,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language,
            @RequestParam(name = PAGE_KEY, defaultValue = DEFAULT_PAGE_HEADER_VALUE, required = false) int page,
            @RequestParam(name = PAGE_SIZE_KEY, defaultValue = DEFAULT_PAGE_SIZE_HEADER_VALUE, required = false) int pageSize
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        return (T) successResponse(service.getServices(hostId, keywords, statuses, page, pageSize));
    }

    @GetMapping(
            path = "/" + STATUS_KEY
    )
    public <T> T getServicesStatus(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @RequestParam(name = SERVICES_KEY, defaultValue = "[]") JSONArray services,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        return (T) successResponse(service.getServicesStatus(services));
    }

    @PatchMapping(
            path = "/{" + SERVICE_IDENTIFIER_KEY + "}" + START_ENDPOINT
    )
    public String startService(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @PathVariable(SERVICE_IDENTIFIER_KEY) String serviceId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!host.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        BrownieHostService brownieHostService = host.getService(serviceId);
        if (brownieHostService == null || !brownieHostService.isStopped())
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        try {
            service.startService(host, brownieHostService);
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
        return successResponse();
    }

    @PatchMapping(
            path = "/{" + SERVICE_IDENTIFIER_KEY + "}" + REBOOT_ENDPOINT
    )
    public String rebootService(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @PathVariable(SERVICE_IDENTIFIER_KEY) String serviceId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!host.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        BrownieHostService brownieHostService = host.getService(serviceId);
        if (brownieHostService == null || !brownieHostService.isRunning())
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        try {
            service.rebootService(host, brownieHostService);
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
        return successResponse();
    }

    @PatchMapping(
            path = "/{" + SERVICE_IDENTIFIER_KEY + "}" + STOP_ENDPOINT
    )
    public String stopService(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @PathVariable(SERVICE_IDENTIFIER_KEY) String serviceId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!host.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        BrownieHostService brownieHostService = host.getService(serviceId);
        if (brownieHostService == null || !brownieHostService.isRunning())
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        try {
            service.stopService(host, brownieHostService);
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
        return successResponse();
    }

    @DeleteMapping(
            path = "/{" + SERVICE_IDENTIFIER_KEY + "}"
    )
    public String removeService(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @PathVariable(SERVICE_IDENTIFIER_KEY) String serviceId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language,
            @RequestParam(
                    value = REMOVE_FROM_THE_HOST_KEY,
                    required = false,
                    defaultValue = "false"
            ) boolean removeFromTheHost
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!host.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        BrownieHostService brownieHostService = host.getService(serviceId);
        if (brownieHostService == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        try {
            service.removeService(host, brownieHostService, removeFromTheHost);
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
        return successResponse();
    }

}
