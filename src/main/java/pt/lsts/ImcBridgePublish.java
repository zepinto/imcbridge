package pt.lsts;

import io.vertx.core.net.SocketAddress;
import pt.lsts.imc4j.def.SystemType;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.net.ImcNetwork;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class ImcBridgePublish {

    private DatagramSocket socket = new DatagramSocket();
    int remotePort;
    String remoteHost;

    private void listenImc(int port) {
        ImcNetwork imc = new ImcNetwork("BridgePublisher", 8008, SystemType.CCU);
        imc.setConnectionPolicy(p -> p.getId() != 8008);

        try {
            imc.startListening(port);
            imc.subscribe(Message.class, msg -> {
                handle(msg);
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ImcBridgePublish(int localPort, SocketAddress cloudServer) throws Exception {
        this.remoteHost = cloudServer.host();
        this.remotePort = cloudServer.port();
        listenImc(localPort);
    }

    void handle(Message localMessage) {
        try {
            byte[] data = localMessage.serialize();
            DatagramPacket packet = new DatagramPacket(data, data.length,
                    new InetSocketAddress(remoteHost, remotePort));
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        int localPort = Integer.valueOf(args[0]);
        String host = args[1];
        int hostPort = Integer.valueOf(args[2]);

        SocketAddress addr = SocketAddress.inetSocketAddress(hostPort, host);
        new ImcBridgePublish(localPort, addr);
    }
}
