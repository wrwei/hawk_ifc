/*******************************************************************************
 * Copyright (c) 2015-2017 The University of York, Aston University.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 3.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-3.0
 *
 * Contributors:
 *     Antonio Garcia-Dominguez - initial API and implementation
 ******************************************************************************/
package org.hawk.ifc;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.hawk.backend.tests.factories.IGraphDatabaseFactory;
import org.eclipse.hawk.core.IMetaModelResourceFactory;
import org.eclipse.hawk.core.IModelIndexer;
import org.eclipse.hawk.core.IModelResourceFactory;
import org.eclipse.hawk.core.IModelIndexer.ShutdownRequestType;
import org.eclipse.hawk.core.graph.IGraphChangeListener;
import org.eclipse.hawk.core.graph.IGraphDatabase;
import org.eclipse.hawk.core.query.InvalidQueryException;
import org.eclipse.hawk.core.query.QueryExecutionException;
import org.eclipse.hawk.core.runtime.ModelIndexerImpl;
import org.eclipse.hawk.core.security.FileBasedCredentialsStore;
import org.eclipse.hawk.core.util.DefaultConsole;
import org.eclipse.hawk.epsilon.emc.EOLQueryEngine;
import org.eclipse.hawk.graph.syncValidationListener.SyncValidationListener;
import org.eclipse.hawk.graph.syncValidationListener.SyncValidationListener.ValidationError;
import org.eclipse.hawk.graph.updater.GraphMetaModelUpdater;
import org.eclipse.hawk.graph.updater.GraphModelUpdater;
import org.eclipse.hawk.localfolder.LocalFolder;
import org.eclipse.hawk.workspace.Workspace;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Base class for all integration test cases involving the indexing of a certain
 * graph and querying afterwards.
 */
@RunWith(Parameterized.class)
public class ModelIndexingTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Rule
	public TestName testName = new TestName();

	/**
	 * Rule that will attach a {@link SyncValidationListener} to the instance
	 * during the test.
	 */
	public class GraphChangeListenerRule<T extends IGraphChangeListener> extends ExternalResource {
		private T listener;

		public T getListener() {
			return listener;
		}

		public GraphChangeListenerRule(T listener) {
			this.listener = listener;
		}

		@Override
		protected void before() throws Throwable {
			if (indexer == null) {
				ModelIndexingTest.this.setup();
			}
			indexer.addGraphChangeListener(listener);
			listener.setModelIndexer(indexer);
		}

		@Override
		protected void after() {
			indexer.removeGraphChangeListener(listener);
		}
	}

	public interface IModelSupportFactory {
		IMetaModelResourceFactory createMetaModelResourceFactory();

		IModelResourceFactory createModelResourceFactory();
	}

	protected DefaultConsole console;
	protected IModelIndexer indexer;
	protected EOLQueryEngine queryEngine;
	protected IGraphDatabase db;

	/**
	 * Base directory for all the resources.
	 */
	protected final File baseDir;

	private IGraphDatabaseFactory dbFactory;
	private IModelSupportFactory msFactory;

	public ModelIndexingTest(File baseDir, IGraphDatabaseFactory dbFactory, IModelSupportFactory msFactory) {
		this.baseDir = baseDir;
		this.dbFactory = dbFactory;
		this.msFactory = msFactory;
	}

	@Before
	public void setup() throws Throwable {
		if (indexer != null) {
			// Might have been invoked by a rule before
			return;
		}

		final File indexerFolder = tempFolder.getRoot();
		final File dbFolder = new File(indexerFolder, "test_" + testName.getMethodName());
		dbFolder.mkdir();

		console = new DefaultConsole();
		db = dbFactory.create();
		db.run(dbFolder, console);

		final FileBasedCredentialsStore credStore = new FileBasedCredentialsStore(new File("keystore"),
				"admin".toCharArray());

		indexer = createIndexer(indexerFolder, credStore);
		indexer.addMetaModelResourceFactory(msFactory.createMetaModelResourceFactory());
		indexer.addModelResourceFactory(msFactory.createModelResourceFactory());

		queryEngine = new EOLQueryEngine();
		indexer.addQueryEngine(queryEngine);
		indexer.setMetaModelUpdater(new GraphMetaModelUpdater());
		indexer.addModelUpdater(createModelUpdater());
		indexer.setDB(db, true);

		indexer.init(0, 0);
	}

	protected IModelIndexer createIndexer(final File indexerFolder, final FileBasedCredentialsStore credStore) {
		return new ModelIndexerImpl("test", indexerFolder, credStore, console);
	}

	protected GraphModelUpdater createModelUpdater() {
		return new GraphModelUpdater();
	}

	@After
	public void teardown() throws Exception {
		indexer.shutdown(ShutdownRequestType.ALWAYS);
		db.delete();
	}

	/**
	 * Simple version which just waits for the indexing to happen.
	 */
	protected void scheduleAndWait() throws Throwable {
		scheduleAndWait(() -> null);
	}

	/**
	 * Schedules a piece of code on the same thread as Hawk's indexing, and waits
	 * for it to be run. Times out with a test failure after 10 minutes.
	 */
	protected void scheduleAndWait(final Callable<?> r) throws Throwable {
		final Semaphore sem = new Semaphore(0);
		final ScheduledTask<?> task = new ScheduledTask<>(r, sem);
		indexer.scheduleTask(task, 0);
		if (!sem.tryAcquire(1200, TimeUnit.SECONDS)) {
			fail("Synchronization timed out");
		} else {
			if (task.getThrowable() != null) {
				throw task.getThrowable();
			}
		}
	}

	protected LocalFolder requestFolderIndex(final File folder) throws Exception {
		final LocalFolder vcs = new LocalFolder();
		vcs.init(folder.getCanonicalPath(), indexer);
		vcs.run();
		indexer.addVCSManager(vcs, true);
		return vcs;
	}

	protected void requestWorkspaceIndex() throws Exception {
		final Workspace vcs = new Workspace();
		vcs.init("/", indexer);
		vcs.run();
		indexer.addVCSManager(vcs, true);
	}
	
	protected void assertNoErrors(SyncValidationListener listener) {
		List<ValidationError> errors = listener.getErrors();

		if (!errors.isEmpty()) {
			System.err.println("ERRORS DURING VALIDATION");
			for (ValidationError e : errors) {
				System.err.println("- " + e.getMessage());
				System.err.println();
			}
			fail("Errors during validation");
		}
	}

	protected Object eol(final String eolQuery) throws InvalidQueryException, QueryExecutionException {
		return eol(eolQuery, null);
	}

	protected Object eol(final String eolQuery, Map<String, Object> context) throws InvalidQueryException, QueryExecutionException {
		return queryEngine.query(indexer, eolQuery, context);
	}

	protected Object eolWorkspace(final String query) throws InvalidQueryException, QueryExecutionException {
		return eol(query,
			Collections.singletonMap(EOLQueryEngine.PROPERTY_REPOSITORYCONTEXT, Workspace.REPOSITORY_URL));
	}

	protected IProject openProject(final File projectFolder) throws CoreException {
		final File projectFile = new File(projectFolder, ".project");
		final Path projectPath = new Path(projectFile.getAbsolutePath());

		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IProjectDescription description = ws.loadProjectDescription(projectPath);
		IProject project = ws.getRoot().getProject(description.getName());
		if (!project.exists()) {
			project.create(description, null);
		}
		if (!project.isOpen()) {
			project.open(null);
		}

		return project;
	}
}