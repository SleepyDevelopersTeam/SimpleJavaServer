import java.net.*;
import java.io.*;

public class Server {
    public static void main(String[] ar)
    {
        int port = 9090;
        try
        {
            ServerSocket ss = new ServerSocket(port); //new server
            System.out.println("Waiting for a client...");

            Socket socket = ss.accept();
            System.out.println("Client connect");
            System.out.println();

            InputStream sin = socket.getInputStream(); //input socket stream
            OutputStream sout = socket.getOutputStream(); //output socket stream

            // text string
            DataInputStream in = new DataInputStream(sin);
            DataOutputStream out = new DataOutputStream(sout);

            String line = null;
            while(true) {
                line = in.readUTF(); // ready string
                System.out.println("client sent line : " + line);
                out.writeUTF(line); //write outback
                System.out.println();
            }
        }
        catch(Exception x)
        {
            x.printStackTrace();
        }
    }
}