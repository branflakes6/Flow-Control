import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
/**
 *
 * 
 *
 */
public class controllerPacket extends PacketContent {
    //The packet sent by the Controller with the requested flowTable, simply just tells the user or Router the next address in the table
	String filename;
	int size;
	InetSocketAddress address1;
	InetSocketAddress address2;
	controllerPacket(InetSocketAddress address1) {
		type= CONTROLLPACKET;
		this.address1 = address1;
	}

	protected controllerPacket(ObjectInputStream oin) {
		try {
			type= CONTROLLPACKET;
			address1= (InetSocketAddress) oin.readObject();
		}
		catch(Exception e) {e.printStackTrace();}
	}

	protected void toObjectOutputStream(ObjectOutputStream oout) {
		try {
			oout.writeObject(address1);
		}
		catch(Exception e) {e.printStackTrace();}
	}
	public InetSocketAddress getTo()
	{
		return address1;
	}
}
