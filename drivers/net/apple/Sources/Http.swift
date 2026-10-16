import Foundation
import PrivmxNetC

private let BODYLESS: Set<String> = ["GET", "HEAD"]

struct HttpResult {
    let statusCode: Int32
    let body: UnsafeMutablePointer<CChar>?
    let len: Int
}

final class HttpSession {
    let baseUrlHost: String
    let session = URLSession(configuration: .default)

    init?(baseUrl: String) {
        guard let url = URL(string: baseUrl), let scheme = url.scheme, let host = url.host else {
            return nil
        }
        baseUrlHost = url.port.map { "\(scheme)://\(host):\($0)" } ?? "\(scheme)://\(host)"
    }

    func invalidate() { session.finishTasksAndInvalidate() }

    func perform(
        _ data: UnsafePointer<CChar>?,
        _ datalen: Int32,
        _ options: UnsafePointer<privmxDrvNet_HttpRequestOptions>
    ) -> HttpResult? {
        let o = options.pointee
        let method = (string(o.method) ?? "POST").uppercased()
        guard let url = URL(string: joinUrl(baseUrlHost, string(o.path) ?? "")) else { return nil }

        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue(string(o.contentType) ?? DEFAULT_CONTENT_TYPE, forHTTPHeaderField: "Content-Type")
        if let headers = o.headers {
            for i in 0..<Int(o.headerslen) {
                guard let name = string(headers[i].name) else { continue }
                request.setValue(string(headers[i].value) ?? "", forHTTPHeaderField: name)
            }
        }
        if !BODYLESS.contains(method) { request.httpBody = dataOf(data, datalen) }

        return awaitCallback { done in
            session.dataTask(with: request) { body, response, error in
                guard error == nil else { return done(nil) }
                let code = (response as? HTTPURLResponse)?.statusCode ?? 0
                let (buf, len) = body.map(copyToMalloc) ?? (nil, 0)
                done(HttpResult(statusCode: Int32(code), body: buf, len: len))
            }.resume()
        }
    }
}
