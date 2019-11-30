import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
/**
 *
 * 
 *
 */
public class workPacket extends PacketContent {
    //Basically the same as a Routing Packet but sent by end users to Routers so they can distinguish between packets from the Controller and end user
    String filename;
    int size;
    String sendTo;
    InetSocketAddress from;

    workPacket(String sendTo, InetSocketAddress from) {
        type = WORKPACKET;
        this.sendTo = sendTo;
        this.from = from;
    }
    protected workPacket(ObjectInputStream oin) {
        try {
            type = WORKPACKET;
            sendTo = oin.readUTF();
            from = (InetSocketAddress) oin.readObject();
        } catch(Exception e) {e.printStackTrace();}
    }

    protected void toObjectOutputStream(ObjectOutputStream oout) {
        try {
            oout.writeUTF(sendTo);
            oout.writeObject(from);
        }
        catch(Exception e) {e.printStackTrace();}
    }

    public String sendTo() {
        return sendTo;
    }

    public InetSocketAddress from() {
        return from;
    }

}
