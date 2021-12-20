//
// Created by Andrew on 1/11/18.
//

import Foundation
import java_swift
import JavaCoder

// TODO: fix this hack
private let AndroidPackage = "com/readdle/swiftjava/sample"

public extension Array {

    // Decoding SwiftValue type with JavaCoder
    static func from<T>(javaObject: jobject) throws -> Array<T> where T: Decodable {
        // ignore forPackage for basic impl
        return try JavaDecoder(forPackage: AndroidPackage, missingFieldsStrategy: .ignore).decode(Array<T>.self, from: javaObject)
    }

}

public extension Array where Element: Encodable {

    // Encoding SwiftValue type with JavaCoder
    func javaObject() throws -> jobject {
        return try JavaEncoder(forPackage: AndroidPackage, missingFieldsStrategy: .ignore).encode(self)
    }

}

public extension Dictionary where Key: Decodable, Value: Decodable {

		// Decoding SwiftValue type with JavaCoder
		static func from(javaObject: jobject) throws -> Dictionary<Key, Value> {
				return try JavaDecoder(forPackage: AndroidPackage, missingFieldsStrategy: .throw).decode(Self.self, from: javaObject)
		}

}

public extension Dictionary where Key: Encodable, Value: Encodable {

		// Encoding SwiftValue type with JavaCoder
		func javaObject() throws -> jobject {
				// ignore forPackage for basic impl
				return try JavaEncoder(forPackage: AndroidPackage, missingFieldsStrategy: .throw).encode(self)
		}

}
