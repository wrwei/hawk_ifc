/*******************************************************************************
 * Copyright (c) 2019 The University of York, Aston University.
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

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

public class ScheduledTask<T> implements Callable<T> {
	private final Callable<T> r;
	private final Semaphore sem;
	private Throwable ex = null;

	public ScheduledTask(Callable<T> wrapped, Semaphore sem) {
		this.r = wrapped;
		this.sem = sem;
	}

	@Override
	public T call() throws Exception {
		T result = null;
		try {
			if (r != null) {
				result = r.call();
			}
		} catch (Throwable e) {
			ex = e;
		}
		sem.release();
		return result;
	}

	public Throwable getThrowable() {
		return ex;
	}
}