/**
 * 
 */
package application.processing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @author Sokhna Amy Diop
 *
 */
public class ProcessingImage {
	
	public static BufferedImage GrayScale(File img)
	{
		BufferedImage image = null;
		try {
			image = ImageIO.read(img);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Obtention des dimensions de l'image
		for(int y = 0; y < image.getHeight(); y++)
		{
			for(int x = 0; x < image.getWidth(); x++)
			{
				int p = image.getRGB(x, y);
				int a = (p>>24)&0xff;
				int r = (p>>16)&0xff;
				int g =  (p>>8)&0xff;
				int b =  p&0xff;
				
				// Calculer la moyenne
				int avg = (r+g+b)/3;
				
				// Remplacer RVG avec la moyenne
				p = (a<<24) | (avg<<16) | (avg<<8) | avg;
				image.setRGB(x, y, p);
			}
		}
		
		return image;
	}
	
}
