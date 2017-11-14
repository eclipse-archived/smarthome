/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.scheduler;

import org.eclipse.smarthome.core.scheduler.ExpressionThreadPoolManager.ExpressionThreadPoolExecutor;

/**
 * Wrapper for scheduled {@link Runnable}s.
 *
 * It protects the {@link ExpressionThreadPoolExecutor} from runnables implementing
 * {@link #hashCode()} and {@link #equals(Object)} in their own way by falling back on
 * the delegate's identity hash code.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
class RunnableWrapper implements Runnable {

    private final Runnable delegate;
    private int code;

    RunnableWrapper(Runnable delegate) {
        this.delegate = delegate;
        this.code = System.identityHashCode(delegate);
    }

    @Override
    public void run() {
        delegate.run();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + code;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RunnableWrapper other = (RunnableWrapper) obj;
        if (code != other.code) {
            return false;
        }
        return true;
    }

}
