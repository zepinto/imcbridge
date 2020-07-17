package pt.lsts;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;
import pt.lsts.imc4j.annotations.Consume;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.net.UdpClient;

import java.util.ArrayList;

public class ImcBridgeCloud {
    private Vertx vertx = Vertx.vertx();
    ArrayList<NetSocket> clients = new ArrayList<NetSocket>();
    public ImcBridgeCloud(int port) {
        vertx.createNetServer()
                .connectHandler(socket -> {
                    System.out.println("Client "+socket.remoteAddress()+" connected.");
                    clients.add(socket);
                    socket.endHandler(s -> {
                        clients.remove(socket);
                        System.out.println("Client "+socket.remoteAddress()+" disconnected.");
                    });
                })
                .exceptionHandler(exception -> {
                    exception.printStackTrace();
                })
                .listen(port);
    }

    @Consume
    public void on(Message message) {
        log(message);
        disseminate(message);
    }

    private void log(Message m) {
        System.out.println(m.toString());
    }

    private void disseminate(Message m) {
        clients.forEach(client -> {
            client.write(m.toString());
        });
    }

    public static void main(String[] args) throws Exception {
        UdpClient udpServer = new UdpClient();
        int port = Integer.valueOf(args[0]);
        udpServer.bind(port);
        udpServer.register(new ImcBridgeCloud(port));
        System.out.println("Bound to port "+port);
    }
}
