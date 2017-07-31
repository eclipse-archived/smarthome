/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.service

import static java.nio.file.StandardWatchEventKinds.*
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import java.nio.file.Path
import java.nio.file.WatchEvent
import java.nio.file.WatchEvent.Kind
import java.util.concurrent.CopyOnWriteArrayList

import org.apache.commons.lang.SystemUtils
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

/**
 * Test for {@link AbstractWatchService}.
 *
 * @author Dimitar Ivanov - Initial implementation
 * @author Svilen Valkanov - Tests are modified to run on different Operating Systems
 * @author Ana Dimova - reduce to a single watch thread for all class instances
 */
class AbstractWatchServiceTest extends OSGiTest {

    def static WATCHED_DIRECTORY = "watchDirectory"

    // Fail if no event has been received within the given timeout
    def static NO_EVENT_TIMEOUT_IN_SECONDS;

    RelativeWatchService watchService

    @BeforeClass
    static void setUpBeforeClass(){
        // set the NO_EVENT_TIMEOUT_IN_SECONDS according to the operating system used
        if(SystemUtils.IS_OS_MAC_OSX) {
            NO_EVENT_TIMEOUT_IN_SECONDS = 10
        } else {
            NO_EVENT_TIMEOUT_IN_SECONDS = 3;
        }
    }

    @Before
    public void setup() {
        File watchDir = new File(WATCHED_DIRECTORY);
        watchDir.mkdirs()
    }

    @After
    public void tearDown(){
        watchService.deactivate()
        waitForAssert{assertThat watchService.watchQueueReader,is(nullValue())}
        clearWatchedDir()
        watchService.allFullEvents.clear()

        File watchedDirectory = new File(WATCHED_DIRECTORY);
        watchedDirectory.deleteDir()
    }

    void clearWatchedDir(){
        File watchedDirectory = new File(WATCHED_DIRECTORY)
        watchedDirectory.listFiles().each { File mockedFile ->
            mockedFile.isFile() ? mockedFile.delete() : mockedFile.deleteDir()
        }
    }

    @Test
    void 'AbstractWatchQueueReader processWatchEvent path in root provided with correct arguments'() {
        watchService = new RelativeWatchService(WATCHED_DIRECTORY,true,false)

        // File created in the watched directory
        assertByRelativePath("rootWatchFile")
    }

    @Test
    void 'AbstractWatchQueueReader processWatchEvent path in subdir provided with correct arguments'() {
        watchService = new RelativeWatchService(WATCHED_DIRECTORY,true,false)

        // File created in a subdirectory of the watched directory
        assertByRelativePath("subDir" + File.separatorChar + "subDirWatchFile")
    }

    @Test
    void 'AbstractWatchQueueReader processWatchEvent path in subsubdir provided with correct arguments'() {
        watchService = new RelativeWatchService(WATCHED_DIRECTORY,true,false)

        // File created in a sub sub directory of the watched directory
        assertByRelativePath("subDir" + File.separatorChar + "subSubDir" + File.separatorChar + "innerWatchFile")
    }

