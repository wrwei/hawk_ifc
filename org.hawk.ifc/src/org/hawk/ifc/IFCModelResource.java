/*******************************************************************************
 * Copyright (c) 2011-2015 The University of York.
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
 *     Konstantinos Barmpis - initial API and implementation
 *     Antonio Garcia-Dominguez - updates and maintenance
 ******************************************************************************/
package org.hawk.ifc;

import java.io.File;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.plugins.deserializers.DeserializeException;
import org.bimserver.plugins.deserializers.Deserializer;
import org.hawk.ifc.IFCModelResourceFactory.IFCModelType;

public class IFCModelResource extends IFCAbstractModelResource {

	protected File ifc;

	public IFCModelResource(File f, IFCModelResourceFactory p, IFCModelType type) {
		super(p, type);
		ifc = f;
	}

	@Override
	public void unload() {
		ifc = null;
	}

	@Override
	protected IfcModelInterface readModel(Deserializer d) throws DeserializeException {
		return d.read(ifc);
	}

	@Override
	public boolean providesSingletonElements() {
		return false;
	}
}
