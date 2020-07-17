package pt.lsts;

import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import pt.lsts.imc4j.def.SystemType;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.net.ImcNetwork;
import pt.lsts.imc4j.util.FormatConversion;

import java.text.ParseException;

public class ImcBridgeClient {
    public static void main(String[] args) {
        int localPort = Integer.valueOf(args[0]);
        String host = args[1];
        int hostPort = Integer.valueOf(args[2]);
        SocketAddress addr = SocketAddress.inetSocketAddress(hostPort, host);
        ImcNetwork network = new ImcNetwork("BridgeViewer", 8008, SystemType.CCU);
        network.setConnectionPolicy(p -> p.getType() == SystemType.CCU);

        Vertx.vertx().createNetClient().connect(addr, event -> {
            if (event.succeeded()) {
                event.result().handler(data -> {
                    String msgJson = data.getString(0, data.length());

                    try {
                        Message m = FormatConversion.fromJson(msgJson);
                        if (m.dst == 0xFFFF)
                            network.publish(m);
                        System.out.println(msgJson);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                });
            }
            else {
                System.err.println("Unable to connect to server: "+addr);
                event.cause().printStackTrace();
                System.exit(1);
            }
        });
    }
}
