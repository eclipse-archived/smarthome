/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.provider.file;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.ParsingException;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.eclipse.smarthome.core.common.registry.Provider;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.core.service.AbstractWatchQueueReader;
import org.eclipse.smarthome.core.service.AbstractWatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is base for {@link ModuleTypeProvider} and {@link TemplateProvider} which are
 * responsible for importing the automation objects from local file system.
 * <p>
 * It provides functionality for tracking {@link Parser} services and provides common functionality for importing the
 * automation objects.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractFileProvider<E> extends AbstractWatchService implements Provider<E> {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * This Map provides structure for fast access to the {@link Parser}s. This provides opportunity for high
     * performance at runtime of the system.
     */
    protected Map<String, Parser<E>> parsers = new HashMap<String, Parser<E>>();

    /**
     * This Map provides structure for fast access to the provided automation objects. This provides opportunity for
     * high performance at runtime of the system, when the Rule Engine asks for any particular object, instead of
     * waiting it for parsing every time.
     * <p>
     * The Map has for keys URLs of the files containing automation objects and for values - parsed objects.
     */
    protected Map<String, E> providedObjectsHolder = new HashMap<String, E>();

    /**
     * This map is used for mapping the imported automation objects to the file that contains them. This provides
     * opportunity when an event for deletion of the file is received, how to recognize which objects are removed.
     */
    protected Map<URL, List<String>> providerPortfolio = new HashMap<URL, List<String>>();

    /**
     * This Map holds URL resources that waiting for a parser to be loaded.
     */
    protected Map<String, List<URL>> urls = new HashMap<String, List<URL>>();

    protected List<ProviderChangeListener<E>> listeners = new ArrayList<ProviderChangeListener<E>>();

    @Override
    public void deactivate() {
        super.deactivate();
        parsers.clear();
        urls.clear();
        providedObjectsHolder.clear();
    }

    @Override
    public void addProviderChangeListener(ProviderChangeListener<E> listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }

    }

    @Override
    public Collection<E> getAll() {
        synchronized (providedObjectsHolder) {
            return providedObjectsHolder.values();
        }
    }

    @Override
    public void removeProviderChangeListener(ProviderChangeListener<E> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    protected void addParser(Parser<E> parser, Map properties) {
        String parserType = (String) properties.get(Parser.FORMAT);
        parserType = parserType == null ? Parser.FORMAT_JSON : parserType;
        parsers.put(parserType, parser);
        List<URL> value = urls.get(parserType);
        if (value != null && !value.isEmpty()) {
            for (URL url : value) {
                importFile(parserType, url);
            }
        }
    }

    protected void removeParser(Parser<E> parser, Map properties) {
        String parserType = (String) properties.get(Parser.FORMAT);
        parserType = parserType == null ? Parser.FORMAT_JSON : parserType;
        parsers.remove(parserType);
    }

    protected void importResources(File file) {
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    importResources(f);
                }
            } else {
                try {
                    URL url = file.toURI().toURL();
                    String parserType = getParserType(url);
                    importFile(parserType, url);
                } catch (MalformedURLException e) {
                    // should not happen
                }
            }
        }
    }

    protected void remove(File path) {
        try {
            URL url = path.toURI().toURL();
            synchronized (providerPortfolio) {
                removeElements(providerPortfolio.remove(url));
            }
        } catch (MalformedURLException e) {
            // should not happen
        }
    }

    /**
     * This method is responsible for importing a set of Automation objects from a specified URL resource.
     *
     * @param parserType is relevant to the format that you need for conversion of the Automation objects in text.
     * @param url a specified URL for import.
     */
    protected void importFile(String parserType, URL url) {
        Parser<E> parser = parsers.get(parserType);
        if (parser != null) {
            InputStream is = null;
            InputStreamReader inputStreamReader = null;
            try {
                is = url.openStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                inputStreamReader = new InputStreamReader(bis);
                Set<E> providedObjects = parser.parse(inputStreamReader);
                if (providedObjects != null) {
                    updateProvidedObjectsHolder(url, providedObjects);
                }
            } catch (ParsingException e) {
                logger.debug(e.getMessage(), e);
            } catch (IOException e) {
                logger.debug(e.getMessage(), e);
            } finally {
                try {
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                }
            }
        } else {
            synchronized (urls) {
                List<URL> value = urls.get(parserType);
                if (value == null) {
                    value = new ArrayList<URL>();
                    urls.put(parserType, value);
                }
                value.add(url);
            }
            logger.debug("Parser {} not available", parserType, new Exception());
        }

    }

    @Override
    protected AbstractWatchQueueReader buildWatchQueueReader(WatchService watchService, Path toWatch) {
        return new WatchQueueReader(watchService, toWatch, this);
    }

    @Override
    protected boolean watchSubDirectories() {
        return true;
    }

    @Override
    protected void registerDirectory(Path subDir) throws IOException {
        subDir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    }

    protected void notifyListeners(E oldElement, E newElement) {
        synchronized (listeners) {
            for (ProviderChangeListener<E> listener : listeners) {
                if (oldElement != null) {
                    listener.updated(this, oldElement, newElement);
                }
                listener.added(this, newElement);
            }
        }
    }

    protected void notifyListeners(E removededObject) {
        if (removededObject != null) {
            synchronized (listeners) {
                for (ProviderChangeListener<E> listener : listeners) {
                    listener.removed(this, removededObject);
                }
            }
        }
    }

    protected E getOldElement(String uid) {
        synchronized (providedObjectsHolder) {
            return providedObjectsHolder.get(uid);
        }
    }

    protected abstract void updateProvidedObjectsHolder(URL url, Set<E> providedObjects);

    protected abstract void removeElements(List<String> objectsForRemove);

    private String getParserType(URL url) {
        String fileName = url.getPath();
        int fileExtesionStartIndex = fileName.lastIndexOf(".") + 1;
        if (fileExtesionStartIndex == -1) {
            return Parser.FORMAT_JSON;
        }
        String fileExtesion = fileName.substring(fileExtesionStartIndex);
        if (fileExtesion.equals("txt")) {
            return Parser.FORMAT_JSON;
        }
        return fileExtesion;
    }

    private static class WatchQueueReader extends AbstractWatchQueueReader {

        private AbstractFileProvider provider;

        WatchQueueReader(WatchService watchService, Path dirToWatch, AbstractFileProvider provider) {
            super(watchService, dirToWatch);
            this.provider = provider;
        }

        @Override
        protected void processWatchEvent(WatchEvent<?> event, Kind<?> kind, Path path) {
            if (kind.equals(ENTRY_DELETE)) {
                provider.remove(new File(provider.getSourcePath() + File.separator + path.toString()));
            } else {
                provider.importResources(new File(provider.getSourcePath() + File.separator + path.toString()));
            }
        }

    }

}