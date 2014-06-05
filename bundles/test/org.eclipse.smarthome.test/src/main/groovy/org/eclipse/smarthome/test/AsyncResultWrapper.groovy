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
}

