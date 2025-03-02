package com.tecknobit.brownie.services.hosts.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tecknobit.browniecore.enums.HostEventType;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import static com.tecknobit.browniecore.ConstantsKt.EVENT_DATE_KEY;
import static com.tecknobit.browniecore.ConstantsKt.HOST_EVENTS_KEY;

@Entity
@Table(name = HOST_EVENTS_KEY)
public class HostHistoryEvent extends EquinoxItem {

    @Enumerated(EnumType.STRING)
    private final HostEventType type;

    @Column(name = EVENT_DATE_KEY)
    private final long eventDate;

    @Column
    private final String extra;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnoreProperties(HOST_EVENTS_KEY)
    private BrownieHost host;

    public HostHistoryEvent() {
        this(null, null, 0, null);
    }

    public HostHistoryEvent(String id, HostEventType type, long eventDate, Object extra) {
        super(id);
        this.type = type;
        this.eventDate = eventDate;
        String extraValue = null;
        if (extra != null)
            extraValue = extra.toString();
        this.extra = extraValue;
    }

    public HostEventType getType() {
        return type;
    }

    @JsonGetter(EVENT_DATE_KEY)
    public long getEventDate() {
        return eventDate;
    }

    public Object getExtra() {
        return extra;
    }

}
