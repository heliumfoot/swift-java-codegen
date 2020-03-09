package com.readdle.codegen;

import com.readdle.codegen.anotation.SwiftGetter;
import com.readdle.codegen.anotation.SwiftSetter;

import java.io.IOException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

public class SwiftPropertyDescriptor {

	private SwiftGetterDescriptor getterDescriptor;
	private SwiftSetterDescriptor setterDescriptor;

	private SwiftCallbackFuncDescriptor getterCallbackFuncDescriptor;
	private SwiftCallbackFuncDescriptor setterCallbackFuncDescriptor;

	private String swiftName;
	private SwiftEnvironment.Type returnType;
	private boolean isReturnTypeOptional;
	private boolean isStatic;

	public static class Builder {

		JavaSwiftProcessor processor;
		ExecutableElement getterElement;
		ExecutableElement setterElement;

		SwiftSetterDescriptor swiftSetterDescriptor;
		SwiftGetterDescriptor swiftGetterDescriptor;

		SwiftCallbackFuncDescriptor setterCallbackFuncDescriptor;
		SwiftCallbackFuncDescriptor getterCallbackFuncDescriptor;

		public Builder(JavaSwiftProcessor processor) {
			this.processor = processor;
		}

		public SwiftPropertyDescriptor build() {
			if (swiftGetterDescriptor == null) {
				throw new IllegalStateException("A Swift property requires at least a @SwiftGetter.");
			}

			return new SwiftPropertyDescriptor(
				swiftGetterDescriptor,
				getterCallbackFuncDescriptor,
				swiftSetterDescriptor,
				setterCallbackFuncDescriptor,
				processor
			);
		}

		public Builder addGetterElement(ExecutableElement element) {
			SwiftGetterDescriptor descriptor = new SwiftGetterDescriptor(element, element.getAnnotation(SwiftGetter.class), processor);

			if (swiftSetterDescriptor != null && !descriptor.getSwiftName().equals(swiftSetterDescriptor.getSwiftName())) {
				throw new IllegalArgumentException("A @SwiftDelegate @SwiftGetter's Swift name must match its @SwiftSetter's Swift name.");
			} else if (element.getParameters().size() != 0) {
				throw new IllegalArgumentException("@SwiftDelegate's @SwiftGetter-annotated methods should not have any parameters");
			} else if (setterElement != null && element.getModifiers().contains(Modifier.STATIC) && !setterElement.getModifiers().contains(Modifier.STATIC)) {
				throw new IllegalArgumentException("@SwiftDelegate's @SwiftGetter-annotated must match its @SwiftGetter and be static.");
			}

			this.swiftGetterDescriptor = descriptor;
			this.getterElement = element;
			this.getterCallbackFuncDescriptor = new SwiftCallbackFuncDescriptor(element, processor);

			return this;
		}

		public Builder addSetterElement(ExecutableElement element) {
			SwiftSetterDescriptor descriptor = new SwiftSetterDescriptor(element, element.getAnnotation(SwiftSetter.class), processor);

			if (swiftGetterDescriptor != null && !descriptor.getSwiftName().equals(swiftGetterDescriptor.getSwiftName())) {
				throw new IllegalArgumentException("A @SwiftDelegate @SwiftSetter's Swift name must match its @SwiftGetter's Swift name.");
			} else if (element.getParameters().size() > 1) {
				throw new IllegalArgumentException("@SwiftDelegate's @SwiftSetter-annotated methods can only have one parameter (the value to be set).");
			} else if (getterElement != null && element.getModifiers().contains(Modifier.STATIC) && !getterElement.getModifiers().contains(Modifier.STATIC)) {
				throw new IllegalArgumentException("@SwiftDelegate's @SwiftSetter-annotated must match its @SwiftGetter and be static.");
			}

			this.swiftSetterDescriptor = descriptor;
			this.setterElement = element;
			this.setterCallbackFuncDescriptor = new SwiftCallbackFuncDescriptor(element, processor);

			return this;
		}
	}

	private SwiftPropertyDescriptor(
		SwiftGetterDescriptor getterDescriptor,
		SwiftCallbackFuncDescriptor getterCallbackFuncDescriptor,
		SwiftSetterDescriptor setterDescriptor,
		SwiftCallbackFuncDescriptor setterCallbackFuncDescriptor,
		JavaSwiftProcessor processor) throws IllegalArgumentException {

		this.getterDescriptor = getterDescriptor;
		this.getterCallbackFuncDescriptor = getterCallbackFuncDescriptor;

		this.setterDescriptor = setterDescriptor;
		this.setterCallbackFuncDescriptor = setterCallbackFuncDescriptor;

		this.swiftName = getterDescriptor.getSwiftName();
		this.returnType = getterCallbackFuncDescriptor.getReturnSwiftType();
		this.isReturnTypeOptional = getterCallbackFuncDescriptor.isReturnTypeOptional();
		this.isStatic = getterCallbackFuncDescriptor.isStatic();
	}

	public void generateCode(SwiftWriter swiftWriter, String javaFullName, String swiftType) throws IOException {
		swiftWriter.emitEmptyLine();
		swiftWriter.emitStatement(String.format("public var %s: %s%s {", swiftName, returnType.swiftType, isReturnTypeOptional ? "?" : ""));

		if (hasGetter()) {
			swiftWriter.emitStatement("get {");
			swiftWriter.emitStatement(String.format("return %s()", getterCallbackFuncDescriptor.getSwiftMethodName()));
			swiftWriter.emitStatement("}");
		}

		if (hasSetter()) {
			SwiftCallbackFuncDescriptor funcDescriptor = setterCallbackFuncDescriptor;
			SwiftParamDescriptor paramDescriptor = funcDescriptor.getParams().get(0);
			String param = "newValue";

			swiftWriter.emitStatement("set {");
			swiftWriter.emitStatement(String.format("%s(%s)", setterCallbackFuncDescriptor.getSwiftMethodName(), param));
			swiftWriter.emitStatement("}");
		}

		swiftWriter.emitStatement("}");

		swiftWriter.emitEmptyLine();

		if (hasGetter()) {
			getterCallbackFuncDescriptor.generateCode(swiftWriter, javaFullName, swiftType);
		}

		if (hasSetter()) {
			setterCallbackFuncDescriptor.generateCode(swiftWriter, javaFullName, swiftType);
		}
	}

	@Override
	public String toString() {
		return "SwiftPropertyDescriptor {" +
			"getterDescriptor=" + getterDescriptor +
			", setterDescriptor=" + setterDescriptor +
			", setterCallbackFuncDescriptor=" + setterCallbackFuncDescriptor +
			", getterCallbackFuncDescriptor=" + getterCallbackFuncDescriptor +
			", swiftName=" + swiftName +
			", isStatic=" + isStatic +
			", returnSwiftType=" + returnType +
			", isReturnTypeOptional=" + isReturnTypeOptional +
			'}';
	}

	private boolean hasGetter() {
		return getterDescriptor != null;
	}

	private boolean hasSetter() {
		return hasGetter() && setterDescriptor != null;
	}
}
