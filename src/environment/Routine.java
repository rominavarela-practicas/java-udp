package environment;

public class Routine {

	/**
	 * Close opened ports to safely exit
	 */
	public static void exit()
	{
		// client connection
		if(Env.client!=null && Env.client.con!=null)
		{
			System.out.println("Closing "+Env.client.con.socket.getPort());
			Env.client.con.socket.close();
		}
		
		// server connection
        if(Env.server!=null && Env.server.con!=null)
        {
      	  System.out.println("Closing "+Env.server.con.socket.getPort());
      	  Env.server.con.socket.close();
        }
        
        System.out.println("All connections closed");
        System.exit(0);
	}
}
