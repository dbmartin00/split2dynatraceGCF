package io.split.dbm.integrations.split2dynatrace;

public class Environment {

	public String dynatraceTag;
	public String splitEnvironment;
	
	public Environment(String dynatraceTag, String splitEnvironment) {
		super();
		this.dynatraceTag = dynatraceTag;
		this.splitEnvironment = splitEnvironment;
	}

	public String getDynatraceTag() {
		return dynatraceTag;
	}

	public void setDynatraceTag(String dynatraceTag) {
		this.dynatraceTag = dynatraceTag;
	}

	public String getSplitEnvironment() {
		return splitEnvironment;
	}

	public void setSplitEnvironment(String splitEnvironment) {
		this.splitEnvironment = splitEnvironment;
	}
	
	
}
