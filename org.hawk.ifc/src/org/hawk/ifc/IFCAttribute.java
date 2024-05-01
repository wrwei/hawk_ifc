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

import java.util.HashSet;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.hawk.core.model.IHawkAnnotation;
import org.eclipse.hawk.core.model.IHawkAttribute;
import org.eclipse.hawk.core.model.IHawkClassifier;

public class IFCAttribute extends IFCObject implements IHawkAttribute {

	private EAttribute emfattribute;

	public IFCAttribute(EAttribute att) {
		super(att);
		emfattribute = att;
	}

	// public EAttribute getEmfattribute() {
	// return emfattribute;
	// }

	public boolean isDerived() {
		return emfattribute.isDerived();
	}

	public String getName() {
		return emfattribute.getName();
	}

	public HashSet<IHawkAnnotation> getAnnotations() {

		HashSet<IHawkAnnotation> ann = new HashSet<IHawkAnnotation>();

		for (EAnnotation e : emfattribute.getEAnnotations()) {

			IHawkAnnotation a = new IFCAnnotation(e);

			ann.add(a);

		}

		return ann;

	}

	// @Override
	// public EStructuralFeature getEMFattribute() {
	//
	// return emfattribute;
	// }

	@Override
	public boolean isMany() {
		return emfattribute.isMany();
	}

	@Override
	public boolean isUnique() {
		return emfattribute.isUnique();
	}

	@Override
	public boolean isOrdered() {
		return emfattribute.isOrdered();
	}

	@Override
	public IHawkClassifier getType() {
		EClassifier type = emfattribute.getEType();
		if (type instanceof EClass)
			return new IFCClass((EClass) emfattribute.getEType());
		else if (type instanceof EDataType)
			return new IFCDataType((EDataType) emfattribute.getEType());
		else {
			// System.err.println("attr: "+emfattribute.getEType());
			return null;
		}
	}

	public EAttribute getEObject() {
		return null;
	}

}
