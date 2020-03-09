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
	String getRegularGetter();

	@NonNull
	@SwiftGetter
	String getRegularProperty();

	@SwiftSetter
	void setRegularProperty(@NonNull String property);

	@Nullable
	@SwiftGetter
	String getOptionalGetter();

	@Nullable
	@SwiftGetter
	Integer getOptionalProperty();

	@SwiftSetter
	void setOptionalProperty(@Nullable Integer property);
}
