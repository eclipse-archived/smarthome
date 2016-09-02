/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.persistence.h2.internal;

import java.io.File;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringListType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.FilterCriteria.Ordering;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.ModifiablePersistenceService;
import org.eclipse.smarthome.core.persistence.PersistenceItemInfo;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.core.types.UnDefType;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of the H2 {@link PersistenceService}.
 * See http://h2database.com
 *
 * H2 database licensed under EPL (http://h2database.com/html/license.html)
 *
 * In the store method, type conversion is performed where the default type for
 * an item is not as above For example, DimmerType can return OnOffType, so to
 * keep the best resolution, we store as a number in SQL and convert to
 * DecimalType before persisting to H2.
 *
 * @author Chris Jackson - Initial contribution
 * @author Markus Rathgeb - Use prepared statement, try-with-resources and rewrite most stuff
 */
public class H2PersistenceService implements ModifiablePersistenceService, ManagedService {

    private static class SqlType {
        public static final String TIMESTAMP = "TIMESTAMP";
        public static final String VARCHAR = "VARCHAR";
    }

    private static class Column {
        public static final String TIME = "time";
        public static final String CLAZZ = "clazz";
        public static final String VALUE = "value";
    }

    private static class Schema {
        public static final String METAINFO = "ESH_METAINFO";
        public static final String ITEM = "ESH_ITEM";
    }

    private final Map<String, List<Class<? extends State>>> stateClasses = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(H2PersistenceService.class);

    private final String h2Url = "jdbc:h2:file:";

    private ItemRegistry itemRegistry;
    private I18nProvider i18nProvider;

    private Connection connection;
    private final List<String> itemCache = new ArrayList<>();

    private BundleContext bundleContext;

    public H2PersistenceService() {
        // Ensure that known types are accessible by the classloader
        addStateClass(DateTimeType.class);
        addStateClass(DecimalType.class);
        addStateClass(HSBType.class);
        addStateClass(OnOffType.class);
        addStateClass(OpenClosedType.class);
        addStateClass(PlayPauseType.class);
        addStateClass(PointType.class);
        addStateClass(RawType.class);
        addStateClass(RewindFastforwardType.class);
        addStateClass(StringListType.class);
        addStateClass(StringType.class);
        addStateClass(UnDefType.class);
        addStateClass(UpDownType.class);
    }

    private void addStateClass(final Class<? extends State> stateClass) {
        List<Class<? extends State>> list = new ArrayList<>();
        list.add(stateClass);
        stateClasses.put(stateClass.getSimpleName(), list);
    }

    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    protected void deactivate() {
        disconnectFromDatabase();
        this.bundleContext = null;
    }

    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    public void setI18nProvider(I18nProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    public void unsetI18nProvider(I18nProvider i18nProvider) {
        this.i18nProvider = null;
    }

    @Override
    public String getId() {
        return "h2";
    }

    @Override
    public String getLabel(Locale locale) {
        return i18nProvider.getText(bundleContext.getBundle(), "h2.label", "H2 Embedded Database", locale);
    }

    @Override
    public void store(Item item, String alias) {
        store(item);
    }

    @Override
    public void store(Item item) {
        store(item, new Date(), item.getState());
    }

    @Override
    public void store(Item item, Date date, State state) {
        if (state == null) {
            logger.warn("Skip store... Received a null state for item '{}' of type '{}'", item,
                    item.getClass().getSimpleName());
            return;
        }

        // Connect to H2 server if we're not already connected
        if (!connectToDatabase()) {
            logger.warn("H2: No connection to database. Can not persist item '{}'", item.getName());
            return;
        }

        final String tableName = getTableName(item.getName());

        if (!itemCache.contains(item.getName())) {
            itemCache.add(item.getName());
            if (createTable(tableName)) {
            } else {
                logger.error("H2: Could not create table for item '{}'", item.getName());
                return;
            }
        }

        // Firstly, try an INSERT. This will work 99.9% of the time
        if (!insert(tableName, date, state)) {
            // The INSERT failed. This might be because we tried persisting data too quickly, or it might be
            // because we really want to UPDATE the data.
            // So, let's try an update. If the reason for the exception isn't due to the primary key already
            // existing, then we'll throw another exception.
            // Note that H2 stores times using the Java Date class, so resolution is milliseconds. We really
            // shouldn't be persisting data that quickly!
            if (!update(tableName, date, state)) {
                logger.error("H2: Could not store item '{}' in database.", item.getName());
                return;
            }
        }
        logger.debug("H2: Stored item '{}' state '{}'", item.getName(), state);
    }

    private boolean createTable(final String tableName) {
        final String sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s %s, %s %s,  %s %s, PRIMARY KEY(%s));",
                tableName, Column.TIME, SqlType.TIMESTAMP, Column.CLAZZ, SqlType.VARCHAR, Column.VALUE, SqlType.VARCHAR,
                Column.TIME);
        try (final Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
            return true;
        } catch (final SQLException ex) {
            logger.error("H2: create table failed; statement '{}'", sql, ex);
            return false;
        }
    }

    private boolean insert(final String tableName, final Date date, final State state) {
        final String sql = String.format("INSERT INTO %s (%s, %s, %s) VALUES(?,?,?);", tableName, Column.TIME,
                Column.CLAZZ, Column.VALUE);
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
            int i = 0;
            stmt.setTimestamp(++i, new Timestamp(date.getTime()));
            stmt.setString(++i, state.getClass().getSimpleName());
            stmt.setString(++i, state.toString());
            stmt.executeUpdate();
            return true;
        } catch (final SQLException ex) {
            logger.warn("H2: insert failed; statement '{}'", sql, ex);
            return false;
        }
    }

    private boolean update(final String tableName, final Date date, final State state) {
        final String sql = String.format("UPDATE %s SET %s = ?, %s = ? WHERE TIME = ?", tableName, Column.CLAZZ,
                Column.VALUE);
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
            int i = 0;
            stmt.setString(++i, state.getClass().getSimpleName());
            stmt.setString(++i, state.toString());
            stmt.setTimestamp(++i, new Timestamp(date.getTime()));
            stmt.executeUpdate();
            return true;
        } catch (final SQLException ex) {
            logger.trace("H2: update failed; statement '{}'", sql, ex);
            return false;
        }
    }

    @Override
    public void updated(Dictionary<String, ?> config) throws ConfigurationException {
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        // Connect to H2 server if we're not already connected
        if (!connectToDatabase()) {
            logger.warn("Query aborted on item {} - H2 not connected!", filter.getItemName());
            return Collections.emptyList();
        }

        // Get the item name from the filter
        final String itemName = filter.getItemName();

        final FilterWhere filterWhere = getFilterWhere(filter);

        final String queryString = String.format("SELECT %s, %s, %s FROM %s%s%s%s", Column.TIME, Column.CLAZZ,
                Column.VALUE, getTableName(filter.getItemName()), filterWhere.prepared, getFilterStringOrder(filter),
                getFilterStringLimit(filter));

        try (final PreparedStatement st = connection.prepareStatement(queryString)) {
            int i = 0;
            if (filterWhere.begin) {
                st.setTimestamp(++i, new Timestamp(filter.getBeginDate().getTime()));
            }
            if (filterWhere.end) {
                st.setTimestamp(++i, new Timestamp(filter.getEndDate().getTime()));
            }

            // Turn use of the cursor on.
            st.setFetchSize(50);

            try (final ResultSet rs = st.executeQuery()) {
                final List<HistoricItem> items = new ArrayList<>();
                while (rs.next()) {
                    final Date time;
                    final String clazz;
                    final String value;

                    i = 0;
                    time = rs.getTimestamp(++i);
                    clazz = rs.getString(++i);
                    value = rs.getString(++i);
                    logger.trace("itemName: {}, time: {}, clazz: {}, value: {}", itemName, time, clazz, value);

                    final State state;
                    if (!stateClasses.containsKey(clazz)) {
                        if (itemRegistry != null) {
                            try {
                                final Item item = itemRegistry.getItem(itemName);
                                if (item != null) {
                                    for (final Class<? extends State> it : item.getAcceptedDataTypes()) {
                                        final String key = it.getSimpleName();
                                        if (!stateClasses.containsKey(key)) {
                                            addStateClass(it);
                                            logger.warn("Add new state class '{}'", clazz);
                                        }
                                    }
                                }
                            } catch (final ItemNotFoundException ex) {
                                logger.warn("Cannot lookup state class because item '{}' is not known.", itemName, ex);
                                continue;
                            }
                        }
                    }

                    if (stateClasses.containsKey(clazz)) {
                        state = TypeParser.parseState(stateClasses.get(clazz), value);
                    } else {
                        logger.warn("Unknown state class '{}'", clazz);
                        continue;
                    }

                    final H2HistoricItem sqlItem = new H2HistoricItem(itemName, state, time);
                    items.add(sqlItem);
                }
                return items;
            }

        } catch (final SQLException ex) {
            logger.error("H2: Error running query", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public Set<PersistenceItemInfo> getItemInfo() {
        // Connect to H2 server if we're not already connected
        if (!connectToDatabase()) {
            logger.warn("H2: No connection to database.");
            return Collections.emptySet();
        }

        // Retrieve the table array
        try (final Statement st = connection.createStatement()) {
            final String queryString = String.format(
                    "SELECT TABLE_NAME, ROW_COUNT_ESTIMATE FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='%s';",
                    Schema.ITEM);

            // Turn use of the cursor on.
            st.setFetchSize(50);

            try (final ResultSet rs = st.executeQuery(queryString)) {

                Set<PersistenceItemInfo> items = new HashSet<PersistenceItemInfo>();
                while (rs.next()) {
                    try (final Statement stTimes = connection.createStatement()) {
                        final String minMax = String.format("SELECT MIN(%s), MAX(%s) FROM %s", Column.TIME, Column.TIME,
                                getTableName(rs.getString(1)));
                        try (final ResultSet rsTimes = stTimes.executeQuery(minMax)) {

                            final Date earliest;
                            final Date latest;
                            if (rsTimes.next()) {
                                earliest = rsTimes.getTimestamp(1);
                                latest = rsTimes.getTimestamp(2);
                            } else {
                                earliest = null;
                                latest = null;
                            }
                            final H2PersistenceItem item = new H2PersistenceItem(rs.getString(1), rs.getInt(2),
                                    earliest, latest);
                            items.add(item);
                        }
                    }
                }
                return items;
            }

        } catch (final SQLException ex) {
            logger.error("H2: Error running query", ex);
            return Collections.emptySet();
        }
    }

    @Override
    public boolean remove(FilterCriteria filter) throws InvalidParameterException {
        // Connect to H2 server if we're not already connected
        if (!connectToDatabase()) {
            logger.warn("H2: No connection to database.");
            return false;
        }

        if (filter == null || filter.getItemName() == null) {
            throw new InvalidParameterException(
                    "Invalid filter. Filter must be specified and item name must be supplied.");
        }

        final FilterWhere filterWhere = getFilterWhere(filter);

        final String queryString = String.format("DELETE FROM %s%s;", getTableName(filter.getItemName()),
                filterWhere.prepared);

        // Retrieve the table array
        try (final PreparedStatement st = connection.prepareStatement(queryString)) {
            int i = 0;
            if (filterWhere.begin) {
                st.setTimestamp(++i, new Timestamp(filter.getBeginDate().getTime()));
            }
            if (filterWhere.end) {
                st.setTimestamp(++i, new Timestamp(filter.getEndDate().getTime()));
            }

            st.execute(queryString);
            // final int rowsDeleted = st.getUpdateCount();

            // Do some housekeeping...
            // See how many rows remain - if it's 0, we should remove the table from the database
            try (final ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + getTableName(filter.getItemName()))) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    final String drop = "DROP TABLE " + getTableName(filter.getItemName());
                    st.execute(drop);
                }

                return true;
            }
        } catch (final SQLException ex) {
            logger.error("H2: Error running query", ex);
            return false;
        }
    }

    /**
     * Checks if we have a database connection
     *
     * @return true if connection has been established, false otherwise
     */
    private boolean isConnected() {
        // Check if connection is valid
        try {
            if (connection != null && !connection.isValid(5000)) {
                logger.error("H2: Connection is not valid!");
            }
        } catch (final SQLException ex) {
            logger.error("H2: Error while checking connection", ex);
        }
        return connection != null;
    }

    /**
     * Connects to the database
     */
    private boolean connectToDatabase() {
        // First, check if we're connected
        if (isConnected() == true) {
            return true;
        }

        // We're not connected, so connect
        try {
            logger.info("H2: Connecting to database");

            final String folderName = ConfigConstants.getUserDataFolder() + "/h2";

            // Create path for serialization.
            final File folder = new File(folderName);
            if (!folder.exists() && !folder.mkdirs() && !folder.exists()) {
                logger.error("Cannot create directory.");
                return false;
            }

            final String databaseFileName = folderName + "/smarthome";

            String url = h2Url + databaseFileName;

            // Disable logging and defrag on shutdown
            url += ";TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0;DEFRAG_ALWAYS=true;";
            connection = DriverManager.getConnection(url);

            logger.info("H2: Connected to database {}", databaseFileName);

            try (final Statement statement = connection.createStatement()) {
                for (final String schema : new String[] { Schema.ITEM, Schema.METAINFO }) {
                    statement.execute(String.format("CREATE SCHEMA IF NOT EXISTS %s;", schema));
                }
                // statement.executeUpdate(String.format("SET SCHEMA %s;", Schema.ITEM));
            }
        } catch (final RuntimeException | SQLException ex) {
            logger.error("H2: Failed connecting to the database", ex);
        }

        return isConnected();
    }

    /**
     * Disconnects from the database
     */
    private void disconnectFromDatabase() {
        logger.debug("H2: Disconnecting from database.");
        if (connection != null) {
            try {
                connection.close();
                logger.debug("H2: Disconnected from database.");
            } catch (final SQLException ex) {
                logger.error("H2: Failed disconnecting from the database", ex);
            }
            connection = null;
        }
    }

    private String getTableName(String itemName) {
        return String.format("%s.\"%s\"", Schema.ITEM, itemName);
    }

    private class FilterWhere {
        public final boolean begin;
        public final boolean end;
        public String prepared;

        public FilterWhere(final FilterCriteria filter) {
            this.begin = filter.getBeginDate() != null;
            this.end = filter.getEndDate() != null;
            prepared = "";
        }
    }

    private FilterWhere getFilterWhere(final FilterCriteria filter) {
        final FilterWhere filterWhere = new FilterWhere(filter);

        if (filterWhere.begin) {
            if (filterWhere.prepared.isEmpty()) {
                filterWhere.prepared += " WHERE";
            } else {
                filterWhere.prepared += " AND";
            }
            filterWhere.prepared += String.format(" %s >= ?", Column.TIME);
        }
        if (filterWhere.end) {
            if (filterWhere.prepared.isEmpty()) {
                filterWhere.prepared += " WHERE";
            } else {
                filterWhere.prepared += " AND";
            }
            filterWhere.prepared += String.format(" %s <= ?", Column.TIME);
        }
        return filterWhere;
    }

    private String getFilterStringOrder(FilterCriteria filter) {
        if (filter.getOrdering() == Ordering.ASCENDING) {
            return String.format(" ORDER BY %s ASC", Column.TIME);
        } else {
            return String.format(" ORDER BY %s DESC", Column.TIME);
        }
    }

    private String getFilterStringLimit(FilterCriteria filter) {
        if (filter.getPageSize() != 0x7fffffff) {
            return " LIMIT " + filter.getPageSize() + " OFFSET " + (filter.getPageNumber() * filter.getPageSize());
        } else {
            return "";
        }
    }
}
