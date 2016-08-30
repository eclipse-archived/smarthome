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
import java.text.SimpleDateFormat;
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
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.FilterCriteria.Ordering;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.ModifiablePersistenceService;
import org.eclipse.smarthome.core.persistence.PersistenceItemInfo;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
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
 * @author Markus Rathgeb - Use prepared statement and try-with-resources
 */
public class H2PersistenceService implements ModifiablePersistenceService, ManagedService {

    private final Logger logger = LoggerFactory.getLogger(H2PersistenceService.class);

    private final String h2Url = "jdbc:h2:file:";
    private final String schema = "SMARTHOME";

    private final SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private ItemRegistry itemRegistry;
    private I18nProvider i18nProvider;

    private Connection connection;
    private final Map<Class<? extends Item>, String> sqlTypes = new HashMap<>();
    private final List<String> itemCache = new ArrayList<>();

    private BundleContext bundleContext;

    public H2PersistenceService() {
        // Initialize the type array
        sqlTypes.put(DimmerItem.class, "TINYINT");
        sqlTypes.put(NumberItem.class, "DECIMAL");
        sqlTypes.put(RollershutterItem.class, "TINYINT");
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
        // Do some type conversion to ensure we know the data type.
        // This is necessary for items that have multiple types and may return their
        // state in a format that's not preferred or compatible with the H2 type.
        // eg. DimmerItem can return OnOffType (ON, OFF), or PercentType (0-100).
        // We need to make sure we cover the best type for serialization.
        final State state;
        if (item instanceof DimmerItem || item instanceof RollershutterItem) {
            state = item.getStateAs(PercentType.class);
        } else if (item instanceof ColorItem) {
            state = item.getStateAs(HSBType.class);
        } else {
            // All other items should return the best format by default
            state = item.getState();
        }
        store(item, new Date(), state);
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
            final String sqlType = getSqlType(item);
            if (createTable(tableName, sqlType)) {
            } else {
                logger.error("H2: Could not create table for item '{}'", item.getName());
                return;
            }
        }

