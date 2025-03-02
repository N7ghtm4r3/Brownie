package com.tecknobit.brownie.services.shared.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import com.tecknobit.equinoxcore.annotations.Structure;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import static com.tecknobit.browniecore.ConstantsKt.EVENT_DATE_KEY;

@Structure
@MappedSuperclass
public abstract class BrownieEvent extends EquinoxItem {

    @Column(name = EVENT_DATE_KEY)
    protected final long eventDate;

    @Column
    protected final String extra;

    public BrownieEvent() {
        this(null, 0, null);
    }

    public BrownieEvent(String id, long eventDate, Object extra) {
        super(id);
        this.eventDate = eventDate;
        String extraValue = null;
        if (extra != null)
            extraValue = extra.toString();
        this.extra = extraValue;
    }

    @JsonGetter(EVENT_DATE_KEY)
    public long getEventDate() {
        return eventDate;
    }

    public Object getExtra() {
        return extra;
    }

}
