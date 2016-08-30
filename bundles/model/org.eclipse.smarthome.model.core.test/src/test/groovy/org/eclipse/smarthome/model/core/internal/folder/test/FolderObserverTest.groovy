/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.core.internal.folder.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.apache.commons.io.FileUtils
import org.eclipse.emf.ecore.EObject
import org.eclipse.smarthome.config.core.ConfigConstants
import org.eclipse.smarthome.core.service.AbstractWatchQueueReader
import org.eclipse.smarthome.model.core.*
import org.eclipse.smarthome.model.core.internal.folder.FolderObserver
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test
import org.junit.After
import org.junit.Ignore
import org.junit.AfterClass
import org.osgi.service.cm.Configuration
import org.osgi.service.cm.ConfigurationAdmin
import org.osgi.service.cm.ManagedService
import org.apache.commons.lang.SystemUtils

/**  A test class for {@link FolderObserver} class. The following test cases aim
 *  to check if {@link FolderObserver} invokes the correct {@link ModelRepository}'s methods 
 *  with correct arguments when certain events in the watched directory are triggered.
 *
 *  {@link AbstractWatchService#initializeWatchService} method is called in the
 *  {@link FolderObserver#updated} method and initializing a new WatchService
 *  is related to creating a new {@link AbstractWatchQueueReader} which starts in a new Thread.
 *  Since we rely on the {@link AbstractWatchQueueReader} to "listen" for changes, we have to
 *  be sure that it has been started.
 *  Based on that putting the current Thread to sleep after each invocation of
 *  {@link FolderObserver#updated} method is necessary.
 *  On the other hand, creating, modifying and deleting files and folders causes invocation
 *  of {@link AbstractWatchQueueReader#processWatchEvent} method. That method is called asynchronously 
 *  and we do not know exactly when the event will be handled (it is OS specific). 
 *  Since the assertions in the tests depend on handling the events,
 *  putting the current Thread to sleep after the file operations is also necessary. 
 *
 *  @author Mihaela Memova
 *
 */

class FolderObserverTest extends OSGiTest {

	/** The directory which is watched for changes */
	private final static String WATCHED_DIRECTORY = "watched_dir"
	/** The name of the existing subdirectory which is used in most of the test cases */
	private final static String EXISTING_SUBDIR_NAME = "existing_subdir"
	/** The path of the existing subdirectory which is used in most of the test cases */
	private final static String EXISTING_SUBDIR_PATH = WATCHED_DIRECTORY + File.separator + EXISTING_SUBDIR_NAME
	/** Time to sleep (miliseconds) when updated() method is called, so the AbstractWatchQueueReader can start and be able to process events */
	private final static int WAIT_ABSTRACTWATCHQUEUEREADER_TO_START = 200
	/** Time to sleep when a file is created/modified/deleted, so the event can be handled */
	private final static int WAIT_EVENT_TO_BE_HANDLED = 1000
	/** Persistent identifier(PID) of the FolderObserver class, taken from folderobserver.xml */
	private final static String FOLDEROBSERVER_PID = "org.eclipse.smarthome.folder"

	/** The only element in the array of model names returned by {@link ModelRepository#getAllModelNamesOfType(String modelType)} **/
	private static final String MOCK_MODEL_TO_BE_REMOVED = "MockFileInModelForDeletion.java"

	/** Text to be inserted in the newly created files, so the tests can run properly on all OS */
	private final String INITIAL_FILE_CONTENT = "Initial content"

	private ConfigurationAdmin configAdmin
	private FolderObserver folderObserverService
	private Configuration config
	/** An instance of the ModelRepoMock class which implements the ModelRepository interface */
	private ModelRepoMock modelRepo
	/** An instance of the ModelParserMock class which implements the ModelParser interface */
	private ModelParserMock modelParserMock
	/** The default watched directory */
	private static String defaultWatchedDir

	@Before
	void setUp() {
		setupWatchedDirectory()
		setUpServices()
		config = configAdmin.getConfiguration(FOLDEROBSERVER_PID)
		assertNotNull(config)
	}

