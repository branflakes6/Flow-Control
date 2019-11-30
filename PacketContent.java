import java.net.DatagramPacket;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
/**
 *
 * 
 *
 */
public abstract class PacketContent {

	public static final int ACKPACKET= 10;
	public static final int CONTROLLPACKET= 100;
	public static final int STARTPACKET = 200;
	public static final int USERPACKET = 300;
	public static final int ROUTINGPACKET = 400;
	public static final int WORKPACKET = 500;
	public static final int USERSTART = 600;
	int type= 0;

	public static PacketContent fromDatagramPacket(DatagramPacket packet) {
		PacketContent content= null;

		try {
			int type;
			byte[] data;
			ByteArrayInputStream bin;
			ObjectInputStream oin;

			data= packet.getData();  // use packet content as seed for stream
			bin= new ByteArrayInputStream(data);
			oin= new ObjectInputStream(bin);

			type= oin.readInt();  // read type from beginning of packet

			switch(type) {   // depending on type create content object
			case ROUTINGPACKET:
				content= new routingPacket(oin);
				break;
			case CONTROLLPACKET:
				content= new controllerPacket(oin);
				break;
             case STARTPACKET:
                 content = new startPacket();
                 break;
			case USERPACKET:
				content = new userPacket();
				break;
			case WORKPACKET:
				content = new workPacket(oin);
				break;
            case USERSTART:
                 content = new startPacket(oin);
                 break;
			default:
				content= null;
				break;
			}
			oin.close();
			bin.close();

		}
		catch(Exception e) {e.printStackTrace();}

		return content;
	}


	protected abstract void toObjectOutputStream(ObjectOutputStream out);

	public DatagramPacket toDatagramPacket() {
		DatagramPacket packet= null;

		try {
			ByteArrayOutputStream bout;
			ObjectOutputStream oout;
			byte[] data;

			bout= new ByteArrayOutputStream();
			oout= new ObjectOutputStream(bout);

			oout.writeInt(type);         // write type to stream
			toObjectOutputStream(oout);  // write content to stream depending on type

			oout.flush();
			data= bout.toByteArray(); // convert content to byte array

			packet= new DatagramPacket(data, data.length); // create packet from byte array
			oout.close();
			bout.close();
		}
		catch(Exception e) {e.printStackTrace();}

		return packet;
	}
    public int getType() {
        return type;
    }

}
