// swift-tools-version:5.3
import Foundation
import PackageDescription

let packageName = "SampleAppCore"

// generated sources integration
let generatedName = "Generated"
let generatedPath = ".build/\(generatedName.lowercased())"

let isSourcesGenerated: Bool = {
    let baseURL = URL(fileURLWithPath: #file).deletingLastPathComponent()
    let generatedURL = baseURL.appendingPathComponent(generatedPath)

    var isDirectory: ObjCBool = false
    let exists = FileManager.default.fileExists(atPath: generatedURL.path, isDirectory: &isDirectory)

    return exists && isDirectory.boolValue
}()

func addGenerated(_ products: [Product]) -> [Product] {
    if isSourcesGenerated == false {
        return products
    }

    return products + [
        .library(name: packageName, type: .dynamic, targets: [generatedName])
    ]
}

func addGenerated(_ targets: [Target]) -> [Target] {
    if isSourcesGenerated == false {
        return targets
    }

    return targets + [
        .target(
            name: generatedName,
            dependencies: [
                .byName(name: packageName),
                "java_swift",
                "Java",
                "JavaCoder",
            ],
            path: generatedPath
        )
    ]
}
// generated sources integration end

let package = Package(
    name: packageName,
    products: addGenerated([
    ]),
    dependencies: [
        .package(name: "java_swift", url: "https://github.com/readdle/java_swift.git", .upToNextMinor(from: "2.1.8")),
        .package(name: "Java", url: "https://github.com/readdle/swift-java.git", .upToNextMinor(from: "0.2.2")),
        .package(name: "JavaCoder", url: "https://github.com/readdle/swift-java-coder.git", .branch("dev/kotlin-support")),
        .package(name: "AnyCodable", url: "https://github.com/readdle/swift-anycodable.git", .upToNextMinor(from: "1.0.3")),
    ],
    targets: addGenerated([
        .target(name: packageName, dependencies: ["AnyCodable", "java_swift", "JavaCoder"])
    ])
)
