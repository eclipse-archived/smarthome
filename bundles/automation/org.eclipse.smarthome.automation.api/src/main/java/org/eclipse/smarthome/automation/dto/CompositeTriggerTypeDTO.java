/*******************************************************************************
 *
 * Copyright (c) 2016  Bosch Software Innovations GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 *******************************************************************************/
package org.eclipse.smarthome.automation.dto;

import java.util.List;

/**
 * This is a data transfer object that is used to serialize the respective class.
 *
 * @author Ana Dimova - Initial contribution
 *
 */
public class CompositeTriggerTypeDTO extends TriggerTypeDTO {

    public List<TriggerDTO> children;

}
