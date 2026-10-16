import Foundation

let DEFAULT_CONTENT_TYPE = "application/octet-stream"

func copyToMalloc(_ data: Data) -> (UnsafeMutablePointer<CChar>?, Int) {
    guard !data.isEmpty, let buf = malloc(data.count) else { return (nil, 0) }
    data.copyBytes(to: buf.assumingMemoryBound(to: UInt8.self), count: data.count)
    return (buf.assumingMemoryBound(to: CChar.self), data.count)
}

func dataOf(_ src: UnsafePointer<CChar>?, _ len: Int32) -> Data {
    guard let src, len > 0 else { return Data() }
    return Data(bytes: src, count: Int(len))
}

func string(_ ptr: UnsafePointer<CChar>?) -> String? {
    ptr.map { String(cString: $0) }
}

func joinUrl(_ baseUrl: String, _ path: String) -> String {
    var base = Substring(baseUrl)
    while base.hasSuffix("/") { base = base.dropLast() }
    var tail = Substring(path)
    while tail.hasPrefix("/") { tail = tail.dropFirst() }
    return base + "/" + tail
}

func awaitCallback<T>(_ start: (@escaping (T) -> Void) -> Void) -> T {
    let sem = DispatchSemaphore(value: 0)
    var result: T?
    start { value in
        result = value
        sem.signal()
    }
    sem.wait()
    return result!
}
