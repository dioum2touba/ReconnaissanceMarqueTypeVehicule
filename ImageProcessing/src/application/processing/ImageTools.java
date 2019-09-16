package application.processing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;

public class ImageTools {
	
	DefaultListModel<Filter> filtersModel;
	BufferedImage image, imageCopy;
	String filename = "C:\\Users\\Sokhna Amy Diop\\Documents\\Projects\\RAPI_Segmentation\\Photo\\image13.jpEg";

	
	public static BufferedImage merge(BufferedImage bi1, BufferedImage bi2){
		return ImageTools.merge(bi1, bi2, true);
	}
	
	public static BufferedImage merge(BufferedImage bi1, BufferedImage bi2, boolean takeMaxValue){
		BufferedImage m = (bi1 != null)? bi1 : bi2;
		if(m == null)
			return null;
		m = copy(m);
		if(bi1 == null || bi2 == null)
			return m;

		int a, b;
		if(takeMaxValue){
			for(int i=0; i < bi1.getWidth(); i++)
				for(int j=0; j < bi1.getHeight(); j++){
					a = bi1.getRGB(i,j);
					b = bi2.getRGB(i,j);
					if(takeMaxValue)
						m.setRGB(i, j, Math.max(a, b));
				}
		}
		else{
			for(int i=0; i < bi1.getWidth(); i++)
				for(int j=0; j < bi1.getHeight(); j++){
					a = bi1.getRGB(i,j);
					b = bi2.getRGB(i,j);
					if(takeMaxValue)
						m.setRGB(i, j, Math.min(a, b));
				}
		}
		return m;
	}

	public static BufferedImage merge(BufferedImage bi1, BufferedImage bi2, BufferedImage bi3){
		BufferedImage m = (bi1 != null)? bi1 : ((bi2 != null)? bi2 : bi3);
		m = ImageTools.copy(m);
		int a, b, c;
		for(int i=0; i < m.getWidth(); i++)
			for(int j=0; j < m.getHeight(); j++){
				a = (bi1 == null)? Integer.MIN_VALUE : bi1.getRGB(i,j);
				b = (bi2 == null)? Integer.MIN_VALUE : bi2.getRGB(i,j);
				c = (bi3 == null)? Integer.MIN_VALUE : bi3.getRGB(i,j);
				m.setRGB(i, j, Math.max(Math.max(a, b), c));
			}
		return m;
	}


