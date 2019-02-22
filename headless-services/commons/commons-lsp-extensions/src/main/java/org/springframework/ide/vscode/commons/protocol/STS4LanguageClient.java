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
package org.springframework.ide.vscode.commons.protocol;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageClient;
import org.springframework.ide.vscode.commons.protocol.java.ClasspathListenerParams;
import org.springframework.ide.vscode.commons.protocol.java.JavaDataParams;
import org.springframework.ide.vscode.commons.protocol.java.JavaSearchParams;
import org.springframework.ide.vscode.commons.protocol.java.JavaTypeHierarchyParams;
import org.springframework.ide.vscode.commons.protocol.java.TypeData;

/**
 * Some 'custom' extensions to standard LSP {@link LanguageClient}.
 *
 * @author Kris De Volder
 */
public interface STS4LanguageClient extends LanguageClient {

	@JsonNotification("sts/highlight")
	void highlight(HighlightParams highlights);

	@JsonNotification("sts/progress")
	void progress(ProgressParams progressEvent);

	@JsonRequest("sts/moveCursor")
	CompletableFuture<Object> moveCursor(CursorMovement cursorMovement);

	@JsonRequest("sts/addClasspathListener")
	CompletableFuture<Object> addClasspathListener(ClasspathListenerParams params);

	@JsonRequest("sts/removeClasspathListener")
	CompletableFuture<Object> removeClasspathListener(ClasspathListenerParams classpathListenerParams);

	@JsonRequest("sts/javadoc")
	CompletableFuture<MarkupContent> javadoc(JavaDataParams params);

	@JsonRequest("sts/javaType")
	CompletableFuture<TypeData> javaType(JavaDataParams params);

	@JsonRequest("sts/javadocHoverLink")
	CompletableFuture<String> javadocHoverLink(JavaDataParams params);

	@JsonRequest("sts/javaLocation")
	CompletableFuture<Location> javaLocation(JavaDataParams params);
	
	@JsonRequest("sts/searchJavaTypes")
	CompletableFuture<List<TypeData>> javaSearchTypes(JavaSearchParams params);
	
	@JsonRequest("sts/searchJavaPackages")
	CompletableFuture<List<String>> javaSearchPackages(JavaSearchParams params);
	
	@JsonRequest("sts/javaSubTypes")
	CompletableFuture<List<TypeData>> javaSubTypes(JavaTypeHierarchyParams params);
	
	@JsonRequest("sts/javaSuperTypes")
	CompletableFuture<List<TypeData>> javaSuperTypes(JavaTypeHierarchyParams params);

}
