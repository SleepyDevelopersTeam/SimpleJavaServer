import java.net.*;
import java.io.*;

public class Server {
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
	static short width, height;
	
	static boolean awaitCommand(byte command) throws IOException
	{
		byte response = in.readByte();
		if (response == command) return true;
		out.writeByte(ERROR);
		return false;
	}
	static void writeAnswer(byte answer) throws IOException
	{
		out.writeByte(answer);
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
	
	static void readData() throws IOException
	{
		width = in.readShort();
		height = in.readShort();
		
		int read = in.read(data);
		while (read != data.length)
		{
			// not everything was read
			int rest = data.length - read;
			read += in.read(data, read, rest);
		}
	}
	
	static boolean validateData()
	{
//		for (int i = 0; i < data.length; i++)
//		{
//			if (data[i] != (i % 10))
//			{
//				System.out.println(data[i] + " " + (i % 10));
//				System.exit(0);
//				return false;
//			}
//		}
		return true;
	}
	
    public static void main(String[] ar)
    {
        int port = 9090;
        ServerSocket ss = null;
        try
        {
            ss = new ServerSocket(port);
            System.out.println("Server started successfully");
            
            VideoFrame window = null;

            while(true)
            {
            	System.out.println("Waiting for a client...");
	            Socket socket = ss.accept();
	            System.out.println("Client connected");
	            System.out.println();
	            InputStream sin = socket.getInputStream();
	            OutputStream sout = socket.getOutputStream();
	            in = new DataInputStream(sin);
	            out = new DataOutputStream(sout);
	            
	            // SDTUDTP3K communication:
	            boolean b = awaitCommand(HELLO_SERVER);
	            assert b: "Hello client lost";
	            writeAnswer(HELLO_CLIENT);
	            
	            int l = in.readInt();
	            setDataLength(l);
	            
	            System.out.println("Handshake done");
	            
	            //VSThread th = new VSThread(data, (short) 400, (short) 300);
	            //window = th.getFrame();
	            window = new VideoFrame(data, (short) 400, (short) 300);
	            
	            boolean dataExchange = true;
	
	            while(dataExchange) {
	                // data exchanging
	            	byte cmd = readCommand();
	            	switch (cmd)
	            	{
	            	case DATA:
	            		readData();
	            		window.dataChanged(width, height);
	            		if (validateData()) System.out.print(".");
	            		else  System.out.print("!");
	            		writeAnswer(DATA_RECEIVED);
	            		break;
	            		
	            	case LENGTH_CHANGE:
	            		setDataLength(in.readInt());
	            		window.refChanged(data);
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
	            		window.setVisible(false);
	            		break;
	            		
	            	case ERROR:
	            		System.out.println("Client error!");
	            		dataExchange = false;
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