package pt.lsts;

import io.vertx.core.net.SocketAddress;
import pt.lsts.imc4j.def.SystemType;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.net.ImcNetwork;
import pt.lsts.imc4j.net.UdpClient;

import java.io.IOException;

public class ImcBridgePublish {

    private UdpClient udpClient = new UdpClient();

    private void listenImc(int port) {
        ImcNetwork imc = new ImcNetwork("BridgePublisher", 8008, SystemType.CCU);
        imc.setConnectionPolicy(p -> p.getId() != 8008);//!(p.getType() == SystemType.CCU));

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
        udpClient.connect(cloudServer.host(), cloudServer.port());
        listenImc(localPort);
    }

    void handle(Message localMessage) {
        try {
            udpClient.send(localMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*

     */

    public static void main(String[] args) throws Exception {
        int localPort = Integer.valueOf(args[0]);
        String host = args[1];
        int hostPort = Integer.valueOf(args[2]);

        SocketAddress addr = SocketAddress.inetSocketAddress(hostPort, host);
        new ImcBridgePublish(localPort, addr);
    }
}
