package org.eclipse.smarthome.core.scheduler;

import static org.junit.Assert.*;

import java.text.ParseException;

import org.eclipse.smarthome.core.scheduler.ExpressionThreadPoolManager.ExpressionThreadPoolExecutor;
import org.junit.Ignore;
import org.junit.Test;

public class ExpressionThreadPoolExecutorTest {

    private ExpressionThreadPoolExecutor scheduler = ExpressionThreadPoolManager.getExpressionScheduledPool("test");
    protected boolean success;

    @Test
    @Ignore
    public void testScheduleAndRemove() throws ParseException, InterruptedException {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                success = true;
            }
        };

        success = false;
        CronExpression expression = new CronExpression("0/1 * * * * ?");
        scheduler.schedule(runnable, expression);
        Thread.sleep(1500);
        assertTrue(success);

        // FIXME: We have to use the method with the expression as a parameter. If we do
        // scheduler.remove(runnable) instead, the task is never removed (as it is kept in the "scheduled" set).
        boolean removed = scheduler.remove(expression);

        // FIXME: If TRACE is enabled and thus the execution of the task takes more than 2 seconds, removed is false
        assertTrue(removed);

        success = false;
        Thread.sleep(1500);
        assertFalse(success);

        assertEquals(0, scheduler.getQueue().size());

        // FIXME: What is this task that remains in the queue here? Shouldn't the queue be empty?
        // assertEquals(0, scheduler.getQueue().size());
    }

}