    @Test
    void 'same file names in root and subdir are correctly processed'(){
        watchService = new RelativeWatchService(WATCHED_DIRECTORY,true,false)

        def fileName = "duplicateFile"
        def innerFileName = "duplicateDir" + File.separatorChar + fileName

        File innerfile = new File(WATCHED_DIRECTORY + File.separatorChar + innerFileName)

        // Make all the directories needed
        innerfile.getParentFile().mkdirs()

        // Activate the service when the subdir is also present. Else the subdir will not be registered
        watchService.activate()
        waitForAssert {assertThat watchService.watchService,is(notNullValue())}

        boolean isCreated = innerfile.createNewFile()
        assertThat "The file '$innerfile.absolutePath' was not created successfully", isCreated, is(true)

        // Assure that the ordering of the events will be always the same
        sleep NO_EVENT_TIMEOUT_IN_SECONDS*1000

        File file = new File(WATCHED_DIRECTORY + File.separatorChar + fileName)
        isCreated = file.createNewFile()

        assertThat "The file '$file.absolutePath' was not created successfully", isCreated, is(true)

        def expectedEvents = 2
        waitForAssert{assertThat "Exactly two watch events were expected, but were: " + watchService.allFullEvents, watchService.allFullEvents.size(), is(expectedEvents)}

        FullEvent innerFileEvent = watchService.allFullEvents[0]
        assertThat "The inner file '$innerfile.absolutePath' creation was not detected. All events detected: " + watchService.allFullEvents,innerFileEvent.eventKind,is(ENTRY_CREATE)
        assertThat "The path of the first detected event should be for $innerFileName", innerFileEvent.eventPath.toString(), is(WATCHED_DIRECTORY + File.separatorChar + innerFileName)

        FullEvent fileEvent = watchService.allFullEvents[1]
        assertThat "The root file '$file.absolutePath' creation  was not detected. All events detected: " + watchService.allFullEvents,fileEvent.eventKind,is(ENTRY_CREATE)
        assertThat "The path of the second event should be for $fileName", fileEvent.eventPath.toString(), is(WATCHED_DIRECTORY + File.separatorChar + fileName)
    }

    @Test
    void 'subdirs are registered and modifications are watched'(){
        // Watch subdirectories and their modifications
        watchService = new RelativeWatchService(WATCHED_DIRECTORY,true,true)

        def subDirName = "correctlyWatchedSubDir"
        def fileName = "correctSubDirInnerFile"
        def innerFileName = subDirName + File.separatorChar + fileName

        File innerFile = new File(WATCHED_DIRECTORY + File.separatorChar + innerFileName)

        // Make all the subdirectories before running the service
        innerFile.getParentFile().mkdirs()

        watchService.activate()
        waitForAssert {assertThat watchService.watchService,is(notNullValue())}

        if(SystemUtils.IS_OS_MAC_OSX) {
            sleep 1000
        }

        boolean isCreated = innerFile.createNewFile()
        assertThat "The file '$innerFile.absolutePath' was not created successfully", isCreated, is(true)

        assertAllEventsAreProcessed(subDirName,innerFile,WATCHED_DIRECTORY + File.separatorChar + innerFileName)
    }

    @Test
    void 'subdirs are not registered and modifications are not watched'(){
        // Do not watch the subdirectories of the root directory
        watchService = new RelativeWatchService(WATCHED_DIRECTORY,false,false)

        def innerFileName = "watchRequestSubDir"+ File.separatorChar + "watchRequestInnerFile"

        File innerFile = new File(WATCHED_DIRECTORY + File.separatorChar + innerFileName)
        innerFile.getParentFile().mkdirs()

        watchService.activate()
        waitForAssert {assertThat watchService.watchService,is(notNullValue())}

        // Consequent creation and deletion in order to generate any watch events for the subdirectory
        boolean isCreated = innerFile.createNewFile()
        assertThat "The file '$innerFile.absolutePath' was not created successfully", isCreated, is(true)

        boolean isDeleted = innerFile.delete()
        assertThat "Inner file is not deleted", isDeleted, is(true)

        assertNoEventsAreProcessed()
    }

    @Test
    void 'subdirs are registered, but dirs modifications are not watched'() {
        // Do watch the subdirectories of the root directory, but do not watch directory modifications
        watchService = new RelativeWatchService(WATCHED_DIRECTORY,true,false)

        def innerFileName = "watchRequestSubDir"+ File.separatorChar + "watchRequestInnerFile"
        File innerFile = new File(WATCHED_DIRECTORY + File.separatorChar + innerFileName)
        // Make all the subdirectories before running the service
        innerFile.getParentFile().mkdirs()

        watchService.activate()
        waitForAssert {assertThat watchService.watchService,is(notNullValue())}

        boolean isCreated = innerFile.createNewFile()
        assertThat "The file '$innerFile.absolutePath' was not created successfully", isCreated, is(true)

        assertFileCreateEventIsProcessed(innerFile,innerFileName)

        watchService.allFullEvents.clear();

        assertNoEventsAreProcessed()
    }

