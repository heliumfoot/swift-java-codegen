package com.readdle.codegen;

import com.readdle.codegen.anotation.SwiftCallbackFunc;
import com.readdle.codegen.anotation.SwiftGetter;
import com.readdle.codegen.anotation.SwiftSetter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class SwiftCallbackFuncDescriptor {

	private ExecutableElement executableElement;
    private String javaMethodName;
    private String swiftMethodName;

    private boolean isStatic;
    private boolean isThrown;

    private SwiftEnvironment.Type returnSwiftType;
    private boolean isReturnTypeOptional;

    private String sig;

    private List<SwiftParamDescriptor> params = new LinkedList<>();
    private List<String> paramNames = new LinkedList<>();

    SwiftCallbackFuncDescriptor(ExecutableElement executableElement, JavaSwiftProcessor processor) {
        String elementName = executableElement.getSimpleName().toString();
        this.executableElement = executableElement;
        this.javaMethodName = elementName;
        this.swiftMethodName = elementName;

        this.isStatic = executableElement.getModifiers().contains(Modifier.STATIC);
        this.isThrown = executableElement.getThrownTypes() != null && executableElement.getThrownTypes().size() > 0;
        this.returnSwiftType = processor.parseJavaType(executableElement.getReturnType().toString());
        this.isReturnTypeOptional = JavaSwiftProcessor.isNullable(executableElement);

        StringBuilder signatureBuilder = new StringBuilder("(");

        for (VariableElement variableElement : executableElement.getParameters()) {
            params.add(new SwiftParamDescriptor(variableElement, processor));
            String javaClass = variableElement.asType().toString();
            signatureBuilder.append(Utils.javaClassToSig(javaClass));
        }

        signatureBuilder.append(")");

        if (returnSwiftType != null) {
            String javaClass = executableElement.getReturnType().toString();
            signatureBuilder.append(Utils.javaClassToSig(javaClass));
        }
        else {
            signatureBuilder.append("V");
        }

        this.sig = signatureBuilder.toString();

        SwiftCallbackFunc swiftFunc = executableElement.getAnnotation(SwiftCallbackFunc.class);

        if (swiftFunc != null && !swiftFunc.value().isEmpty()) {
            String funcFullName = swiftFunc.value();
            int paramStart = funcFullName.indexOf("(");
            int paramEnd = funcFullName.indexOf(")");

            if ((paramStart <= 0 || paramEnd <= 0 || paramEnd <= paramStart)) {
                throw new SwiftMappingException("Wrong func name", executableElement);
            }

            this.swiftMethodName = funcFullName.substring(0, paramStart);
            String arguments = funcFullName.substring(paramStart + 1, paramEnd);
            String[] paramNames = arguments.split(":");

            if (paramNames.length != params.size()) {
                throw new SwiftMappingException("Wrong count of arguments in func name", executableElement);
            }

            this.paramNames = Arrays.asList(paramNames);
        }
        else {
            for (int i = 0; i < params.size(); i++) {
                paramNames.add("_");
            }
        }
    }

	public ExecutableElement getExecutableElement() {
		return executableElement;
	}

	public boolean isReturnTypeOptional() {
		return isReturnTypeOptional;
	}

	public SwiftEnvironment.Type getReturnSwiftType() {
		return returnSwiftType;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public String getSwiftMethodName() {
		return swiftMethodName;
	}

	public List<SwiftParamDescriptor> getParams() {
		return params;
	}

	void generateCode(SwiftWriter swiftWriter, String javaFullName, String swiftType) throws IOException {
        swiftWriter.emitEmptyLine();
        swiftWriter.emitStatement(String.format("static let javaMethod%1$s = try! JNI.%4$s(forClass: \"%2$s\", method: \"%1$s\", sig: \"%3$s\")",
                javaMethodName,
                javaFullName,
                sig,
                isStatic ? "getStaticJavaMethod" : "getJavaMethod"));

        swiftWriter.emitEmptyLine();
        swiftWriter.emit(String.format("public %sfunc %s(", isStatic ? "static " : "", swiftMethodName));
        for (int i = 0; i < params.size(); i++) {
            boolean isFirst = i == 0;

            SwiftParamDescriptor param = params.get(i);

            String paramLabel = paramNames.get(i);
            String paramName = param.name;
            String paramType = param.swiftType.swiftType + (param.isOptional ? "?" : "");

            String paramString;
            if (paramLabel.equals(paramName)) {
                paramString = paramName;
            }
            else {
                paramString = paramLabel + " " + paramName;
            }

            if (isFirst) {
                swiftWriter.emit(paramString + ": " + paramType);
            }
            else {
                swiftWriter.emit(", " + paramString + ": " + paramType);
            }
        }

        if (returnSwiftType == null) {
            swiftWriter.emit(String.format(")%s {\n", isThrown ? " throws" : ""));
        }
        else {
            String returnParamType = isReturnTypeOptional ? returnSwiftType.swiftType + "?" : returnSwiftType.swiftType;
            swiftWriter.emit(String.format(")%s -> %s {\n", isThrown ? " throws" : "", returnParamType));
        }

        for (SwiftParamDescriptor param : params) {
            swiftWriter.emitStatement(String.format("var java%s: JNIArgumentProtocol = jnull()", param.name));
        }

        swiftWriter.emitStatement("defer {");
        for (SwiftParamDescriptor param : params) {
            swiftWriter.emitStatement(String.format("if let localRef = java%1$s as? jobject {", param.name));
            swiftWriter.emitStatement("JNI.DeleteLocalRef(localRef)");
            swiftWriter.emitStatement("}");
        }
        swiftWriter.emitStatement("}");

        if (params.size() > 0) {
            swiftWriter.emitStatement("do {");
            for (SwiftParamDescriptor param : params) {
                if (param.isOptional) {
                    swiftWriter.emitStatement(String.format("if let %1$s = %1$s {", param.name));
                    swiftWriter.emitStatement(String.format("java%1$s = try %1$s.javaObject()", param.name));
                    swiftWriter.emitStatement("}");
                } else {
                    swiftWriter.emitStatement(String.format("java%s = try %s.javaObject()", param.name, param.name));
                }
            }

            swiftWriter.emitStatement("}");
            swiftWriter.emitStatement("catch {");
            swiftWriter.emitStatement("let errorString = String(reflecting: type(of: error)) + String(describing: error)");
            if (returnSwiftType == null) {
                swiftWriter.emitStatement("assert(false, errorString)");
                swiftWriter.emitStatement("return");
            } else if (isReturnTypeOptional) {
                swiftWriter.emitStatement("assert(false, errorString)");
                swiftWriter.emitStatement("return nil");
            } else {
                swiftWriter.emitStatement("fatalError(errorString)");
            }
            swiftWriter.emitStatement("}");
        }

        String jniMethodTemplate;
        if (returnSwiftType != null) {
            if (!isStatic) {
                jniMethodTemplate = "let optionalResult = JNI.CallObjectMethod(jniObject, %s.javaMethod%s";
            }
            else {
                jniMethodTemplate = "let optionalResult = JNI.CallStaticObjectMethod(javaClass, %s.javaMethod%s";
            }
        }
        else {
            if (!isStatic) {
                jniMethodTemplate = "JNI.CallVoidMethod(jniObject, %s.javaMethod%s";
            }
            else {
                jniMethodTemplate = "JNI.CallStaticVoidMethod(javaClass, %s.javaMethod%s";
            }
        }
        swiftWriter.emit(String.format(jniMethodTemplate, swiftType, javaMethodName));
        for (SwiftParamDescriptor param : params) {
            swiftWriter.emit(String.format(", java%s", param.name));
        }
        swiftWriter.emitStatement(")");

        swiftWriter.emitStatement("if let throwable = JNI.ExceptionCheck() {");
        swiftWriter.emitStatement("let javaThrowable = Throwable(javaObject: throwable)");
        swiftWriter.emitStatement("javaThrowable.printStackTrace()");
        if (isThrown) {
            swiftWriter.emitStatement("if let error = try? NSError.from(javaObject: throwable) {");
            swiftWriter.emitStatement("throw error");
            swiftWriter.emitStatement("}");
            swiftWriter.emitStatement("else {");
            swiftWriter.emitStatement("fatalError(\"UnknownJavaException\")");
            swiftWriter.emitStatement("}");
        }
        else {
            swiftWriter.emitStatement("fatalError(\"\\(javaThrowable.className()): \\(javaThrowable.getMessage() ?? \"\")\\(javaThrowable.stackTraceString())\")");
        }
        swiftWriter.emitStatement("}");

        if (returnSwiftType != null) {
            swiftWriter.emitStatement("guard let result = optionalResult else {");
            if (isReturnTypeOptional) {
                swiftWriter.emitStatement("return nil");
            }
            else {
                swiftWriter.emitStatement("fatalError(\"Don't support nil here!\")");
            }
            swiftWriter.emitStatement("}");

            swiftWriter.emitStatement("defer {");
            swiftWriter.emitStatement("JNI.DeleteLocalRef(result)");
            swiftWriter.emitStatement("}");
            swiftWriter.emitStatement("do {");
            swiftWriter.emitStatement(String.format("return try %s.from(javaObject: result)", returnSwiftType.swiftConstructorType));
            swiftWriter.emitStatement("}");
            swiftWriter.emitStatement("catch {");
            swiftWriter.emitStatement("let errorString = String(reflecting: type(of: error)) + String(describing: error)");
            swiftWriter.emitStatement("fatalError(errorString)");
            swiftWriter.emitStatement("}");
        }

        swiftWriter.emitStatement("}");
    }

    @Override
    public String toString() {
        return "SwiftFuncDescriptor{" +
                "javaMethodName='" + javaMethodName + '\'' +
                ", swiftMethodName='" + swiftMethodName + '\'' +
                ", isStatic=" + isStatic +
                ", isThrown=" + isThrown +
                ", returnSwiftType='" + returnSwiftType + '\'' +
                ", isReturnTypeOptional=" + isReturnTypeOptional +
                ", params=" + params +
                '}';
    }
}
