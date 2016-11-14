/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.fsinternetradio.test;

import java.io.IOException
import java.lang.reflect.WeakCache.Value;

import javax.servlet.ServletException
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.eclipse.jetty.http.HttpStatus
import org.eclipse.smarthome.binding.fsinternetradio.internal.radio.FrontierSiliconRadio
import org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID;;

public class RadioServiceMock extends HttpServlet {

	def MOCK_RADIO_PIN = "1234"

	def REQUEST_SET_POWER = "/" + FrontierSiliconRadio.REQUEST_SET_POWER
	def REQUEST_GET_POWER = "/" + FrontierSiliconRadio.REQUEST_GET_POWER
	def REQUEST_GET_MODE = "/" + FrontierSiliconRadio.REQUEST_GET_MODE
	def REQUEST_SET_MODE = "/" + FrontierSiliconRadio.REQUEST_SET_MODE
	def REQUEST_SET_VOLUME = "/" + FrontierSiliconRadio.REQUEST_SET_VOLUME
	def REQUEST_GET_VOLUME = "/" + FrontierSiliconRadio.REQUEST_GET_VOLUME
	def REQUEST_SET_MUTE = "/" + FrontierSiliconRadio.REQUEST_SET_MUTE
	def REQUEST_GET_MUTE = "/" + FrontierSiliconRadio.REQUEST_GET_MUTE
	def REQUEST_SET_PRESET = "/" + FrontierSiliconRadio.REQUEST_SET_PRESET
	def REQUEST_SET_PRESET_ACTION = "/" + FrontierSiliconRadio.REQUEST_SET_PRESET_ACTION
	def REQUEST_GET_PLAY_INFO_TEXT = "/" + FrontierSiliconRadio.REQUEST_GET_PLAY_INFO_TEXT
	def REQUEST_GET_PLAY_INFO_NAME = "/" + FrontierSiliconRadio.REQUEST_GET_PLAY_INFO_NAME

	def httpStatus

	static def tagToReturn
	static def responseToReturn

	static boolean isInvalidResponseExpected
	static boolean isInvalidValueExpected
	static boolean isOKAnswerExpected = true

	private static def powerValue
	private static def powerTag

	private static def muteValue
	private static def muteTag

	private def absoluteVolumeValue
	private def absoluteVolumeTag

	private def modeValue
	private def modeTag

	private def radioStation

	public def getRadioStation() {
		return radioStation;
	}

	public void setRadioStation(java.lang.Object radioStation) {
		this.radioStation = radioStation;
	}

	/*
	 * For the purposes of the tests it is assumed that the current station and the additional information
	 * are always the same (random_station and additional_info)
	 */
	private def playInfoNameValue = "random_station"
	private def playInfoNameTag = makeC8_arrayTag(playInfoNameValue)

	private def playInfoTextValue = "additional_info"
	private def playInfoTextTag = makeC8_arrayTag(playInfoTextValue)



	public RadioServiceMock() {
		super()
		this.httpStatus = HttpStatus.OK_200
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String pin = request.getParameter("pin")
		if(!pin.equals(MOCK_RADIO_PIN)) {
			response.setStatus(HttpStatus.FORBIDDEN_403)
		} else if (!isOKAnswerExpected){
			response.setStatus(HttpStatus.NOT_FOUND_404)
		} else {
			response.setStatus(HttpStatus.OK_200)
			response.setContentType("text/xml")
			String commandString =  request.getPathInfo()
			switch (commandString) {

				case (REQUEST_SET_POWER):
					if(isInvalidValueExpected) powerValue = null
					else powerValue = request.getParameter("value")

				case (REQUEST_GET_POWER):
					powerTag = makeU8Tag(powerValue)
					tagToReturn = powerTag
					break

				case (REQUEST_SET_MUTE):
					if(isInvalidValueExpected) muteValue = null
					else muteValue = request.getParameter("value")

				case (REQUEST_GET_MUTE):
					muteTag = makeU8Tag(muteValue)
					tagToReturn = muteTag
					break

				case (REQUEST_SET_MODE):
					if(isInvalidValueExpected) {
						modeValue = null
					} else {
						modeValue = request.getParameter("value")
					}

				case (REQUEST_GET_MODE):
					modeTag = makeU32Tag(modeValue)
					tagToReturn = modeTag
					break

				case (REQUEST_SET_VOLUME) :
					if(isInvalidValueExpected) {
						absoluteVolumeValue = null
					} else {
						absoluteVolumeValue = request.getParameter("value")
					}

				case (REQUEST_GET_VOLUME) :
					absoluteVolumeTag = makeU8Tag(absoluteVolumeValue)
					tagToReturn = absoluteVolumeTag
					break

				case (REQUEST_SET_PRESET_ACTION) :
					def station = request.getParameter("value")
					setRadioStation(station)
					break

				case (REQUEST_GET_PLAY_INFO_NAME) :
					tagToReturn = playInfoNameTag
					break

				case (REQUEST_GET_PLAY_INFO_TEXT) :
					tagToReturn = playInfoTextTag
					break

				default : tagToReturn=""
			}

			if(isInvalidResponseExpected) {
				responseToReturn = makeInvalidXMLResponse()
			} else {
				responseToReturn = makeValidXMLResponse()
			}
			PrintWriter out = response.getWriter()
			out.print(responseToReturn)
		}
	}

	private static def makeU8Tag(def value) {
		return "<value><u8>${value}</u8></value>"
	}

	private static def makeU32Tag(def value) {
		return "<value><u32>${value}</u32></value>"
	}

	private static def makeC8_arrayTag(def value) {
		return "<value><c8_array>${value}</c8_array></value>"
	}

	private static String makeValidXMLResponse () {
		return  """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
		 <pre>
		 <sessionId>111</sessionId>
	     <xmp>
		 <fsapiResponse><status>FS_OK</status>${tagToReturn}</fsapiResponse>
		 </xmp>
		 </pre>"""
	}

	private static String makeInvalidXMLResponse () {
		return  """<--xmmmmt version=\"1.0\" encoding=\"UTF-8\"?>
		 <pre>
		 <sessionId>111</sessionId>
	     <xmp>
		 <fsapiResponse><status>FS_OK</status>${tagToReturn}</fsapiResponse>
		 </xmp>
		 </pre>"""
	}
}