    @Test
    void 'subdirs are not registered, but dirs modifications are watched'(){
        // Do not watch the subdirectories of the root directory, but watch directory modifications
        watchService = new RelativeWatchService(WATCHED_DIRECTORY,false,true)

        def subDirName = "subDirModifications"
        def innerFileName = subDirName + File.separatorChar + "modificationsInnerFile"

        File innerFile = new File(WATCHED_DIRECTORY + File.separatorChar + innerFileName)
        innerFile.getParentFile().mkdirs()

        watchService.activate()
        waitForAssert {assertThat watchService.watchService,is(notNullValue())}

        if(SystemUtils.IS_OS_MAC_OSX) {
            sleep 1000
        }

        boolean isCreated = innerFile.createNewFile()
        assertThat "The file '$innerFile.absolutePath' was not created successfully", isCreated, is(true)

        assertDirectoryModifyEventIsProcessed(WATCHED_DIRECTORY + File.separatorChar + subDirName)

        // Clear the asserted event
        watchService.allFullEvents.clear()

        boolean isDeleted = innerFile.delete()
        assertThat "Inner file '$innerFile.absolutePath' is not deleted", isDeleted, is(true)

    }
    void assertNoEventsAreProcessed(){
        // Wait for a possible event for the maximum timeout
        sleep NO_EVENT_TIMEOUT_IN_SECONDS*1000

        assertThat "No watch events are expected, but were: " + watchService.allFullEvents, watchService.allFullEvents.size(), is(0)
    }

    void assertAllEventsAreProcessed(def subDirName,def innerFile, def innerFileName){
        //This could vary across different platforms. For more information see "Platform dependencies" section in the WatchService documentation
        def expectedEvents = SystemUtils.IS_OS_LINUX ? 1 : 2

        waitForAssert({assertThat "Exactly $expectedEvents watch events were expected, but were: " + watchService.allFullEvents, watchService.allFullEvents.size(), is(expectedEvents)},30*1000)

        if(SystemUtils.IS_OS_MAC_OSX) {
            FullEvent dirEvent = watchService.allFullEvents[0]
            assertThat "Directory $subDirName modification was not detected. All events detected: " + watchService.allFullEvents, dirEvent.eventKind, is(ENTRY_MODIFY)
            assertThat "Subdirectory was not found in the modified event", dirEvent.eventPath.toString(), is(WATCHED_DIRECTORY + File.separatorChar + subDirName)

            FullEvent fileEvent = watchService.allFullEvents[1]
            assertThat "File '$innerFile.absolutePath' creation was not detected. All events detected: " + watchService.allFullEvents, fileEvent.eventKind, is(ENTRY_CREATE)
            assertThat "File '$innerFile.absolutePath' name expected in the modified event. All events detected: " + watchService.allFullEvents, fileEvent.eventPath.toString(), is(innerFileName)

        } else {
            FullEvent fileEvent = watchService.allFullEvents[0]
            assertThat "File '$innerFile.absolutePath' creation was not detected. All events detected: " + watchService.allFullEvents, fileEvent.eventKind, is(ENTRY_CREATE)
            assertThat "File '$innerFile.absolutePath' name expected in the modified event. All events detected: " + watchService.allFullEvents, fileEvent.eventPath.toString(), is(innerFileName)

            if(SystemUtils.IS_OS_WINDOWS) {
                FullEvent dirEvent = watchService.allFullEvents[1]
                assertThat "Directory $subDirName modification was not detected. All events detected: " + watchService.allFullEvents, dirEvent.eventKind, is(ENTRY_MODIFY)
                assertThat "Subdirectory was not found in the modified event", dirEvent.eventPath.toString(), is(WATCHED_DIRECTORY + File.separatorChar + subDirName)
            }
        }

    }

