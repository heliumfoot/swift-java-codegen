package com.readdle.swiftjava.sample

import com.readdle.codegen.anotation.SwiftFunc
import com.readdle.codegen.anotation.SwiftGetter
import com.readdle.codegen.anotation.SwiftValue

@SwiftValue
enum class MapKey(val rawValue: String) {
    one("one"),
    two("two");
}
//
//@SwiftValue
//data class Mapper(
//    var map: HashMap<MapKey, Int> = hashMapOf()
//)

@SwiftValue
class Mapper private constructor() {
    @get:SwiftGetter
    val map: HashMap<MapKey, Int> external get

    companion object {
        @JvmStatic
        @SwiftFunc("init(map:)")
        external fun init(
            map: HashMap<MapKey, Int>
        ): Mapper
    }
}
