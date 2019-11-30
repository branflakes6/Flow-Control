import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
/**
 *
 * 
 *
 */
public class controller extends Node {
    static final int DEFAULT_PORT = 50001;
    int connectedRouters = 0; //number of connected routers
    InetSocketAddress[] addresses = new InetSocketAddress[9]; //Socket Addresses that map to our flowTable
    int[][] flowTable = {
            {0, 3, 4, 5, 3}, //E1 to E4 through Routers 1 and 2
            {0, 1, 4, 7, 1}, //E1 to E2 through Routers 1 and 4
            {0, 2, 4, 6, 8, 2}, //E1 to E3 through Routers 1, 3 and 5
            {1, 3, 7, 6, 5, 3}, //E2 to E4 through Routers 4, 3 and 2
            {1, 2, 7, 8, 2}, //E2 to E3 through Routers 4 and 5
            {2, 3, 8, 6, 3} //E3 to E4 through Routers 5 and 2
    }; // flowTable consists of a 2D array of ints that correspond to indexes of addresses[]
    controller(int port) {
        try {
            socket= new DatagramSocket(port);
            listener.go();
        }
        catch(java.lang.Exception e) {e.printStackTrace();}
    }

    public void onReceipt(DatagramPacket packet) {
        try {
            System.out.println("Received packet");
            PacketContent content = PacketContent.fromDatagramPacket(packet);
            if (content.getType() == PacketContent.USERPACKET) //A User Packet signals that an endUser is contacting the Controller for the first time.
            {
                boolean sendRespone = false;
                System.out.println("An End User has Connected");
                //As we can only have 4 endUsers we simply check the four possible slots an endUser could occupy to see if we have any space for more.
                if (addresses[0] == null) {
                    addresses[0] = (InetSocketAddress) packet.getSocketAddress();
                    sendRespone = true;
                } else if (addresses[1] == null) {
                    addresses[1] = (InetSocketAddress) packet.getSocketAddress();
                    sendRespone = true;
                }
                else if (addresses[2] == null) {
                    addresses[2] = (InetSocketAddress) packet.getSocketAddress();
                    sendRespone = true;
                }
                else if (addresses[3] == null) {
                    addresses[3] = (InetSocketAddress) packet.getSocketAddress();
                    sendRespone = true;
                }
                else {
                    System.out.println("Maximum End Users");
                }
                //Let the endUser know we have recieved its message
                if (sendRespone) {
                    DatagramPacket response;
                    response = new startPacket((InetSocketAddress)packet.getSocketAddress()).toDatagramPacket();
                    response.setSocketAddress(packet.getSocketAddress());
                    socket.send(response);
                    System.out.println("Sent ACK \n");
                }
            }
            if (content.getType() == PacketContent.STARTPACKET) //A Start Packet indicates a new Router has contacted us
            {
                if (connectedRouters >= 5) //We can only have a maximum of 5 Routers in this protocol
                {
                    System.out.println("Maximum Routers");
                    //first 4 indexes are occupied by endUsers not Routers
                    for (int i = 4; i < 9; i++) {
                        System.out.println("Addresses = " + addresses[i]);
                    }
                } else //Adding our router to the list of addresses for our flowTable and sending a reply
                    {
                    System.out.println("Received 'Hello' from " + packet.getSocketAddress());
                    connectedRouters++;
                    addresses[3 + connectedRouters] = (InetSocketAddress) packet.getSocketAddress(); //first 4 indexes occupied by endUsers
                    DatagramPacket response;
                    response = new startPacket().toDatagramPacket();
                    response.setSocketAddress(packet.getSocketAddress());
                    socket.send(response);
                    System.out.println("Sent ACK");
                    System.out.println("Have " + connectedRouters + " routers \n");
                }
            }
            if (content.getType() == PacketContent.ROUTINGPACKET) //A Routing Packet indicates a request for the flowTable
            {
                System.out.println("Recieved a routing request \n");
                try {
                    InetSocketAddress sendTo = null;
                    InetSocketAddress source = (((routingPacket)content).from()); //The End User we want to send a packet to
                    InetSocketAddress destination = stringToAddress(((routingPacket)content).sendTo()); //The End User who is sending the packet
                    for (int i = 0; i < 6; i++) { //Going through the flowTable, all possible paths
                        if (source.equals(addresses[flowTable[i][0]]) && destination.equals(addresses[flowTable[i][1]])) { //Checking if the Source and Destination are a valid path going left to right i.e E1 to E4
                            System.out.println("Found the route");
                            if (packet.getSocketAddress().equals(addresses[flowTable[i][0]])) { //Has this packet come straight from the Source? If so we know exactly where to send it
                                sendTo = addresses[flowTable[i][2]]; //This is the first Router in the path
                            }
                            else { //Else the packet has come from a Router along the path
                                for (int j = 2; j < flowTable[i].length; j++) { //Go through all Routers in the path to figure out which one it is
                                    if (packet.getSocketAddress().equals(addresses[flowTable[i][j]])) {
                                        sendTo = addresses[flowTable[i][j + 1]]; //Tell the Router to send it to the next Router in the path
                                        }
                                    }
                                }
                            }
                        else if (source.equals(addresses[flowTable[i][1]]) && destination.equals(addresses[flowTable[i][0]])) { //Checking if the Source and Destination are a valid path going right to left i.e E4 to E1
                            if (packet.getSocketAddress().equals(addresses[flowTable[i][1]])) { //This is basically the same code just checing if our flow is in the opposite direction
                                sendTo = addresses[flowTable[i][(flowTable[i].length) - 2]];
                            }
                            else {
                                    for (int j = 2; j < flowTable[i].length; j++) {
                                        if (packet.getSocketAddress().equals(addresses[flowTable[i][j]])) {
                                            if(addresses[flowTable[i][j - 2]].equals(destination)) { //if we are going right to left and we reach the end we need to skip one or we will send the packet back where it came
                                                sendTo = addresses[flowTable[i][j - 2]];
                                            }
                                            else {
                                                sendTo = addresses[flowTable[i][j-1]];
                                            }
                                        }
                                    }
                                }
                            }
                    }
                    if(sendTo != null) { //Have found the path so we send a Controller Packet with that info
                        DatagramPacket response;
                        response = new controllerPacket(sendTo).toDatagramPacket();
                        response.setSocketAddress(packet.getSocketAddress());
                        socket.send(response);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InetSocketAddress stringToAddress(String destination) { //Function that takes in a String name and returns the SocketAddress of that user
        InetSocketAddress address = null;
        switch(destination)
        {
         case "e1":
             address = addresses[0];
             break;
         case "e2":
             address = addresses[1];
             break;
         case "e3":
             address = addresses[2];
             break;
         case "e4":
             address = addresses[3];
             break;
         default:
             address = null;
             break;
        }
        return address;
    }
    public synchronized void start() throws Exception {
        System.out.println("Waiting for contact");
        this.wait();
    }

    public static void main(String[] args) {
        try {
            (new controller(DEFAULT_PORT)).start();
            System.out.println("Program completed");
        } catch(java.lang.Exception e) {e.printStackTrace();}
    }


}
