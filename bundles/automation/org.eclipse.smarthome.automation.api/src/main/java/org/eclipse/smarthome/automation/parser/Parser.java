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
package org.eclipse.smarthome.automation.parser;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Set;

/**
 * This interface provides opportunity to plug different parsers, for example JSON, GSON or other.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public interface Parser<T> {

    /**
     * This constant can be used to specify the type of the parser.
     * <p>
     * Example : "parser.type" = "parser.module.type";
     * It is used as registration property of the corresponding service.
     */
    public static String PARSER_TYPE = "parser.type";

    /**
     * Following constants are representing the possible values of property {@link #PARSER_TYPE}
     */
    public static String PARSER_MODULE_TYPE = "parser.module.type";
    public static String PARSER_TEMPLATE = "parser.template";
    public static String PARSER_RULE = "parser.rule";

    /**
     * This constant can be used for recognition of which file format is supported by the parser.
     * <p>
     * Example : "format" = "json";
     * It is used as registration property of the corresponding service.
     */
    public static String FORMAT = "format";

    /**
     * Following constant representing the possible value of property {@link #FORMAT} It means that the parser supports
     * json format.
     */
    public static String FORMAT_JSON = "json";

    /**
     * This method is used for loading file with some particular format and parse it to the automation objects.
     *
     * @param reader {@link InputStreamReader} which reads from a file containing automation object representations.
     * @return a set of automation objects. Each object represents the result of parsing of one object.
     * @throws ParsingException is thrown when json format is wrong or there is a semantic error in description of
     *                          the automation objects.
     */
    public Set<T> parse(InputStreamReader reader) throws ParsingException;

    /**
     * This method is used to record automation objects in a file with some particular format.
     *
     * @param dataObjects provides an objects for export.
     * @param writer      is {@link OutputStreamWriter} used to write the automation objects in a file.
     * @throws Exception is thrown when I/O operation has failed or has been interrupted or generating of the text fails
     *                   for some reasons.
     */
    public void serialize(Set<T> dataObjects, OutputStreamWriter writer) throws Exception;

}
