
public class VSThread implements Runnable
{
	private VideoFrame frm;
	private Thread th;
	private byte[] dataRef;
	private short w, h;
	
	public VideoFrame getFrame() { return frm; }
	
	public VSThread(byte[] dataRef, short w, short h)
	{
		this.dataRef = dataRef;
		this.w = w;
		this.h = h;
		this.th = new Thread(this);
		th.start();
	}
	
	@Override
	public void run()
	{
		frm = new VideoFrame(dataRef, w, h);
	}
	
}
