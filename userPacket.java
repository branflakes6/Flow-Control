import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;

public class userPacket extends PacketContent {

    String filename;
    int size;
   
    userPacket() {
            type = USERPACKET;
        }

   
    protected userPacket(ObjectInputStream oin) {
    }

   
    protected void toObjectOutputStream(ObjectOutputStream oout) {
    }

    public String toString() {
        return "Filename: " + filename + " - Size: " + size;
    }
}
