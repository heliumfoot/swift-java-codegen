import Foundation

public protocol SwiftProtocol {
  var regularGetter: String { get }
  var regularProperty: String { get set }
  var optionalGetter: String? { get }
  var optionalProperty: Int? { get set }
}
