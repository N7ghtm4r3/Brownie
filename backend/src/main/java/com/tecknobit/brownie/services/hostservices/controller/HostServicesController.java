package com.tecknobit.brownie.services.hostservices.controller;

import com.jcraft.jsch.JSchException;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hostservices.entity.BrownieHostService;
import com.tecknobit.brownie.services.hostservices.services.HostServicesService;
import com.tecknobit.brownie.services.shared.controllers.DefaultBrownieController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody Map<String, Object> payload
    ) {
        BrownieHost brownieHost = getBrownieHostIfAllowed(sessionId, hostId);
        if (brownieHost == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!brownieHost.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        loadJsonHelper(payload);
        String serviceName = jsonHelper.getString(NAME_KEY);
        if (!INSTANCE.isItemNameValid(serviceName))
            return failedResponse(WRONG_NAME_MESSAGE);
        try {
            hostsService.addService(brownieHost, serviceName, jsonHelper);
            return successResponse();
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
    }

    @PatchMapping(
            path = "/{" + SERVICE_IDENTIFIER_KEY + "}"
    )
    public String editService(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @PathVariable(SERVICE_IDENTIFIER_KEY) String serviceId,
            @RequestBody Map<String, Object> payload
    ) {
        BrownieHost brownieHost = getBrownieHostIfAllowed(sessionId, hostId);
        if (brownieHost == null || !brownieHost.hasService(serviceId))
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!brownieHost.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        loadJsonHelper(payload);
        String serviceName = jsonHelper.getString(NAME_KEY);
        if (!INSTANCE.isItemNameValid(serviceName))
            return failedResponse(WRONG_NAME_MESSAGE);
        try {
            hostsService.editService(brownieHost, serviceId, serviceName, jsonHelper);
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
    public <T> T getServices(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @PathVariable(SERVICE_IDENTIFIER_KEY) String serviceId,
            @RequestParam(name = KEYWORDS_KEY, defaultValue = "", required = false) Set<String> keywords,
            @RequestParam(
                    name = STATUSES_KEY,
                    defaultValue = "RUNNING, STOPPED, REBOOTING",
                    required = false
            ) List<String> statuses,
            @RequestParam(name = PAGE_KEY, defaultValue = DEFAULT_PAGE_HEADER_VALUE, required = false) int page,
            @RequestParam(name = PAGE_SIZE_KEY, defaultValue = DEFAULT_PAGE_SIZE_HEADER_VALUE, required = false) int pageSize
    ) {
        BrownieHost brownieHost = getBrownieHostIfAllowed(sessionId, hostId);
        if (brownieHost == null || !brownieHost.hasService(serviceId))
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!brownieHost.isOnline())
            return (T) failedResponse(WRONG_PROCEDURE_MESSAGE);
        return (T) successResponse(service.getServices(hostId, keywords, statuses, page, pageSize));
    }

    @PatchMapping(
            path = "/{" + SERVICE_IDENTIFIER_KEY + "}" + START_ENDPOINT
    )
    public String startService(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @PathVariable(SERVICE_IDENTIFIER_KEY) String serviceId
    ) {
        BrownieHost brownieHost = getBrownieHostIfAllowed(sessionId, hostId);
        if (brownieHost == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!brownieHost.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        BrownieHostService brownieHostService = brownieHost.getService(serviceId);
        if (brownieHostService == null || !brownieHostService.isStopped())
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        try {
            service.startService(brownieHost, brownieHostService);
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
            @PathVariable(SERVICE_IDENTIFIER_KEY) String serviceId
    ) {
        BrownieHost brownieHost = getBrownieHostIfAllowed(sessionId, hostId);
        if (brownieHost == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!brownieHost.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        BrownieHostService brownieHostService = brownieHost.getService(serviceId);
        if (brownieHostService == null || !brownieHostService.isRunning())
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        try {
            service.rebootService(brownieHost, brownieHostService);
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
            @PathVariable(SERVICE_IDENTIFIER_KEY) String serviceId
    ) {
        BrownieHost brownieHost = getBrownieHostIfAllowed(sessionId, hostId);
        if (brownieHost == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!brownieHost.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        BrownieHostService brownieHostService = brownieHost.getService(serviceId);
        if (brownieHostService == null || !brownieHostService.isRunning())
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        try {
            service.stopService(brownieHost, brownieHostService);
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
        return successResponse();
    }

}
