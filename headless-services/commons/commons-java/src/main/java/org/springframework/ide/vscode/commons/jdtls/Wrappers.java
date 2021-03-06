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
package org.springframework.ide.vscode.commons.jdtls;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.ide.vscode.commons.java.IAnnotation;
import org.springframework.ide.vscode.commons.java.IArrayType;
import org.springframework.ide.vscode.commons.java.IClassType;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IField;
import org.springframework.ide.vscode.commons.java.IJavaModuleData;
import org.springframework.ide.vscode.commons.java.IJavaType;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IMemberValuePair;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IParameterizedType;
import org.springframework.ide.vscode.commons.java.IPrimitiveType;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.java.ITypeVariable;
import org.springframework.ide.vscode.commons.java.IUnresolvedTypeVariable;
import org.springframework.ide.vscode.commons.java.IVoidType;
import org.springframework.ide.vscode.commons.java.IWildcardType;
import org.springframework.ide.vscode.commons.javadoc.IJavadoc;
import org.springframework.ide.vscode.commons.protocol.java.JavaTypeData;
import org.springframework.ide.vscode.commons.protocol.java.TypeData;
import org.springframework.ide.vscode.commons.protocol.java.TypeData.AnnotationData;
import org.springframework.ide.vscode.commons.protocol.java.TypeData.FieldData;
import org.springframework.ide.vscode.commons.protocol.java.TypeData.MethodData;

import com.google.common.base.Supplier;

public class Wrappers {

	public static IJavaType wrap(JavaTypeData data) {
		switch (data.getKind()) {
		case INT:
			return IPrimitiveType.INT;
		case CHAR:
			return IPrimitiveType.CHAR;
		case BOOLEAN:
			return IPrimitiveType.BOOLEAN;
		case FLOAT:
			return IPrimitiveType.FLOAT;
		case BYTE:
			return IPrimitiveType.BYTE;
		case DOUBLE:
			return IPrimitiveType.DOUBLE;
		case LONG:
			return IPrimitiveType.LONG;
		case SHORT:
			return IPrimitiveType.SHORT;
		case VOID:
			return IVoidType.DEFAULT;
		case CLASS:
			return new IClassType() {

				@Override
				public String name() {
					return data.getName();
				}

				@Override
				public boolean equals(Object obj) {
					if (obj instanceof IClassType) {
						return data.getName().equals(((IClassType)obj).name());
					}
					return false;
				}

				@Override
				public String getFQName() {
					String key = data.getName();
					return key.substring(0, key.length() - 1).replace('/', '.');
				}

			};
		case ARRAY:
			return new IArrayType() {

				@Override
				public String name() {
					return data.getName();
				}

				@Override
				public int dimensions() {
					if (data.getExtras() != null && data.getExtras().containsKey("dimensions")) {
						return (Integer) data.getExtras().get("dimenions");
					}
					return 0;
				}

				@Override
				public IJavaType component() {
					if (data.getExtras() != null && data.getExtras().containsKey("component")) {
						return wrap((JavaTypeData) data.getExtras().get("component"));
					}
					return null;
				}

				@Override
				public boolean equals(Object obj) {
					if (obj instanceof IArrayType) {
						return data.getName().equals(((IArrayType)obj).name());
					}
					return false;
				}
			};
		case PARAMETERIZED:
			return new IParameterizedType() {

				@Override
				public String name() {
					return data.getName();
				}

				@Override
				public IJavaType owner() {
					if (data.getExtras() != null && data.getExtras().containsKey("owner")) {
						return wrap((JavaTypeData) data.getExtras().get("owner"));
					}
					return null;
				}

				@SuppressWarnings("unchecked")
				@Override
				public Stream<IJavaType> arguments() {
					if (data.getExtras() != null && data.getExtras().containsKey("arguments")) {
						return ((List<JavaTypeData>) data.getExtras().get("arguments")).stream().map(Wrappers::wrap);
					}
					return Stream.of();
				}

				@Override
				public boolean equals(Object obj) {
					if (obj instanceof IParameterizedType) {
						return data.getName().equals(((IParameterizedType)obj).name());
					}
					return false;
				}

			};
		case TYPE_VARIABLE:
			return new ITypeVariable() {

				@Override
				public String name() {
					return data.getName();
				}

				@Override
				public boolean equals(Object obj) {
					if (obj instanceof ITypeVariable) {
						return data.getName().equals(((ITypeVariable)obj).name());
					}
					return false;
				}
			};
		case WILDCARD:
			return new IWildcardType() {
				@Override
				public String name() {
					return data.getName();
				}

				@Override
				public boolean equals(Object obj) {
					if (obj instanceof IWildcardType) {
						return data.getName().equals(((IWildcardType)obj).name());
					}
					return false;
				}
			};
		case UNRESOLVED:
			return new IUnresolvedTypeVariable() {
				@Override
				public String name() {
					return data.getName();
				}

				@Override
				public boolean equals(Object obj) {
					if (obj instanceof IUnresolvedTypeVariable) {
						return data.getName().equals(((IUnresolvedTypeVariable)obj).name());
					}
					return false;
				}
			};
		}
		return null;
	}

