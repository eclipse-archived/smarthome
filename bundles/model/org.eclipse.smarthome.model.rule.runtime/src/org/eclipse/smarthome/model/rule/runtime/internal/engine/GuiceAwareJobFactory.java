package org.eclipse.smarthome.model.rule.runtime.internal.engine;  

import org.quartz.Job;  
import org.quartz.Scheduler;  
import org.quartz.SchedulerException;  
import org.quartz.spi.JobFactory;  
import org.quartz.spi.TriggerFiredBundle;  

import com.google.inject.Inject;  
import com.google.inject.Injector;  

public class GuiceAwareJobFactory implements JobFactory {  

    @Inject  
    private Injector injector;  


    @Override  
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler)  
            throws SchedulerException {  

        return (Job) injector.getInstance(  
            bundle.getJobDetail().getJobClass());  
    }  
}  