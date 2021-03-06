package org.springframework.ide.vscode.project.harness;

import java.util.Set;

import org.springframework.ide.vscode.commons.boot.app.cli.requestmappings.RequestMapping;

import com.google.common.collect.ImmutableSet;

public class MockRequestMapping implements RequestMapping {

	private String[] paths = {};
	private String className;
	private String methodName;
	private String[] methodParams;
	private Set<String> requestMethods = ImmutableSet.of();

	@Override
	public String[] getSplitPath() {
		return paths;
	}

	@Override
	public String getFullyQualifiedClassName() {
		return className;
	}

	@Override
	public String getMethodName() {
		return methodName;
	}

	@Override
	public String[] getMethodParameters() {
		return methodParams;
	}

	@Override
	public String getMethodString() {
		return null;
	}

	@Override
	public Set<String> getRequestMethods() {
		return requestMethods;
	}

	public MockRequestMapping paths(String... paths) {
		this.paths = paths;
		return this;
	}

	public MockRequestMapping className(String className) {
		this.className = className;
		return this;
	}

	public MockRequestMapping methodName(String methodName) {
		this.methodName = methodName;
		return this;
	}

	public MockRequestMapping methodParams(String... params) {
		this.methodParams = params;
		return this;
	}
}
