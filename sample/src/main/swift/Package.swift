// swift-tools-version:5.3
import Foundation
import PackageDescription

let packageName = "SampleAppCore"

let generatedName = "Generated"
let generatedPath = ".build/\(generatedName.lowercased())"

let package = Package(
    name: packageName,
    products: [
        .library(name: packageName, type: .dynamic, targets: [generatedName])
    ],
    dependencies: [
        .package(name: "java_swift", url: "https://github.com/readdle/java_swift.git", .upToNextMinor(from: "2.2.3")),
        .package(name: "Java", url: "https://github.com/readdle/swift-java.git", .upToNextMinor(from: "0.3.0")),
        .package(name: "JavaCoder",  url: "https://github.com/readdle/swift-java-coder.git", .upToNextMinor(from: "1.1.2")),
        .package(name: "AnyCodable", url: "https://github.com/readdle/swift-anycodable.git", .upToNextMinor(from: "1.0.3")),
    ],
    targets: [
        .target(name: packageName, dependencies: ["AnyCodable", "java_swift", "JavaCoder"]),
        .target(name: generatedName, dependencies: [
                .byName(name: packageName),
                "java_swift",
                "Java",
                "JavaCoder",
            ],
            path: generatedPath
        )
    ]
)
