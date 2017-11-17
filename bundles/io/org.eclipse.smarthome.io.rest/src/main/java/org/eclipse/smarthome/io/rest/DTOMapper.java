/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest;

import java.util.stream.Stream;

/**
 * Utilities for mapping/transforming DTOs.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public interface DTOMapper {

    <T> Stream<T> limitToFields(Stream<T> itemStream, String fields);

}
