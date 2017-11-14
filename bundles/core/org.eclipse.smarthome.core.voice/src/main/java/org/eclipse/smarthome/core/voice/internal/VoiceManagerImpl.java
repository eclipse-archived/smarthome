/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.voice.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioManager;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioSource;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.audio.UnsupportedAudioStreamException;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.voice.KSService;
import org.eclipse.smarthome.core.voice.STTService;
import org.eclipse.smarthome.core.voice.TTSException;
import org.eclipse.smarthome.core.voice.TTSService;
import org.eclipse.smarthome.core.voice.Voice;
import org.eclipse.smarthome.core.voice.VoiceManager;
import org.eclipse.smarthome.core.voice.text.HumanLanguageInterpreter;
import org.eclipse.smarthome.core.voice.text.InterpretationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service provides functionality around voice services and is the central service to be used directly by others.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Yannick Schaus - Added ability to provide a item for feedback during listening phases
 * @author Christoph Weitkamp - Added getSupportedStreams() and UnsupportedAudioStreamException
 */
public class VoiceManagerImpl implements VoiceManager, ConfigOptionProvider {

    // the default keyword to use if no other is configured
    private static final String DEFAULT_KEYWORD = "Wakeup";

    // constants for the configuration properties
    private static final String CONFIG_URI = "system:voice";
    private static final String CONFIG_KEYWORD = "keyword";
    private static final String CONFIG_LISTENING_ITEM = "listeningItem";
    private static final String CONFIG_DEFAULT_HLI = "defaultHLI";
    private static final String CONFIG_DEFAULT_KS = "defaultKS";
    private static final String CONFIG_DEFAULT_STT = "defaultSTT";
    private static final String CONFIG_DEFAULT_TTS = "defaultTTS";
    private static final String CONFIG_DEFAULT_VOICE = "defaultVoice";
    private static final String CONFIG_PREFIX_DEFAULT_VOICE = "defaultVoice.";

    private final Logger logger = LoggerFactory.getLogger(VoiceManagerImpl.class);

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
    private String listeningItem = null;
    private String defaultTTS = null;
    private String defaultSTT = null;
    private String defaultKS = null;
    private String defaultHLI = null;
    private String defaultVoice = null;
    private Map<String, String> defaultVoices = new HashMap<>();
    private AudioManager audioManager;
    private EventPublisher eventPublisher;

    protected void activate(Map<String, Object> config) {
        modified(config);
    }

    protected void deactivate() {
    }

    protected void modified(Map<String, Object> config) {
        if (config != null) {
            this.keyword = config.containsKey(CONFIG_KEYWORD) ? config.get(CONFIG_KEYWORD).toString() : DEFAULT_KEYWORD;
            this.listeningItem = config.containsKey(CONFIG_LISTENING_ITEM)
                    ? config.get(CONFIG_LISTENING_ITEM).toString()
                    : null;
            this.defaultTTS = config.containsKey(CONFIG_DEFAULT_TTS) ? config.get(CONFIG_DEFAULT_TTS).toString() : null;
            this.defaultSTT = config.containsKey(CONFIG_DEFAULT_STT) ? config.get(CONFIG_DEFAULT_STT).toString() : null;
            this.defaultKS = config.containsKey(CONFIG_DEFAULT_KS) ? config.get(CONFIG_DEFAULT_KS).toString() : null;
            this.defaultHLI = config.containsKey(CONFIG_DEFAULT_HLI) ? config.get(CONFIG_DEFAULT_HLI).toString() : null;
            this.defaultVoice = config.containsKey(CONFIG_DEFAULT_VOICE) ? config.get(CONFIG_DEFAULT_VOICE).toString()
                    : null;

            for (String key : config.keySet()) {
                if (key.startsWith(CONFIG_PREFIX_DEFAULT_VOICE)) {
                    String tts = key.substring(CONFIG_PREFIX_DEFAULT_VOICE.length());
                    defaultVoices.put(tts, config.get(key).toString());
                }
            }
        }
    }

    @Override
    public void say(String text) {
        say(text, null);
    }

    @Override
    public void say(String text, String voiceId) {
        say(text, voiceId, null);
    }

