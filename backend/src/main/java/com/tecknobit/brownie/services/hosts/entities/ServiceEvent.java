package com.tecknobit.brownie.services.hosts.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tecknobit.brownie.services.hostservices.entity.BrownieHostService;
import com.tecknobit.brownie.services.shared.entities.BrownieEvent;
import com.tecknobit.browniecore.enums.ServiceEventType;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import static com.tecknobit.browniecore.ConstantsKt.SERVICE_EVENTS_KEY;

@Entity
@Table(name = SERVICE_EVENTS_KEY)
public class ServiceEvent extends BrownieEvent {

    @Column
    @Enumerated(value = EnumType.STRING)
    private final ServiceEventType type;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnoreProperties(SERVICE_EVENTS_KEY)
    private BrownieHostService service;

    public ServiceEvent() {
        this(null, 0, null, null);
    }

    public ServiceEvent(String id, long eventDate, Object extra, ServiceEventType type) {
        super(id, eventDate, extra);
        this.type = type;
    }

    public ServiceEventType getType() {
        return type;
    }

}
