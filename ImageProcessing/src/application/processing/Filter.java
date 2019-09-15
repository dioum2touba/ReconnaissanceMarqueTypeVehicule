package application.processing;

import javax.swing.JTextArea;

public class Filter {
	
	public float[] data;

	protected Filter(float[] data){
		this.data = data;
	}

	public String toString(){
		return data[0] + "\t" + data[1] + "\t" + data[2] + "\n" 
			+ data[3] + "\t" + data[4] + "\t" + data[5] + "\n" 
			+ data[6] + "\t" + data[7] + "\t" + data[8];
	}

	public static Filter getFilter(JTextArea jta) {
		String s = jta.getText();
		Filter filter = Filter.getFilter(s);
		if(filter != null)
			jta.setText(filter.toString());
		return filter;
	}

	public static Filter getFilter(String s) {
		System.out.println(" -- Filter = " + s);
		while(s.contains("  "))
			s = s.replace("  ", " ");
		while(s.contains("\t\t"))
			s = s.replace("\t\t", "\t");
		s= s.replace(" ", "\t").replace(";", "\t").replace(",", "\t");

		if(s == null || s.length() < 17){
			return null;
		}

		String[] p = s.split("\n");
		if(p.length < 3){
			return null;
		}

		for(int i=0; i < 3; i++){
			while(p[i].startsWith("\t") || p[i].startsWith(" "))
				p[i] = p[i].substring(1);
		}

		String[] p2;
		int pos=0;
		String currentVal = "";
		float[] data = new float[9];

		for(int i=0; i < 3; i++){
			p2 = p[i].split("\t");
			if(p2.length < 3){
				return null; 
			}
			for(int j=0; j < 3; j++){
				currentVal = p2[j];
				data[pos++] = Float.parseFloat(currentVal);
			}
		}

		System.out.println(" -- Filter[9] : [ " 
				+ data[0] + " , " + data[1] + " , " + data[2] 
						+ "\n" + data[3] + " , " + data[4] + " , " + data[5] 
								+ "\n" + data[6] + " , " + data[7] + " , " + data[8] 
										+ " ]");

		return new Filter(data);
	}
}