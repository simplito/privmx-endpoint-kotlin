import Foundation

public typealias OnOpenCb = @convention(c) (UnsafeMutableRawPointer?) -> Void
public typealias OnDataCb = @convention(c) (UnsafeMutableRawPointer?, UnsafePointer<CChar>?, Int32) -> Void
public typealias OnCloseCb = @convention(c) (UnsafeMutableRawPointer?, Int32) -> Void

struct NativeListener {
    let onOpen: OnOpenCb?
    let onMessage: OnDataCb?
    let onError: OnDataCb?
    let onClose: OnCloseCb?
    let ctx: UnsafeMutableRawPointer?

    func emitError(_ text: String) {
        text.utf8CString.withUnsafeBufferPointer { buf in
            onError?(ctx, buf.baseAddress, Int32(buf.count - 1))
        }
    }

    func emitMessage(_ data: Data) {
        data.withUnsafeBytes { buf in
            onMessage?(ctx, buf.bindMemory(to: CChar.self).baseAddress, Int32(buf.count))
        }
    }
}

private final class WsDelegate: NSObject, URLSessionWebSocketDelegate {
    let listener: NativeListener
    let callbackQueue: DispatchQueue

    init(_ listener: NativeListener, _ callbackQueue: DispatchQueue) {
        self.listener = listener
        self.callbackQueue = callbackQueue
    }

    func urlSession(
        _ session: URLSession,
        webSocketTask: URLSessionWebSocketTask,
        didOpenWithProtocol protocol: String?
    ) {
        let listener = listener
        callbackQueue.async { listener.onOpen?(listener.ctx) }
    }

    func urlSession(
        _ session: URLSession,
        webSocketTask: URLSessionWebSocketTask,
        didCloseWith closeCode: URLSessionWebSocketTask.CloseCode,
        reason: Data?
    ) {
        let listener = listener
        let wasClean: Int32 = closeCode == .normalClosure ? 1 : 0
        callbackQueue.async { listener.onClose?(listener.ctx, wasClean) }
    }
}

final class WsSession {
    private let listener: NativeListener
    private let session: URLSession
    private let task: URLSessionWebSocketTask
    private let callbackQueue = DispatchQueue(label: "privmx-net-drv.ws.callbacks")
    private let lock = NSLock()
    private var closed = false

    init?(url: String, listener: NativeListener) {
        guard let nsUrl = URL(string: url) else { return nil }
        self.listener = listener
        let queue = OperationQueue()
        queue.maxConcurrentOperationCount = 1
        session = URLSession(
            configuration: .default, delegate: WsDelegate(listener, callbackQueue), delegateQueue: queue
        )
        task = session.webSocketTask(with: nsUrl)
    }

    private var isClosed: Bool {
        return closed
    }

    func start() {
        task.resume()
        receiveNext()
    }

    private func receiveNext() {
        task.receive { [weak self] result in
            guard let self, !self.isClosed else { return }
            switch result {
            case .failure(let error):
                let message = error.localizedDescription
                self.callbackQueue.async { self.listener.emitError(message) }
                return
            case .success(.data(let data)):
                self.callbackQueue.async { self.listener.emitMessage(data) }
            case .success(.string(let text)):
                self.callbackQueue.async { self.listener.emitMessage(Data(text.utf8)) }
            case .success:
                break
            }
            self.receiveNext()
        }
    }

    func send(_ data: UnsafePointer<CChar>?, _ datalen: Int32) -> Bool {
        awaitCallback { done in
            task.send(.data(dataOf(data, datalen))) { error in done(error == nil) }
        }
    }

    func close() {
        lock.lock()
        closed = true
        lock.unlock()
        task.cancel(with: .normalClosure, reason: nil)
        session.finishTasksAndInvalidate()
    }
}
