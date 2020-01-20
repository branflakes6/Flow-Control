# Flow-Control
A Telecomunications project for multiple machines on a network to communicate with each other by using Datagram Packets and Sockets.
endUsers(endUser.java) communicate with each other by sending packets through a network of routers(Routers.java) who forward packets according to a flowTable that is maintained by the Controller(controller.java). 
The Controller tells the various routers where to forward their packets in order to maintain the best possible path.