	public static BufferedImage loadImage(String filename){
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File(filename));
		} 
		catch (Exception e){
			//System.out.println(e.getMessage() + "\n" + e.getStackTrace());
			e.printStackTrace();
		}

		return bi;
	}

	public static BufferedImage testt(BufferedImage bi) {
		BufferedImage copy = ImageTools.copy(bi);
		//ImageTools.toGrayScale(copy);
		float filter[] = {
				0,0,0,0,1,
				0,-1,0,1,0,
				-1,0,0,0,0
		};
		BufferedImage destination = null;
		ConvolveOp c = new ConvolveOp(new Kernel(3, 5, filter), ConvolveOp.EDGE_NO_OP, null);
		destination = c.filter(copy, null);
		return destination;
	}
	
	public static BufferedImage contaminer(BufferedImage bi, int n){ // n = neighborhoodSize
		//BufferedImage copy = ImageTools.copy(bi);
		BufferedImage copy = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
		ImageTools.toBinaryFromAverageGray(copy);
		
		for(int i=n; i < (bi.getWidth()-n); i++)
			for(int j=n; j < (bi.getHeight()-n); j++){
				if ((bi.getRGB(i, j) & 0x00ffffff) == 0)
					continue;
				for(int k=(i-n); k <= (i+n); k++)
					for(int l=(j-n); l <= (j+n); l++)
						// Find non-black pixels 
						if ((bi.getRGB(k, l) & 0x00ffffff) == 0){
							copy.setRGB(k, l, 0x00ffffff);
						}
			}
		
		return copy;
	}
		
	public static BufferedImage toHeatMap(BufferedImage bi, int n){ // n = neighborhoodSize
		BufferedImage copy = ImageTools.copy(bi);
		//ImageTools.toBinaryFromAverageGray(copy);

		long[][] vals = new long[bi.getWidth()][bi.getHeight()];
		long val, maxVal = Long.MIN_VALUE;
		long sumVals = 0;
		long nbVals = 0, avgVal;
		// double ref = Math.pow(2*n+1, 2);
		int m = n; //Math.max(1, n/2);
		double ref = (2*n+1)*(2*m+1);
		
		for(int i=n; i < (vals.length-n); i++)
			for(int j=m; j < (vals[0].length-m); j++){
				val = 0;
				for(int k=(i-n); k <= (i+n); k++)
					for(int l=(j-m); l <= (j+m); l++)
						// Find non-black pixels 
						if ((bi.getRGB(k, l) & 0x00ffffff) != 0){
							val++;
							//val += bi.getRGB(k, l);
						}
				
				/*if((val/ref) < 0.6)
					continue;*/
				
				nbVals++;
				vals[i][j] = val;	
				if(val > maxVal)
					maxVal = val;
				sumVals += val;
			}
		
		
		// see: https://fr.wikipedia.org/wiki/Teinte_Saturation_Valeur
		float hue, maxHue = 300.0f; // Hue of Magento color
		float saturation = 100.0f;
		float brightness = 100.0f;
		int rgb;
		
		for(int i=0; i < vals.length; i++)
			for(int j=0; j < vals[0].length; j++){
				hue = maxHue - ((vals[i][j] * maxHue) / maxVal);
				rgb = Color.HSBtoRGB(hue, saturation, brightness);
				copy.setRGB(i, j, rgb);
			}
				
		return copy;
	}
	
	public static BufferedImage scaleDown(BufferedImage image, int scale){
		BufferedImage bi = ImageTools.copy(image);

		int width = (int)Math.floor(bi.getWidth()/(1.0*scale));
		int height = (int)Math.floor(bi.getHeight()/(1.0*scale));
		BufferedImage img = new BufferedImage(width, height, bi.getType());

		double squaredScale = Math.pow(scale, 2);
		int i, j, k, l, w = (bi.getWidth()/scale), h = (bi.getHeight()/scale);
		long nb = 0;

		for(i=0; i < w; i++){
			if(((i+1)*scale) > bi.getWidth())
				continue;
			for(j=0; j < h; j++){
				if(((j+1)*scale) > bi.getHeight())
					continue;
				nb = 0;
				for(k=0; k<scale; k++)
					for(l=0; l<scale; l++)
						nb += bi.getRGB((i*scale)+k, (j*scale)+l);
				img.setRGB(i, j, (int)(nb/squaredScale));
			}
		}

		return img;
	}

	public static BufferedImage copy(BufferedImage bi){
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	public static void toBinaryFromHSB(BufferedImage bi){
		int blackRgb = Color.BLACK.getRGB();
		int whiteRgb = Color.WHITE.getRGB();

		int r, g, b;
		//Color color;
		int a, p;
		for(int i=0; i < bi.getWidth(); i++)
			for(int j=0; j < bi.getHeight(); j++){
				p = bi.getRGB(i,j);
				a = (p>>24)&0xff;
				r = (p>>16)&0xff;
				g = (p>>8)&0xff;
				b = p&0xff;

				float[] hsv = new float[3];
				Color.RGBtoHSB(r,g,b,hsv);

				if((hsv[2]>=0.5) && (hsv[0]*hsv[1])<=0.2)
					bi.setRGB(i, j, whiteRgb);
				else
					bi.setRGB(i, j, blackRgb);
			}
	}

	public static void toBinary(BufferedImage bi){
		int threshold = Color.LIGHT_GRAY.getRGB();
		ImageTools.toBinary(bi, threshold);
	}

	public static void toBinaryFromAverageGray(BufferedImage bi){
		ImageTools.toGrayScale(bi);

		int w = bi.getWidth();
		int h = bi.getHeight();
		int rgb;

		int blackRgb = Color.BLACK.getRGB();
		int whiteRgb = Color.WHITE.getRGB();

		long sum = 0;
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				sum += bi.getRGB(x, y);
		int threshold =  (int)((0.9 * sum) / (h*w));

		System.out.println(" -- threshold = " + threshold + " (" + Integer.toHexString(threshold) + ")");

		for (int y = 0; y < h; y++){
			for (int x = 0; x < w; x++){
				rgb = bi.getRGB(x, y);
				if (rgb >= threshold)
					bi.setRGB(x, y, whiteRgb);
				else
					bi.setRGB(x, y, blackRgb);
			}
		}
	}

	// From: https://stackoverflow.com/questions/47618735/sobel-edge-detection-with-bufferedimage-type-byte-binary-as-output
	public static void toBinary(BufferedImage bi, int threshold){
		ImageTools.toGrayScale(bi);

		int w = bi.getWidth();
		int h = bi.getHeight();
		int rgb;

		int blackRgb = Color.BLACK.getRGB();
		int whiteRgb = Color.WHITE.getRGB();

		for (int y = 0; y < h; y++){
			for (int x = 0; x < w; x++){
				rgb = bi.getRGB(x, y);
				if (rgb >= threshold)
					bi.setRGB(x, y, whiteRgb);
				else
					bi.setRGB(x, y, blackRgb);
			}
		}
	}

	public static int  getGrayScale(int rgb) {
		int a = (rgb>>24)&0xff;
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = (rgb) & 0xff;

		//from https://en.wikipedia.org/wiki/Grayscale, calculating luminance
		int gray = (int)(0.2126 * r + 0.7152 * g + 0.0722 * b);
		//int gray = (r + g + b) / 3;

		return (a<<24) | (gray<<16) | (gray<<8) | gray;
	}

	// See: https://www.dyclassroom.com/image-processing-project/how-to-convert-a-color-image-into-grayscale-image-in-java
	public static void toGrayScale(BufferedImage bi){
		/*float r, g, b;
		Color color, newColor;*/
		int a, r, g, b, avg, p;
		//int rgb;
		for(int i=0; i < bi.getWidth(); i++)
			for(int j=0; j < bi.getHeight(); j++){
				p = bi.getRGB(i,j);

				a = (p>>24)&0xff;
				r = (p>>16)&0xff;
				g = (p>>8)&0xff;
				b = p&0xff;

				//calculate average
				//avg = (r+g+b)/3;
				avg = (int) (0.2126*r + 0.7152*g + 0.0722*b);
				//avg = (int) (0.114*r + 0.587*g + 0.299*b);

				//replace RGB value with avg
				p = (a<<24) | (avg<<16) | (avg<<8) | avg;

				bi.setRGB(i, j, p);
			}
	}

	public static void EX_applyBlueFilter(BufferedImage bi){
		int r, g, b;
		//Color color;
		int a, p;
		for(int i=0; i < bi.getWidth(); i++)
			for(int j=0; j < bi.getHeight(); j++){
				p = bi.getRGB(i,j);

				a = (p>>24)&0xff;
				r = (p>>16)&0xff;
				g = (p>>8)&0xff;
				b = p&0xff;
				if(((1.0*b*b) / (r*g)) > 1.5){
					bi.setRGB(i, j, p);
				}
				else
					bi.setRGB(i, j, 0x00ffffff);
			}
	}

	public static void applyBlueFilter(BufferedImage bi){
		int r, g, b;
		//Color color;
		int a, p;
		for(int i=0; i < bi.getWidth(); i++)
			for(int j=0; j < bi.getHeight(); j++){
				p = bi.getRGB(i,j);
				a = (p>>24)&0xff;
				r = (p>>16)&0xff;
				g = (p>>8)&0xff;
				b = p&0xff;

				float[] hsv = new float[3];
				Color.RGBtoHSB(r,g,b,hsv);

				if((hsv[0]>=0.558) && hsv[0]<=0.63 && (hsv[1]*hsv[2])>=0.25){ // BON	
				}
				else
					bi.setRGB(i, j, 0x00ffffff);
			}
	}

	public static BufferedImage applyFilter(BufferedImage bi, float filter[]) {
		return ImageTools.applyFilter(bi, filter, true);
	}

	public static BufferedImage applyFilter(BufferedImage bi, float filter[], boolean toGrayScale) {
		BufferedImage copy = ImageTools.copy(bi);
		if(toGrayScale)
			ImageTools.toGrayScale(copy);
		BufferedImage destination = null;
		ConvolveOp c = new ConvolveOp(new Kernel(3, 3, filter), ConvolveOp.EDGE_NO_OP, null);
		destination = c.filter(copy, null);
		return destination;
	}

	public static void applySobelFilter(BufferedImage bi) {
		ImageTools.toGrayScale(bi);
		BufferedImage destination = ImageTools.copy(bi);

		float data1[] = {
				-1,0,1,
				-2,0,2,
				-1,0,1,
		};

		float data2[] = {
				1,0,-1,
				2,0,-2,
				1,0,-1,
		};

		ConvolveOp c = new ConvolveOp(new Kernel(3, 3, data1), ConvolveOp.EDGE_NO_OP, null);
		c.filter(destination, bi);
		new ConvolveOp(new Kernel(3, 3, data2), ConvolveOp.EDGE_NO_OP, null).filter(destination, bi);
	}

	public static void applyPrewittFilter(BufferedImage bi) {
		ImageTools.toGrayScale(bi);
		BufferedImage destination = ImageTools.copy(bi);

		float data1[] = {
				-1,0,1,
				-1,0,1,
				-1,0,1,
		};

		float data2[] = {
				1,0,-1,
				1,0,-1,
				1,0,-1,
		};

		ConvolveOp c = new ConvolveOp(new Kernel(3, 3, data1), ConvolveOp.EDGE_NO_OP, null);
		c.filter(destination, bi);
		new ConvolveOp(new Kernel(3, 3, data2), ConvolveOp.EDGE_NO_OP, null).filter(destination, bi);
	}

	public static void aA_applySobelFilter(BufferedImage bi){
		//ImageTools.toBinary(bi);
		ImageTools.toGrayScale(bi);

		int w = bi.getWidth();
		int h = bi.getHeight();

		int[][] sobel_x = new int[][] {{-1,0,1}, {-2,0,2}, {-1,0,1}};
		int[][] sobel_y = new int[][] {{-1,-2,-1}, {0,0,0}, {1,2,1}};

		int[][] edgeColors = new int[w][h];
		int gx, gy;
		double g;
		for (int i = 1; i < w - 1; i++) {
			for (int j = 1; j < h - 1; j++) {
				gx = (sobel_x[0][0] * bi.getRGB(i-1,j-1)) + (sobel_x[0][1] * bi.getRGB(i,j-1)) + (sobel_x[0][2] * bi.getRGB(i+1,j-1)) +
						(sobel_x[1][0] * bi.getRGB(i-1,j))   + (sobel_x[1][1] * bi.getRGB(i,j))   + (sobel_x[1][2] * bi.getRGB(i+1,j)) +
						(sobel_x[2][0] * bi.getRGB(i-1,j+1)) + (sobel_x[2][1] * bi.getRGB(i,j+1)) + (sobel_x[2][2] * bi.getRGB(i+1,j+1));
				//gx = Math.abs(gx)/9;

				gy = (sobel_y[0][0] * bi.getRGB(i-1,j-1)) + (sobel_y[0][1] * bi.getRGB(i,j-1)) + (sobel_y[0][2] * bi.getRGB(i+1,j-1)) +
						(sobel_y[1][0] * bi.getRGB(i-1,j))   + (sobel_y[1][1] * bi.getRGB(i,j))   + (sobel_y[1][2] * bi.getRGB(i+1,j)) +
						(sobel_y[2][0] * bi.getRGB(i-1,j+1)) + (sobel_y[2][1] * bi.getRGB(i,j+1)) + (sobel_y[2][2] * bi.getRGB(i+1,j+1));
				//gy = Math.abs(gy)/9;

				g = Math.sqrt((gx * gx) + (gy * gy));
				//edgeColors[i][j] = ImageTools.getGrayScale(g);
				if(g < 20726564.99) 
					edgeColors[i][j] = Color.black.getRGB();
				else 
					edgeColors[i][j] = Color.white.getRGB();
			}
		}

		for (int i = 1; i < w - 1; i++) {
			for (int j = 1; j < h - 1; j++) {
				int edgeColor = edgeColors[i][j];
				//edgeColor = (int)(edgeColor * scale);
				edgeColor = 0xff000000 | (edgeColor << 16) | (edgeColor << 8) | edgeColor;

				bi.setRGB(i, j, edgeColor);
			}
		}
	}

	public static void EX_applySobelFilter(BufferedImage bi){
		ImageTools.toGrayScale(bi);

		int blackRgb = Color.BLACK.getRGB();
		int whiteRgb = Color.WHITE.getRGB();

		int w = bi.getWidth();
		int h = bi.getHeight();

		int[][] edgeColors = new int[w][h];
		int maxGradient = -1;

		for (int i = 1; i < w - 1; i++) {
			for (int j = 1; j < h - 1; j++) {

				int val00 = bi.getRGB(i - 1, j - 1);
				int val01 = bi.getRGB(i - 1, j);
				int val02 = bi.getRGB(i - 1, j + 1);

				int val10 = bi.getRGB(i, j - 1);
				int val11 = bi.getRGB(i, j);
				int val12 = bi.getRGB(i, j + 1);

				int val20 = bi.getRGB(i + 1, j - 1);
				int val21 = bi.getRGB(i + 1, j);
				int val22 = bi.getRGB(i + 1, j + 1);

				int gx =  (int)(Math.abs(((-1 * val00) + (0 * val01) + (1 * val02)) 
						+ ((-2 * val10) + (0 * val11) + (2 * val12))
						+ ((-1 * val20) + (0 * val21) + (1 * val22))) / 9);

				int gy =  (int)(Math.abs(((-1 * val00) + (-2 * val01) + (-1 * val02))
						+ ((0 * val10) + (0 * val11) + (0 * val12))
						+ ((1 * val20) + (2 * val21) + (1 * val22))) / 9);

				double gval = Math.sqrt((gx * gx) + (gy * gy));
				int g = (int) gval;

				if(maxGradient < g) {
					maxGradient = g;
				}
				edgeColors[i][j] = g;
			}
		}

		double scale = 255.0 / maxGradient;
		for (int i = 1; i < w - 1; i++) {
			for (int j = 1; j < h - 1; j++) {
				int edgeColor = edgeColors[i][j];
				edgeColor = (int)(edgeColor * scale);
				edgeColor = 0xff000000 | (edgeColor << 16) | (edgeColor << 8) | edgeColor;

				bi.setRGB(i, j, edgeColor);
			}
		}
	}

	public static void toHorizontalGradient(BufferedImage bi){
		ImageTools.toGrayScale(bi);
		BufferedImage destination = ImageTools.copy(bi);

		float data1[] = {
				-2,0,2
		};

		float data2[] = {
				2,0,-2
		};

		new ConvolveOp(new Kernel(3, 1, data2), ConvolveOp.EDGE_NO_OP, null).filter(destination, bi);
		ConvolveOp c = new ConvolveOp(new Kernel(1, 3, data1), ConvolveOp.EDGE_NO_OP, null);
		c.filter(destination, bi);
	}	
	


	public BufferedImage getFilteredImage(BufferedImage image){
		return this.getFilteredImage(image, true);
	}

	public BufferedImage getFilteredImage(BufferedImage image, boolean toGrayScale){
		BufferedImage bi = ImageTools.copy(image);
		Filter filter;
		int size = this.filtersModel.getSize();
		if(size == 0)
			return null;
		for(int i=0; i < size; i++){
			filter = (Filter)this.filtersModel.getElementAt(i);
			if(filter.data != null)
				bi = ImageTools.applyFilter(bi, filter.data, toGrayScale);
		}
		return bi;
	}
	
	public BufferedImage actionPerformed() {
		this.image = null;
		System.out.println("DIOUM");
		//ImageTools.applyBlueFilter(this.imageCopy);
		BufferedImage img1 = this.getFilteredImage(this.imageCopy);
		BufferedImage img2 = this.getFilteredImage(this.imageCopy);
		BufferedImage img3 = this.getFilteredImage(this.imageCopy);
		
		if(img1 != null || img2 != null || img3 != null){
			BufferedImage img4 = ImageTools.merge(img1, img2);
			this.image = ImageTools.merge(img4, img3, true);			
		}	
		return image;
	}
}
