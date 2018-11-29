/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.model.script.actions;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * The ZonedDateTime objects exposed to the rules engine (e.g. {@link ZonedDateTime.now()} are
 * augmented with the methods in {@link JodaTimeCompatibilityExtensions}. This is done to make
 * sure that rules files which were written previously, when {@link DateTime.now()} was used
 * instead, are still as compatible as possible.
 *
 * This test class checks to make sure that every method available on {@link DateTime} is present
 * on {@link ZonedDateTime}, or a compatible version of it exists in {@link JodaTimeCompatibilityExtensions}.
 *
 * @author Jon Evans - Initial contribution
 *
 */
@SuppressWarnings("deprecation")
@RunWith(Parameterized.class)
public class JodaTimeCompatibilityExtensionsTest {
    private final ZonedDateTime javaTime;
    private final DateTime jodaTime;

    /**
     * Set of method names that we will ignore (e.g. hashCode)
     */
    private static final Set<String> ignoredMethods;
    static {
        HashSet<String> s = new HashSet<>();
        s.add("equals");
        s.add("hashCode");
        s.add("toString");
        s.add("compareTo");
        ignoredMethods = Collections.unmodifiableSet(s);
    }

    /**
     * Map of parameter type used in org.joda.time.DateTime -> parameter type used in java.time.ZonedDateTime
     */
    private static final Map<Class<?>, Class<?>> parameterTypeMap;
    static {
        HashMap<Class<?>, Class<?>> m = new HashMap<>();
        m.put(DateTime.class, ZonedDateTime.class);
        parameterTypeMap = Collections.unmodifiableMap(m);
    }

    /**
     * Constructor for this test case
     *
     * @param javaTime
     */
    public JodaTimeCompatibilityExtensionsTest(ZonedDateTime javaTime) {
        this.javaTime = javaTime;
        this.jodaTime = getDateTime(javaTime);
    }

    @Parameters
    public static List<ZonedDateTime[]> dates() {
        return Arrays.asList(new ZonedDateTime[][] {
                // now
                { ZonedDateTime.now() },
                // 1st Jan 1970 in Paris
                { LocalDateTime.parse("1970-01-01 00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        .atZone(ZoneId.of("Europe/Paris")) },
                // Lunar landing
                { LocalDateTime.parse("1969-07-20 20:17", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        .atZone(ZoneId.of("UTC")) },
                // Linux announced
                { LocalDateTime.parse("1991-08-25 20:57:08", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        .atZone(ZoneId.of("GMT")) }
                // TODO: add more dates
        });
    }

    @Test
    public void allRelevantMethodsAreSupported() {
        // The methods we want to support, from org.joda.time.DateTime
        Method[] jodaTimeMethods = getJodaDateTimeMethods();

        // The methods implemented directly by java.time.ZonedDateTime
        Method[] javaTimeMethods = getZonedDateTimeMethods();

        // The compatibility methods in the JodaTimeCompatibilityExtensions class
        Method[] compatibilityMethods = getJodaTimeCompatibilityExtensionsMethods();

        // Set of all joda time methods which aren't implemented anywhere
        Set<Method> notFound = new HashSet<>();

        // Map of methods implemented by our compatibility class.
        // Joda time method -> compatible method
        Map<Method, Method> compatibleMethods = new HashMap<>();

        for (Method method : jodaTimeMethods) {
            String methodName = method.getName();
            Class<?>[] convertedParamTypes = convertParameterTypes(method.getParameterTypes());

            // Try to find the joda time method on the java time class
            Optional<Method> javaTimeMethod = Arrays.stream(javaTimeMethods)
                    .filter(withMethodSignature(methodName, convertedParamTypes)).findFirst();

            // If the method isn't implemented by ZonedDateTime,
            // see if the compatibility class implements it
            if (!javaTimeMethod.isPresent()) {
                Class<?>[] compatibleParamTypes = convertParameterTypesForCompatibilityExtensions(convertedParamTypes);
                // Try to find the joda method in the JodaTimeCompatibilityExtensions class
                Optional<Method> compatibilityMethod = Arrays.stream(compatibilityMethods)
                        .filter(withMethodSignature(methodName, compatibleParamTypes)).findFirst();
                if (compatibilityMethod.isPresent()) {
                    compatibleMethods.put(method, compatibilityMethod.get());
                } else {
                    notFound.add(method);
                }
            }
        }
        if (!notFound.isEmpty()) {
            debug("\nThese methods were not found:\n%s",
                    notFound.stream().map(m -> m.toString()).collect(Collectors.joining("\n")));
            debug("%d unimplemented methods", notFound.size());
        }
        testCompatibleMethods(compatibleMethods);
        assertThat("Some methods are not implemented", notFound.size(), is(0));
    }

    /**
     * Test all of the methods that are implemented in {@link JodaTimeCompatibilityExtensions}
     *
     * @param compatibleMethods a Map of org.joda.time.DateTime method -> JodaTimeCompatibilityExtensions method
     */
    private void testCompatibleMethods(Map<Method, Method> compatibleMethods) {
        compatibleMethods.keySet().stream().forEach(m -> {
            Method compatibleMethod = compatibleMethods.get(m);
            Object[] params = getTestParameters(m);
            Object[] compatibleParams = getCompatibleParameters(javaTime, m);
            try {
                Object jodaResult = m.invoke(jodaTime, params);
                Object javaResult = compatibleMethod.invoke(JodaTimeCompatibilityExtensions.class, compatibleParams);
                if (compatibleMethod.getReturnType().equals(long.class)
                        || compatibleMethod.getReturnType().equals(int.class)) {
                    BigDecimal jodaNumber = number(jodaResult);
                    BigDecimal javaNumber = number(javaResult);
                    assertThat(m.getName(), jodaNumber, equalTo(javaNumber));
                } else if (m.getReturnType().equals(DateTime.class)
                        && compatibleMethod.getReturnType().equals(ZonedDateTime.class)) {
                    assertThat(m.toString(), (DateTime) jodaResult, timeEqualTo((ZonedDateTime) javaResult));
                } else if (Calendar.class.isAssignableFrom(m.getReturnType())
                        && Calendar.class.isAssignableFrom(compatibleMethod.getReturnType())) {
                    assertThat(m.toString(), (Calendar) jodaResult, timeEqualTo((Calendar) javaResult));
                } else {
                    if (m.getName().equals("toGregorianCalendar")) {
                        debug("%s %s", m.getReturnType().getCanonicalName(),
                                compatibleMethod.getReturnType().getCanonicalName());
                    }
                    assertThat(m.toString(), jodaResult.getClass(), equalTo(javaResult.getClass()));
                    assertThat(m.toString(), jodaResult, equalTo(javaResult));
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
                fail(m.toString());
            }
        });
    }

    /**
     * Generate some test parameters to use for invoking the method.
     *
     * @param m the method to invoke
     * @return an Object array of method parameters
     */
    private Object[] getTestParameters(Method m) {
        Object[] params = new Object[m.getParameterTypes().length];
        int i = 0;
        for (Class<?> parameterType : m.getParameterTypes()) {
            if (parameterType.equals(int.class)) {
                if (m.getName().equals("withEra")) {
                    params[i++] = new Integer(1);
                } else if (m.getName().equals("withWeekOfWeekyear")) {
                    params[i++] = new Integer(10);
                } else if (m.getName().equals("withDayOfWeek")) {
                    params[i++] = new Integer(2);
                } else {
                    params[i++] = new Integer(88);
                }
            } else if (parameterType.equals(long.class)) {
                params[i++] = new Long(88);
            } else if (parameterType.equals(Locale.class)) {
                params[i++] = Locale.UK;
            } else {
                fail("Parameter type " + parameterType.getCanonicalName() + " for method " + m.toString()
                        + " not yet supported");
            }
        }

        return params;
    }

    /**
     * Generate some test parameters to use for invoking the method.
     *
     * @param zdt the ZonedDateTime (the first argument of all of the compatibility methods)
     * @param m the method to invoke
     * @return an Object array of method parameters
     */
    private Object[] getCompatibleParameters(ZonedDateTime zdt, Method m) {
        Object[] params = new Object[m.getParameterCount() + 1];
        Object[] origParams = getTestParameters(m);
        System.arraycopy(origParams, 0, params, 1, origParams.length);
        params[0] = zdt;
        return params;
    }

    /**
     * Normalize a numeric object into a BigDecimal for comparison purposes.
     *
     * @param value a supported instance (Integer or Long)
     * @return a BigDecimal
     */
    private BigDecimal number(Object value) {
        if (value instanceof Long) {
            return new BigDecimal((Long) value);
        } else if (value instanceof Integer) {
            return new BigDecimal((Integer) value);
        }
        fail(value.getClass() + " has not been converted to BigDecimal");
        return null;
    }

    @Test
    public void testGetEra() {
        ZonedDateTime zdt = ZonedDateTime.now();
        DateTime dt = getDateTime(zdt);
        assertThat(JodaTimeCompatibilityExtensions.getEra(zdt), equalTo((long) dt.getEra()));
    }

    @Test
    public void testGetYearOfEra() {
        ZonedDateTime zdt = ZonedDateTime.now();
        DateTime dt = getDateTime(zdt);
        assertThat(JodaTimeCompatibilityExtensions.getYearOfEra(zdt), equalTo((long) dt.getYearOfEra()));
    }

    @Test
    public void testGetWeekOfWeekyear() {
        ZonedDateTime zdt = ZonedDateTime.now();
        DateTime dt = getDateTime(zdt);
        assertThat(JodaTimeCompatibilityExtensions.getWeekOfWeekyear(zdt), equalTo((long) dt.getWeekOfWeekyear()));
    }

    @Test
    public void testGetSecondOfDay() {
        ZonedDateTime zdt = ZonedDateTime.now();
        DateTime dt = getDateTime(zdt);
        assertThat(JodaTimeCompatibilityExtensions.getSecondOfDay(zdt), equalTo((long) dt.getSecondOfDay()));
    }

    /**
     * For the purposes of the test, we need an instance of DateTime which represents the same time
     * as the given instance of ZonedDateTime.
     *
     * @param zdt the ZonedDateTime object
     * @return a DateTime equivalent to the ZonedDateTime
     */
    private DateTime getDateTime(ZonedDateTime zdt) {
        DateTime dt = new DateTime(zdt.toInstant().toEpochMilli(), DateTimeZone.forID(zdt.getZone().getId()));
        assertThat(dt.toInstant().getMillis(), equalTo(zdt.toInstant().toEpochMilli()));
        assertThat(dt, timeEqualTo(zdt));
        // assertThat(dt.getZone().getID(), equalTo(zdt.getZone().getId()));
        return dt;
    }

    /**
     * Get all of the org.joda.time.DateTime methods that we want to support
     *
     * @return an array of Method
     */
    private Method[] getJodaDateTimeMethods() {
        // We want all methods from the DateTime class hierarchy
        Method[] methods = DateTime.class.getMethods();
        // Only interested in public non-static methods that are declared in classes from the org.joda.time hierarchy
        // and which don't use org.joda.time classes as parameters
        return Arrays.stream(methods)
                // Only interested in public non-static methods
                .filter(isPublicNotStatic())
                // Only want methods that are declared in classes from the org.joda.time hierarchy
                .filter(isFromClassInPackage("org.joda.time"))
                // Don't want hashCode, equals etc.
                .filter(isInteresting())
                // Don't want methods that take org.joda.time.* parameter types
                .filter(hasNoJodaClassesInParameters())
                // Don't want methods that return org.joda.time.* types
                .filter(notReturnJodaType()).toArray(Method[]::new);
    }

    /**
     * Get all of the java.time.ZonedDateTime methods
     *
     * @return an array of Method
     */
    private Method[] getZonedDateTimeMethods() {
        // ZonedDateTime doesn't extend any other class, but does include
        // getChronology() which is a default method on the ChronoZonedDateTime interface
        // which ZonedDateTime implements
        Method[] methods = ZonedDateTime.class.getMethods();
        return Arrays.stream(methods)
                // Only interested in public non-static methods
                .filter(isPublicNotStatic())
                // Only want methods implemented by classes in this package
                .filter(isFromClassInPackage("java.time"))
                // Don't want hashCode, equals etc.
                .filter(isInteresting())
                //
                .toArray(Method[]::new);
    }

    /**
     * Get all of the compatibility methods implemented in {@link JodaTimeCompatibilityExtensions}
     *
     * @return an array of Method
     */
    private Method[] getJodaTimeCompatibilityExtensionsMethods() {
        Method[] methods = JodaTimeCompatibilityExtensions.class.getDeclaredMethods();
        // Only interested in public static methods
        return Arrays.stream(methods).filter(isPublicStatic()).toArray(Method[]::new);
    }

    /**
     * Convert Joda Time parameter types to Java Time
     *
     * @return
     */
    private Class<?>[] convertParameterTypes(Class<?>[] parameterTypes) {
        Class<?>[] convertedTypes = new Class<?>[parameterTypes.length];
        int i = 0;
        for (Class<?> c : parameterTypes) {
            convertedTypes[i++] = parameterTypeMap.getOrDefault(c, c);
        }
        return convertedTypes;
    }

    /**
     * Convert Joda Time parameter types to Java Time for compatibility extensions
     *
     * @return
     */
    private Class<?>[] convertParameterTypesForCompatibilityExtensions(Class<?>[] parameterTypes) {
        Class<?>[] convertedTypes = new Class<?>[parameterTypes.length + 1];
        convertedTypes[0] = ZonedDateTime.class;
        int i = 1;
        for (Class<?> c : parameterTypes) {
            convertedTypes[i++] = parameterTypeMap.getOrDefault(c, c);
        }
        return convertedTypes;
    }

    /**
     * Predicate filter to return only methods declared public static
     *
     * @return a Predicate
     */
    private static Predicate<Method> isPublicStatic() {
        return m -> Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers());
    }

    /**
     * Predicate filter to return only methods declared public and not static
     *
     * @return a Predicate
     */
    private static Predicate<Method> isPublicNotStatic() {
        return m -> Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers());
    }

    /**
     * Predicate filter to return only methods with a specific name and signature
     *
     * @return a Predicate
     */
    private static Predicate<Method> withMethodSignature(String name, Class<?>[] parameterTypes) {
        return m -> {
            return m.getName().equals(name) && Arrays.equals(m.getParameterTypes(), parameterTypes);
        };
    }

    /**
     * Predicate filter to exclude methods we aren't interested in (like "equals")
     *
     * @return a Predicate
     */
    private static Predicate<Method> isInteresting() {
        return m -> !ignoredMethods.contains(m.getName());
    }

    /**
     * Predicate filter to return only methods declared by classes in the given package
     *
     * @return a Predicate
     */
    private static Predicate<Method> isFromClassInPackage(String packagePrefix) {
        return m -> m.getDeclaringClass().getPackage().getName().startsWith(packagePrefix);
    }

    /**
     * Predicate filter to return only methods which don't have org.joda.time classes as arguments
     *
     * @return a Predicate
     */
    private static Predicate<Method> hasNoJodaClassesInParameters() {
        return m -> Arrays.stream(m.getParameterTypes()).noneMatch(c -> {
            Package p = c.getPackage();
            return p == null || p.getName().startsWith("org.joda.time");
        });
    }

    /**
     * Predicate filter to return only methods which don't have a return type in the org.joda.time package.
     *
     * @return a Predicate
     */
    private static Predicate<Method> notReturnJodaType() {
        return m -> m.getReturnType().getPackage() == null
                || !m.getReturnType().getPackage().getName().startsWith("org.joda.time");
    }

    private static void debug(String messageFormat, Object... args) {
        System.out.println(String.format(messageFormat, args));
    }

    /**
     * Hamcrest matcher that tests whether a DateTime object is equal to a ZonedDateTime object
     *
     * @param zdt the ZonedDateTime to test against
     * @return a Matcher
     */
    private static org.hamcrest.Matcher<DateTime> timeEqualTo(final ZonedDateTime zdt) {
        return new TypeSafeMatcher<DateTime>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("is the same instant as " + zdt);
            }

            @Override
            protected boolean matchesSafely(DateTime item) {
                return item.toInstant().getMillis() == zdt.toInstant().toEpochMilli();
            }
        };
    }

    /**
     * Special case matcher for Calendar / GregorianCalendar
     *
     * The instance of GregorianCalendar returned from DateTime#getGregorianCalendar() and
     * ZonedDateTime#getGregorianCalendar() appear to be identical, but equals() returns false. To avoid this we use a
     * more simple check that the calendar objects represent the same instant in a timezone with the same offset.
     *
     * @param other
     * @return
     */
    private static org.hamcrest.Matcher<Calendar> timeEqualTo(final Calendar other) {
        return new TypeSafeMatcher<Calendar>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("is the same instant as " + other);
            }

            @Override
            protected boolean matchesSafely(Calendar item) {
                TimeZone tz1 = item.getTimeZone();
                TimeZone tz2 = other.getTimeZone();
                return tz1.getRawOffset() == tz2.getRawOffset() && item.getTimeInMillis() == other.getTimeInMillis();
            }
        };
    }
}
