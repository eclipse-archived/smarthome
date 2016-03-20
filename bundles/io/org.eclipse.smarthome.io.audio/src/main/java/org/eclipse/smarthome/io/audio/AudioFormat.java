/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.audio;

/**
 * An audio format definition
 *
 * @author Harald Kuhn - Initial API
 * @author Kelly Davis - Modified to match discussion in #584
 */
public class AudioFormat {

    /**
     * Codec
     */
    private final String codec;

    /**
     * Container
     */
    private final String container;

    /**
     * Big endian or little endian
     */
    private final Boolean bigEndian;

    /**
     * Bit depth
     *
     * @see <a href="http://bit.ly/1OTydad">Bit Depth</a>
     */
    private final Integer bitDepth;

    /**
     * Bit rate
     *
     * @see <a href="http://bit.ly/1OTy5rk">Bit Rate</a>
     */
    private final Integer bitRate;

    /**
     * Sample frequency
     */
    private final Long frequency;

   /**
    * Constructs an instance with the specified peoperties.
    *
    * Note that any properties that are null indicate that
    * the corresponding AudioFormat allows any value for
    * the property.
    *
    * Concretely this implies that if, for example, one
    * passed null for the value of frequency, this would
    * mean the created AudioFormat allowed for any valid
    * frequency.
    *
    * @param container The container for the audio
    * @param codec The audio codec
    * @param bigEndian If the audo data is big endian
    * @param bitDepth The bit depth of the audo data
    * @param bitRate The bit rate of the audio
    * @param frequency The frequency at which the audio was sampled
    */
    public AudioFormat(String container, String codec, Boolean bigEndian,
                       Integer bitDepth, Integer bitRate, Long frequency) {
        super();
        this.container = container;
        this.codec = codec;
        this.bigEndian = bigEndian;
        this.bitDepth = bitDepth;
        this.bitRate = bitRate;
        this.frequency = frequency;
    }

    /**
     * Gets codec
     *
     * @return The codec
     */
    public String getCodec() {
        return codec;
    }

    /**
     * Gets container
     *
     * @return The container
     */
    public String getContainer() {
        return container;
    }

    /**
     * Is big endian?
     *
     * @return If format is big endian
     */
    public Boolean isBigEndian() {
        return bigEndian;
    }

    /**
     * Gets bit depth
     *
     * @see <a href="http://bit.ly/1OTydad">Bit Depth</a>
     * @return Bit depth
     */
    public Integer getBitDepth() {
        return bitDepth;
    }

    /**
     * Gets bit rate
     *
     * @see <a href="http://bit.ly/1OTy5rk">Bit Rate</a>
     * @return Bit rate
     */
    public Integer getBitRate() {
        return bitRate;
    }

    /**
     * Gets frequency
     *
     * @return The frequency
     */
    public Long getFrequency() {
        return frequency;
    }

   /**
    * Determines if the passed AudioFormat is compatable with this AudioFormat.
    *
    * This AudioFormat is compatible with the passed AudioFormat if both have
    * the same value for all non-null members of this instance.
    */
    boolean isCompatible(AudioFormat audioFormat) {
    	if(audioFormat == null) {
    		return false;
    	}
        if ((null != getContainer()) && (getContainer() != audioFormat.getContainer())) {
            return false;
        }
        if ((null != getCodec()) && (getCodec() != audioFormat.getCodec())) {
            return false;
        }
        if ((null != isBigEndian()) && (isBigEndian() != audioFormat.isBigEndian())) {
            return false;
        }
        if ((null != getBitDepth()) && (getBitDepth() != audioFormat.getBitDepth())) {
            return false;
        }
        if ((null != getBitRate()) && (getBitRate() != audioFormat.getBitRate())) {
            return false;
        }
        if ((null != getFrequency()) && (getFrequency() != audioFormat.getFrequency())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AudioFormat) {
            AudioFormat format = (AudioFormat) obj;
            if (format.getCodec() != getCodec()) {
                return false;
            }
            if (format.getContainer() != getContainer()) {
                return false;
            }
            if (format.isBigEndian() != isBigEndian()) {
                return false;
            }
            if (format.getBitDepth() != getBitDepth()) {
                return false;
            }
            if (format.getBitRate() != getBitRate()) {
                return false;
            }
            if (format.getFrequency() != getFrequency()) {
                return false;
            }
            return true;
        }
        return super.equals(obj);
    }
}
