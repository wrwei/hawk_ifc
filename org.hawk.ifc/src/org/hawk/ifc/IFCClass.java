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
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.hawk.core.model.IHawkAttribute;
import org.eclipse.hawk.core.model.IHawkClass;
import org.eclipse.hawk.core.model.IHawkReference;
import org.eclipse.hawk.core.model.IHawkStructuralFeature;

public class IFCClass extends IFCObject implements IHawkClass {

	private EClass eclass;

	// private String containingFeatureName = null;

	// private static HashMap<EClass, Collection<EClass>> eAllSubTypes;

	public IFCClass(EClass o) {

		super(o);
		eclass = ((EClass) o);

	}

	public EObject getEObject() {
		return eclass;

	}

	@Override
	public String getName() {
		return eclass.getName();
	}

	@Override
	public String getInstanceType() {

		String it = eclass.getInstanceClassName();

		it = it == null ? "NULL_INSTANCE_TYPE" : it;

		switch (it) {
		case "long":
			return Long.class.getName();
		case "int":
			return Integer.class.getName();
		case "float":
			return Float.class.getName();
		case "double":
			return Double.class.getName();
		case "boolean":
			return Boolean.class.getName();
		}

		return it;
	}

	@Override
	public String getPackageNSURI() {
		return eclass.getEPackage().getNsURI();
	}

	@Override
	public HashSet<IHawkAttribute> getAllAttributes() {

		HashSet<IHawkAttribute> atts = new HashSet<IHawkAttribute>();

		for (EAttribute att : eclass.getEAllAttributes())
			atts.add(new IFCAttribute(att));

		return atts;
	}

	@Override
	public HashSet<IHawkClass> getAllSuperTypes() {

		HashSet<IHawkClass> c = new HashSet<IHawkClass>();

		for (EClass e : eclass.getEAllSuperTypes()) {

			c.add(new IFCClass(e));

		}

		return c;

	}

	@Override
	public Set<IHawkClass> getSuperTypes() {
		return getAllSuperTypes();
	}

	@Override
	public HashSet<IHawkReference> getAllReferences() {

		HashSet<IHawkReference> c = new HashSet<IHawkReference>();

		for (EReference e : eclass.getEAllReferences()) {

			c.add(new IFCReference(e));

		}

		return c;

	}

	@Override
	public boolean isAbstract() {
		return eclass.isAbstract();
	}

	@Override
	public boolean isInterface() {
		return eclass.isInterface();
	}

	@Override
	public IHawkStructuralFeature getStructuralFeature(String name) {

		EStructuralFeature esf = eclass.getEStructuralFeature(name);

		if (esf instanceof EAttribute)
			return new IFCAttribute((EAttribute) esf);
		else if (esf instanceof EReference)
			return new IFCReference((EReference) esf);
		else {
			System.err.println("getEStructuralFeature( " + name
					+ " ) is not an attribute or a reference, debug:");
			return null;
		}
	}

	// @Override
	// public Set<HawkClass> eAllContents() {
	// Iterator<EObject> it = eclass.eAllContents();
	//
	// HashSet<HawkClass> ret = new HashSet<HawkClass>();
	//
	// while (it.hasNext()) {
	//
	// ret.add(new EMFclass(((EClass) it.next())));
	//
	// }
	//
	// return ret;
	//
	// }

	// @Override
	// public boolean isContained() {
	//
	// for (EClassifier e : eclass.getEPackage().getEClassifiers()) {
	//
	// if (e instanceof EClass) {
	//
	// for (EReference r : ((EClass) e).getEAllContainments()) {
	//
	// // System.err.println(r.getName() + " ->" + r.getEType());
	// EClassifier type = r.getEType();
	//
	// if (type instanceof EClass) {
	//
	// // System.err.print(eclass.getName()+" :: ");
	//
	// Collection<EClass> eclasssubtypes = getEAllSubTypes(((EClass) eclass));
	//
	// for (EClass s : eclasssubtypes) {
	//
	// // System.err.print(s.getName()+" ");
	//
	// if (//!eclasssubtypes.contains(e) &&
	// s.getName().equals(type.getName())) {
	// System.err.println("containment found! "
	// + eclass.getName()
	// + " is contained by: " + r.getName()
	// + " in " + e.getName());
	// containingFeatureName = r.getName();
	// // why 3 times the check on success?? and why all these containments on
	// commenting:
	// //return true;
	//
	// }
	// }
	// // System.err.println();
	// }
	// }
	// }
	// }
	//
	// System.err.println("warning isContained called on class: "
	// + eclass.getName() + " but this class is not contained!");
	// return false;
	//
	// }

	// @Override
	// public String eContainingFeatureName() {
	// return containingFeatureName;
	// }

	// private Collection<EClass> getEAllSubTypes(EClass eClass) {
	//
	// if (eAllSubTypes == null) {
	//
	// eAllSubTypes = new HashMap<>();
	//
	// for (EClassifier e1 : eclass.getEPackage().getEClassifiers()) {
	//
	// if (e1 instanceof EClass) {
	// for (EClass e2 : ((EClass) e1).getEAllSuperTypes()) {
	//
	// Collection<EClass> col = eAllSubTypes.get(e1);
	//
	// if (col != null) {
	// col.add(e2);
	// eAllSubTypes.put((EClass) e1, col);
	// } else {
	// col = new HashSet<>();
	// col.add((EClass) e1);
	// col.add(e2);
	// eAllSubTypes.put((EClass) e1, col);
	// }
	//
	// }
	// }
	//
	// }
	// }
	//
	// // for(EClass e : eAllSubTypes.keySet()){
	// // System.err.print(e.getName()+" :: ");
	// // for(EClass e2 : eAllSubTypes.get(e)){
	// // System.err.print(e2.getName()+" ");
	// // }
	// // System.err.println();
	// // }
	//
	// Collection<EClass> ret = eAllSubTypes.get(eClass);
	//
	// return ret == null ? new HashSet<EClass>() : ret;
	//
	// }

}
