package com.readdle.swiftjava.sample

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.readdle.codegen.anotation.JavaSwift
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HashMapTests {
    @Before
    fun setUp() {
        System.loadLibrary("SampleAppCore")
        JavaSwift.init()
        SwiftEnvironment.initEnvironment()
    }

    @Test
    fun testHashMapAsParameter() {
        val mapper = Mapper.init(
            map = hashMapOf(
                MapKey.one to 1
            )
        )

        assertEquals("Should have been 1", 1, mapper.map[MapKey.one])
    }
}
