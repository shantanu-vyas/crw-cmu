package edu.cmu.ri.airboat.cosmUpload.cosm;

import java.util.ArrayList;

public class DataPoints {

	private ArrayList<Double> values;
	private ArrayList<String> timeStamps;	


	public DataPoints(ArrayList<Double> values, ArrayList<String> timeStamps) {
		super();
		this.values = values;
		this.timeStamps = timeStamps;
	}

	public DataPoints() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ArrayList<Double> getValues() {
		return values;
	}

	public void setValue(ArrayList<Double> values) {
		this.values = values;
	}

	public ArrayList<String> getTimeStamp() {
		return this.timeStamps;
	}

	public void setTimeStamp(ArrayList<String> timeStamps) {
		this.timeStamps = timeStamps;
	}

	public String toXMLWithWrapper(){
		String ret = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<eeml xmlns=\"http://www.eeml.org/xsd/0.5.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"0.5.1\" xsi:schemaLocation=\"http://www.eeml.org/xsd/0.5.1 http://www.eeml.org/xsd/0.5.1/0.5.1.xsd\">\n\t<environment>\n\t\t";
		ret = ret + this.toXML() + "\n\t</environment>\n</eeml>";
		return ret;
	}

	public String toXML() {
		String ret = "";
		ret = "<data>\n\t\t\t";
		ret = ret + "<datapoints>\n";
		for(int i=0; i<timeStamps.size(); i++)
			ret = ret + "\t\t\t\t<value at=\"" + this.timeStamps.get(i) + "\">" + this.values.get(i) +"</value>\n";
		ret = ret + "\t\t\t</datapoints>\n\t\t";
		ret = ret + "</data>";

		return ret;
	}

	@Override
	public String toString() {
		String ret;
		
		ret = "Data [";
		for(int i=0; i<timeStamps.size(); i++)
			ret += ", time=" + timeStamps.get(i) + ":value=" + values.get(i);
		ret += "]";
		return ret;
	}

}
