package org.brian.blueirisviewer.util;

public interface ObjectSerializer
{
	String serialize(Object object);
	void deserialize(String xml, Object root);
}
