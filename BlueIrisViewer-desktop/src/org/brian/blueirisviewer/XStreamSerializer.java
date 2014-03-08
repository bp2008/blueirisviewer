package org.brian.blueirisviewer;

import com.thoughtworks.xstream.XStream;

public class XStreamSerializer implements org.brian.blueirisviewer.util.ObjectSerializer
{
	XStream xstream = new XStream();
	
	@Override
	public String serialize(Object object)
	{
		return xstream.toXML(object);
	}

	@Override
	public void deserialize(String xml, Object root)
	{
		xstream.fromXML(xml, root);
	}
}
