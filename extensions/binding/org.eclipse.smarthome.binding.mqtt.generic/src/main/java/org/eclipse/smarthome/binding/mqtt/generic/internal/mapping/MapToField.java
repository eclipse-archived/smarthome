/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.mqtt.generic.internal.mapping;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a class field if the MQTT topic value needs to be translated first before assigned to that field.
 *
 * <p>
 * Example: The MQTT topic is my-example/testname with value "abc" and my-example/values with value "abc,def". The
 * corresponding bean class looks like this:
 * </p>
 *
 * <pre>
 * class MyExample {
 *     enum Testnames {
 *         abc_
 *     };
 *
 *     &#64;MapToField(suffix = "_")
 *     Testnames testname;
 *
 *     &#64;MapToField(splitCharacter = ",")
 *     String[] values;
 * }
 * </pre>
 *
 * @author David Graeff - Initial contribution
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(FIELD)
public @interface MapToField {
    String suffix() default "";

    String prefix() default "";

    String splitCharacter() default "";
}
