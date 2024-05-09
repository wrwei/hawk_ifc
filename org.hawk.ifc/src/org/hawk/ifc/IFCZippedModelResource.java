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

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.plugins.deserializers.DeserializeException;
import org.bimserver.plugins.deserializers.Deserializer;
import org.hawk.ifc.IFCModelResourceFactory.IFCModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IFCZippedModelResource extends IFCAbstractModelResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(IFCZippedModelResource.class);

	private ZipFile zipFile;
	private List<ZipEntry> ifcEntries;

	public IFCZippedModelResource(ZipFile zf, List<ZipEntry> ifcEntries, IFCModelResourceFactory ifcModelFactory,
			IFCModelType type) {
		super(ifcModelFactory, type);
		this.zipFile = zf;
		this.ifcEntries = ifcEntries;
	}

	@Override
	public void unload() {
		try {
			zipFile.close();
		} catch (IOException e) {
			LOGGER.error("Could not close the zip file", e);
		}
		zipFile = null;
		ifcEntries = null;
	}

	@Override
	protected IfcModelInterface readModel(Deserializer d) throws DeserializeException, IOException {
		// The factory only reports a non-unknown type if the zip has at least
		// one .ifc* file, so this should be safe.
		final ZipEntry first = ifcEntries.get(0);

		return d.read(zipFile.getInputStream(first), first.getName(), first.getSize());
	}

	@Override
	public boolean providesSingletonElements() {
		return false;
	}

}