    void assertDirectoryCraeteEventIsProcessed(def subDirName) {
        //Single event for directory creation is present
        def expectedEvents = 1
        waitForAssert{assertThat "Exactly $expectedEvents watch events were expected, but were: " + watchService.allFullEvents, watchService.allFullEvents.size(), is(expectedEvents)}
        FullEvent event = watchService.allFullEvents[0]
        assertThat "Directory $subDirName craetion was not detected. All events detected: " + watchService.allFullEvents, event.eventKind, is(ENTRY_CREATE)
        assertThat "Subdirectory was not found in the creation event", event.eventPath.toString(), is(subDirName)
    }

    void assertFileCreateEventIsProcessed(def innerFile, def innerFileName) {
        //Single event for file creation is present
        def expectedEvents = 1
        waitForAssert{assertThat "Exactly $expectedEvents watch events were expected, but were: " + watchService.allFullEvents, watchService.allFullEvents.size(), is(expectedEvents)}
        FullEvent fileEvent = watchService.allFullEvents[0]
        assertThat "File '$innerFile.absolutePath' creation was not detected. All events detected: " + watchService.allFullEvents, fileEvent.eventKind, is(ENTRY_CREATE)
        assertThat "File '$innerFile.absolutePath' name expected in the modified event. All events detected: " + watchService.allFullEvents, fileEvent.eventPath.toString(), is(WATCHED_DIRECTORY + File.separatorChar + innerFileName)
    }

    void assertDirectoryModifyEventIsProcessed(def subDirName) {
        //Create file is not detected, only the modification event is detected
        def expectedEvents = SystemUtils.IS_OS_LINUX ? 0 : 1
        waitForAssert{assertThat "Exactly $expectedEvents watch events were expected, but were: " + watchService.allFullEvents, watchService.allFullEvents.size(), is(expectedEvents)}

        if(!SystemUtils.IS_OS_LINUX){
            FullEvent dirEvent = watchService.allFullEvents[0]
            assertThat "Directory $subDirName modification was not detected. All events detected: " + watchService.allFullEvents, dirEvent.eventKind, is(ENTRY_MODIFY)
            assertThat "Subdirectory was not found in the modified event", dirEvent.eventPath.toString(), is(subDirName)
        } else {
            assertThat "No events are expected in Linux OS" + watchService.allFullEvents, watchService.allFullEvents.size(), is(0)
        }
    }

    void assertByRelativePath(String fileName) {
        File file = new File(WATCHED_DIRECTORY + File.separatorChar + fileName)
        file.getParentFile().mkdirs()

        assertThat "The file '$file.absolutePath' should not be present before the watch service activation", file.exists(), is(false)

        // We have to be sure that all the subdirectories of the watched directory are created when the watched service is activated
        watchService.activate()
        waitForAssert {assertThat watchService.watchQueueReader,is(notNullValue())}

        boolean isCreated = file.createNewFile()
        assertThat "The file '$file.absolutePath' was not created successfully", isCreated, is(true)

        fullEventAssertionsByKind(fileName, ENTRY_CREATE, false)

        // File modified
        file << "Additional content"
        fullEventAssertionsByKind(fileName, ENTRY_MODIFY, false)

        // File deleted
        boolean isDeleted = file.delete()
        assertThat "Test file '$file.absolutePath' is not deleted", isDeleted, is(true)
        fullEventAssertionsByKind(fileName, ENTRY_DELETE, true)
    }