	public static IAnnotation wrap(AnnotationData data, IJavadocProvider javadocProvider) {
		return new IAnnotation() {

			@Override
			public IJavadoc getJavaDoc() {
				return javadocProvider.getJavadoc(this);
			}

			@Override
			public String getElementName() {
				return data.getFqName();
			}

			@Override
			public String getBindingKey() {
				return data.getFqName() == null ? null : "L" + data.getFqName().replace('.', '/') + ";";
			}

			@Override
			public boolean exists() {
				return true;
			}

			@Override
			public Stream<IMemberValuePair> getMemberValuePairs() {
				return data.getValuePairs().entrySet().stream().map(e -> new IMemberValuePair() {

					@Override
					public String getMemberName() {
						return e.getKey();
					}

					@Override
					public Object getValue() {
						return e.getValue();
					}

				});
			}

			@Override
			public String fqName() {
				return data.getFqName();
			}
		};
	}

	public static IMethod wrap(MethodData data, IType declaringType, IJavadocProvider javadocProvider) {
		return new IMethod() {

			@Override
			public int getFlags() {
				return data.getFlags();
			}

			@Override
			public IType getDeclaringType() {
				return declaringType;
			}

			@Override
			public String signature() {
				return data.getLabel();
			}

			@Override
			public String getElementName() {
				return data.getName();
			}

			@Override
			public IJavadoc getJavaDoc() {
				return javadocProvider.getJavadoc(this);
			}

			@Override
			public String getBindingKey() {
				return data.getBindingKey();
			}

			@Override
			public boolean exists() {
				return true;
			}

			@Override
			public Stream<IAnnotation> getAnnotations() {
				return data.getAnnotations().stream().map(a -> Wrappers.wrap(a, javadocProvider));
			}

			@Override
			public IJavaType getReturnType() {
				return Wrappers.wrap(data.getReturnType());
			}

			@Override
			public Stream<IJavaType> parameters() {
				return data.getParameters().stream().map(Wrappers::wrap);
			}

			@Override
			public boolean isConstructor() {
				return data.isConstructor();
			}

		};
	}

	public static IField wrap(FieldData data, IType declaringType, IJavadocProvider javadocProvider) {
		return new IField() {

			@Override
			public int getFlags() {
				return data.getFlags();
			}

			@Override
			public IType getDeclaringType() {
				return declaringType;
			}

			@Override
			public String signature() {
				return data.getLabel();
			}

			@Override
			public String getElementName() {
				return data.getName();
			}

			@Override
			public IJavadoc getJavaDoc() {
				return javadocProvider.getJavadoc(this);
			}

			@Override
			public String getBindingKey() {
				return data.getBindingKey();
			}

			@Override
			public boolean exists() {
				return true;
			}

			@Override
			public Stream<IAnnotation> getAnnotations() {
				return data.getAnnotations().stream().map(a -> Wrappers.wrap(a, javadocProvider));
			}

			@Override
			public boolean isEnumConstant() {
				return data.isEnumConstant();
			}

			@Override
			public IJavaType type() {
				return wrap(data.getType());
			}

		};
	}

	public static IType wrap(TypeData data, Supplier<IType> declaringTypeSupplier, IJavadocProvider javadocProvider) {
		return new IType() {

			@Override
			public int getFlags() {
				return data.getFlags();
			}

			@Override
			public IType getDeclaringType() {
				return declaringTypeSupplier.get();
			}

			@Override
			public IJavaModuleData classpathContainer() {
				return new IJavaModuleData() {

					@Override
					public String getModule() {
						return data.getClasspathEntry().getModule();
					}

					@Override
					public File getContainer() {
						return IClasspathUtil.binaryLocation(data.getClasspathEntry().getCpe());
					}
				};
			}

			@Override
			public String signature() {
				return data.getLabel();
			}

			@Override
			public String getElementName() {
				return data.getName();
			}

			@Override
			public IJavadoc getJavaDoc() {
				return javadocProvider.getJavadoc(this);
			}

			@Override
			public String getBindingKey() {
				return data.getBindingKey();
			}

			@Override
			public boolean exists() {
				return true;
			}

			@Override
			public Stream<IAnnotation> getAnnotations() {
				return data.getAnnotations().stream().map(a -> Wrappers.wrap(a, javadocProvider));
			}

			@Override
			public boolean isClass() {
				return data.isClass();
			}

			@Override
			public boolean isEnum() {
				return data.isEnum();
			}

			@Override
			public boolean isInterface() {
				return data.isInterface();
			}

			@Override
			public boolean isAnnotation() {
				return data.isAnnotation();
			}

			@Override
			public String getFullyQualifiedName() {
				return data.getFqName();
			}

			@Override
			public IField getField(String name) {
				return getFields().filter(f -> name.equals(f.getElementName())).findFirst().orElse(null);
			}

			@Override
			public Stream<IField> getFields() {
				return data.getFields().stream().map(f -> wrap(f, this, javadocProvider));
			}

			@Override
			public IMethod getMethod(String name, Stream<IJavaType> parameters) {
				List<IJavaType> arguments = parameters.collect(Collectors.toList());
				return data.getMethods().stream()
						.filter(m -> name.equals(m.getName()))
						.filter(m -> arguments.equals(m.getParameters().stream()
								.map(Wrappers::wrap)
								.collect(Collectors.toList())))
						.findFirst()
						.map(m -> wrap(m, this, javadocProvider))
						.orElse(null);
			}

			@Override
			public Stream<IMethod> getMethods() {
				return data.getMethods().stream().map(m -> wrap(m, this, javadocProvider));
			}

			@Override
			public String getSuperclassName() {
				return data.getSuperClassName();
			}

			@Override
			public String[] getSuperInterfaceNames() {
				return data.getSuperInterfaceNames();
			}

		};
	}

}
