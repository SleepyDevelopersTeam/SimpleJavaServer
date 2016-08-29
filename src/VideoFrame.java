import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class VideoFrame extends JFrame
{
	Canvas cv;
	
	public VideoFrame(byte[] dataRef, short w, short h)
	{
		// TODO Auto-generated constructor stub
		setTitle("Live stream");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		
		cv = new Canvas(dataRef, w, h, this);
		add(cv);
	}
	
	
	public void dataChanged(short w, short h)
	{
		cv.dataChanged(w, h);
		setSize(w, h);
	}
	
	public void refChanged(byte[] newRef)
	{
		cv.refChanged(newRef);
	}
}

class Canvas extends JPanel
{
	private byte[] imageData;
	private int[] argb;
	//private ByteArrayInputStream imageStream;
	private final Object mutex = new Object();
	private VideoFrame parent;
	private BufferedImage bmp;
	private WritableRaster raster;
	private short width, height;
	
	public Canvas(byte[] dataRef, short w, short h, VideoFrame parent)
	{
		parent.setSize(400, 300);
		this.parent = parent;
		refChanged(dataRef);
		parent.setSize(w, h);
		width = w;
		height = h;
		setSize(w, h);
	}
	
	public void refChanged(byte[] newRef)
	{
		synchronized(mutex)
		{
			imageData = newRef;
			//imageStream = new (argb);
		}
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.setColor(Color.cyan);
		// g.fillRect(0, 0, getWidth(), getHeight());
		if (argb == null) return;
		synchronized(mutex)
		{
			try
			{
				//imageStream.reset();
				//bmp = ImageIO.read(imageStream);
				g.drawImage(bmp, 0, 0, null);
				//g.fillRect(50, 50, 10, height-10);
			}
			catch (Exception ex)
			{
				System.out.println("Exception! " + ex.getMessage());
				ex.printStackTrace();
			}
		}
	}
	
	private void decodeImage()
	{
//		int rgbIndex = 0;
//		for (int rawIndex = 0; rawIndex < imageData.length; rawIndex+=2)
//		{
//			// rgb 565 -> rgb 888
//			byte b1 = imageData[rawIndex], b2 = imageData[rawIndex + 1];
//			rgb[rgbIndex + 2] = (byte) ( ((b1>>3)&31) *255/31);
//			rgb[rgbIndex + 2] = (byte) ( ( ((b1<<3)&56) + ((b2>>5)&7) ) *255/63);
//			rgb[rgbIndex + 2] = (byte) ( ((b2)&31) *255/31);
//		}
		synchronized(mutex)
		{
			try
			{
				YUV_NV21_TO_RGB(argb, imageData, width, height);
				//((WritableRaster) bmp.getRaster()).setPixels(0, 0, width, height, argb);
				raster.setPixels(0, 0, width, height, argb);
			}
			catch (Exception e)
			{
				System.out.print(" !" + e.getMessage() + ", " + e.getClass().getName() + "! ");
				//e.printStackTrace();
			}
		}
	}
	
	void dataChanged(short w, short h)
	{
		if (width != w || height != h)
		{
			System.out.println("Size changed: " + w + "; " + h);
			synchronized (mutex)
			{
				width = w; height = h;
				argb = new int[width * height * 3];
				bmp = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
				raster = (WritableRaster) bmp.getRaster();
			}
			setSize(w, h);
		}
		decodeImage();
		repaint();
	}
	
	private void YUV_NV21_TO_RGB(int[] argb, byte[] yuv, int width, int height) {
	    final int frameSize = width * height;

	    final int ii = 0;
	    final int ij = 0;
	    final int di = +1;
	    final int dj = +1;

	    int a = 0;
	    for (int i = 0, ci = ii; i < height; ++i, ci += di) {
	        for (int j = 0, cj = ij; j < width; ++j, cj += dj) {
	            int y = (0xff & ((int) yuv[ci * width + cj]));
	            int v = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 0]));
	            int u = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 1]));
	            y = y < 16 ? 16 : y;

	            int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
	            int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
	            int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

	            r = r < 0 ? 0 : (r > 255 ? 255 : r);
	            g = g < 0 ? 0 : (g > 255 ? 255 : g);
	            b = b < 0 ? 0 : (b > 255 ? 255 : b);

	            argb[a++] = b;//0xff000000 | (r << 16) | (g << 8) | b;
	            argb[a++] = g;
	            argb[a++] = r;
	        }
	    }
	}
}
