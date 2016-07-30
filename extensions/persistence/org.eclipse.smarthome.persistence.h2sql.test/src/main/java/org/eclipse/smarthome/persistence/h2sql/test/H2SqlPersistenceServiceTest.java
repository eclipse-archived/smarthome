package org.eclipse.smarthome.persistence.h2sql.test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;
import java.util.Date;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemNotUniqueException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.DateTimeItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.LocationItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.PlayerItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.FilterCriteria.Ordering;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.persistence.h2sql.internal.H2SqlPersistenceService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class H2SqlPersistenceServiceTest implements ItemRegistry {
    H2SqlPersistenceService service;
    Item myItem;

    @Before
    public void Initialise() {
        service = new H2SqlPersistenceService();
        service.setItemRegistry(this);
    }

    @After
    public void Shutdown() {
        // clean up database files ...
        deleteDir(new File(ConfigConstants.getUserDataFolder()));
    }

    void TestType(Item item, State state) {
        myItem = item;

        Date date = new Date(946684800000L);
        service.store(item, date, state);

        FilterCriteria filter = new FilterCriteria();
        filter.setBeginDate(date);
        filter.setEndDate(date);
        filter.setItemName(item.getName());

        Iterable<HistoricItem> history = service.query(filter);
        assertNotNull(history);
        assertTrue(history.iterator().hasNext());

        HistoricItem historicItem = history.iterator().next();
        assertTrue(historicItem.getTimestamp().getTime() == date.getTime());
        assertTrue(historicItem.getState().equals(state));
    }

    @Test
    public void NumberItem() {
        TestType(new NumberItem("NumberItem"), new DecimalType(1234.56));
    }

    @Test
    public void CallItem() {
        // TestType(new CallItem("CallItem"), new CallType("10,20,30"));
    }

    @Test
    public void ColorItem() {
        TestType(new ColorItem("ColorItem"),
                new HSBType(new DecimalType(10), new PercentType(20), new PercentType(30)));
        TestType(new ColorItem("ColorItem"), new PercentType(66));
    }

    @Test
    public void ContactItem() {
        TestType(new ContactItem("ContactItem"), OpenClosedType.OPEN);
        TestType(new ContactItem("ContactItem"), OpenClosedType.CLOSED);
    }

    @Test
    public void DateTimeItem() {
        TestType(new DateTimeItem("DateTimeItem"), new DateTimeType("1999-10-11T01:02:03:456Z"));
    }

    @Test
    public void DimmerItem() {
        TestType(new DimmerItem("DimmerItem"), new PercentType(54));
    }

    @Test
    public void LocationItem() {
        TestType(new LocationItem("LocationItem"), new PointType("12.345678,-23.456789"));
    }

    @Test
    public void PlayerItem() {
        TestType(new PlayerItem("PlayerItem"), PlayPauseType.PAUSE);
        TestType(new PlayerItem("PlayerItem"), PlayPauseType.PLAY);
        TestType(new PlayerItem("PlayerItem"), RewindFastforwardType.REWIND);
        TestType(new PlayerItem("PlayerItem"), RewindFastforwardType.FASTFORWARD);
    }

    @Test
    public void RollershutterItem() {
        TestType(new RollershutterItem("RollershutterItem"), new PercentType(23));
    }

    private String createString(int length) {
        StringBuffer outputBuffer = new StringBuffer(length);
        for (int i = 0; i < length; i++) {
            outputBuffer.append(" ");
        }
        return outputBuffer.toString();
    }

    @Test
    public void StringItem() {
        TestType(new StringItem("StringItem"), new StringType("Hello"));
        TestType(new StringItem("StringItem"), new StringType(createString(254)));
        TestType(new StringItem("StringItem"), new StringType(createString(65000)));
    }

    @Test
    public void SwitchItem() {
        TestType(new SwitchItem("SwitchItem"), OnOffType.ON);
        TestType(new SwitchItem("SwitchItem"), OnOffType.OFF);
    }

    void BenchmarkType(Item item, State state, int counter) {
        myItem = item;

        Initialise();
        long storeStart;
        long storeEnd;
        long queryStart;
        long queryEnd;

        storeStart = System.currentTimeMillis();
        for (int c = 0; c < counter; c++) {
            Date date = new Date(946684800000L + c);
            service.store(item, date, state);
        }
        storeEnd = System.currentTimeMillis();

        FilterCriteria filter = new FilterCriteria();
        filter.setBeginDate(new Date(946684800000L));
        filter.setEndDate(new Date(956684800000L));
        filter.setItemName(item.getName());
        filter.setOrdering(Ordering.ASCENDING);

        queryStart = System.currentTimeMillis();
        Iterable<HistoricItem> history = service.query(filter);
        queryEnd = System.currentTimeMillis();

        assertNotNull(history);
        assertTrue(history.iterator().hasNext());

        final String folderName = ConfigConstants.getUserDataFolder() + "/h2sql/";
        service.deactivate();

        System.out.println(item.getName() + ",  " + (storeEnd - storeStart) + ",  " + (queryEnd - queryStart) + ",  "
                + getFolderSize(new File(folderName)));

        Shutdown();
    }

    void doBenchmark(int counter) {
        System.out.println("Benchmark " + counter + " cycles");
        BenchmarkType(new ContactItem("ContactItem1"), OpenClosedType.OPEN, counter);
        BenchmarkType(new NumberItem("NumberItem1"), new DecimalType(1234.56), counter);
        BenchmarkType(new DateTimeItem("DateTimeItem1"), new DateTimeType("1999-10-11T01:02:03:456Z"), counter);
        BenchmarkType(new PlayerItem("PlayerItem1"), RewindFastforwardType.REWIND, counter);
        BenchmarkType(new RollershutterItem("RollershutterItem"), new PercentType(23), counter);
        BenchmarkType(new StringItem("StringItem"), new StringType("Hello"), counter);
        BenchmarkType(new SwitchItem("SwitchItem"), OnOffType.ON, counter);
        BenchmarkType(new ContactItem("ContactItem2"), OpenClosedType.OPEN, counter);
        BenchmarkType(new NumberItem("NumberItem2"), new DecimalType(1234.56), counter);
        BenchmarkType(new DateTimeItem("DateTimeItem2"), new DateTimeType("1999-10-11T01:02:03:456Z"), counter);
        BenchmarkType(new PlayerItem("PlayerItem2"), RewindFastforwardType.REWIND, counter);
    }

    // @Test
    public void Benchmark10000() {
        TestType(new RollershutterItem("RollershutterItem"), new PercentType(23));

        doBenchmark(10000);
    }

    // @Test
    public void Benchmark100000() {
        TestType(new RollershutterItem("RollershutterItem"), new PercentType(23));

        doBenchmark(100000);
    }

    private long getFolderSize(File folder) {
        long length = 0;
        File[] files = folder.listFiles();

        int count = files.length;

        for (int i = 0; i < count; i++) {
            if (files[i].isFile()) {
                length += files[i].length();
            } else {
                length += getFolderSize(files[i]);
            }
        }
        return length;
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty or this is a file so delete it
        return dir.delete();
    }

    @Override
    public void addRegistryChangeListener(RegistryChangeListener<Item> listener) {
    }

    @Override
    public Collection<Item> getAll() {
        return null;
    }

    @Override
    public Item get(String key) {
        return null;
    }

    @Override
    public void removeRegistryChangeListener(RegistryChangeListener<Item> listener) {
    }

    @Override
    public Item add(Item element) {
        return null;
    }

    @Override
    public Item update(Item element) {
        return null;
    }

    @Override
    public Item remove(String key) {
        return null;
    }

    @Override
    public Item getItem(String name) throws ItemNotFoundException {
        return myItem;
    }

    @Override
    public Item getItemByPattern(String name) throws ItemNotFoundException, ItemNotUniqueException {
        return null;
    }

    @Override
    public Collection<Item> getItems() {
        return null;
    }

    @Override
    public Collection<Item> getItemsOfType(String type) {
        return null;
    }

    @Override
    public Collection<Item> getItems(String pattern) {
        return null;
    }

    @Override
    public Collection<Item> getItemsByTag(String... tags) {
        return null;
    }

    @Override
    public Collection<Item> getItemsByTagAndType(String type, String... tags) {
        return null;
    }

    @Override
    public <T extends GenericItem> Collection<T> getItemsByTag(Class<T> typeFilter, String... tags) {
        return null;
    }

    @Override
    public Item remove(String itemName, boolean recursive) {
        return null;
    }

}
