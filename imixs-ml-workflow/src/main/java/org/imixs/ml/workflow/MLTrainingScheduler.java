/*  
 *  Imixs-Workflow 
 *  
 *  Copyright (C) 2001-2020 Imixs Software Solutions GmbH,  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Project: 
 *      https://www.imixs.org
 *      https://github.com/imixs/imixs-workflow
 *  
 *  Contributors:  
 *      Imixs Software Solutions GmbH - Project Management
 *      Ralph Soika - Software Developer
 */

package org.imixs.ml.workflow;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RunAs;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.imixs.workflow.engine.EventLogService;
import org.imixs.workflow.engine.jpa.EventLog;

/**
 * The MLTrainingScheduler starts a scheduler service to process ml training
 * events in an asynchronous way by calling the MLService.
 * 
 * @see MLService
 * @version 1.0
 * @author rsoika
 *
 */
@DeclareRoles({ "org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@RunAs("org.imixs.ACCESSLEVEL.MANAGERACCESS")
@Startup
@Singleton
@LocalBean
public class MLTrainingScheduler {

    public static final String ML_TRAINING_SCHEDULER_ENABLED = "ml.trainng.scheduler.enabled";
    public static final String ML_TRAINING_SCHEDULER_INTERVAL = "ml.training.scheduler.interval";
    public static final String ML_TRAINING_SCHEDULER_INITIALDELAY = "ml.training.scheduler.initialdelay";

    // enabled
    @Inject
    @ConfigProperty(name = ML_TRAINING_SCHEDULER_ENABLED, defaultValue = "false")
    boolean enabled;

    // timeout interval in ms - default every 10 minutes
    @Inject
    @ConfigProperty(name = ML_TRAINING_SCHEDULER_INTERVAL, defaultValue = "600000")
    long interval;

    // initial delay in ms - default 1 min
    @Inject
    @ConfigProperty(name = ML_TRAINING_SCHEDULER_INITIALDELAY, defaultValue = "60000")
    long initialDelay;

    private static Logger logger = Logger.getLogger(MLTrainingScheduler.class.getName());

    @Resource
    javax.ejb.TimerService timerService;

    @Inject
    MLService mlService;

    @Inject
    EventLogService eventLogService;

    @PostConstruct
    public void init() {
        if (enabled) {
            logger.info(
                    "Starting MLTrainingScheduler - initalDelay=" + initialDelay + "  inverval=" + interval + " ....");

            // Registering a non-persistent Timer Service.
            final TimerConfig timerConfig = new TimerConfig();
            timerConfig.setInfo("Imixs-Workflow MLTrainingScheduler");
            timerConfig.setPersistent(false);
            timerService.createIntervalTimer(initialDelay, interval, timerConfig);
        }
    }

    /**
     * The method reads the eventLog for training events and delegates the training
     * to the stateless ejb MLService
     *
     */
    @Timeout
    public void run(Timer timer) {
        long l = System.currentTimeMillis();
        boolean debug = logger.isLoggable(Level.FINE);

        // test for new event log entries by timeout...
        List<EventLog> events = eventLogService.findEventsByTimeout(10, MLService.EVENTLOG_TOPIC_TRAINING);

        if (events.size()>0) {
            logger.finest("...starting ml training. " + events.size() + " new MLTrainingEvents found....");
        }
        for (EventLog eventLogEntry : events) {
            // start training
            mlService.trainWorkitem(eventLogEntry.getRef());
        }

        if (debug) {
            logger.fine("..." + events.size() + " MLTrainingEvents processed in " + (System.currentTimeMillis() - l)
                    + "ms");
        }
    }

}