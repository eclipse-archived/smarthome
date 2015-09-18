package org.eclipse.smarthome.notification.consumer.filter;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RateFilter implements EventFilter {

    public final static String TYPE = "Rate";

    private final Logger logger = LoggerFactory.getLogger(RateFilter.class);

    protected TokenBucket bucket = null;

    public RateFilter(List<String> options) {

        String size = options.get(0);
        String interval = options.get(1);
        String intervalUnit = options.get(2);

        if (size != null && interval != null && interval != null) {
            bucket = TokenBuckets.builder().withCapacity(Long.valueOf(size)).withFixedIntervalRefillStrategy(
                    Long.valueOf(size), Long.valueOf(interval), TimeUnit.valueOf(intervalUnit)).build();
        }
    }

    @Override
    public boolean apply(Event event) {
        if (bucket != null) {
            logger.debug("The rate filter bucket contains '{}' tokens, and next refill is after '{}' minutes",
                    bucket.getNumTokens(), bucket.getDurationUntilNextRefill(TimeUnit.MINUTES));
            if (bucket.tryConsume(1)) {
                return true;
            }
        }
        return false;
    }

}
