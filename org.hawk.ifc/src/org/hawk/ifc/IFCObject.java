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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.hawk.core.model.IHawkAttribute;
import org.eclipse.hawk.core.model.IHawkClass;
import org.eclipse.hawk.core.model.IHawkClassifier;
import org.eclipse.hawk.core.model.IHawkDataType;
import org.eclipse.hawk.core.model.IHawkObject;
import org.eclipse.hawk.core.model.IHawkReference;
import org.eclipse.hawk.core.model.IHawkStructuralFeature;

public class IFCObject implements IHawkObject {

	protected EObject eob;

	public IFCObject(EObject o) {
		eob = o;
	}

	public EObject getEObject() {
		return eob;
	}

	@Override
	public String getUri() {
		return EcoreUtil.getURI(eob).toString();
	}

	@Override
	public String getUriFragment() {
		String frag = EcoreUtil.getURI(eob).fragment();
		return frag;
	}

	@Override
	public boolean isFragmentUnique() {
		return false;
	}

	@Override
	public IHawkClassifier getType() {

		return new IFCClass(eob.eClass());
	}

	@Override
	public boolean isSet(IHawkStructuralFeature hsf) {
		return eob.eIsSet(eob.eClass().getEStructuralFeature(hsf.getName()));
	}

	@Override
	public Object get(IHawkAttribute attribute) {
		return eob
				.eGet(eob.eClass().getEStructuralFeature(attribute.getName()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object get(IHawkReference reference, boolean b) {

		Object ret;

		Object source = eob.eGet(
				eob.eClass().getEStructuralFeature(reference.getName()), b);

		if (source instanceof Iterable<?>) {
			ret = new HashSet<EObject>();
			for (EObject e : ((Iterable<EObject>) source)) {
				((HashSet<IFCObject>) ret).add(new IFCObject(e));
			}
		} else
			ret = new IFCObject((EObject) source);

		return ret;

	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof IFCObject)
			return eob.equals(((IFCObject) o).getEObject());
		else
			return false;
	}

	@Override
	public int hashCode() {
		//System.err.println("WARNING HASHCODE CALLED ON IFCOBJECT -- this is inaccuarate, use signature() instead!");
		return eob.hashCode();
	}

	byte[] signature = null;

	@SuppressWarnings("unchecked")
	@Override
	public byte[] signature() {

		if (signature == null) {

			if (eob.eIsProxy()) {

				System.err
						.println("signature called on proxy object returning null");
				return null;

			} else {

				MessageDigest md = null;

				try {
					md = MessageDigest.getInstance("SHA-1");
				} catch (NoSuchAlgorithmException e) {
					System.err
							.println("signature() tried to create a SHA-1 digest but a NoSuchAlgorithmException was thrown, returning null");
					return null;
				}

				md.update(getUri().getBytes());
				md.update(getUriFragment().getBytes());

				IHawkClassifier type = getType();

				md.update(type.getName().getBytes());
				md.update(type.getPackageNSURI().getBytes());

				if (type instanceof IHawkDataType) {

					//

				} else if (type instanceof IHawkClass) {

					for (IHawkAttribute eAttribute : ((IHawkClass) type)
							.getAllAttributes()) {
						if (eAttribute.isDerived() || isSet(eAttribute)) {

							md.update(eAttribute.getName().getBytes());

							if (!eAttribute.isDerived())
								// XXX NOTE: using toString for hashcode of
								// attribute values as primitives in java have
								// different hashcodes each time, not fullproof
								// true == "true" here
								md.update(get(eAttribute).toString().getBytes());
							else {

								// handle derived attributes for metamodel
								// evolution

							}
						}
					}

					for (IHawkReference eRef : ((IHawkClass) type)
							.getAllReferences()) {
						if (isSet(eRef)) {

							md.update(eRef.getName().getBytes());

							Object destinationObjects = get(eRef, false);
							if (destinationObjects instanceof Iterable<?>) {
								for (IHawkObject o : ((Iterable<IHawkObject>) destinationObjects)) {
									md.update(o.getUriFragment().getBytes());
								}
							} else {
								md.update(((IHawkObject) destinationObjects)
										.getUriFragment().getBytes());
							}
						}
					}
				} else {
					System.err
							.println("warning emf object tried to create signature, but found type: "
									+ type);
				}
				signature = md.digest();
			}
		}
		return signature;
	}

	@Override
	public String toString() {

		String ret = "";

		ret += ">" + eob + "\n";

		for (EAttribute e : eob.eClass().getEAllAttributes())
			ret += e + " : " + eob.eGet(e);
		ret += "\n";

		return ret;

	}

	@Override
	public boolean isRoot() {
		return eob.eContainer() == null;
	}

	@Override
	public boolean URIIsRelative() {

		return EcoreUtil.getURI(eob).isRelative();

	}

	@Override
	public boolean isInDifferentResourceThan(IHawkObject o) {
		if (o instanceof IFCObject) {
			final IFCObject otherR = (IFCObject)o;
			return eob.eIsProxy() || otherR.eob.eResource() != eob.eResource();
		}
		return false;
	}

}