    @Override
    public void say(String text, String voiceIda, String sinkId) {
        try {
            TTSService tts = null;
            Voice voice = null;
            String selectedVoiceId = voiceIda;
            if (selectedVoiceId == null) {
                // use the configured default, if set
                selectedVoiceId = defaultVoice;
            }
            if (selectedVoiceId == null) {
                tts = getTTS();
                if (tts != null) {
                    voice = getPreferredVoice(tts.getAvailableVoices());
                }
            } else if (selectedVoiceId.contains(":")) {
                // it is a fully qualified unique id
                String[] segments = selectedVoiceId.split(":");
                tts = getTTS(segments[0]);
                if (tts != null) {
                    voice = getVoice(tts.getAvailableVoices(), segments[1]);
                }
            } else {
                // voiceId is not fully qualified
                tts = getTTS();
                if (tts != null) {
                    voice = getVoice(tts.getAvailableVoices(), selectedVoiceId);
                }
            }
            if (tts == null) {
                throw new TTSException("No TTS service can be found for voice " + selectedVoiceId);
            }
            if (voice == null) {
                throw new TTSException(
                        "Unable to find a voice for language " + localeProvider.getLocale().getLanguage());
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
                    } catch (UnsupportedAudioFormatException | UnsupportedAudioStreamException e) {
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

    @Override
    public String interpret(String text) throws InterpretationException {
        return interpret(text, null);
    }

    @Override
    public String interpret(String text, String hliId) throws InterpretationException {
        HumanLanguageInterpreter interpreter;
        if (hliId == null) {
            interpreter = getHLI();
            if (interpreter == null) {
                throw new InterpretationException("No human language interpreter available!");
            }
        } else {
            interpreter = getHLI(hliId);
            if (interpreter == null) {
                throw new InterpretationException("No human language interpreter can be found for " + hliId);
            }
        }
        return interpreter.interpret(localeProvider.getLocale(), text);
    }

    private Voice getVoice(Set<Voice> voices, String id) {
        for (Voice voice : voices) {
            if (voice.getUID().endsWith(":" + id)) {
                return voice;
            }
        }
        return null;
    }

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

    @Override
    public Voice getPreferredVoice(Set<Voice> voices) {
        // Express preferences with a Language Priority List
        Locale locale = localeProvider.getLocale();

        // Get collection of voice locales
        Collection<Locale> locales = new ArrayList<Locale>();
        for (Voice currentVoice : voices) {
            locales.add(currentVoice.getLocale());
        }

        // Determine preferred locale based on RFC 4647
        String ranges = locale.toLanguageTag();
        List<Locale.LanguageRange> languageRanges = Locale.LanguageRange.parse(ranges + "-*");
        Locale preferredLocale = Locale.lookup(languageRanges, locales);

        // As a last resort choose some Locale
        if (preferredLocale == null) {
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

    @Override
    public void startDialog() {
        startDialog(null, null, null, null, null, null, null, this.keyword, this.listeningItem);
    }

    @Override
    public void startDialog(KSService ksService, STTService sttService, TTSService ttsService,
            HumanLanguageInterpreter interpreter, AudioSource audioSource, AudioSink audioSink, Locale locale,
            String keyword, String listeningItem) {

        // use defaults, if null
        KSService ks = (ksService == null) ? getKS() : ksService;
        STTService stt = (sttService == null) ? getSTT() : sttService;
        TTSService tts = (ttsService == null) ? getTTS() : ttsService;
        HumanLanguageInterpreter hli = (interpreter == null) ? getHLI() : interpreter;
        AudioSource source = (audioSource == null) ? audioManager.getSource() : audioSource;
        AudioSink sink = (audioSink == null) ? audioManager.getSink() : audioSink;
        Locale loc = (locale == null) ? localeProvider.getLocale() : locale;
        String kw = (keyword == null) ? this.keyword : keyword;
        String item = (listeningItem == null) ? this.listeningItem : listeningItem;

        if (ks != null && stt != null && tts != null && hli != null && source != null && sink != null && loc != null
                && kw != null) {
            DialogProcessor processor = new DialogProcessor(ks, stt, tts, hli, source, sink, loc, kw, item,
                    this.eventPublisher);
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

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    @Override
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

    @Override
    public TTSService getTTS(String id) {
        return ttsServices.get(id);
    }

    @Override
    public Collection<TTSService> getTTSs() {
        return new HashSet<>(ttsServices.values());
    }

    @Override
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

    @Override
    public STTService getSTT(String id) {
        return sttServices.get(id);
    }

    @Override
    public Collection<STTService> getSTTs() {
        return new HashSet<>(sttServices.values());
    }

    @Override
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

    @Override
    public KSService getKS(String id) {
        return ksServices.get(id);
    }

    @Override
    public Collection<KSService> getKSs() {
        return new HashSet<>(ksServices.values());
    }

    @Override
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

    @Override
    public HumanLanguageInterpreter getHLI(String id) {
        return humanLanguageInterpreters.get(id);
    }

    @Override
    public Collection<HumanLanguageInterpreter> getHLIs() {
        return new HashSet<>(humanLanguageInterpreters.values());
    }

    @Override
    public Set<Voice> getAllVoices() {
        Set<Voice> voices = new HashSet<>();
        for (TTSService tts : ttsServices.values()) {
            voices.addAll(tts.getAvailableVoices());
        }
        return voices;
    }

    @Override
    public Collection<ParameterOption> getParameterOptions(URI uri, String param, Locale locale) {
        if (uri.toString().equals(CONFIG_URI)) {
            if (CONFIG_DEFAULT_HLI.equals(param)) {
                List<ParameterOption> options = new ArrayList<>();
                for (HumanLanguageInterpreter hli : humanLanguageInterpreters.values()) {
                    ParameterOption option = new ParameterOption(hli.getId(), hli.getLabel(locale));
                    options.add(option);
                }
                return options;
            } else if (CONFIG_DEFAULT_KS.equals(param)) {
                List<ParameterOption> options = new ArrayList<>();
                for (KSService ks : ksServices.values()) {
                    ParameterOption option = new ParameterOption(ks.getId(), ks.getLabel(locale));
                    options.add(option);
                }
                return options;
            } else if (CONFIG_DEFAULT_STT.equals(param)) {
                List<ParameterOption> options = new ArrayList<>();
                for (STTService stt : sttServices.values()) {
                    ParameterOption option = new ParameterOption(stt.getId(), stt.getLabel(locale));
                    options.add(option);
                }
                return options;
            } else if (CONFIG_DEFAULT_TTS.equals(param)) {
                List<ParameterOption> options = new ArrayList<>();
                for (TTSService tts : ttsServices.values()) {
                    ParameterOption option = new ParameterOption(tts.getId(), tts.getLabel(locale));
                    options.add(option);
                }
                return options;
            } else if (CONFIG_DEFAULT_VOICE.equals(param)) {
                List<ParameterOption> options = new ArrayList<>();
                for (Voice voice : getAllVoices()) {
                    ParameterOption option = new ParameterOption(voice.getUID(),
                            voice.getLabel() + " - " + voice.getLocale().getDisplayName());
                    options.add(option);
                }
                return options;
            }
        }
        return null;
    }

}
