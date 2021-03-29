//
//  File.swift
//  
//
//  Created by Danny Ruiz on 3/29/21.
//

import Foundation

public struct TestProgress: Codable {
	public let elapsed: Int
	public let total: Int
	
	public var percentage: Double {
		return Double(elapsed) / Double(total)
	}
	
	public init(elapsed: Int, total: Int) {
		self.elapsed = elapsed
		self.total = total
	}
	
	public func calculatePercentage() -> Double {
		return percentage
	}
}

public class ReferenceTestProgress: Codable {
	public let elapsed: Int
	public let total: Int
	
	public var percentage: Double {
		return Double(elapsed) / Double(total)
	}
	
	public init(elapsed: Int, total: Int) {
		self.elapsed = elapsed
		self.total = total
	}
	
	public func calculatePercentage() -> Double {
		return percentage
	}
}
