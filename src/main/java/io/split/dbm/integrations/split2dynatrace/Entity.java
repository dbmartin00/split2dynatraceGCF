package io.split.dbm.integrations.split2dynatrace;

public class Entity {

	@Override
	public String toString() {
		return "Entity [displayName=" + displayName + ", entityId=" + entityId + "]";
	}
	
	public String displayName;
	public String entityId;
	public Tag[] tags;
	
}
