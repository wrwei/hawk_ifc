/*******************************************************************************
 * Copyright (c) 2011-2016 The University of York.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Antonio Garcia-Dominguez - initial implementation
 ******************************************************************************/
package org.hawk.ifc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.hawk.core.model.IHawkModelResource;
import org.eclipse.hawk.core.model.IHawkObject;
import org.hawk.ifc.IFCModelResourceFactory.IFCModelType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class IFCModelFactoryTests {
	/*
	 * Path to the directory with all the unzipped example files sent by Bruno
	 * through WeTransfer on 2015-08-27 (ask Antonio)
	 */
	private static final Path BRUNO_WETRANSFER_FOLDER = Paths.get("/tmp/wetransfer");
	private static final Path SAMPLES_FOLDER = Paths.get("samples");

	@Parameterized.Parameter(0)
	public Path filePath;

	@Parameterized.Parameter(1)
	public IFCModelType expectedType;

	private IFCModelResourceFactory factory;

	@Before
	public void setup() {
		factory = new IFCModelResourceFactory();
	}

	@Parameterized.Parameters
	public static Iterable<Object[]> data() {
		final List<Object[]> data = new ArrayList<>();

		data.addAll(
			Arrays.asList(new Object[][]{
				{SAMPLES_FOLDER.resolve("Paolo_.ifc"), IFCModelType.IFC2X3_STEP},
				{SAMPLES_FOLDER.resolve("Paolo_.ifczip"), IFCModelType.IFC2X3_STEP},
				{SAMPLES_FOLDER.resolve("DDS-DuplexHouse_Sanitary_V1.0.ifc"), IFCModelType.IFC2X3_STEP},
				{SAMPLES_FOLDER.resolve("WallOnly.ifc"), IFCModelType.IFC2X3_STEP},
				{SAMPLES_FOLDER.resolve("WallOnlyAddedSpace.ifc"), IFCModelType.IFC2X3_STEP},
				{SAMPLES_FOLDER.resolve("WallOnlyAddedSpace.ifc.zip"), IFCModelType.IFC2X3_STEP},
			}));

		if (BRUNO_WETRANSFER_FOLDER.toFile().exists()) {
			data.addAll(Arrays.asList(new Object[][] {
				// To be parsable, the FILE_NAME header needs to have the trailing $,$ replaced by '',$ for
				// some reason: the BIMserver IfcStepDeserializer doesn't allow the originating system
				// to be unset, it seems.
				{BRUNO_WETRANSFER_FOLDER.resolve("Statsbygg-HIBO-ARK-20080410.ifc"), IFCModelType.IFC2X3_STEP},
				{BRUNO_WETRANSFER_FOLDER.resolve("SB-HIBO-ARK-20070830.ifc"), IFCModelType.IFC2X3_STEP},

				// This needs replacing -1.#IND with -1 all over the file (what does .#IND mean?)
				{BRUNO_WETRANSFER_FOLDER.resolve("hotel.ifc.txt"), IFCModelType.IFC2X3_STEP},

				// These parse fine as is
				{BRUNO_WETRANSFER_FOLDER.resolve("A_JCC_Central_V9.ifc.txt"), IFCModelType.IFC2X3_STEP},
				{BRUNO_WETRANSFER_FOLDER.resolve("DDS-DuplexHouse_Sanitary_V1.0.ifc.txt"), IFCModelType.IFC2X3_STEP},
				{BRUNO_WETRANSFER_FOLDER.resolve("SEC_archi.ifc.txt"), IFCModelType.IFC2X3_STEP},
				{BRUNO_WETRANSFER_FOLDER.resolve("zulu.ifc.txt"), IFCModelType.IFC2X3_STEP},

				// These cannot be parsed with the BIMserver deserializers (only IFC 2x3 and 4 are supported),
				// but at least we can tell them apart.
				{BRUNO_WETRANSFER_FOLDER.resolve("Bodo.IFC"), IFCModelType.IFC2X2_STEP},
				{BRUNO_WETRANSFER_FOLDER.resolve("HiB_DuctWork.Ifc"), IFCModelType.IFC2X2_STEP},
				{BRUNO_WETRANSFER_FOLDER.resolve("HiB_PipeWork.Ifc"), IFCModelType.IFC2X2_STEP},
				{BRUNO_WETRANSFER_FOLDER.resolve("Munkerud_PipeWork_Complete_2x2.Ifc"), IFCModelType.IFC2X2_STEP},
				{BRUNO_WETRANSFER_FOLDER.resolve("Planer 4B Full.IFC"), IFCModelType.IFC2X2_STEP},
				{BRUNO_WETRANSFER_FOLDER.resolve("operahouse.ifcxml.txt"), IFCModelType.IFC2X2_XML},
			}));
		} else {
			System.err.println("Warning: Bruno's samples are not available");
		}

		return data;
	}

	@Test
	public void typeIsCorrectlyDetected() {
		final File f = filePath.toFile();
		assertEquals("The correct model type should be detected for " + f,
			expectedType, factory.getIFCModelType(f));
	}

	@Test
	public void fullParseIsSuccessful() {
		final File f = filePath.toFile();
		if (factory.canParse(f)) {
			final Set<IHawkObject> contents = factory.parse(null, f).getAllContentsSet();
			IHawkModelResource resource = factory.parse(null, f);
//			runStringEOLQuery(resource, "1.println();");
			System.out.println(contents.size());
			assertFalse("The model contents should not be empty for " + f.getName(), contents.isEmpty());
		}
	}
	
	public static String runStringEOLQuery(IModel model, String query) throws Exception {
		Object result = null;
		EolModule eolModule = new EolModule();
		eolModule.getContext().getModelRepository().addModel(model);
		try {
			eolModule.parse(query);
		} catch (Exception e) {
			throw new IllegalStateException("Pattern expression cannot be parsed.", e);
		}

		try {
			result = eolModule.execute();
		} catch (EolRuntimeException e) {
			throw new IllegalStateException(String.format("The query <%s> could not be evaluated.", query), e);
		}
		return "";
	}
}
