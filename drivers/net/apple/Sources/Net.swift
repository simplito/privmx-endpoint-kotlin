import Foundation
import PrivmxNetC

let OK: Int32 = 0
let ERR_NULL_ARG: Int32 = 1
let ERR_TRANSPORT: Int32 = 2
let ERR_BAD_URL: Int32 = 3

private func box<T: AnyObject>(_ obj: T) -> OpaquePointer {
    OpaquePointer(Unmanaged.passRetained(obj).toOpaque())
}

private func unbox<T: AnyObject>(_ ptr: OpaquePointer) -> T {
    Unmanaged<T>.fromOpaque(UnsafeRawPointer(ptr)).takeUnretainedValue()
}

private func release<T: AnyObject>(_ ptr: OpaquePointer, as _: T.Type) {
    Unmanaged<T>.fromOpaque(UnsafeRawPointer(ptr)).release()
}

@_cdecl("privmxDrvNet_version")
public func privmxDrvNet_version(_ version: UnsafeMutablePointer<UInt32>?) -> Int32 {
    guard let version else { return ERR_NULL_ARG }
    version.pointee = 1
    return OK
}

@_cdecl("privmxDrvNet_setConfig")
public func privmxDrvNet_setConfig(_ config: UnsafePointer<CChar>?) -> Int32 { OK }

@_cdecl("privmxDrvNet_httpCreateSession")
public func privmxDrvNet_httpCreateSession(
    _ options: UnsafePointer<privmxDrvNet_HttpOptions>?,
    _ res: UnsafeMutablePointer<OpaquePointer?>?
) -> Int32 {
    guard let options, let res, let baseUrl = options.pointee.baseUrl else { return ERR_NULL_ARG }
    guard let session = HttpSession(baseUrl: String(cString: baseUrl)) else { return ERR_BAD_URL }
    res.pointee = box(session)
    return OK
}

@_cdecl("privmxDrvNet_httpDestroySession")
public func privmxDrvNet_httpDestroySession(_ http: OpaquePointer?) -> Int32 {
    guard let http else { return ERR_NULL_ARG }
    let session: HttpSession = unbox(http)
    session.invalidate()
    return OK
}

@_cdecl("privmxDrvNet_httpFree")
public func privmxDrvNet_httpFree(_ http: OpaquePointer?) -> Int32 {
    guard let http else { return ERR_NULL_ARG }
    release(http, as: HttpSession.self)
    return OK
}

@_cdecl("privmxDrvNet_httpRequest")
public func privmxDrvNet_httpRequest(
    _ http: OpaquePointer?,
    _ data: UnsafePointer<CChar>?,
    _ datalen: Int32,
    _ options: UnsafePointer<privmxDrvNet_HttpRequestOptions>?,
    _ statusCode: UnsafeMutablePointer<Int32>?,
    _ out: UnsafeMutablePointer<UnsafeMutablePointer<CChar>?>?,
    _ outlen: UnsafeMutablePointer<UInt32>?
) -> Int32 {
    guard let http, let options, let statusCode, let out, let outlen else { return ERR_NULL_ARG }
    let session: HttpSession = unbox(http)
    guard let result = session.perform(data, datalen, options) else { return ERR_TRANSPORT }

    statusCode.pointee = result.statusCode
    out.pointee = result.body
    outlen.pointee = UInt32(result.len)
    return OK
}

@_cdecl("privmxDrvNet_wsConnect")
public func privmxDrvNet_wsConnect(
    _ options: UnsafePointer<privmxDrvNet_WsOptions>?,
    _ onopen: OnOpenCb?,
    _ onmessage: OnDataCb?,
    _ onerror: OnDataCb?,
    _ onclose: OnCloseCb?,
    _ ctx: UnsafeMutableRawPointer?,
    _ res: UnsafeMutablePointer<OpaquePointer?>?
) -> Int32 {
    guard let options, let res, let url = options.pointee.url else { return ERR_NULL_ARG }
    let listener = NativeListener(
        onOpen: onopen, onMessage: onmessage, onError: onerror, onClose: onclose, ctx: ctx
    )
    guard let ws = WsSession(url: String(cString: url), listener: listener) else {
        return ERR_TRANSPORT
    }
    ws.start()
    res.pointee = box(ws)
    return OK
}

@_cdecl("privmxDrvNet_wsSend")
public func privmxDrvNet_wsSend(
    _ ws: OpaquePointer?,
    _ data: UnsafePointer<CChar>?,
    _ datalen: Int32
) -> Int32 {
    guard let ws else { return ERR_NULL_ARG }
    let session: WsSession = unbox(ws)
    return session.send(data, datalen) ? OK : ERR_TRANSPORT
}

@_cdecl("privmxDrvNet_wsClose")
public func privmxDrvNet_wsClose(_ ws: OpaquePointer?) -> Int32 {
    guard let ws else { return ERR_NULL_ARG }
    let session: WsSession = unbox(ws)
    session.close()
    return OK
}

@_cdecl("privmxDrvNet_wsFree")
public func privmxDrvNet_wsFree(_ ws: OpaquePointer?) -> Int32 {
    guard let ws else { return ERR_NULL_ARG }
    release(ws, as: WsSession.self)
    return OK
}

@_cdecl("privmxDrvNet_freeMem")
public func privmxDrvNet_freeMem(_ ptr: UnsafeMutableRawPointer?) -> Int32 {
    free(ptr)
    return OK
}