	private void setupWatchedDirectory() {
		/* 
		 * The main configuration folder's path is saved in the defaultWatchedDir variable 
		 * in order to be restored after all the tests are finished. 
		 * For the purpose of the FolderObserverTest class a new folder is created.
		 * Its path is set to the ConfigConstants.CONFIG_DIR_PROG_ARGUMENT property.
		 */
		defaultWatchedDir = System.getProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT)
		File watchDir = new File(WATCHED_DIRECTORY)
		watchDir.mkdirs()
		System.setProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT, watchDir.getPath())
		new File(EXISTING_SUBDIR_PATH).mkdirs()
	}

	void setUpServices() {
		modelParserMock = new ModelParserMock()
		/* 
		 * FolderObserver calls the addModelParser() method every time a ModelParser is 
		 * registered in the service registry.
		 * So calling that method explicitly is not necessary. 
		 */
		registerService(modelParserMock)
		modelRepo = new ModelRepoMock()
		registerService(modelRepo)
		folderObserverService = getService(ManagedService, FolderObserver)
		assertThat("Folder Observer service cannot be found",folderObserverService, is(notNullValue()))
		/*
		 * FolderObserver is referencing to the ModelRepository service using the 
		 * cardinality "1..1". In order to be sure it will use our mock ModelRepository object,
		 *  it is necessary to explicitly set it.
		 */
		folderObserverService.setModelRepository(modelRepo)
		configAdmin = getService(ConfigurationAdmin)
		assertThat("Configuration Admin service cannot be found",configAdmin, is(notNullValue()))
	}

	@After
	void tearDown() {
		/*
		 * The FolderObserver service have to be stopped at the end of each test
		 * as most of the tests are covering assertions on its initialization actions
		 */
		folderObserverService.deactivate()
		FileUtils.deleteDirectory(new File(WATCHED_DIRECTORY))
		modelRepo.clean()
		config.delete()
	}

	@AfterClass
	static void tearDownClass(){
		/*
		 * After all the tests are finished we need to reset the state of the system.
		 */
		if(defaultWatchedDir != null){
			System.setProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT, defaultWatchedDir)
		}
		else {
			System.clearProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT)
		}
	}

	@Test
	void 'test creation of file with valid extension in an existing subdirectory'() {
		/*
		 * The following method creates a file in an existing directory. The file's extension is
		 * in the configuration properties and there is a registered ModelParser for it.
		 * addOrRefreshModel() method invocation is expected
		 */
		String validExtension = "java"

		Dictionary<String, String> configProps = new Hashtable<String, String>()
		configProps.put(EXISTING_SUBDIR_NAME, "txt,jpg," + validExtension)
		config.update(configProps)
		sleep(WAIT_ABSTRACTWATCHQUEUEREADER_TO_START)

		String mockFileWithValidExtName = "NewlyCreatedMockFile" + "." + validExtension
		String mockFileWithValidExtPath = EXISTING_SUBDIR_PATH + File.separatorChar + mockFileWithValidExtName
		File mockFileWithValidExt = new File(mockFileWithValidExtPath)
		mockFileWithValidExt.createNewFile()
		sleep(WAIT_EVENT_TO_BE_HANDLED)
		/*
		 * In some OS, like MacOS, creating an empty file is not related to sending an ENTRY_CREATE event. 
		 * So, it's necessary to put some initial content in that file.
		 */
		if(!SystemUtils.IS_OS_WINDOWS) {
			mockFileWithValidExt << INITIAL_FILE_CONTENT
		}
		
		waitForAssert{
			assertThat("The " + mockFileWithValidExtName +" file was not created successfully", mockFileWithValidExt.exists(), is (true))
		}
		waitForAssert{
			assertThat("A model was not added/refreshed adequately on new valid file creation in the watched directory",
					modelRepo.isAddOrRefreshModelMethodCalled,is(true))
		}
		waitForAssert{
			assertThat("A model was added/refreshed with a wrong filename",
					modelRepo.calledFileName,is(mockFileWithValidExt.getName()))
		}
	}

	@Test
	void 'test modification of file with valid extension in an existing subdirectory'() {
		/* 
		 * The following method creates a file in an existing directory. The file's extension is
		 * in the configuration properties and there is a registered ModelParser for it.
		 * Then the file's content is changed.
		 * addOrRefreshModel() method invocation is expected
		 */

		String validExtension = "java"

		Dictionary<String, String> configProps = new Hashtable<String, String>()
		configProps.put(EXISTING_SUBDIR_NAME, "txt,jpg," + validExtension)

		String mockFileWithValidExtName = "MockFileForModification" + "." + validExtension
		String mockFileWithValidExtPath = EXISTING_SUBDIR_PATH + File.separatorChar + mockFileWithValidExtName
		File mockFileWithValidExt = new File(mockFileWithValidExtPath)
		mockFileWithValidExt.createNewFile()

		/* The configuration is updated after the file creation in order to check if FolderObserver
		 * has the expected behavior when there are existing files in the watched directory before
		 * the updating of the configuration
		 */
		config.update(configProps)
		sleep(WAIT_EVENT_TO_BE_HANDLED)

		/*
		 * In some OS, like MacOS, creating an empty file is not related to sending an ENTRY_CREATE event. So, it's necessary to put some
		 * initial content in that file.
		 */
		if(!SystemUtils.IS_OS_WINDOWS) {
			mockFileWithValidExt << INITIAL_FILE_CONTENT
		}

		waitForAssert{
			assertThat("The " + mockFileWithValidExtName +" file was not created successfully", mockFileWithValidExt.exists(), is (true))
		}
		waitForAssert{
			assertThat("The creation of the " + mockFileWithValidExtName + " file was not handled adequately and the model was not updated",
					modelRepo.isAddOrRefreshModelMethodCalled,is(true))
		}
		
		/*
		 * setting the isAddOrRefreshModelMethodCalled variable to false,
		 * otherwise the next assertion will always be true
		 */
		modelRepo.isAddOrRefreshModelMethodCalled = false;

		String textToBeInsertedInTheFile= "Additional content"
		mockFileWithValidExt << textToBeInsertedInTheFile
		sleep(WAIT_EVENT_TO_BE_HANDLED)

		waitForAssert{
			assertThat("A model was not added/refreshed adequately on valid file modification in the watched directory",
					modelRepo.isAddOrRefreshModelMethodCalled,is(true))
		}
		waitForAssert{
			assertThat("A model was added/refreshed with a wrong filename",
					modelRepo.calledFileName,is(mockFileWithValidExt.getName()))
		}

		String finalFileContent;
		if(!SystemUtils.IS_OS_WINDOWS){
			finalFileContent = INITIAL_FILE_CONTENT + textToBeInsertedInTheFile
		} else {
			finalFileContent = textToBeInsertedInTheFile
		}

		waitForAssert{
			assertThat("The content of the " + mockFileWithValidExtName + " file is not updated successfully",
					modelRepo.fileContent,is(finalFileContent))
		}
	}
	@Test
	void 'test creation of file with extension which does not have a registered ModelParser in an existing subdirectory'() {
		/*
		 * The following method creates a file in an existing directory. The file's extension is
		 * in the configuration properties but there is no parser for it.
		 * No ModelRepository's method invocation is expected
		 */
		String noParserExtension = "jpg"

		Dictionary<String, String> configProps = new Hashtable<String, String>()
		configProps.put(EXISTING_SUBDIR_NAME, "java,txt," + noParserExtension)
		config.update(configProps)
		sleep(WAIT_ABSTRACTWATCHQUEUEREADER_TO_START)

		String mockFileWithNoParserExtName = "NewlyCreatedMockFile" + "." + noParserExtension
		String mockFileWithNoParserExtPath = EXISTING_SUBDIR_PATH + File.separatorChar + mockFileWithNoParserExtName
		File mockFileWithNoParserExt = new File(mockFileWithNoParserExtPath)
		mockFileWithNoParserExt.createNewFile()
		sleep(WAIT_EVENT_TO_BE_HANDLED)

		waitForAssert{
			assertThat("The " + mockFileWithNoParserExtName +" file was not created successfully", mockFileWithNoParserExt.exists(), is (true))
		}
		waitForAssert{
			assertThat("A model was added/refreshed on creation of file with extension which is does not have a registered ModelParser",
					modelRepo.isAddOrRefreshModelMethodCalled,is(false))
		}
		waitForAssert{
			assertThat("A model was deleted on creation of file with extension which does not have a registered ModelParser",
					modelRepo.isRemoveModelMethodCalled,is(false))
		}
	}

	@Test
	void 'test creation of file with extension which is not in the configuration properties in an existing subdirectory'() {
		/* 
		 * The following method creates a file in an existing directory. The file's extension is not
		 * in the configuration properties but there is a parser for it. 
		 * No ModelRepository's method invocation is expected
		 */
		String notInPropsExtension = "java"

		Dictionary<String, String> configProps = new Hashtable<String, String>()
		configProps.put(EXISTING_SUBDIR_NAME, "txt,jpg")
		config.update(configProps)
		sleep(WAIT_ABSTRACTWATCHQUEUEREADER_TO_START)

		String mockFileWithNotInPropsExtName = "NewlyCreatedMockFile" + "." + notInPropsExtension
		String mockFileWithNotInPropsExtPath = EXISTING_SUBDIR_PATH + File.separatorChar + mockFileWithNotInPropsExtName
		File mockFileWithNotInPropsExt = new File(mockFileWithNotInPropsExtPath)
		mockFileWithNotInPropsExt.createNewFile()
		sleep(WAIT_EVENT_TO_BE_HANDLED)

		waitForAssert{
			assertThat("The " + mockFileWithNotInPropsExtName +" file was not created successfully", mockFileWithNotInPropsExt.exists(), is (true))
		}
		waitForAssert{
			assertThat("A model was added/refreshed on creation of file with extension which is not in the configuration properties",
					modelRepo.isAddOrRefreshModelMethodCalled,is(false))
		}
		waitForAssert{
			assertThat("A model was added/refreshed on creation of file with extension which is not in the configuration properties",
					modelRepo.isRemoveModelMethodCalled,is(false))
		}
	}
	
	@Test
	void 'test deletion of a folder-extensions pair from the configuration properties when folderFileExtMap becomes empty'(){
		/*
		 * The following method tests the correct invocation of removeModel() method when a 
		 * folder-extensions pair is deleted from the configuration properties 
		 * and folderFileExtMap becomes empty
		 */

		Dictionary<String, String> configProps = new Hashtable<String, String>()
		configProps.put(EXISTING_SUBDIR_NAME, "java,txt,jpg")
		config.update(configProps)
		sleep(WAIT_ABSTRACTWATCHQUEUEREADER_TO_START)

		configProps.remove(EXISTING_SUBDIR_NAME)
		config.update(configProps)
		sleep(WAIT_ABSTRACTWATCHQUEUEREADER_TO_START)

		waitForAssert{
			assertThat("The deletion of a folder-extensions pair from the configuration properties was not handled properly and the $MOCK_MODEL_TO_BE_REMOVED was not removed.",
					modelRepo.isRemoveModelMethodCalled,is(true))
		}
		waitForAssert{
			assertThat("The $MOCK_MODEL_TO_BE_REMOVED model is expected to be removed on deletion of folder-extensions pair from the configuration properties",
					modelRepo.calledFileName,is(MOCK_MODEL_TO_BE_REMOVED))
		}
	}

	@Test
	void 'test deletion of a folder-extensions pair from the configuration properties when folderFileExtMap remains non-empty'(){
		/* 
		 * The following method tests the correct invocation of removeModel() method when a
		 * folder-extensions pair is deleted from the configuration properties
		 * and folderFileExtMap remains non-empty
		 */

		new File(EXISTING_SUBDIR_PATH).mkdirs()

		Dictionary<String, String> configProps = new Hashtable<String, String>()
		configProps.put(EXISTING_SUBDIR_NAME, "java,txt,jpg")

		String anotherExistingSubdirName = "another_existing_subdir"
		String anotherExistingSubdirPath = WATCHED_DIRECTORY + File.separator + anotherExistingSubdirName
		new File(anotherExistingSubdirPath).mkdirs()

		configProps.put(anotherExistingSubdirName, "txt,jpg,java")
		config.update(configProps)
		sleep(WAIT_ABSTRACTWATCHQUEUEREADER_TO_START)

		configProps.remove(anotherExistingSubdirName)
		config.update(configProps)
		sleep(WAIT_EVENT_TO_BE_HANDLED)

		waitForAssert{
			assertThat("The deletion of a folder-extensions pair from the configuration properties was not handled properly and the $MOCK_MODEL_TO_BE_REMOVED was not removed.",
					modelRepo.isRemoveModelMethodCalled,is(true))
		}
		waitForAssert{
			assertThat("The $MOCK_MODEL_TO_BE_REMOVED model is expected to be removed on deletion of folder-extensions pair from the configuration properties",
					modelRepo.calledFileName,is(MOCK_MODEL_TO_BE_REMOVED))
		}
	}

	@Test
	void 'test the updating of the configuration with a non existing subdirectory'() {
		/*
		 * The following method test the updating of the configuration with a non existing subdirectory.
		 * Only log message ("Directory 'non_existing_subdir' does not exist in 'watched_dir'...") is expected
		 */
		String nonExistingSubDirName = "non_existing_subdir"
		String nonExistingSubDirPath = WATCHED_DIRECTORY + File.separator + nonExistingSubDirName

		Dictionary<String, String> configProps = new Hashtable<String, String>()
		configProps.put(nonExistingSubDirName, "txt,jpg,java")
		config.update(configProps)
		sleep(WAIT_ABSTRACTWATCHQUEUEREADER_TO_START)

		waitForAssert{
			assertThat("A model was added/refreshed on updating of the configuration with a non existing subdirectory",
					modelRepo.isAddOrRefreshModelMethodCalled,is(false))
		}
		waitForAssert{
			assertThat("A model was deleted on updating of the configuration with a non existing subdirectory",
					modelRepo.isRemoveModelMethodCalled,is(false))
		}
	}

	@Test
	void 'test creation of file in subdirectory with no declared extensions'() {
		/* 
		 * The following method creates a file in an existing directory 
		 * which has no valid extensions declared.
		 * No ModelRepository's method invocation is expected
		 */	 
		String noExtensionsSubDirName = "no_extensions_subdir"
		String noExtensionsSubDirPath = WATCHED_DIRECTORY + File.separator + noExtensionsSubDirName
		File noExtensionsDirectory = new File(noExtensionsSubDirPath)
		noExtensionsDirectory.mkdirs()

		Dictionary<String, String> configProps = new Hashtable<String, String>()
		configProps.put(noExtensionsSubDirName, "")
		config.update(configProps)
		sleep(WAIT_ABSTRACTWATCHQUEUEREADER_TO_START)


		String mockFileInNoExtSubDirName = "MockFileInNoExtSubDir.txt"
		String mockFileInNoExtSubDirPath = noExtensionsSubDirPath + File.separatorChar + mockFileInNoExtSubDirName
		File mockFileInNoExtSubDir = new File(mockFileInNoExtSubDirPath)
		mockFileInNoExtSubDir.createNewFile()
		sleep(WAIT_EVENT_TO_BE_HANDLED)

		waitForAssert{
			assertThat("The " + mockFileInNoExtSubDirName +" file was not created successfully", mockFileInNoExtSubDir.exists(), is (true))
		}
		waitForAssert{
			assertThat("A model was added/refreshed on creation of file in subdirectory with no declared extensions",
					modelRepo.isAddOrRefreshModelMethodCalled,is(false))
		}
		waitForAssert{
			assertThat("A model was deleted on creation of file in subdirectory with no declared extensions",
					modelRepo.isRemoveModelMethodCalled,is(false))
		}
	}

	private static class ModelRepoMock implements ModelRepository{

		public static boolean isAddOrRefreshModelMethodCalled
		public static boolean isRemoveModelMethodCalled
		public static String calledFileName
		public static String fileContent

		@Override
		public boolean addOrRefreshModel(String name, InputStream inputStream) {
			calledFileName = name
			isAddOrRefreshModelMethodCalled = true
			fileContent = inputStream.getText()
			inputStream.close()
			return true
		}

		@Override
		public boolean removeModel(String name) {
			calledFileName = name
			isRemoveModelMethodCalled = true
			return true
		}

		/**
		 * This method is invoked when a model is about to be deleted.
		 * For the purposes of the FolderObserverTest class it is overridden and 
		 * it returns an array of exactly one model name.
		 */
		@Override
		public Iterable<String> getAllModelNamesOfType(String modelType) {

			ArrayList<String> arrayOfModelsToBeRemoved = [MOCK_MODEL_TO_BE_REMOVED]
			return arrayOfModelsToBeRemoved
		}

		@Override
		public void reloadAllModelsOfType(String modelType) {
		}

		@Override
		public void addModelRepositoryChangeListener(ModelRepositoryChangeListener listener) {
		}

		@Override
		public void removeModelRepositoryChangeListener(ModelRepositoryChangeListener listener) {
		}

		@Override
		public EObject getModel(String name) {
			return null
		}

		public void clean() {
			isAddOrRefreshModelMethodCalled = false
			isRemoveModelMethodCalled = false
			calledFileName = null
			fileContent = null
		}
	}

	private static class ModelParserMock implements ModelParser {

		@Override
		public String getExtension() {
			return "java"
		}
	}
}
