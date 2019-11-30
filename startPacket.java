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
public class startPacket extends PacketContent {
    //The start packet is used to send "Hello" messages between Routers and the Controller so the Controller knows the Routers are live
    String filename;
    int size;
    InetSocketAddress address;
    startPacket() {
            type = STARTPACKET;
        }
     startPacket(InetSocketAddress address)
     {
         //Used by the end users to send some information about the addresses in the flow table
         type = USERSTART;
         this.address = address;
     }

    protected startPacket(ObjectInputStream oin) {
        try {
            type = USERSTART;
            address = (InetSocketAddress) oin.readObject();
        } catch(Exception e) {e.printStackTrace();}
    }

    protected void toObjectOutputStream(ObjectOutputStream oout) {
        try {
            oout.writeObject(address);
        }
        catch(Exception e) {e.printStackTrace();}
    }

    public InetSocketAddress getAddress() {
        return address;
    }
}
