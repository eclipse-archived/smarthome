/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.persistence.h2.internal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of a H2 {@link PersistenceService}.
 *
 * @author Chris Jackson - Initial contribution
 * @author Markus Rathgeb - Rewrite the implementation of Chris Jackson
 */
public class H2SqlPersistenceService extends H2AbstractPersistenceService {
    private static class Schema extends H2AbstractPersistenceService.Schema {
        public static final String ITEM = "SMARTHOME";
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public H2SqlPersistenceService() {
        super(Schema.ITEM);
    }

    @Override
    public String getId() {
        return "h2sql";
    }

    private String getSqlType(final Class<? extends State> stateClass) {
        if (stateClass.isAssignableFrom(PercentType.class)) {
            return SqlType.TINYINT;
        } else if (stateClass.isAssignableFrom(DecimalType.class)) {
            return SqlType.DECIMAL;
        } else {
            return SqlType.VARCHAR;
        }
    }

    private void setStateValue(final PreparedStatement stmt, int pos, final State state) throws SQLException {
        if (state instanceof PercentType) {
            stmt.setInt(pos, ((PercentType) state).intValue());
        } else if (state instanceof DecimalType) {
            stmt.setBigDecimal(pos, ((DecimalType) state).toBigDecimal());
        } else {
            stmt.setString(pos, state.toString());
        }
    }

    @Override
    protected State getStateForItem(final Item item) {
        // Do some type conversion to ensure we know the data type.
        // This is necessary for items that have multiple types and may return their
        // state in a format that's not preferred or compatible with the H2SQL type.
        // eg. DimmerItem can return OnOffType (ON, OFF), or PercentType (0-100).
        // We need to make sure we cover the best type for serialisation.
        if (item instanceof DimmerItem || item instanceof RollershutterItem) {
            return item.getStateAs(PercentType.class);
        } else if (item instanceof ColorItem) {
            return item.getStateAs(HSBType.class);
        } else {
            // All other items should return the best format by default
            return item.getState();
        }
    }

    @Override
    protected boolean createTable(Class<? extends State> stateClass, final String tableName) {
        final String sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s %s, %s %s, PRIMARY KEY(%s));", tableName,
                Column.TIME, SqlType.TIMESTAMP, Column.VALUE, getSqlType(stateClass), Column.TIME);
        try (final Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
            return true;
        } catch (final SQLException ex) {
            logger.error("{}: create table failed; statement '{}'", getId(), sql, ex);
            return false;
        }
    }

    @Override
    protected boolean insert(final String tableName, final Date date, final State state) {
        final String sql = String.format("INSERT INTO %s (%s, %s) VALUES(?,?);", tableName, Column.TIME, Column.VALUE);
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
            int i = 0;
            stmt.setTimestamp(++i, new Timestamp(date.getTime()));
            setStateValue(stmt, ++i, state);
            stmt.executeUpdate();
            return true;
        } catch (final SQLException ex) {
            logger.warn("{}: insert failed; statement '{}'", getId(), sql, ex);
            return false;
        }
    }

    @Override
    protected boolean update(final String tableName, final Date date, final State state) {
        final String sql = String.format("UPDATE %s SET %s = ? WHERE TIME = ?", tableName, Column.VALUE);
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
            int i = 0;
            setStateValue(stmt, ++i, state);
            stmt.setTimestamp(++i, new Timestamp(date.getTime()));
            stmt.executeUpdate();
            return true;
        } catch (final SQLException ex) {
            logger.trace("{}: update failed; statement '{}'", getId(), sql, ex);
            return false;
        }
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        // Connect to H2 server if we're not already connected
        if (!connectToDatabase()) {
            logger.warn("{}: Query aborted on item {} - H2 not connected!", getId(), filter.getItemName());
            return Collections.emptyList();
        }

        // Get the item name from the filter
        final String itemName = filter.getItemName();

        // Get the item name from the filter
        // Also get the Item object so we can determine the type
        Item item = null;
        try {
            if (itemRegistry != null) {
                item = itemRegistry.getItem(itemName);
            }
        } catch (ItemNotFoundException e) {
            logger.error("H2SQL: Unable to get item type for {}", itemName);
            logger.error("     : " + e.getMessage());

            // Set type to null - data will be returned as StringType
            item = null;
        }
        if (item instanceof GroupItem) {
            // For Group Items is BaseItem needed to get correct Type of Value.
            item = GroupItem.class.cast(item).getBaseItem();
        }

        final FilterWhere filterWhere = getFilterWhere(filter);

        final String queryString = String.format("SELECT %s, %s FROM %s%s%s%s", Column.TIME, Column.VALUE,
                getTableName(filter.getItemName()), filterWhere.prepared, getFilterStringOrder(filter),
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
                    final String value;

                    i = 0;
                    time = rs.getTimestamp(++i);
                    value = rs.getString(++i);
                    logger.trace("{}: itemName: {}, time: {}, value: {}", getId(), itemName, time, value);

                    final State state;
                    if (item == null) {
                        state = new StringType(value);
                    } else {
                        state = TypeParser.parseState(item.getAcceptedDataTypes(), value);
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
}
