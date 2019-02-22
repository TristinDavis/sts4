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
package org.springframework.tooling.ls.eclipse.commons.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.ide.vscode.commons.protocol.java.JavaDataParams;
import org.springframework.ide.vscode.commons.protocol.java.JavaSearchParams;
import org.springframework.ide.vscode.commons.protocol.java.JavaTypeHierarchyParams;
import org.springframework.ide.vscode.commons.protocol.java.TypeData;
import org.springframework.tooling.ls.eclipse.commons.STS4LanguageClientImpl;

public class JavaLangugeClientTest {

	private static STS4LanguageClientImpl client = new STS4LanguageClientImpl();

	private static IProject project;

	@AfterClass
	public static void tearDown() throws Exception {
		TestUtils.deleteAllProjects();
	}

	@BeforeClass
	public static void setupAll() throws Exception {
		project = TestUtils.importMavenProject("test-webflux-project");
		assertNotNull(project);
	}

	@Test
	public void findTypeInJRE() throws Exception {
		TypeData data = client
				.javaType(new JavaDataParams(project.getLocationURI().toString(), "Ljava/util/Map;", true))
				.get(10, TimeUnit.SECONDS);
		assertNotNull(data);
		assertEquals(TestUtils.loadJsonString("Map.json"), TestUtils.toJsonString(data));
	}

	@Test
	public void findTypeInJar() throws Exception {
		TypeData data = client
				.javaType(new JavaDataParams(project.getLocationURI().toString(),
						"Lorg/springframework/boot/autoconfigure/web/ServerProperties;", true))
				.get(10, TimeUnit.SECONDS);
		assertNotNull(data);
		assertEquals(TestUtils.loadJsonString("ServerProperties.json"), TestUtils.toJsonString(data));
	}

	@Test
	public void findTypeInSource1() throws Exception {
		TypeData data = client
				.javaType(new JavaDataParams(project.getLocationURI().toString(), "Lorg/test/Quote;", true))
				.get(10, TimeUnit.SECONDS);
		assertNotNull(data);
		assertEquals(TestUtils.loadJsonString("Quote.json"), TestUtils.toJsonString(data));
	}

	@Test
	public void findTypeInSource2() throws Exception {
		TypeData data = client
				.javaType(new JavaDataParams(project.getLocationURI().toString(), "Lorg/test/NestedRouter3;", true))
				.get(10, TimeUnit.SECONDS);
		assertNotNull(data);
		assertEquals(TestUtils.loadJsonString("NestedRouter3.json"), TestUtils.toJsonString(data));
	}

	@Test
	public void fuzzyFindTypesIncludingSysLibs() throws Exception {
		List<TypeData> data = client
				.javaSearchTypes(new JavaSearchParams(project.getLocationURI().toString(), "util.Map", true, true))
				.get(100, TimeUnit.SECONDS);
		assertNotNull(data);
		assertTrue(data.size() > 500);
		List<TypeData> closeMatches = data.stream().filter(t -> t.getFqName().contains("util.Map")).collect(Collectors.toList());
		assertEquals(2, closeMatches.size());
		assertNotNull(closeMatches.stream().filter(t -> "java.util.Map".equals(t.getFqName())).findFirst().orElse(null));
	}
	
	@Test
	public void fuzzyFindTypesExcludingSysLibs() throws Exception {
		List<TypeData> data = client
				.javaSearchTypes(new JavaSearchParams(project.getLocationURI().toString(), "util.Map", true, false))
				.get(10, TimeUnit.SECONDS);
		assertNotNull(data);
		assertEquals(186, data.size());
		TestUtils.saveJsonData("search-util-map.json", data);
		List<TypeData> closeMatches = data.stream().filter(t -> t.getFqName().contains("util.Map")).collect(Collectors.toList());
		assertEquals(1, closeMatches.size());
		assertEquals("io.netty.util.Mapping", closeMatches.get(0).getFqName());
	}
	
	@Test
	public void searchPackagesIncludingSysLibs() throws Exception {
		List<String> packages = client.javaSearchPackages(new JavaSearchParams(project.getLocationURI().toString(), "java.lang", true, true)).get(30, TimeUnit.SECONDS);
		assertTrue(packages.size() > 15 && packages.size() < 25);
	}

	@Test
	public void searchPackagesExcludingSysLibs() throws Exception {
		List<String> packages = client.javaSearchPackages(new JavaSearchParams(project.getLocationURI().toString(), "java.lang", true, false)).get(30, TimeUnit.SECONDS);
		assertEquals(1, packages.size());
		packages = client.javaSearchPackages(new JavaSearchParams(project.getLocationURI().toString(), "org.test", true, false)).get(30, TimeUnit.SECONDS);
		assertTrue(packages.contains("org.test"));
	}

	@Test
	public void map_Subtypes() throws Exception {
		List<TypeData> data = client
				.javaSubTypes(new JavaTypeHierarchyParams(project.getLocationURI().toString(), "java.util.Map"))
				.get(10, TimeUnit.SECONDS);
		assertNotNull(data);
		assertTrue(data.size() > 200);
		assertTrue(data.stream().filter(t -> "java.util.AbstractMap".equals(t.getFqName())).findFirst().isPresent());
	}
	
	@Test
	public void arrayList_SuperTypes() throws Exception {
		List<TypeData> data = client
				.javaSuperTypes(new JavaTypeHierarchyParams(project.getLocationURI().toString(), "java.util.ArrayList"))
				.get(10, TimeUnit.SECONDS);
		assertNotNull(data);
		Set<String> actual = data.stream().map(t -> t.getFqName()).collect(Collectors.toSet());
		Set<String> expected = new HashSet<>(Arrays.asList(
				"java.util.List",
				"java.util.RandomAccess",
				"java.lang.Cloneable",
				"java.io.Serializable",
				"java.util.AbstractList",
				"java.util.Collection",
				"java.lang.Object",
				"java.util.AbstractCollection",
				"java.lang.Iterable"
		));
		assertEquals(expected, actual);
	}

	@Test
	public void taskExecutorFactoryBean_SuperTypes() throws Exception {
		List<TypeData> data = client
				.javaSuperTypes(new JavaTypeHierarchyParams(project.getLocationURI().toString(), "org.springframework.scheduling.config.TaskExecutorFactoryBean"))
				.get(100000, TimeUnit.SECONDS);
		assertNotNull(data);
		Set<String> actual = data.stream().map(t -> t.getFqName()).collect(Collectors.toSet());
		Set<String> expected = new HashSet<>(Arrays.asList(
				"org.springframework.beans.factory.FactoryBean",
				"org.springframework.beans.factory.DisposableBean",
				"org.springframework.beans.factory.Aware",
				"java.lang.Object",
				"org.springframework.beans.factory.BeanNameAware",
				"org.springframework.beans.factory.InitializingBean"
		));
		assertEquals(expected, actual);
	}
}