        final String value = state.toString();
        // Firstly, try an INSERT. This will work 99.9% of the time
        if (!insert(tableName, date, value)) {
            // The INSERT failed. This might be because we tried persisting data too quickly, or it might be
            // because we really want to UPDATE the data.
            // So, let's try an update. If the reason for the exception isn't due to the primary key already
            // existing, then we'll throw another exception.
            // Note that H2 stores times using the Java Date class, so resolution is milliseconds. We really
            // shouldn't be persisting data that quickly!
            if (!update(tableName, date, value)) {
                logger.error("H2: Could not store item '{}' in database.", item.getName());
                return;
            }
        }
        logger.debug("H2: Stored item '{}' as '{}'", item.getName(), value);
    }

    private boolean createTable(final String tableName, final String sqlType) {
        final String sql = String.format("CREATE TABLE IF NOT EXISTS %s (Time DATETIME, Value %s, PRIMARY KEY(Time));",
                tableName, sqlType);
        try (final Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
            return true;
        } catch (final SQLException ex) {
            logger.error("H2: create table failed; statement '{}'", sql, ex);
            return false;
        }
    }

    private boolean insert(final String tableName, final Date date, final String value) {
        final String sql = String.format("INSERT INTO %s (TIME, VALUE) VALUES(?,?);", tableName);
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, sqlDateFormat.format(date));
            stmt.setString(2, value);
            stmt.executeUpdate();
            return true;
        } catch (final SQLException ex) {
            logger.warn("H2: insert failed; statement '{}'", sql, ex);
            return false;
        }
    }

    private boolean update(final String tableName, final Date date, final String value) {
        final String sql = String.format("UPDATE %s SET VALUE = ? WHERE TIME = ?", tableName);
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.setString(2, sqlDateFormat.format(date));
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

        // Also get the Item object so we can determine the type
        Item item = null;
        if (itemRegistry != null) {
            try {
                item = itemRegistry.getItem(itemName);
            } catch (final ItemNotFoundException ex) {
                logger.error("H2: Unable to get item type for {}", itemName, ex);
                // Set type to null - data will be returned as StringType
                item = null;
            }
        }
        if (item instanceof GroupItem) {
            // For Group Items is BaseItem needed to get correct Type of Value.
            item = GroupItem.class.cast(item).getBaseItem();
        }

        final String filterString = getFilterStringWhere(filter) + getFilterStringOrder(filter)
                + getFilterStringLimit(filter);

        try (final Statement st = connection.createStatement()) {
            String queryString = "SELECT Time, Value FROM " + getTableName(filter.getItemName());
            if (!filterString.isEmpty()) {
                queryString += filterString;
            }

            // Turn use of the cursor on.
            st.setFetchSize(50);

            try (final ResultSet rs = st.executeQuery(queryString)) {
                final List<HistoricItem> items = new ArrayList<>();
                while (rs.next()) {
                    final State state;
                    if (item == null) {
                        state = new StringType(rs.getString(2));
                    } else {
                        state = TypeParser.parseState(item.getAcceptedDataTypes(), rs.getString(2));
                    }

                    final H2HistoricItem sqlItem = new H2HistoricItem(itemName, state, rs.getTimestamp(1));
                    items.add(sqlItem);
                }
                return items;
            }

        } catch (final SQLException ex) {
            logger.error("H2: Error running query", ex);
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<PersistenceItemInfo> getItemInfo() {
        // Connect to H2 server if we're not already connected
        if (!connectToDatabase()) {
            logger.warn("H2: No connection to database.");
            return Collections.emptySet();
        }

        // Retrieve the table array
        try (final Statement st = connection.createStatement()) {
            final String queryString = "SELECT TABLE_NAME, ROW_COUNT_ESTIMATE FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='"
                    + schema + "'";

            // Turn use of the cursor on.
            st.setFetchSize(50);

            try (final ResultSet rs = st.executeQuery(queryString)) {

                Set<PersistenceItemInfo> items = new HashSet<PersistenceItemInfo>();
                while (rs.next()) {
                    try (final Statement stTimes = connection.createStatement()) {
                        final String minMax = "SELECT MIN(Time), MAX(Time) FROM " + getTableName(rs.getString(1));
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

    /**
     * {@inheritDoc}
     */
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

        final String filterString = getFilterStringWhere(filter);

        // Retrieve the table array
        try (final Statement st = connection.createStatement()) {
            final String queryString = "DELETE FROM " + getTableName(filter.getItemName()) + filterString;

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
                statement.executeUpdate("CREATE SCHEMA IF NOT EXISTS " + schema + ";");
                statement.executeUpdate("SET SCHEMA " + schema + ";");
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
        return schema + ".\"" + itemName + "\"";
    }

    private String getSqlType(final Item item) {
        final Class<?> itemClass = item.getClass();
        if (sqlTypes.containsKey(itemClass)) {
            return sqlTypes.get(itemClass);
        } else {
            // Default type is 'String'
            return "VARCHAR";
        }
    }

    private String getFilterStringWhere(FilterCriteria filter) {
        String filterString = new String();

        if (filter.getBeginDate() != null) {
            if (filterString.isEmpty()) {
                filterString += " WHERE";
            } else {
                filterString += " AND";
            }
            filterString += " TIME>='" + sqlDateFormat.format(filter.getBeginDate()) + "'";
        }
        if (filter.getEndDate() != null) {
            if (filterString.isEmpty()) {
                filterString += " WHERE";
            } else {
                filterString += " AND";
            }
            filterString += " TIME<='" + sqlDateFormat.format(filter.getEndDate().getTime()) + "'";
        }
        return filterString;
    }

    private String getFilterStringOrder(FilterCriteria filter) {
        if (filter.getOrdering() == Ordering.ASCENDING) {
            return " ORDER BY Time ASC";
        } else {
            return " ORDER BY Time DESC";
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
