package application.processing;

import boofcv.abst.denoise.FactoryImageDenoise;
import boofcv.abst.denoise.WaveletDenoiseFilter;
import boofcv.alg.misc.GImageMiscOps;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.UtilIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayF32;

import java.util.Random;

/**
 * Example of how to "remove" noise from images using wavelet based algorithms.  A simplified interface is used
 * which hides most of the complexity.  Wavelet image processing is still under development and only floating point
 * images are currently supported.  Which is why the image  type is hard coded.
 */
public class ExampleWaveletDenoise {

	public static void main( String args[] ) {

		// charge l'image d'entrée, déclare les structures de données, crée une image bruyante 
		Random rand = new Random(234);
		GrayF32 input = UtilImageIO.loadImage(UtilIO.pathExample("C:\\Users\\Sokhna Amy Diop\\Documents\\Projects\\RAPI_Segmentation\\Photo\\taxi.jpg"),GrayF32.class);

		GrayF32 noisy = input.clone();
		GImageMiscOps.addGaussian(noisy, rand, 20, 0, 255);
		GrayF32 denoised = noisy.createSameShape();

		// Combien de niveaux dans Wavelet Transform 
		int numLevels = 4;
		// Création de l'algorithme de suppression de bruit 
		WaveletDenoiseFilter<GrayF32> denoiser =
				FactoryImageDenoise.waveletBayes(GrayF32.class,numLevels,0,255);

		// supprime le bruit du  image
		denoiser.process(noisy,denoised);

		// affiche les résultats 
		ListDisplayPanel gui = new ListDisplayPanel();
		gui.addImage(ConvertBufferedImage.convertTo(input,null),"Input");
		gui.addImage(ConvertBufferedImage.convertTo(noisy,null),"Noisy");
		gui.addImage(ConvertBufferedImage.convertTo(denoised,null),"Denoised");

		ShowImages.showWindow(gui,"Wavelet Noise Removal Example",true);
	}
}