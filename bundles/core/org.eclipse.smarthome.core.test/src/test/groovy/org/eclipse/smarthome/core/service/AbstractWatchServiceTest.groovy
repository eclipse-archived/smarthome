package org.eclipse.smarthome.core.service

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.Path
import java.nio.file.WatchEvent;
import java.nio.file.WatchService
import java.nio.file.WatchEvent.Kind

import org.eclipse.smarthome.core.service.AbstractWatchServiceTest.FullEvent;
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for {@link AbstractWatchService}.
 * 
 * @author Dimitar Ivanov
 *
 */
class AbstractWatchServiceTest extends OSGiTest {

    def static ROOT = "testRoot" + File.separatorChar + "files"

    RelativeWatchService relativeService

    @BeforeClass
    static void setUpBeforeClass(){
        File root = new File(ROOT);
        root.mkdirs()
    }

    @AfterClass
    static void tearDownClass(){
        File root = new File(ROOT);
        root.deleteDir()
    }

    @Before
    public void setUp(){
        relativeService = new RelativeWatchService(ROOT)
        relativeService.activate()
    }

    @After
    public void tearDown(){
        relativeService.deactivate()
    }

    @Test
    void 'AbstractWatchQueueReader processWatchEvent path provided with correct arguments'() {
        // File created
        def fileName = "watchFile"
        File watchFile1 = new File(ROOT,fileName)
        watchFile1.createNewFile()

        fullEventAssertionsByKind(fileName,ENTRY_CREATE,false)

        // File modified
        watchFile1 << "Additional content"
        fullEventAssertionsByKind(fileName,ENTRY_MODIFY,false)

        // File deleted
        boolean isDeleted = watchFile1.delete()
        assertThat "Test file is not deleted", isDeleted,is(true)
        fullEventAssertionsByKind(fileName,ENTRY_DELETE,true)
    }

    void fullEventAssertionsByKind(fileName, kind, osSpecific){
        // Wait one second for any change to be detected by the watch service
        sleep 1000

        // As the implementation of the watch events is OS specific, it can happen that when the file is deleted two events are fired - ENTRY_MODIFY followed by an ENTRY_DELETE
        // This is usually observed on Windows and below is the workaround
        // Related discussion in StackOverflow: http://stackoverflow.com/questions/28201283/watchservice-windows-7-when-deleting-a-file-it-fires-both-entry-modify-and-e
        boolean isDeletedWithPrecedingModify = osSpecific && kind.equals(ENTRY_DELETE) && relativeService.allFullEvents.size() == 2 && relativeService.allFullEvents[0].eventKind.equals(ENTRY_MODIFY)
        if(isDeletedWithPrecedingModify){
            // Remove the ENTRY_MODIFY element as it is not needed
            relativeService.allFullEvents.remove(0)
        }

        waitForAssert {assertThat "Only one event for file $fileName is expected shortly after the file has been altered. Here are the found events: " + relativeService.allFullEvents, relativeService.allFullEvents.size(), is(1)}
        FullEvent fullEvent = relativeService.allFullEvents[0]

        assertThat "The path of the processed event should be relative to the root", fullEvent.eventPath.toString(), is(ROOT + File.separatorChar + fileName)
        assertThat "An event of corresponding kind $kind is expected", fullEvent.eventKind, is(kind)
        assertThat "At least one watch event of kind $kind is expected", fullEvent.watchEvent.count() >= 1, is(true)
        assertThat "The watch event kind should be the same as the kind provided", fullEvent.watchEvent.kind(), is(fullEvent.eventKind)
        assertThat "The watch event context have to be the file name only", fullEvent.watchEvent.context().toString(),is(fileName)

        // Remove the only event as it is already asserted
        relativeService.allFullEvents.clear()
    }

    class RelativeWatchService extends AbstractWatchService{

        String rootWatchPath

        def allFullEvents = []

        RelativeWatchService(String rootPath){
            rootWatchPath = rootPath
        }


        @Override
        protected AbstractWatchQueueReader buildWatchQueueReader(WatchService watchService, Path toWatch) {
            return new AbstractWatchQueueReader(watchService, toWatch) {
                                @Override
                                protected void processWatchEvent(WatchEvent<?> event, Kind<?> kind, Path path) {
                                    allFullEvents << new FullEvent(event,kind,path)
                                }
                            };
        }

        @Override
        protected String getSourcePath() {
            return rootWatchPath
        }

        @Override
        protected void registerDirectory(Path subDir) throws IOException {
            subDir.register(watchService,ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
        }

        @Override
        protected boolean watchSubDirectories() {
            return true;
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
            return "Watch event: " + watchEvent +  ", kind: " + eventKind + ", path: " + eventPath;
        }
    }
}
