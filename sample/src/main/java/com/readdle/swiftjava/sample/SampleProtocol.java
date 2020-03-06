package com.readdle.swiftjava.sample;

import android.support.annotation.NonNull;

import com.readdle.codegen.anotation.SwiftCallbackFunc;
import com.readdle.codegen.anotation.SwiftDelegate;
import com.readdle.codegen.anotation.SwiftGetter;

@SwiftDelegate(protocols = "SwiftProtocol")
public interface SampleProtocol {
	@NonNull
	@SwiftGetter
	@SwiftCallbackFunc
	String getRegularGetter();
}
