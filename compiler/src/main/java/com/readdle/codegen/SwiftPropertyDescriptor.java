package com.readdle.codegen;

import com.readdle.codegen.anotation.SwiftGetter;
import com.readdle.codegen.anotation.SwiftSetter;

import java.io.IOException;

import javax.lang.model.element.ExecutableElement;

public class SwiftPropertyDescriptor implements JavaSwiftProcessor.WritableElement {
	private SwiftDelegateDescriptor.SwiftProperty property;

	private boolean hasSetter;
	private boolean hasGetter;

	private String swiftName;

	private SwiftEnvironment.Type returnType;
	private boolean isReturnTypeOptional;

	public SwiftPropertyDescriptor(SwiftDelegateDescriptor.SwiftProperty property, JavaSwiftProcessor processor) throws IllegalArgumentException {
		this.property = property;
		this.hasGetter = property.getterDescriptor != null;
		this.hasSetter = property.setterDescriptor != null;

		if (!this.hasSetter && !this.hasGetter) {
			throw new IllegalArgumentException("A Swift Property Descriptor needs to have at least a @SwiftGetter or @SwiftSetter descriptor");
		}

		this.swiftName = property.getterDescriptor.getSwiftName();
		this.returnType = property.getterCallbackFuncDescriptor.getReturnSwiftType();
		this.isReturnTypeOptional = property.getterCallbackFuncDescriptor.isReturnTypeOptional();
	}

	@Override
	public void generateCode(SwiftWriter swiftWriter, String javaFullName, String swiftType) throws IOException {
		swiftWriter.emitEmptyLine();

		swiftWriter.emitEmptyLine();
		swiftWriter.emitStatement(String.format("var %s: %s%s {", swiftName, returnType.swiftType, isReturnTypeOptional ? "?" : ""));

		if (this.hasGetter) {
			swiftWriter.emitStatement("get {");
			swiftWriter.emitStatement(String.format("return %s", property.getterCallbackFuncDescriptor.getSwiftMethodName()));
			swiftWriter.emitStatement("}");
		}

		if (this.hasSetter) {
			SwiftCallbackFuncDescriptor funcDescriptor = property.setterCallbackFuncDescriptor;
			SwiftParamDescriptor paramDescriptor = funcDescriptor.getParams().get(0);
			String param = "newValue";

			if (!paramDescriptor.name.isEmpty()) {
				param = String.format("%s: newValue", paramDescriptor.name);
			}

			swiftWriter.emitStatement("set {");
			swiftWriter.emitStatement(String.format("%s(%s);", property.setterCallbackFuncDescriptor.getSwiftMethodName(), param));
			swiftWriter.emitStatement("}");
		}
	}

	@Override
	public String toString(String javaClassname) {
		return null;
	}
}
