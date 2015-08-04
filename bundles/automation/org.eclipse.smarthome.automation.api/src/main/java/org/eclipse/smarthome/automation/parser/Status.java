/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;

/**
 * @author Ana Dimova - Initial Contribution
 */
public class Status {

    public static final int MODULE_TYPE = 1;
    public static final int TEMPLATE = 2;
    public static final int RULE = 3;

    private Logger log;
    private Object result;
    private String id;
    private int type;

    private Map<String, Throwable> errorsMap;

    public Status(Logger log, int type, String id) {
        this.log = log;
        init(type, id);
    }

    public Map<String, Throwable> getErrors() {
        return errorsMap;
    }

    public boolean hasErrors() {
        if (errorsMap == null)
            return false;
        return true;
    }

    public Object getResult() {
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case MODULE_TYPE:
                sb.append("[Module Type");
                break;
            case TEMPLATE:
                sb.append("[Template");
                break;
            case RULE:
                sb.append("[Rule");
                break;
            default:
                break;
        }
        if (id != null)
            sb.append(id);
        sb.append("] Status: ");
        if (errorsMap == null) {
            sb.append("SUCCESS\n");
        } else {
            sb.append("FAIL");
            sb.append(" {\n");
            Iterator i = errorsMap.keySet().iterator();
            while (i.hasNext()) {
                if (i.hasNext())
                    sb.append("    ").append((String) i.next()).append(",\n");
                else
                    sb.append("    ").append((String) i.next()).append("\n");
            }
            sb.append("}\n");
        }
        return sb.toString();
    }

    public void init(int type, String id) {
        this.type = type;
        if (id != null)
            this.id = " UID : " + id;
    }

    public void error(String msg, Throwable t) {
        String id = "";
        if (this.id != null)
            id = this.id;
        if (errorsMap == null)
            errorsMap = new HashMap<String, Throwable>();
        switch (type) {
            case MODULE_TYPE:
                msg = "[Module Type " + id + "] " + msg;
                break;
            case TEMPLATE:
                msg = "[Template " + id + "] " + msg;
                break;
            case RULE:
                msg = "[Rule " + id + "] " + msg;
                break;
            default:
                break;
        }
        log.error(msg, t);
        errorsMap.put(msg, t);
    }

    public void success(Object obj) {
        result = obj;
    }
}
