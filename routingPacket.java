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
public class routingPacket extends PacketContent {
    //Routing packets are sent to the Controller by Router and end users requesting information about the flowTbale
    String filename;
    int size;
    String sendTo;
    InetSocketAddress from;
    routingPacket(String sendTo, InetSocketAddress from) {
        type = ROUTINGPACKET;
        //Theese tell the controller the source and destination of the packet so it can determine the flow
        this.sendTo = sendTo;
        this.from = from;
    }

    protected routingPacket(ObjectInputStream oin) {
        try {
            type = ROUTINGPACKET;
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

    public String sendTo() {return sendTo;}

    public InetSocketAddress from() {return from;}

}
