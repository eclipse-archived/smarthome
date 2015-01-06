/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.test

class AsyncResultWrapper<T> {
	private T wrappedObject
	private boolean isSet = false

	def void set(T wrappedObject) {
		this.wrappedObject = wrappedObject
		isSet = true
	}
	
	def getWrappedObject() {
		wrappedObject
	}
	
	def isSet() {
		isSet
	}
	
	def void reset() {
		wrappedObject = null
		isSet = false
	}
}

