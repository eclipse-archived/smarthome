/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.service;

/**
 * The {@link WorkerThread} is responsible for providing a thread which can be
 * paused and proceeded.
 * 
 * @author Oliver Libutzki - Initial contribution
 * 
 */
class WorkerThread extends Thread {
    private boolean pause = false;
    private boolean dispose = false;
    private int interval = 5000;

    public WorkerThread(Runnable target, String name, int interval) {
        super(target, name);
        this.interval = interval;
    }

    public void run() {
        while (!dispose) {
            if (!pause) {
                super.run();
                synchronized (this) {
                    try {
                        wait(interval);
                    } catch (InterruptedException e) {

                    }
                }
            } else {
                try {
                    wait();
                } catch (InterruptedException e) {

                }
            }
        }
    }

    void dispose() {
        dispose = true;
        synchronized (this) {
            notifyAll();
        }
    }

    void pause() {
        pause = true;
    }

    void proceed() {
        pause = false;
        synchronized (this) {
            notifyAll();
        }
    }
}