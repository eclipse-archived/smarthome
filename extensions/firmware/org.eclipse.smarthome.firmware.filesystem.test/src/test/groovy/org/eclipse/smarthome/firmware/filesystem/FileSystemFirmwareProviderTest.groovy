/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.firmware.filesystem

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import java.nio.file.Files;

import org.eclipse.smarthome.config.core.ConfigConstants
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.firmware.Firmware
import org.eclipse.smarthome.core.thing.firmware.FirmwareUID
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore;
import org.junit.Test

/**
 * Testing the {@link FileSystemFirmwareProvider}.
 *
 * @author Andre Fuechsel
 * @author Dimitar Ivanov
 */
class FileSystemFirmwareProviderTest {

    static USER_DIR = "userdata";
    static ROOT_DIR = USER_DIR + "/firmware";

    def DEFAULT_PROCESS_EVENT_TIMEOUT = 2000

    static BINDING_ID = "simpleBinding"
    static THING_TYPE_ID1 = "simpleThingType1"
    static THING_TYPE_ID2 = "simpleThingType2"
    static THING_TYPE_UID1 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID1)
    static THING_TYPE_UID2 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID2)

    static V111 = "V111"
    static IMAGE_V111 = "fw-v1.1.1-20151212.bin"
    static VERSION_111 = "1.1.1"
    static HASH_111 = "111"
    static V112 = "V112"
    static IMAGE_V112 = "fw-v1.1.2-20160321.bin"
    static VERSION_112 = "1.1.2"
    static HASH_112 = "112"
    static V113 = "V113"
    static IMAGE_V113 = "fw-v1.1.3-20160330.bin"
    static VERSION_113 = "1.1.3"
    static HASH_113 = "113"

    static MODEL_A = "Model A"
    static MODEL_B = "Model B"
    static VENDOR = "Company Ltd."
    static DESCRIPTION_DE = "Das ist eine Kamera";
    static DESCRIPTION_EN = "This is a camera";
    static CHANGELOG_DE = "Das ist das Changelog"
    static CHANGELOG_EN = "This is the change log"
    static CHANGELOG_URL = "http://www.firma.com"

    private Locale defaultLocale

    static FileSystemFirmwareProvider PROVIDER = new FileSystemFirmwareProvider()

    static File BINDING_DIR = new File(ROOT_DIR, BINDING_ID)
    static File THING_TYPE_1_DIR = new File(BINDING_DIR, THING_TYPE_ID1)
    static File THING_TYPE_2_DIR = new File(BINDING_DIR , THING_TYPE_ID2)

    Locale deLocale = new Locale.Builder().setLanguage("de").setRegion("DE").build()

    Locale enLocale = new Locale.Builder().setLanguage("en").setRegion("EN").build()

    @BeforeClass
    static void setupBeforeClass() {
        def userRoot = new File(USER_DIR)
        boolean isRootClean = userRoot.deleteDir()
        assertThat "The root directory cleanup failed", isRootClean, is(true)
        def root = new File(ROOT_DIR)

        BINDING_DIR.mkdirs()
        THING_TYPE_1_DIR.mkdirs()
        THING_TYPE_2_DIR.mkdirs()

        def file = new File(THING_TYPE_1_DIR.getPath(), IMAGE_V111)
        file << IMAGE_V111;

        file = new File(THING_TYPE_1_DIR.getPath(), V111 + ".properties")
        file << "version = " + VERSION_111 + "\n"
        file << "image = " + IMAGE_V111 + "\n"
        file << "model = " + MODEL_A + "\n"
        file << "vendor = " + VENDOR + "\n"
        file << "description = " + DESCRIPTION_EN + "\n"
        file << "changelog = " + CHANGELOG_EN + "\n"
        file << "onlineChangelog = " + CHANGELOG_URL + "\n"
        file << "md5hash = " + HASH_111 + "\n"

        file = new File(THING_TYPE_1_DIR.getPath(), V111 + "_de_DE.properties")
        file << "description = " + DESCRIPTION_DE + "\n"
        file << "changelog = " + CHANGELOG_DE + "\n"
        file << "onlineChangelog = " + CHANGELOG_URL + "\n"

        file = new File(THING_TYPE_1_DIR.getPath(), IMAGE_V112)
        file << IMAGE_V112;

        file = new File(THING_TYPE_1_DIR.getPath(), V112 + ".properties")
        file << "version = " + VERSION_112 + "\n"
        file << "image = " + IMAGE_V112 + "\n"
        file << "model = " + MODEL_A + "\n"
        file << "vendor = " + VENDOR + "\n"
        file << "description = " + DESCRIPTION_EN + "\n"
        file << "prerequisiteVersion = " + V111 + "\n"
        file << "changelog = " + CHANGELOG_EN + "\n"
        file << "onlineChangelog = " + CHANGELOG_URL + "\n"
        file << "md5hash = " + HASH_112 + "\n"

        file = new File(THING_TYPE_1_DIR.getPath(), V112 + "_de_DE.properties")
        file << "description = " + DESCRIPTION_DE + "\n"
        file << "prerequisiteVersion = " + V111 + "\n"
        file << "changelog = " + CHANGELOG_DE + "\n"
        file << "onlineChangelog = " + CHANGELOG_URL + "\n"

        file = new File(THING_TYPE_2_DIR.getPath(), IMAGE_V111)
        file << IMAGE_V111;

        file = new File(THING_TYPE_2_DIR.getPath(), V111 + ".properties")
        file << "version = " + VERSION_111 + "\n"
        file << "image = " + IMAGE_V111 + "\n"
        file << "model = " + MODEL_B + "\n"
        file << "vendor = " + VENDOR + "\n"
        file << "description = " + DESCRIPTION_EN + "\n"
        file << "changelog = " + CHANGELOG_EN + "\n"
        file << "onlineChangelog = " + CHANGELOG_URL + "\n"
        file << "md5hash = " + HASH_111 + "\n"

        file = new File(THING_TYPE_2_DIR.getPath(), V111 + "_de_DE.properties")
        file << "description = " + DESCRIPTION_DE + "\n"
        file << "changelog = " + CHANGELOG_DE + "\n"
        file << "onlineChangelog = " + CHANGELOG_URL + "\n"

        file = new File(THING_TYPE_2_DIR.getPath(), IMAGE_V112)
        file << IMAGE_V112;

        file = new File(THING_TYPE_2_DIR.getPath(), V112 + ".properties")
        file << "version = " + VERSION_112 + "\n"
        file << "image = " + IMAGE_V112 + "\n"
        file << "model = " + MODEL_B + "\n"
        file << "vendor = " + VENDOR + "\n"
        file << "description = " + DESCRIPTION_EN + "\n"
        file << "prerequisiteVersion = " + V111 + "\n"
        file << "changelog = " + CHANGELOG_EN + "\n"
        file << "onlineChangelog = " + CHANGELOG_URL + "\n"
        file << "md5hash = " + HASH_112 + "\n"

        file = new File(THING_TYPE_2_DIR.getPath(), V112 + "_de_DE.properties")
        file << "description = " + DESCRIPTION_DE + "\n"
        file << "prerequisiteVersion = " + V111 + "\n"
        file << "changelog = " + CHANGELOG_DE + "\n"
        file << "onlineChangelog = " + CHANGELOG_URL + "\n"

        System.setProperty(ConfigConstants.USERDATA_DIR_PROG_ARGUMENT, USER_DIR)
        PROVIDER.activate()
    }

    @AfterClass
    static void tearDownAfterClass() {
        PROVIDER.deactivate()
        def userRoot = new File(USER_DIR)
        userRoot.deleteDir()
        def root = new File(ROOT_DIR)
    }

    @Before
    void setup() {
        defaultLocale = Locale.getDefault()
        Locale.setDefault(Locale.ENGLISH)
    }

    @After
    void tearDown() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    void 'get firmware for thing type 1 with version 111 with german locale'() {
        def fw = PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID1, VERSION_111), deLocale)

        assertThat fw, is(not(null))
        assertThat fw.getUID(), is(new FirmwareUID(THING_TYPE_UID1, VERSION_111))
        assertThat fw.getModel(), is(equalTo(MODEL_A))
        assertThat fw.getDescription(), is(equalTo(DESCRIPTION_DE))
        assertThat fw.getVendor(), is(equalTo(VENDOR))
        assertThat fw.getPrerequisiteVersion(), is(null)
        assertThat fw.getChangelog(), is(equalTo(CHANGELOG_DE))
        assertThat fw.getOnlineChangelog().toString(), is(equalTo(CHANGELOG_URL))
        assertThat fw.getInputStream(), is(not(null))
        assertThat fw.getInputStream().readLines().get(0), is(IMAGE_V111)
        assertThat fw.getMd5Hash(), is(HASH_111)
    }

    @Test
    void 'get firmware for thing type 1 with version 111 with english locale'() {
        def fw = PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID1, VERSION_111), enLocale)

        assertThat fw, is(not(null))
        assertThat fw.getUID(), is(new FirmwareUID(THING_TYPE_UID1, VERSION_111))
        assertThat fw.getModel(), is(equalTo(MODEL_A))
        assertThat fw.getDescription(), is(equalTo(DESCRIPTION_EN))
        assertThat fw.getVendor(), is(equalTo(VENDOR))
        assertThat fw.getPrerequisiteVersion(), is(null)
        assertThat fw.getChangelog(), is(equalTo(CHANGELOG_EN))
        assertThat fw.getOnlineChangelog().toString(), is(equalTo(CHANGELOG_URL))
        assertThat fw.getInputStream(), is(not(null))
        assertThat fw.getInputStream().readLines().get(0), is(IMAGE_V111)
        assertThat fw.getMd5Hash(), is(HASH_111)
    }

    @Test
    void 'get firmware for thing type 1 with version 111 without locale'() {
        def fw = PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID1, VERSION_111))

        assertThat fw, is(not(null))
        assertThat fw.getUID(), is(new FirmwareUID(THING_TYPE_UID1, VERSION_111))
        assertThat fw.getModel(), is(equalTo(MODEL_A))
        assertThat fw.getDescription(), is(equalTo(DESCRIPTION_EN))
        assertThat fw.getVendor(), is(equalTo(VENDOR))
        assertThat fw.getPrerequisiteVersion(), is(null)
        assertThat fw.getChangelog(), is(equalTo(CHANGELOG_EN))
        assertThat fw.getOnlineChangelog().toString(), is(equalTo(CHANGELOG_URL))
        assertThat fw.getInputStream(), is(not(null))
        assertThat fw.getInputStream().readLines().get(0), is(IMAGE_V111)
        assertThat fw.getMd5Hash(), is(HASH_111)
    }

    @Test
    void 'get firmware for thing type 1 with version 112 with german locale'() {
        def fw = PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID1, VERSION_112), deLocale)

        assertThat fw, is(not(null))
        assertThat fw.getUID(), is(new FirmwareUID(THING_TYPE_UID1, VERSION_112))
        assertThat fw.getModel(), is(equalTo(MODEL_A))
        assertThat fw.getDescription(), is(equalTo(DESCRIPTION_DE))
        assertThat fw.getVendor(), is(equalTo(VENDOR))
        assertThat fw.getPrerequisiteVersion(), is(equalTo(V111))
        assertThat fw.getChangelog(), is(equalTo(CHANGELOG_DE))
        assertThat fw.getOnlineChangelog().toString(), is(equalTo(CHANGELOG_URL))
        assertThat fw.getInputStream(), is(not(null))
        assertThat fw.getInputStream().readLines().get(0), is(IMAGE_V112)
        assertThat fw.getMd5Hash(), is(HASH_112)
    }

    @Test
    void 'get firmware for thing type 1 with version 112 with english locale'() {
        def fw = PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID1, VERSION_112), enLocale)

        assertThat fw, is(not(null))
        assertThat fw.getUID(), is(new FirmwareUID(THING_TYPE_UID1, VERSION_112))
        assertThat fw.getModel(), is(equalTo(MODEL_A))
        assertThat fw.getDescription(), is(equalTo(DESCRIPTION_EN))
        assertThat fw.getVendor(), is(equalTo(VENDOR))
        assertThat fw.getPrerequisiteVersion(), is(equalTo(V111))
        assertThat fw.getChangelog(), is(equalTo(CHANGELOG_EN))
        assertThat fw.getOnlineChangelog().toString(), is(equalTo(CHANGELOG_URL))
        assertThat fw.getInputStream(), is(not(null))
        assertThat fw.getInputStream().readLines().get(0), is(IMAGE_V112)
        assertThat fw.getMd5Hash(), is(HASH_112)
    }

    @Test
    void 'get firmware for thing type 1 with version 112 with null locale'() {
        def fw = PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID1, VERSION_112), null)

        assertThat fw, is(not(null))
        assertThat fw.getUID(), is(new FirmwareUID(THING_TYPE_UID1, VERSION_112))
        assertThat fw.getModel(), is(equalTo(MODEL_A))
        assertThat fw.getDescription(), is(equalTo(DESCRIPTION_EN))
        assertThat fw.getVendor(), is(equalTo(VENDOR))
        assertThat fw.getPrerequisiteVersion(), is(equalTo(V111))
        assertThat fw.getChangelog(), is(equalTo(CHANGELOG_EN))
        assertThat fw.getOnlineChangelog().toString(), is(equalTo(CHANGELOG_URL))
        assertThat fw.getInputStream(), is(not(null))
        assertThat fw.getInputStream().readLines().get(0), is(IMAGE_V112)
        assertThat fw.getMd5Hash(), is(HASH_112)
    }

    @Test
    void 'get firmware for thing type 1 with version 112 without locale'() {
        def fw = PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID1, VERSION_112))

        assertThat fw, is(not(null))
        assertThat fw.getUID(), is(new FirmwareUID(THING_TYPE_UID1, VERSION_112))
        assertThat fw.getModel(), is(equalTo(MODEL_A))
        assertThat fw.getDescription(), is(equalTo(DESCRIPTION_EN))
        assertThat fw.getVendor(), is(equalTo(VENDOR))
        assertThat fw.getPrerequisiteVersion(), is(equalTo(V111))
        assertThat fw.getChangelog(), is(equalTo(CHANGELOG_EN))
        assertThat fw.getOnlineChangelog().toString(), is(equalTo(CHANGELOG_URL))
        assertThat fw.getInputStream(), is(not(null))
        assertThat fw.getInputStream().readLines().get(0), is(IMAGE_V112)
        assertThat fw.getMd5Hash(), is(HASH_112)
    }

    @Test
    void 'get firmware for thing type 2 with version 111 with german locale'() {
        def fw = PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID2, VERSION_111), deLocale)

        assertThat fw, is(not(null))
        assertThat fw.getUID(), is(new FirmwareUID(THING_TYPE_UID2, VERSION_111))
        assertThat fw.getModel(), is(equalTo(MODEL_B))
        assertThat fw.getDescription(), is(equalTo(DESCRIPTION_DE))
        assertThat fw.getVendor(), is(equalTo(VENDOR))
        assertThat fw.getPrerequisiteVersion(), is(null)
        assertThat fw.getChangelog(), is(equalTo(CHANGELOG_DE))
        assertThat fw.getOnlineChangelog().toString(), is(equalTo(CHANGELOG_URL))
        assertThat fw.getInputStream(), is(not(null))
        assertThat fw.getInputStream().readLines().get(0), is(IMAGE_V111)
        assertThat fw.getMd5Hash(), is(HASH_111)
    }

    @Test
    void 'get firmware for thing type 2 with version 111 with english locale'() {
        def fw = PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID2, VERSION_111), enLocale)

        assertThat fw, is(not(null))
        assertThat fw.getUID(), is(new FirmwareUID(THING_TYPE_UID2, VERSION_111))
        assertThat fw.getModel(), is(equalTo(MODEL_B))
        assertThat fw.getDescription(), is(equalTo(DESCRIPTION_EN))
        assertThat fw.getVendor(), is(equalTo(VENDOR))
        assertThat fw.getPrerequisiteVersion(), is(null)
        assertThat fw.getChangelog(), is(equalTo(CHANGELOG_EN))
        assertThat fw.getOnlineChangelog().toString(), is(equalTo(CHANGELOG_URL))
        assertThat fw.getInputStream(), is(not(null))
        assertThat fw.getInputStream().readLines().get(0), is(IMAGE_V111)
        assertThat fw.getMd5Hash(), is(HASH_111)
    }

    @Test
    void 'get firmware for thing type 2 with version 111 without locale'() {
        def fw = PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID2, VERSION_111))

        assertThat fw, is(not(null))
        assertThat fw.getUID(), is(new FirmwareUID(THING_TYPE_UID2, VERSION_111))
        assertThat fw.getModel(), is(equalTo(MODEL_B))
        assertThat fw.getDescription(), is(equalTo(DESCRIPTION_EN))
        assertThat fw.getVendor(), is(equalTo(VENDOR))
        assertThat fw.getPrerequisiteVersion(), is(null)
        assertThat fw.getChangelog(), is(equalTo(CHANGELOG_EN))
        assertThat fw.getOnlineChangelog().toString(), is(equalTo(CHANGELOG_URL))
        assertThat fw.getInputStream(), is(not(null))
        assertThat fw.getInputStream().readLines().get(0), is(IMAGE_V111)
        assertThat fw.getMd5Hash(), is(HASH_111)
    }

    @Test
    void 'get firmware for thing type 2 with version 112 with german locale'() {
        def fw = PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID2, VERSION_112), deLocale)

        assertThat fw, is(not(null))
        assertThat fw.getUID(), is(new FirmwareUID(THING_TYPE_UID2, VERSION_112))
        assertThat fw.getModel(), is(equalTo(MODEL_B))
        assertThat fw.getDescription(), is(equalTo(DESCRIPTION_DE))
        assertThat fw.getVendor(), is(equalTo(VENDOR))
        assertThat fw.getPrerequisiteVersion(), is(equalTo(V111))
        assertThat fw.getChangelog(), is(equalTo(CHANGELOG_DE))
        assertThat fw.getOnlineChangelog().toString(), is(equalTo(CHANGELOG_URL))
        assertThat fw.getInputStream(), is(not(null))
        assertThat fw.getInputStream().readLines().get(0), is(IMAGE_V112)
        assertThat fw.getMd5Hash(), is(HASH_112)
    }

    @Test
    void 'get firmware for thing type 2 with version 112 with english locale'() {
        def fw = PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID2, VERSION_112), enLocale)

        assertThat fw, is(not(null))
        assertThat fw.getUID(), is(new FirmwareUID(THING_TYPE_UID2, VERSION_112))
        assertThat fw.getModel(), is(equalTo(MODEL_B))
        assertThat fw.getDescription(), is(equalTo(DESCRIPTION_EN))
        assertThat fw.getVendor(), is(equalTo(VENDOR))
        assertThat fw.getPrerequisiteVersion(), is(equalTo(V111))
        assertThat fw.getChangelog(), is(equalTo(CHANGELOG_EN))
        assertThat fw.getOnlineChangelog().toString(), is(equalTo(CHANGELOG_URL))
        assertThat fw.getInputStream(), is(not(null))
        assertThat fw.getInputStream().readLines().get(0), is(IMAGE_V112)
        assertThat fw.getMd5Hash(), is(HASH_112)
    }

    @Test
    void 'get firmware for thing type 2 with version 112 without locale'() {
        def fw = PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID2, VERSION_112))

        assertThat fw, is(not(null))
        assertThat fw.getUID(), is(new FirmwareUID(THING_TYPE_UID2, VERSION_112))
        assertThat fw.getModel(), is(equalTo(MODEL_B))
        assertThat fw.getDescription(), is(equalTo(DESCRIPTION_EN))
        assertThat fw.getVendor(), is(equalTo(VENDOR))
        assertThat fw.getPrerequisiteVersion(), is(equalTo(V111))
        assertThat fw.getChangelog(), is(equalTo(CHANGELOG_EN))
        assertThat fw.getOnlineChangelog().toString(), is(equalTo(CHANGELOG_URL))
        assertThat fw.getInputStream(), is(not(null))
        assertThat fw.getInputStream().readLines().get(0), is(IMAGE_V112)
        assertThat fw.getMd5Hash(), is(HASH_112)
    }

    @Test
    void 'get all firmwares for thing type 1 with german locale'() {
        def list = PROVIDER.getFirmwares(THING_TYPE_UID1, deLocale)

        assertThat list.size(), is(2)
        Firmware fw = list.first();
        assertThat fw.getModel(), is(equalTo(MODEL_A))
        assertThat fw.getDescription(), is(equalTo(DESCRIPTION_DE))
        assertThat fw.getVendor(), is(equalTo(VENDOR))
        assertThat fw.getChangelog(), is(equalTo(CHANGELOG_DE))
        assertThat fw.getOnlineChangelog().toString(), is(equalTo(CHANGELOG_URL))
        assertThat fw.getInputStream(), is(not(null))
    }

    @Test
    void 'get all firmwares for thing type 1 with english locale'() {
        def list = PROVIDER.getFirmwares(THING_TYPE_UID1, enLocale)

        assertThat "The initial list of firmwares is invalid: " + Arrays.toString(list.toArray()), list.size(), is(2)
        Firmware fw = list.first();
        assertThat fw.getModel(), is(equalTo(MODEL_A))
        assertThat fw.getDescription(), is(equalTo(DESCRIPTION_EN))
        assertThat fw.getVendor(), is(equalTo(VENDOR))
        assertThat fw.getChangelog(), is(equalTo(CHANGELOG_EN))
        assertThat fw.getOnlineChangelog().toString(), is(equalTo(CHANGELOG_URL))
        assertThat fw.getInputStream(), is(not(null))
    }

    @Test
    void 'test firmware file system watcher finds added files'() {
        def list = PROVIDER.getFirmwares(THING_TYPE_UID1, deLocale)
        assertThat "Two firmware should be present for the german locale by default", list.size(), is(2)

        File imageFile = new File(THING_TYPE_1_DIR.getPath(), IMAGE_V113)
        imageFile << IMAGE_V113;

        File propertiesFile = new File(THING_TYPE_1_DIR.getPath(), V113 + ".properties")
        propertiesFile << "version = " + VERSION_113 + "\n"
        propertiesFile << "image = " + IMAGE_V113 + "\n"
        propertiesFile << "model = " + MODEL_A + "\n"
        propertiesFile << "vendor = " + VENDOR + "\n"
        propertiesFile << "description = " + DESCRIPTION_EN + "\n"
        propertiesFile << "prerequisiteVersion = " + V112 + "\n"
        propertiesFile << "changelog = " + CHANGELOG_EN + "\n"
        propertiesFile << "onlineChangelog = " + CHANGELOG_URL + "\n"

        File propertiesFileDE = new File(THING_TYPE_1_DIR.getPath(), V113 + "_de_DE.properties")
        propertiesFileDE << "description = " + DESCRIPTION_DE + "\n"
        propertiesFileDE << "prerequisiteVersion = " + V111 + "\n"
        propertiesFileDE << "changelog = " + CHANGELOG_DE + "\n"
        propertiesFileDE << "onlineChangelog = " + CHANGELOG_URL + "\n"

        waitForAssert ({
            list = PROVIDER.getFirmwares(THING_TYPE_UID1, deLocale)
            assertThat "The file, added in the watch directory, is not added in the firmwares cache. Current firmwares are: " + list, list.size(), is(3)
            // Close the opened input streams so that the files can be deleted
            list.each { Firmware fw ->
                fw.getInputStream().close()
            }
        }, DEFAULT_PROCESS_EVENT_TIMEOUT, 1000)

        imageFile.delete()
        propertiesFile.delete()
        propertiesFileDE.delete()

        waitForAssert ({
            list = PROVIDER.getFirmwares(THING_TYPE_UID1, deLocale)
            assertThat "The deleted file in the watch directory is not removed from the firmwares cache", list.size(), is(2)
        }, DEFAULT_PROCESS_EVENT_TIMEOUT, 1000)
    }

    @Test
    void 'modified file updates firmware meta information'() {
        // Assert that provider has the default values only
        def list = PROVIDER.getFirmwares(THING_TYPE_UID1)
        assertThat "Two firmware should be present for $THING_TYPE_UID1 and default locale", list.size(), is(2)

        def v112File = new File(THING_TYPE_1_DIR.getPath(), V112 + ".properties")

        def originalText = v112File.text

        // Add new temporary version
        def tempDescription = "New English description"
        v112File.text = originalText.replace(DESCRIPTION_EN,tempDescription)

        waitForAssert ({
            def firmware = PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID1, VERSION_112))
            assertThat "The firmware with the new description is not present in the firmwares cache",firmware, is(notNullValue())
        }, DEFAULT_PROCESS_EVENT_TIMEOUT, 1000)

        assertThat "The description is not adequately updated", PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID1, VERSION_112)).getDescription(), is(tempDescription)

        v112File.text = originalText
    }

    @Test
    void 'modified file causes conflict with overriding existing meta information'() {
        // Assert that provider has the default values only
        def list = PROVIDER.getFirmwares(THING_TYPE_UID1)
        assertThat "Two firmware should be present for $THING_TYPE_UID1 and default locale", list.size(), is(2)

        def testFile = new File(THING_TYPE_1_DIR.getPath(), "test.new")

        testFile << "version = $VERSION_112\n"
        testFile << "image = $IMAGE_V112\n"

        def conflictDescription = "Conflict description here"
        testFile << "description = $conflictDescription"

        def renameFile = new File(THING_TYPE_1_DIR.getPath(), "overrideMetainformation.properties")

        // Create the rename file in order to avoid exceptions while renaming it
        boolean isCreated = renameFile.createNewFile();
        assertThat "The rename file $renameFile.absolutePath cannot be created", isCreated, is(true)

        testFile.renameTo(renameFile)

        waitForAssert ({
            def firmware = PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID1, VERSION_112))
            assertThat "The firmware for version $VERSION_112 is not present", firmware, is(notNullValue())
        }, DEFAULT_PROCESS_EVENT_TIMEOUT, 1000)

        assertThat "The conflicted owerwriting should not be included in the collection", PROVIDER.getFirmwares(THING_TYPE_UID1).size(), is(2)
        assertThat "The description is not adequately updated", PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID1, VERSION_112)).getDescription(), is(not(conflictDescription))

        renameFile.delete()
    }

    @Test
    void 'modified file changes firmware version'() {
        // Assert that provider has the default values only
        assertThat "Two firmware should be present for $THING_TYPE_UID1 and default locale", PROVIDER.getFirmwares(THING_TYPE_UID1).size(), is(2)

        def v112File = new File(THING_TYPE_1_DIR.getPath(), V112 + ".properties")

        def originalText = v112File.text

        // Add new temporary version
        def tempVersion = "9.8.7"
        v112File.text = originalText.replace("version = " + VERSION_112,"version = " + tempVersion)

        waitForAssert ({
            def firmware = PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID1, tempVersion))
            assertThat "The firmware with the temp version is not present in the firmwares cache",firmware, is(notNullValue())
        }, DEFAULT_PROCESS_EVENT_TIMEOUT, 1000)

        assertThat "Two firmware should be present for $THING_TYPE_UID1 and default locale", PROVIDER.getFirmwares(THING_TYPE_UID1).size(), is(2)
        assertThat "The old version should not be present in the firmwares cache", PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID1, VERSION_112)), is(nullValue())

        v112File.text = originalText
    }

    @Test
    void 'created file causes conflict with overriding existing meta information'() {
        // Assert that provider has the default values only
        assertThat "Two firmware should be present for $THING_TYPE_UID1 and default locale", PROVIDER.getFirmwares(THING_TYPE_UID1).size(), is(2)

        def overwriteFile = new File(THING_TYPE_1_DIR.getPath(), "overwrite.properties")
        boolean isCreated = overwriteFile.createNewFile();
        assertThat "The overwrite file $overwriteFile.absolutePath cannot be created", isCreated, is(true)

        def overWrittenDescription = "Overwritten description"
        overwriteFile << "version = $VERSION_112\nimage = $IMAGE_V112\ndescription = $overWrittenDescription"

        waitForAssert ({
            def firmware = PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID1, VERSION_112))
            assertThat "The firmware for version $VERSION_112 is not present", firmware, is(notNullValue())
            assertThat "The description is not adequately updated", PROVIDER.getFirmware(new FirmwareUID(THING_TYPE_UID1, VERSION_112)).getDescription(), is(not(overWrittenDescription))
        }, DEFAULT_PROCESS_EVENT_TIMEOUT, 1000)

        assertThat "The conflicted owerwriting should not be included in the collection", PROVIDER.getFirmwares(THING_TYPE_UID1).size(), is(2)

        overwriteFile.delete()
    }

    @Test
    void 'stress test on the watch service'() {
        // Simulates adding of lots of files in the watched directory.
        // Note that WatchEvent is generated for every edit of a file within the
        // watched firmwares directory and these events have to be handled adequately.

        // As this is a timing issue it can be met either on iteration 1
        // or on iteration 30. So 100 iteration are reasonable amount
        // to be sure that it will happen with high probability
        int stressCount = 100
        Thread stressThread = Thread.start {
            int count = 0
            while(count < stressCount && !Thread.currentThread().interrupted()){
                synchronized(this){
                    File stressFile = new File(THING_TYPE_1_DIR.getPath(), V113 + ".properties")
                    stressFile << "version = " + VERSION_113 + "\n" + "image = " + IMAGE_V113 + "\n"
                    count ++
                }
            }
        }

        for(int count=0; count < stressCount; count ++){
            def result= []
            result = PROVIDER.getFirmwares(THING_TYPE_UID1, null)
            if(result.size() == 0)
            {
                stressThread.interrupt()
                sleep(100)
                fail("There should be at least one result for THING_TYPE_UID1")
            }
        }

        waitForAssert ({ assertThat stressThread.alive, is(false) }, 10000, 1000)
    }

    @Test
    void 'get firmwares for invalid binding and thing type'() {
        def noFirmwareThingTypeUid = new ThingTypeUID("invalid-binding", "invalid-thing-type")
        def result = PROVIDER.getFirmwares(noFirmwareThingTypeUid)
        assertThat "Provider returned firmwares for invalid binding id and thing type", result.size(), is(0)
    }

    @Test
    void 'get firmware with minimum number of properties'(){
        assertThat "Two firmware should be present for $THING_TYPE_UID1 and default locale", PROVIDER.getFirmwares(THING_TYPE_UID1).size(), is(2)

        def name = "version200.properties"
        def version = "2.0.0"

        File minimumContentFirmwareFile = new File(THING_TYPE_1_DIR.getPath(), name)
        boolean isCreated = minimumContentFirmwareFile.createNewFile();
        assertThat "The overwrite file $minimumContentFirmwareFile.absolutePath cannot be created", isCreated, is(true)

        minimumContentFirmwareFile << "version = " + version + "\n"
        // The name of the image is not important, only the file has to be present
        minimumContentFirmwareFile << "image = " + IMAGE_V112 + "\n"

        def firmwareUid = new FirmwareUID(THING_TYPE_UID1, version)
        Firmware firmware = null
        waitForAssert ({
            firmware = PROVIDER.getFirmware(firmwareUid)
            assertThat "The firmware for the firmware meta file with minimum content is not present", firmware, is(notNullValue())
        },DEFAULT_PROCESS_EVENT_TIMEOUT, 1000)

        assertThat "The version of the found firmware and the version in the meta information file do not match", firmware.getVersion(), is (version)
        assertThat "The input stream of the firmware should not be null", firmware.getInputStream(),is (notNullValue())
        assertThat "The bytes of the firmware should not be null", firmware.getBytes(), is (notNullValue())

        assertThat firmware.getVendor(), is(nullValue())
        assertThat firmware.getModel(), is(nullValue())
        assertThat firmware.getDescription(), is(nullValue())
        assertThat firmware.getPrerequisiteVersion(), is(nullValue())
        assertThat firmware.getChangelog(), is(nullValue())
        assertThat firmware.getOnlineChangelog(), is(nullValue())
        assertThat firmware.getMd5Hash(), is(nullValue())

        boolean isDeleted = minimumContentFirmwareFile.delete()
        assertThat "The file $name was not successfully deleted", isDeleted, is(true)

        waitForAssert ({
            def list = PROVIDER.getFirmwares(THING_TYPE_UID1)
            assertThat "Two firmware should be present for $THING_TYPE_UID1 and default locale at the end of the test, but were " + list, list.size(), is(2)
        },DEFAULT_PROCESS_EVENT_TIMEOUT, 1000)
    }

    @Test
    void 'get firmware without image'(){
        assertThat "Two firmware should be present for $THING_TYPE_UID1 and default locale", PROVIDER.getFirmwares(THING_TYPE_UID1).size(), is(2)

        def name = "noImage.properties"
        def version = "3.0.0"

        File noImageFirmwareFile = new File(THING_TYPE_1_DIR.getPath(), name)
        boolean isCreated = noImageFirmwareFile.createNewFile();
        assertThat "The overwrite file $noImageFirmwareFile.absolutePath cannot be created", isCreated, is(true)

        noImageFirmwareFile << "version = " + version + "\n"

        def firmwareUid = new FirmwareUID(THING_TYPE_UID1, version)
        Firmware firmware = null
        waitForAssert ({
            def exception = null
            try{
                firmware = PROVIDER.getFirmware(firmwareUid)
            } catch (MissingResourceException missingResourceException){
                exception = missingResourceException
            }
            assertThat "Expected MissingResourceException as image is not present", exception, is(notNullValue())
        },DEFAULT_PROCESS_EVENT_TIMEOUT, 1000)

        boolean isDeleted = noImageFirmwareFile.delete()
        assertThat "The file $name was not successfully deleted", isDeleted, is(true)

        waitForAssert ({
            def list = PROVIDER.getFirmwares(THING_TYPE_UID1)
            assertThat "Two firmware should be present for $THING_TYPE_UID1 and default locale at the end of the test, but were " + list, list.size(), is(2)
        },DEFAULT_PROCESS_EVENT_TIMEOUT, 1000)
    }

    @Test(expected=IllegalArgumentException.class)
    void 'get firmware with null thing type uid'(){
        def nullList = PROVIDER.getFirmwares(null)
    }

    private void waitForAssert(Closure<?> assertion, int timeout = 1000, int sleepTime = 50) {
        def waitingTime = 0
        while(waitingTime < timeout) {
            try {
                assertion()
                return
            } catch(Error error) {
                waitingTime += sleepTime
                sleep sleepTime
            }
        }
        assertion()
    }
}

