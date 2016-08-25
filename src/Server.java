import java.net.*;
import java.io.*;

public class Server {
	static final int MAX_BYTES = 508;
	
	static final byte DATA = 0x00;
	
	static final byte HELLO_SERVER = 0x1E;
	static final byte HELLO_CLIENT = 0x1A;
	
	static final byte DATA_RECEIVED = 0x2D;
	
	static final byte LENGTH_CHANGE = 0x31;
	static final byte FONE_RESET = 0x3F;
	static final byte COMMAND_EXECUTED = 0x3E;
	
	static final byte GB_SERVER = 0x45;
	static final byte GB_CLIENT = 0x4C;
	
	static final byte ERROR = 0x66;
	
	static DataOutputStream out;
	static DataInputStream in;
	
	// data
	static int length;
	static byte[] data;
	
	static boolean awaitCommand(byte command) throws IOException
	{
		byte response = in.readByte();
		if (response == command) return true;
		System.out.println("Error! Expected " + command + ", but got " + response);
		out.writeByte(ERROR);
		out.flush();
		return false;
	}
	static void writeAnswer(byte answer) throws IOException
	{
		out.writeByte(answer);
		out.flush();
	}
	
	static byte readCommand() throws IOException
	{
		return in.readByte();
	}
	
	static void setDataLength(int len)
	{
		length = len;
		data = new byte[len];
		System.out.println("Client sent length: " + data.length);
	}
	
	static void readData() throws IOException, Exception
	{
		System.out.print(data.length + " ");
		for (int i = 0; i < data.length; i+= MAX_BYTES)
		{
			System.out.println(Math.min(data.length, i + MAX_BYTES));
			in.readFully(data, i, Math.min(data.length, i + MAX_BYTES));
		}
		for (int i=0; i<data.length; i++)
		{
			if (data[i] != 22)
			{
				System.out.println("FUCK " + data[i] + " at " + i);
				throw new Exception("FFFFFFFFFFFFFUUUUUUUUUUUUUUUUUUUUUUU");
			}
		}
	}
	
    public static void main(String[] ar)
    {
        int port = 9090;
        ServerSocket ss = null;
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        try
        {
            ss = new ServerSocket(port);
            System.out.println("Server started successfully");

            while(true)
            {
            	System.out.println("Waiting for a client...");
	            Socket socket = ss.accept();
	            InputStream sin = socket.getInputStream();
	            OutputStream sout = socket.getOutputStream();
	            in = new DataInputStream(sin);//new BufferedInputStream(sin));
	            out = new DataOutputStream(sout);//new BufferedOutputStream(sout));
	            System.out.println("Client connected");
	            System.out.println();
	            
	            // SDTUDTP3K communication:
	            boolean b = awaitCommand(HELLO_SERVER);
	            if (!b) throw new Exception("Hello client lost");
	            else System.out.println("> Hello, server!");
	            writeAnswer(HELLO_CLIENT);
	            System.out.println("< Hello, client!");
	            
	            int l = in.readInt();
	            setDataLength(l);
	            System.out.println("Handshake done");
	            
	            boolean dataExchange = true, error = false;
	
	            while(dataExchange) {
	                // data exchanging
	            	if (error) error = keyboard.readLine() == " ";
	            	byte cmd = readCommand();
	            	switch (cmd)
	            	{
	            	case DATA:
	            		readData();
	            		System.out.println("Data successfully received");
	            		writeAnswer(DATA_RECEIVED);
	            		break;
	            		
	            	case LENGTH_CHANGE:
	            		setDataLength(in.readInt());
	            		writeAnswer(COMMAND_EXECUTED);
	            		break;
	            		
	            	case FONE_RESET:
	            		System.out.println("Fone reset command");
	            		writeAnswer(COMMAND_EXECUTED);
	            		break;
	            		
	            	case GB_SERVER:
	            		dataExchange = false;
	            		System.out.println("Goodbye, server!");
	            		writeAnswer(GB_CLIENT);
	            		break;
	            		
	            	case ERROR:
	            	default:
	            		System.out.println("Client error! " + cmd);
	            		error = true;
	            		//dataExchange = false;
	            		break;
	            	}
	            }
            }
        }
        catch(Exception x)
        {
            x.printStackTrace();
            try
            {
            	out.writeByte(ERROR);
            }
            catch(Exception ex) { /* whatever */ }
            try
            {
            	ss.close();
            }
            catch(Exception ex) { /* whatever */ }
        }
    }
}