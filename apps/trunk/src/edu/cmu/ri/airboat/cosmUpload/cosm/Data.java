package edu.cmu.ri.airboat.cosmUpload.cosm;

public class Data {

	private String id;

	private String tag;

	private double value;

	private Double minValue;

	private Double maxValue;


	public Data(String id, String tag, double value, Double minValue,
			Double maxValue) {
		super();
		this.id = id;
		this.tag = tag;
		this.value = value;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	public Data() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setId(int id) {
		this.id = Integer.toString(id);
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public double getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = Double.parseDouble(value);
	}

	public void setValue(double value) {
		this.value = value;
	}

	public Double getMinValue() {
		return minValue;
	}

	public void setMinValue(Double minValue) {
		if (minValue != null) {
			this.minValue = minValue;
		}
	}

	public void setMinValue(String minValue) {
		if (minValue != null) {
			this.minValue = Double.parseDouble(minValue);
		}
	}

	public Double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Double maxValue) {
		if (maxValue != null) {
			this.maxValue = maxValue;
		}
	}

	public void setMaxValue(String maxValue) {
		if (minValue != null) {
			this.maxValue = Double.parseDouble(maxValue);
		}
	}


	public String toXMLWithWrapper(){
		String ret = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<eeml xmlns=\"http://www.eeml.org/xsd/0.5.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"0.5.1\" xsi:schemaLocation=\"http://www.eeml.org/xsd/0.5.1 http://www.eeml.org/xsd/0.5.1/0.5.1.xsd\">\n\t<environment>\n\t\t";
		ret = ret + this.toXML() + "\n\t</environment>\n</eeml>";
		return ret;
	}

	public String toXML() {
		String ret = "";
		ret = "<data id=\"" + this.id + "\">\n\t\t\t";

		if( this.tag != null && !this.tag.equals("")) {
			ret = ret + "<tag>" + this.tag + "</tag>\n\t\t\t";
		}

		ret = ret + "<current_value>"+ this.value +"</current_value>\n\t\t\t";

		if(this.maxValue != null){
			ret = ret + "<max_value>" + this.maxValue + "</max_value>\n\t\t\t";
		} 

		if (this.minValue != null) {
			ret = ret + "<min_value>" + this.minValue + "</min_value>\n\t\t";
		}




		ret = ret + "</data>";

		return ret;
	}

	@Override
	public String toString() {
		return "Data [id=" + id + ", maxValue=" + maxValue + ", minValue="
				+ minValue + ", tag=" + tag + ", value=" + value + "]";
	}

}
