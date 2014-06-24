/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.thing.valueconverter;

import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.STRINGValueConverter;
import org.eclipse.xtext.nodemodel.INode;

import com.google.inject.Inject;

public class ValueTypeToStringConverter implements IValueConverter<String> {

	@Inject
	private STRINGValueConverter stringValueConverter;

	@Override
	public String toValue(String string, INode node) throws ValueConverterException {
		if (string == null)
			return null;
		if (string.startsWith("\"") && string.endsWith("\"")) 
			return stringValueConverter.toValue(string, node);
		return string;
	}

	@Override
	public String toString(String value) throws ValueConverterException {
		return stringValueConverter.toString(value);
	}
}
