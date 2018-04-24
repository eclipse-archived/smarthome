package org.eclipse.smarthome.io.rest;

import java.util.Locale;

public interface LocaleService {

    public Locale getLocale(String acceptLanguageHttpHeader);

}
