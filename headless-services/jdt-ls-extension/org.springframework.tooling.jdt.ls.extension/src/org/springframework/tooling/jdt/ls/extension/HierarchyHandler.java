/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.extension;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;

@SuppressWarnings("restriction")
public class HierarchyHandler implements IDelegateCommandHandler {

	@SuppressWarnings("unchecked")
	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
		Map<String, Object> obj = (Map<String, Object>) arguments.get(0);
		String projectUri = (String) obj.get("projectUri");
		String fqName = (String) obj.get("fqName");
		switch (commandId) {
		case "sts.java.hierarchy.subtypes":
			return JavaHelpers.HIERARCHY.get().subTypes(URI.create(projectUri), fqName).collect(Collectors.toList());
		case "sts.java.hierarchy.supertypes":
			return JavaHelpers.HIERARCHY.get().superTypes(URI.create(projectUri), fqName).collect(Collectors.toList());
		default:
			return null;
		}
	}

}
