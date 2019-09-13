package application.controllers;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import application.processing.EdgeDetection;
import application.processing.ImageTools;
import application.processing.ProcessingImage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

public class IndexController implements Initializable  {

	public static List<String> filtres = new ArrayList<String>();
	
    @FXML
    private AnchorPane main;

    @FXML
    private ImageView im_1;

    @FXML
    private ImageView im_2;

    @FXML
    private Button choixImage;

    @FXML
    private Button transformImage;	

    @FXML
    private Button makeFilter;

    @FXML
    private ImageView img3;
    
    // My variables
    File selectedFile = null;
    String nomImageNB = null;
    
    @FXML
    private ComboBox<String> selectFiltre; 
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
        selectFiltre.getItems().addAll("Horizontal Filter", "Vertical Filter", "Sobel Vertical Filter", 
        		"Sobel Horizontal Filter", "Scharr Vertical Filter", "Scharr Horizontal Filter"); 
	}
    
    // Object de detection 
    EdgeDetection edges = new EdgeDetection();
    
    @FXML
    void ChoixImageAction(ActionEvent event) 
    {
    	FileChooser fc = new FileChooser();
    	selectedFile = fc.showOpenDialog(main.getScene().getWindow());
    	System.out.println(selectedFile.toURI().toString());
    	if(selectedFile != null)
    	{
    		im_1.setImage(new Image(selectedFile.toURI().toString()));
    	}
    	else {
			System.out.println("Probleme");
		}
    }
    
    @FXML
    void TransformImage(ActionEvent event) throws IOException
    {
    	BufferedImage image1 = ImageIO.read(selectedFile);
    	System.out.println(selectedFile.toURI().toString() + "\nHeight: " + image1.getHeight() + "\nWidth: " + image1.getWidth());
    	
    	// Processing of my image
    	BufferedImage image = ProcessingImage.GrayScale(selectedFile);
    	//BufferedImage convolveImg = ConvolutionImage(image);
		/* edges.detectEdges(bufferedImage, selectedFilter) */
    	//Convolver s
    	if(image != null)
    	{
    		System.out.println("Resultat: "+"\nHeight: " + image.getHeight() + "\nWidth: " + image.getWidth());
    		nomImageNB = "D:\\Memoire\\Output_"+ (int)Math.floor(Math.random() * 62)+".jpg";
    		    try {
    		      ImageIO.write(image, "jpg", new File(nomImageNB));
    	    		im_2.setImage(new Image(new File(nomImageNB).toURI().toString()));
    		    } catch (IOException e) {
    				System.out.println("Probleme");
    		    }
    	}
    }

    @FXML
    void LancerFiltre(ActionEvent event) {
    	String fltres = selectFiltre.getSelectionModel().getSelectedItem();
    	BufferedImage bufferedImage = null;
    	System.out.println(nomImageNB);
		try {
			bufferedImage = ImageIO.read(new File(nomImageNB));
			System.out.println(bufferedImage.getHeight());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			File convolvedFile = edges.detectEdges(bufferedImage, fltres);
		      ImageIO.write(ImageIO.read(convolvedFile), "jpg", convolvedFile);
			img3.setImage(new Image(convolvedFile.toURI().toString()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public BufferedImage ConvolutionImage(BufferedImage img)
    {
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
		
    	Kernel kernel = new Kernel(3, 3, data1);
    	ConvolveOp convolveOp = new ConvolveOp(kernel);
		BufferedImage destination = ImageTools.copy(img);
    	return convolveOp.filter(img, destination);
    }
    
	/*
	 * @FXML void EdgesDetection(ActionEvent event) {
	 * edges.detectEdges(bufferedImage, selectedFilter) }
	 */
}
