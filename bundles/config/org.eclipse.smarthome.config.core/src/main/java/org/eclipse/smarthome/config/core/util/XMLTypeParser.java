/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;


// TODO: Improve performance.
/**
 * The {@link XMLTypeParser} parses an XML file and unmarshalls it to its according
 * Java object representation.
 *
 * @author Michael Grammling - Initial Contribution (Draft API)
 */
public class XMLTypeParser {

    private Schema xmlSchema;
    private JAXBContext jaxbContext;


    public XMLTypeParser(URL xsdFileURL, JAXBContext jaxbContext)
            throws IOException, SAXException, JAXBException {

        this.xmlSchema = createSchema(xsdFileURL);
        this.jaxbContext = jaxbContext;
    }

    private Schema createSchema(URL xsdFileURL) throws IOException, SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaFile = new StreamSource(xsdFileURL.openStream());
        Schema schema = schemaFactory.newSchema(schemaFile);

        return schema;
    }

    public final Object parse(URL xmlURL) throws ParserConfigurationException, SAXException,
            IOException, IllegalArgumentException, JAXBException, XMLStreamException {

        ByteArrayInputStream byteArrayInputStream;
        try (InputStream inputStream = xmlURL.openStream()) {
            byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(inputStream));
        }

        Validator validator = this.xmlSchema.newValidator();
        validator.validate(new StreamSource(byteArrayInputStream));

        byteArrayInputStream.reset();

        Object xmlObject = unmarshal(byteArrayInputStream);

        byteArrayInputStream.close();

        return xmlObject;
    }

    private Object unmarshal(ByteArrayInputStream byteArrayInputStream)
            throws JAXBException, IOException, XMLStreamException {

        StreamSource source = new StreamSource(byteArrayInputStream);

        Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(this.xmlSchema);

        return unmarshaller.unmarshal(source);
    }

}
