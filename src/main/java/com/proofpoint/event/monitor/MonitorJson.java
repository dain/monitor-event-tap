package com.proofpoint.event.monitor;


import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.proofpoint.event.monitor.EventPredicates.EventTypeEventPredicate;
import com.proofpoint.event.monitor.EventPredicates.StartsWithEventPredicate;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MonitorJson
{
    private final String eventType;
    private final FilterJson filter;
    private final double minFiveMinuteRate;

    @JsonCreator
    public MonitorJson(
            @JsonProperty("eventType") String eventType,
            @JsonProperty("filter") FilterJson filter,
            @JsonProperty("minFiveMinuteRate") double minFiveMinuteRate)
    {
        this.eventType = eventType;
        if (filter != null) {
            this.filter = filter;
        }
        else {
            this.filter = new FilterJson(ImmutableMap.<String, String>of());
        }
        this.minFiveMinuteRate = minFiveMinuteRate;
    }

    @JsonProperty
    public String getEventType()
    {
        return eventType;
    }

    @JsonProperty
    public FilterJson getFilter()
    {
        return filter;
    }

    public Predicate<Event> getEventPredicate()
    {
        return Predicates.and(new EventTypeEventPredicate(eventType), filter.toPredicate());
    }

    @JsonProperty
    public double getMinFiveMinuteRate()
    {
        return minFiveMinuteRate;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("MonitorJson");
        sb.append("{eventType='").append(eventType).append('\'');
        sb.append(", filter=").append(filter);
        sb.append(", minFiveMinuteRate=").append(minFiveMinuteRate);
        sb.append('}');
        return sb.toString();
    }

    public static class FilterJson
    {
        private final Map<String, String> startsWith;

        @JsonCreator
        public FilterJson(
                @JsonProperty("startsWith") Map<String, String> startsWith
        )
        {
            this.startsWith = ImmutableMap.copyOf(startsWith);
        }

        @JsonProperty
        public Map<String, String> getStartsWith()
        {
            return startsWith;
        }

        public Predicate<Event> toPredicate()
        {
            List<Predicate<Event>> and = new ArrayList<Predicate<Event>>();
            for (final Entry<String, String> startWithEntry : startsWith.entrySet()) {
                and.add(new StartsWithEventPredicate(startWithEntry.getKey(), startWithEntry.getValue()));
            }
            if (and.isEmpty()) {
                return Predicates.alwaysTrue();
            }
            return Predicates.and(and);
        }


        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("FilterJson");
            sb.append("{startsWith=").append(startsWith);
            sb.append('}');
            return sb.toString();
        }
    }
}