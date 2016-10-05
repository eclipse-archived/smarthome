/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.voice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioManager;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioSource;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.voice.internal.DialogProcessor;
import org.eclipse.smarthome.core.voice.text.HumanLanguageInterpreter;
import org.eclipse.smarthome.core.voice.text.InterpretationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service provides functionality around voice services and is the central service to be used directly by others.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class VoiceManager {

    // the default keyword to use if no other is configured
    private static final String DEFAULT_KEYWORD = "Wakeup";

    // constants for the configuration properties
    private static final String CONFIG_KEYWORD = "keyword";
    private static final String CONFIG_DEFAULT_HLI = "defaultHLI";
    private static final String CONFIG_DEFAULT_KS = "defaultKS";
    private static final String CONFIG_DEFAULT_STT = "defaultSTT";
    private static final String CONFIG_DEFAULT_TTS = "defaultTTS";
    private static final String CONFIG_PREFIX_DEFAULT_VOICE = "defaultVoice.";

    private final Logger logger = LoggerFactory.getLogger(VoiceManager.class);

    // service maps
    private Map<String, KSService> ksServices = new HashMap<>();
    private Map<String, STTService> sttServices = new HashMap<>();
    private Map<String, TTSService> ttsServices = new HashMap<>();
    private Map<String, HumanLanguageInterpreter> humanLanguageInterpreters = new HashMap<>();

    private LocaleProvider localeProvider = null;

    /**
     * default settings filled through the service configuration
     */
    private String keyword = DEFAULT_KEYWORD;
    private String defaultTTS = null;
    private String defaultSTT = null;
    private String defaultKS = null;
    private String defaultHLI = null;
    private Map<String, String> defaultVoices = new HashMap<>();
    private AudioManager audioManager;

    protected void activate(Map<String, Object> config) {
        modified(config);
    }

    protected void deactivate() {
    }

    protected void modified(Map<String, Object> config) {
        if (config != null) {
            this.keyword = config.containsKey(CONFIG_KEYWORD) ? config.get(CONFIG_KEYWORD).toString() : DEFAULT_KEYWORD;
            this.defaultTTS = config.containsKey(CONFIG_DEFAULT_TTS) ? config.get(CONFIG_DEFAULT_TTS).toString() : null;
            this.defaultSTT = config.containsKey(CONFIG_DEFAULT_STT) ? config.get(CONFIG_DEFAULT_STT).toString() : null;
            this.defaultKS = config.containsKey(CONFIG_DEFAULT_KS) ? config.get(CONFIG_DEFAULT_KS).toString() : null;
            this.defaultHLI = config.containsKey(CONFIG_DEFAULT_HLI) ? config.get(CONFIG_DEFAULT_HLI).toString() : null;

            for (String key : config.keySet()) {
                if (key.startsWith(CONFIG_PREFIX_DEFAULT_VOICE)) {
                    String tts = key.substring(CONFIG_PREFIX_DEFAULT_VOICE.length());
                    defaultVoices.put(tts, config.get(key).toString());
                }
            }
        }
    }

    /**
     * Speaks the passed string using the default TTS service and default audio sink.
     *
     * @param text The text to say
     */
    public void say(String text) {
        say(text, null);
    }

    /**
     * Speaks the passed string using the provided voiceId and the default audio sink.
     * If the voiceId is fully qualified (i.e. with a tts prefix), the according TTS service will be used, otherwise the
     * voiceId is assumed to be available on the default TTS service.
     *
     * @param text The text to say
     * @param voiceId The id of the voice to use (either with or without prefix)
     */
    public void say(String text, String voiceId) {
        say(text, voiceId, null);
    }

    /**
     * Speaks the passed string using the provided voiceId and the given audio sink.
     * If the voiceId is fully qualified (i.e. with a tts prefix), the according TTS service will be used, otherwise the
     * voiceId is assumed to be available on the default TTS service.
     *
     * @param text The text to say
     * @param voiceId The id of the voice to use (either with or without prefix) or null
     * @param sinkId The id of the audio sink to use or null
     */
    public void say(String text, String voiceId, String sinkId) {
        try {
            TTSService tts = null;
            Voice voice = null;
            if (voiceId == null) {
                tts = getTTS();
                if (tts != null) {
                    voice = getPreferredVoice(tts.getAvailableVoices());
                }
            } else if (voiceId.contains(":")) {
                // it is a fully qualified unique id
                String[] segments = voiceId.split(":");
                tts = ttsServices.get(segments[0]);
                voice = getVoice(tts.getAvailableVoices(), segments[1]);
            } else {
                // voiceId is not fully qualified
                tts = getTTS();
                voice = getVoice(tts.getAvailableVoices(), voiceId);
            }
            if (voice == null) {
                throw new TTSException(
                        "Unable to find a voice for language " + localeProvider.getLocale().getLanguage());
            }
            if (tts == null) {
                throw new TTSException("No TTS service can be found for voice " + voiceId);
            }
            Set<AudioFormat> audioFormats = tts.getSupportedFormats();
            AudioSink sink = null;
            if (sinkId == null) {
                sink = audioManager.getSink();
            } else {
                sink = audioManager.getSink(sinkId);
            }
            if (sink != null) {
                AudioFormat audioFormat = getBestMatch(audioFormats, sink.getSupportedFormats());
                if (audioFormat != null) {
                    AudioStream audioStream = tts.synthesize(text, voice, audioFormat);

                    try {
                        sink.process(audioStream);
                    } catch (UnsupportedAudioFormatException e) {
                        logger.error("Error saying '{}': {}", text, e.getMessage());
                    }
                } else {
                    logger.warn("No compatible audio format found for TTS '{}' and sink '{}'", tts.getId(),
                            sink.getId());
                }
            }
        } catch (TTSException e) {
            logger.error("Error saying '{}': {}", text, e.getMessage());
        }
    }

    /**
     * Interprets the passed string using the default services for HLI locale.
     *
     * @param text The text to interpret
     * @throws InterpretationException
     */
    public void interpret(String text) throws InterpretationException {
        getHLI().interpret(localeProvider.getLocale(), text);
    }

    private Voice getVoice(Set<Voice> voices, String id) {
        for (Voice voice : voices) {
            if (voice.getUID().endsWith(":" + id)) {
                return voice;
            }
        }
        return null;
    }

    /**
     * Gets the first concrete AudioFormat in the passed set or a preferred one
     * based on 16bit, 16KHz, big endian default
     *
     * @param audioFormats The AudioFormats from which to choose
     * @return The preferred AudioFormat. A passed concrete format is preferred adding
     *         default values to an abstract AudioFormat in the passed Set.
     */
    public static AudioFormat getPreferredFormat(Set<AudioFormat> audioFormats) {
        // Return the first concrete AudioFormat found
        for (AudioFormat currentAudioFormat : audioFormats) {
            // Check if currentAudioFormat is abstract
            if (null == currentAudioFormat.getCodec()) {
                continue;
            }
            if (null == currentAudioFormat.getContainer()) {
                continue;
            }
            if (null == currentAudioFormat.isBigEndian()) {
                continue;
            }
            if (null == currentAudioFormat.getBitDepth()) {
                continue;
            }
            if (null == currentAudioFormat.getBitRate()) {
                continue;
            }
            if (null == currentAudioFormat.getFrequency()) {
                continue;
            }

            // Prefer WAVE container
            if (!currentAudioFormat.getContainer().equals("WAVE")) {
                continue;
            }

            // As currentAudioFormat is concrete, use it
            return currentAudioFormat;
        }

        // There's no concrete AudioFormat so we must create one
        for (AudioFormat currentAudioFormat : audioFormats) {
            // Define AudioFormat to return
            AudioFormat format = currentAudioFormat;

            // Not all Codecs and containers can be supported
            if (null == format.getCodec()) {
                continue;
            }
            if (null == format.getContainer()) {
                continue;
            }

            // Prefer WAVE container
            if (!format.getContainer().equals(AudioFormat.CONTAINER_WAVE)) {
                continue;
            }

            // If required set BigEndian, BitDepth, BitRate, and Frequency to default values
            if (null == format.isBigEndian()) {
                format = new AudioFormat(format.getContainer(), format.getCodec(), new Boolean(true),
                        format.getBitDepth(), format.getBitRate(), format.getFrequency());
            }
            if (null == format.getBitDepth() || null == format.getBitRate() || null == format.getFrequency()) {
                // Define default values
                int defaultBitDepth = 16;
                long defaultFrequency = 44100;

                // Obtain current values
                Integer bitRate = format.getBitRate();
                Long frequency = format.getFrequency();
                Integer bitDepth = format.getBitDepth();

                // These values must be interdependent (bitRate = bitDepth * frequency)
                if (null == bitRate) {
                    if (null == bitDepth) {
                        bitDepth = new Integer(defaultBitDepth);
                    }
                    if (null == frequency) {
                        frequency = new Long(defaultFrequency);
                    }
                    bitRate = new Integer(bitDepth.intValue() * frequency.intValue());
                } else if (null == bitDepth) {
                    if (null == frequency) {
                        frequency = new Long(defaultFrequency);
                    }
                    bitDepth = new Integer(bitRate.intValue() / frequency.intValue());
                } else if (null == frequency) {
                    frequency = new Long(bitRate.longValue() / bitDepth.longValue());
                }

                format = new AudioFormat(format.getContainer(), format.getCodec(), format.isBigEndian(), bitDepth,
                        bitRate, frequency);
            }

            // Return preferred AudioFormat
            return format;
        }

        // Return null indicating failure
        return null;
    }

    /**
     * Determines the best match between a list of audio formats supported by a source and a sink.
     *
     * @param inputs the supported audio formats of an audio source
     * @param outputs the supported audio formats of an audio sink
     * @return the best matching format or null, if source and sink are incompatible
     */
    public static AudioFormat getBestMatch(Set<AudioFormat> inputs, Set<AudioFormat> outputs) {
        AudioFormat preferredFormat = getPreferredFormat(inputs);
        for (AudioFormat output : outputs) {
            if (output.isCompatible(preferredFormat)) {
                return preferredFormat;
            } else {
                for (AudioFormat input : inputs) {
                    if (output.isCompatible(input)) {
                        return input;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Determines the preferred voice for the currently set locale
     *
     * @param voices a set of voices to chose from
     * @return the preferred voice for the current locale
     */
    public Voice getPreferredVoice(Set<Voice> voices) {
        // Express preferences with a Language Priority List
        Locale locale = localeProvider.getLocale();

        // Get collection of voice locales
        Collection<Locale> locales = new ArrayList<Locale>();
        for (Voice currentVoice : voices) {
            locales.add(currentVoice.getLocale());
        }

        // TODO: This can be activated for Java 8
        // Determine preferred locale based on RFC 4647
        // String ranges = locale.toLanguageTag();
        // List<Locale.LanguageRange> languageRanges = Locale.LanguageRange.parse(ranges);
        // Locale preferedLocale = Locale.lookup(languageRanges,locales);
        Locale preferredLocale = locale;

        // As a last resort choose some Locale
        if (null == preferredLocale) {
            preferredLocale = locales.iterator().next();
        }

        // Determine preferred voice
        Voice preferredVoice = null;
        for (Voice currentVoice : voices) {
            if (preferredLocale.equals(currentVoice.getLocale())) {
                preferredVoice = currentVoice;
            }
        }
        assert (preferredVoice != null);

        // Return preferred voice
        return preferredVoice;
    }

    /**
     * Starts listening for the keyword that starts a dialog
     *
     * @throws IllegalStateException if required services are not available
     */
    public void startDialog() {
        startDialog(null, null, null, null, null, null, null, this.keyword);
    }

    /**
     * Starts listening for the keyword that starts a dialog
     *
     * @throws IllegalStateException if required services are not available
     */
    public void startDialog(KSService ks, STTService stt, TTSService tts, HumanLanguageInterpreter hli,
            AudioSource source, AudioSink sink, Locale locale, String keyword) {

        // use defaults, if null
        ks = (ks == null) ? getKS() : ks;
        stt = (stt == null) ? getSTT() : stt;
        tts = (tts == null) ? getTTS() : tts;
        hli = (hli == null) ? getHLI() : hli;
        source = (source == null) ? audioManager.getSource() : source;
        sink = (sink == null) ? audioManager.getSink() : sink;
        locale = (locale == null) ? localeProvider.getLocale() : locale;

        if (ks != null && stt != null && tts != null && hli != null && source != null && sink != null) {
            DialogProcessor processor = new DialogProcessor(ks, stt, tts, hli, source, sink, localeProvider.getLocale(),
                    keyword);
            processor.start();
        } else {
            String msg = "Cannot start dialog as services are missing.";
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
    }

    protected void setLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

    protected void addKSService(KSService ksService) {
        this.ksServices.put(ksService.getId(), ksService);
    }

    protected void removeKSService(KSService ksService) {
        this.ksServices.remove(ksService.getId());
    }

    protected void addSTTService(STTService sttService) {
        this.sttServices.put(sttService.getId(), sttService);
    }

    protected void removeSTTService(STTService sttService) {
        this.sttServices.remove(sttService.getId());
    }

    protected void addTTSService(TTSService ttsService) {
        this.ttsServices.put(ttsService.getId(), ttsService);
    }

    protected void removeTTSService(TTSService ttsService) {
        this.ttsServices.remove(ttsService.getId());
    }

    protected void addHumanLanguageInterpreter(HumanLanguageInterpreter humanLanguageInterpreter) {
        this.humanLanguageInterpreters.put(humanLanguageInterpreter.getId(), humanLanguageInterpreter);
    }

    protected void removeHumanLanguageInterpreter(HumanLanguageInterpreter humanLanguageInterpreter) {
        this.humanLanguageInterpreters.remove(humanLanguageInterpreter.getId());
    }

    protected void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    protected void unsetAudioManager(AudioManager audioManager) {
        this.audioManager = null;
    }

    /**
     * Retrieves a TTS service.
     * If a default name is configured and the service available, this is returned. Otherwise, the first available
     * service is returned.
     *
     * @return a TTS service or null, if no service is available or if a default is configured, but no according service
     *         is found
     */
    public TTSService getTTS() {
        TTSService tts = null;
        if (defaultTTS != null) {
            tts = ttsServices.get(defaultTTS);
            if (tts == null) {
                logger.warn("Default TTS service '{}' not available!", defaultTTS);
            }
        } else if (!ttsServices.isEmpty()) {
            tts = ttsServices.values().iterator().next();
        } else {
            logger.debug("No TTS service available!");
        }
        return tts;
    }

    /**
     * Retrieves a TTS service with the given id.
     *
     * @param id the id of the TTS service
     * @return a TTS service or null, if no service with this id exists
     */
    public TTSService getTTS(String id) {
        return ttsServices.get(id);
    }

    /**
     * Retrieves all TTS services.
     *
     * @return a collection of TTS services
     */
    public Collection<TTSService> getTTSs() {
        return new HashSet<>(ttsServices.values());
    }

    /**
     * Retrieves a STT service.
     * If a default name is configured and the service available, this is returned. Otherwise, the first available
     * service is returned.
     *
     * @return a STT service or null, if no service is available or if a default is configured, but no according service
     *         is found
     */
    public STTService getSTT() {
        STTService stt = null;
        if (defaultTTS != null) {
            stt = sttServices.get(defaultSTT);
            if (stt == null) {
                logger.warn("Default STT service '{}' not available!", defaultSTT);
            }
        } else if (!sttServices.isEmpty()) {
            stt = sttServices.values().iterator().next();
        } else {
            logger.debug("No STT service available!");
        }
        return stt;
    }

    /**
     * Retrieves a STT service with the given id.
     *
     * @param id the id of the STT service
     * @return a STT service or null, if no service with this id exists
     */
    public STTService getSTT(String id) {
        return sttServices.get(id);
    }

    /**
     * Retrieves all STT services.
     *
     * @return a collection of STT services
     */
    public Collection<STTService> getSTTs() {
        return new HashSet<>(sttServices.values());
    }

    /**
     * Retrieves a KS service.
     * If a default name is configured and the service available, this is returned. Otherwise, the first available
     * service is returned.
     *
     * @return a KS service or null, if no service is available or if a default is configured, but no according service
     *         is found
     */
    public KSService getKS() {
        KSService ks = null;
        if (defaultKS != null) {
            ks = ksServices.get(defaultKS);
            if (ks == null) {
                logger.warn("Default KS service '{}' not available!", defaultKS);
            }
        } else if (!ksServices.isEmpty()) {
            ks = ksServices.values().iterator().next();
        } else {
            logger.debug("No KS service available!");
        }
        return ks;
    }

    /**
     * Retrieves a KS service with the given id.
     *
     * @param id the id of the KS service
     * @return a KS service or null, if no service with this id exists
     */
    public KSService getKS(String id) {
        return ksServices.get(id);
    }

    /**
     * Retrieves all KS services.
     *
     * @return a collection of KS services
     */
    public Collection<KSService> getKSs() {
        return new HashSet<>(ksServices.values());
    }

    /**
     * Retrieves a HumanLanguageInterpreter.
     * If a default name is configured and the service available, this is returned. Otherwise, the first available
     * service is returned.
     *
     * @return a HumanLanguageInterpreter or null, if no service is available or if a default is configured, but no
     *         according service is found
     */
    public HumanLanguageInterpreter getHLI() {
        HumanLanguageInterpreter hli = null;
        if (defaultHLI != null) {
            hli = humanLanguageInterpreters.get(defaultHLI);
            if (hli == null) {
                logger.warn("Default HumanLanguageInterpreter '{}' not available!", defaultHLI);
            }
        } else if (!humanLanguageInterpreters.isEmpty()) {
            hli = humanLanguageInterpreters.values().iterator().next();
        } else {
            logger.debug("No HumanLanguageInterpreter available!");
        }
        return hli;
    }

    /**
     * Retrieves a HumanLanguageInterpreter with the given id.
     *
     * @param id the id of the HumanLanguageInterpreter
     * @return a HumanLanguageInterpreter or null, if no interpreter with this id exists
     */
    public HumanLanguageInterpreter getHLI(String id) {
        return humanLanguageInterpreters.get(id);
    }

    /**
     * Retrieves all HumanLanguageInterpreters.
     *
     * @return a collection of HumanLanguageInterpreters
     */
    public Collection<HumanLanguageInterpreter> getHLIs() {
        return new HashSet<>(humanLanguageInterpreters.values());
    }

    /**
     * Returns all available voices in the system from all TTS services.
     *
     * @return a set of available voices
     */
    public Set<Voice> getAllVoices() {
        Set<Voice> voices = new HashSet<>();
        for (TTSService tts : ttsServices.values()) {
            voices.addAll(tts.getAvailableVoices());
        }
        return voices;
    }

}
