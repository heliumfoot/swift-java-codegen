package com.readdle.swiftjava.sample;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.readdle.codegen.anotation.SwiftCallbackFunc;
import com.readdle.codegen.anotation.SwiftDelegate;
import com.readdle.codegen.anotation.SwiftGetter;
import com.readdle.codegen.anotation.SwiftSetter;

@SwiftDelegate(protocols = "SwiftProtocol")
public interface SampleProtocol {
	@NonNull
	@SwiftGetter
	@SwiftCallbackFunc
	String getRegularGetter();

	@NonNull
	@SwiftGetter
	@SwiftCallbackFunc
	String getRegularProperty();

	@SwiftSetter
	@SwiftCallbackFunc("setRegularProperty(_:)")
	void setRegularProperty(@NonNull String property);

	@Nullable
	@SwiftGetter
	@SwiftCallbackFunc
	String getOptionalGetter();

	@Nullable
	@SwiftGetter
	@SwiftCallbackFunc
	Integer getOptionalProperty();

	@SwiftSetter
	@SwiftCallbackFunc("setOptionalProperty(_:)")
	void setOptionalProperty(@Nullable Integer property);
}
