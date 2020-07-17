package pt.lsts;

import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.util.FormatConversion;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class ImcBridgeClient {

    private String incoming = "";
    private int localport;
    private DatagramSocket socket = new DatagramSocket();


    void handle(String str) {
        if (str.endsWith("\r\n")) {
            System.out.println("'"+incoming+str.trim()+"'\n");

            String[] msgs = (incoming+str).split("\r\n");

            for (int i = 0; i < msgs.length; i++) {
                try {
                        Message m = FormatConversion.fromJson(msgs[i]);
                        byte[] data = m.serialize();
                        DatagramPacket packet = new DatagramPacket(data, data.length,
                                new InetSocketAddress("localhost", localport));
                        socket.send(packet);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            incoming = "";
        }
        else
            incoming += str;
    }

    public ImcBridgeClient(int localPort, String hostname, int port) throws Exception {
        SocketAddress addr = SocketAddress.inetSocketAddress(port, hostname);
        //network.setConnectionPolicy(p -> p.getType() == SystemType.CCU);
        //client.connect("localhost", localPort);
        this.localport = localPort;

        Vertx.vertx().createNetClient().connect(addr, event -> {
            if (event.succeeded()) {
                event.result().handler(data -> {
                    handle(data.getString(0, data.length()));
                });
                System.out.println("connected.");
            }
            else {
                System.err.println("Unable to connect to server: "+addr);
                event.cause().printStackTrace();
                System.exit(1);
            }
        });
    }

    public static void main(String[] args) throws Exception {
        int localPort = Integer.valueOf(args[0]);
        String host = args[1];
        int hostPort = Integer.valueOf(args[2]);

        new ImcBridgeClient(localPort, host, hostPort);



    }
}
