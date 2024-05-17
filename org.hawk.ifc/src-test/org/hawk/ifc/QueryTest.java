package org.hawk.ifc;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.eclipse.hawk.backend.tests.factories.IGraphDatabaseFactory;
import org.eclipse.hawk.core.IModelIndexer;
import org.eclipse.hawk.core.IStateListener.HawkState;
import org.eclipse.hawk.core.graph.IGraphDatabase;
import org.eclipse.hawk.core.runtime.ModelIndexerImpl;
import org.eclipse.hawk.core.security.FileBasedCredentialsStore;
import org.eclipse.hawk.core.util.DefaultConsole;
import org.eclipse.hawk.epsilon.emc.EOLQueryEngine;
import org.eclipse.hawk.graph.updater.GraphMetaModelUpdater;
import org.eclipse.hawk.graph.updater.GraphModelUpdater;
import org.eclipse.hawk.localfolder.LocalFolder;
import org.eclipse.hawk.sqlite.tests.SQLiteDatabaseFactory;
import org.eclipse.hawk.workspace.Workspace;
import org.hawk.ifc.mm.IFCMetaModelResourceFactory;

public class QueryTest {

	public static void main(String[] args) throws Exception {
		Path SAMPLES_FOLDER = Paths.get("index");
//		IGraphDatabaseFactory dbFactory = new OrientDatabaseFactory();
//		IGraphDatabaseFactory dbFactory = new LevelDBGreycatDatabaseFactory();
		IGraphDatabaseFactory dbFactory = new SQLiteDatabaseFactory();
		IGraphDatabase db = dbFactory.create();
		
		final File indexerFolder = SAMPLES_FOLDER.toFile();
		final File dbFolder = new File(indexerFolder, "test_123");
		dbFolder.mkdir();
		
		DefaultConsole console = new DefaultConsole();
		db.run(dbFolder, console);
		
		final FileBasedCredentialsStore credStore = new FileBasedCredentialsStore(new File("keystore"),
				"admin".toCharArray());
		
		IModelIndexer indexer = null;
		indexer = new ModelIndexerImpl("test", indexerFolder, credStore, console);
		indexer.addMetaModelResourceFactory(new IFCMetaModelResourceFactory());
		indexer.addModelResourceFactory(new IFCModelResourceFactory());

		
		
		EOLQueryEngine queryEngine = new EOLQueryEngine();
		indexer.addQueryEngine(queryEngine);
		indexer.setMetaModelUpdater(new GraphMetaModelUpdater());
		indexer.addModelUpdater(new GraphModelUpdater());
		indexer.setDB(db, true);

		try {
			indexer.init(0, 0);
			final LocalFolder vcs = new LocalFolder();
			final File localFolder = new File(indexerFolder.getParentFile(), "models");
			localFolder.mkdir();
			vcs.init(localFolder.getCanonicalPath(), indexer);
			vcs.run();
			indexer.addVCSManager(vcs, true);
			
			queryEngine.load(indexer);
			
			String query = "\'Executing query:\'.errln(); \n"
					+ "IfcPerson.all().println();";
			while(indexer.getCompositeStateListener().getCurrentState() != HawkState.RUNNING) {
				Thread.sleep(1000);
				System.out.println(".");
			}
			System.out.println(queryEngine.query(indexer, query, Collections.emptyMap()));
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
//			indexer.shutdown(ShutdownRequestType.ALWAYS);
		}
	}

}