    void fullEventAssertionsByKind(String fileName, kind, osSpecific){
        waitForAssert{
            assertThat "At least one watch event is expected",
                    watchService.allFullEvents.size() >= 1,
                    is(true)
        }

        if(osSpecific && kind.equals(ENTRY_DELETE)){
            // There is possibility that one more modify event is triggered on some OS
            // so sleep a bit extra time
            sleep 500
            cleanUpOsSpecificModifyEvent()
        }

        waitForAssert {assertThat "Exactly one event of kind $kind for file $fileName is expected shortly after the file has been altered. Here are the found events: " + watchService.allFullEvents, watchService.allFullEvents.size(), is(1)}
        FullEvent fullEvent = watchService.allFullEvents[0]

        assertThat "The path of the processed $kind event should be relative to the watched directory", fullEvent.eventPath.toString(), is(WATCHED_DIRECTORY + File.separatorChar.toString() + fileName)
        assertThat "An event of corresponding kind $kind is expected for file $fileName", fullEvent.eventKind, is(kind)
        assertThat "At least one watch event of kind $kind is expected", fullEvent.watchEvent.count() >= 1, is(true)
        assertThat "The watch event kind should be the same as the kind provided", fullEvent.watchEvent.kind(), is(fullEvent.eventKind)
        def fileNameOnly = fileName.contains(File.separatorChar.toString()) ? fileName.substring(fileName.lastIndexOf(File.separatorChar.toString()) + 1,fileName.length()) : fileName
        assertThat "The watch event context have to be the file name only", fullEvent.watchEvent.context().toString(),is(fileNameOnly)

        // Clear all the asserted events
        watchService.allFullEvents.clear()
    }

    /**
     * Cleanup the OS specific ENTRY_MODIFY event as it will not be needed for the assertion
     */
    private cleanUpOsSpecificModifyEvent() {
        // As the implementation of the watch events is OS specific, it can happen that when the file is deleted two events are fired - ENTRY_MODIFY followed by an ENTRY_DELETE
        // This is usually observed on Windows and below is the workaround
        // Related discussion in StackOverflow: http://stackoverflow.com/questions/28201283/watchservice-windows-7-when-deleting-a-file-it-fires-both-entry-modify-and-e
        boolean isDeletedWithPrecedingModify = watchService.allFullEvents.size() == 2 && watchService.allFullEvents[0].eventKind.equals(ENTRY_MODIFY)
        if(isDeletedWithPrecedingModify){
            // Remove the ENTRY_MODIFY element as it is not needed
            watchService.allFullEvents.remove(0)
        }
    }

    class RelativeWatchService extends AbstractWatchService{

        boolean watchSubDirs

        boolean watchDirectoryChanges

        // Synchronize list as several watcher threads can write into it
        def allFullEvents = new CopyOnWriteArrayList<FullEvent>()

        RelativeWatchService(String rootPath, boolean watchSubDirectories, boolean watchDirChanges){
            super(rootPath);
            watchSubDirs = watchSubDirectories
            watchDirectoryChanges = watchDirChanges
        }

        @Override
        protected void processWatchEvent(WatchEvent<?> event, Kind<?> kind, Path path) {
            FullEvent fullEvent = new FullEvent(event,kind,path)
            allFullEvents << fullEvent
        }

        @Override
        protected Kind<?>[] getWatchEventKinds(Path subDir) {
            return [
                ENTRY_CREATE,
                ENTRY_DELETE,
                ENTRY_MODIFY
            ]
        }

        @Override
        protected boolean watchSubDirectories() {
            return watchSubDirs
        }

        @Override
        protected boolean getWatchingDirectoryChanges(Path subDir) {
            return watchDirectoryChanges
        }
    }

    class FullEvent{
        WatchEvent<?> watchEvent
        Kind<?> eventKind
        Path eventPath

        public FullEvent(WatchEvent<?> event, Kind<?> kind, Path path){
            watchEvent = event
            eventKind = kind
            eventPath = path
        }

        @Override
        public String toString() {
            return "Watch Event: count " + watchEvent.count +  "; kind: " + eventKind + "; path: " + eventPath;
        }
    }
}
