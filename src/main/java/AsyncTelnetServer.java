import static java.lang.System.out;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AsyncTelnetServer {

    public static void main(String[] args) throws Exception {
        int port = 12345;
        InetSocketAddress socket = new InetSocketAddress("0.0.0.0", port);
        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open().bind(socket);
        server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override public void completed(AsynchronousSocketChannel sc, Void ignore) {
                out.println("new connection");
                server.accept(null, this);
                handleEcho(sc, ByteBuffer.allocate(16));
            }

            @Override public void failed(Throwable exc, Void ignore) {
                out.println("fail to accept a connection");
            }
        });
        out.println("listening on " + port);
        while (true) Thread.sleep(1000);
    }

    public static void handleEcho(AsynchronousSocketChannel sc, ByteBuffer buf) {
        out.println("handing echo (open=" + sc.isOpen() + ")");
        buf.clear();
        sc.read(buf, null, new CompletionHandler<Integer, Void>() {
            @Override public void completed(Integer readBytes, Void ignore) {
                if (readBytes < 0) {
                    out.println("connection closed");
                    return;
                }
                out.println("read completed(" + readBytes + ")");
                buf.flip();
                sc.write(buf, null, new CompletionHandler<Integer, Void>() {
                    @Override public void completed(Integer writeBytes, Void ignore) {
                        out.println("write completed(" + writeBytes + ")");
                        handleEcho(sc, buf);
                    }

                    @Override public void failed(Throwable e, Void ignore) {
                        out.println("fail to write message to client");
                    }
                });
            }

            @Override public void failed(Throwable e, Void ignore) {
                out.println("fail to read message from client");
            }
        });
    }
}
