/*
 * Copyright 2011 Proofpoint, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.proofpoint.event.monitor;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import org.weakref.jmx.Managed;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

// todo switch to platform CounterStat once tick method is exposed
@Beta
public class CounterStat
{
    private final AtomicLong count = new AtomicLong(0);
    private final EWMA oneMinute = EWMA.oneMinuteEWMA();
    private final EWMA fiveMinute = EWMA.fiveMinuteEWMA();
    private final EWMA fifteenMinute = EWMA.fifteenMinuteEWMA();
    private final ScheduledExecutorService executor;
    private volatile ScheduledFuture<?> future;

    public CounterStat(ScheduledExecutorService executor)
    {
        this.executor = executor;
    }

    @PostConstruct
    public synchronized void start()
    {
        if (future == null) {
            future = executor.scheduleAtFixedRate(new Runnable()
            {
                @Override
                public void run()
                {
                    tick();
                }
            }, 5, 5, TimeUnit.SECONDS);
        }
    }

    @PreDestroy
    public synchronized void stop()
    {
        if (future != null) {
            future.cancel(false);
            future = null;
        }
    }

    @VisibleForTesting
    public void tick()
    {
        oneMinute.tick();
        fiveMinute.tick();
        fifteenMinute.tick();
    }

    public void update(long count)
    {
        oneMinute.update(count);
        fiveMinute.update(count);
        fifteenMinute.update(count);
        this.count.addAndGet(count);
    }

    @Managed
    public long getCount()
    {
        return count.get();
    }

    @Managed
    public double getOneMinuteRate()
    {
        return oneMinute.rate(TimeUnit.SECONDS);
    }

    @Managed
    public double getFiveMinuteRate()
    {
        return fiveMinute.rate(TimeUnit.SECONDS);
    }

    @Managed
    public double getFifteenMinuteRate()
    {
        return fifteenMinute.rate(TimeUnit.SECONDS);
    }
}
