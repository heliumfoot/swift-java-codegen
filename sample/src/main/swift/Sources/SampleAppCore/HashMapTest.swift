import Foundation

public enum MapKey: String, Codable {
    case one
    case two
}

public typealias MapType = [MapKey: Int]

public struct Mapper: Codable {
    public let map: MapType

    public init(map: MapType) {
        self.map = map
    }
}
