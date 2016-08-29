import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class VideoFrame extends JFrame
{
	Canvas cv;
	
	public VideoFrame(byte[] dataRef)
	{
		// TODO Auto-generated constructor stub
		setTitle("Live stream");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		
		cv = new Canvas(dataRef, this);
		add(cv);
	}
	
	
	public void dataChanged()
	{
		cv.dataChanged();
	}
	
	public void refChanged(byte[] newRef)
	{
		cv.refChanged(newRef);
	}
}

class Canvas extends JPanel
{
	private byte[] imageData;
	private byte[] rgb;
	private ByteArrayInputStream imageStream;
	private final Object mutex = new Object();
	private boolean sizeSet;
	private VideoFrame parent;
	BufferedImage bmp;
	
	public Canvas(byte[] dataRef, VideoFrame parent)
	{
		parent.setSize(400, 300);
		this.parent = parent;
		refChanged(dataRef);
		//setSize(400, 300);
	}
	
	public void refChanged(byte[] newRef)
	{
		synchronized(mutex)
		{
			imageData = newRef;
			sizeSet = false;
			rgb = new byte[newRef.length / 2 * 3];
			imageStream = new ByteArrayInputStream(rgb);
		}
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.setColor(Color.red);
		// g.fillRect(0, 0, getWidth(), getHeight());
		synchronized(mutex)
		{
			try
			{
				imageStream.reset();
				bmp = ImageIO.read(imageStream);
				if (!sizeSet)
				{
					sizeSet = true;
					parent.setSize(bmp.getWidth(), bmp.getHeight());
				}
				g.drawImage(bmp, 0, 0, null);
			}
			catch (IOException ex)
			{
				System.out.println("IOException! " + ex.getMessage());
			}
		}
	}
	
	private void decodeImage()
	{
		int rgbIndex = 0;
		for (int rawIndex = 0; rawIndex < imageData.length; rawIndex+=2)
		{
			// rgb 565 -> rgb 888
			byte b1 = imageData[rawIndex], b2 = imageData[rawIndex + 1];
			rgb[rgbIndex + 2] = (byte) ( ((b1>>3)&31) *255/31);
			rgb[rgbIndex + 2] = (byte) ( ( ((b1<<3)&56) + ((b2>>5)&7) ) *255/63);
			rgb[rgbIndex + 2] = (byte) ( ((b2)&31) *255/31);
		}
	}
	
	void dataChanged()
	{
		decodeImage();
		invalidate();
	}
	
	public static void YUV_NV21_TO_RGB(int[] argb, byte[] yuv, int width, int height) {
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

	            argb[a++] = 0xff000000 | (r << 16) | (g << 8) | b;
	        }
	    }
	}
}
