package org.brian.blueirisviewer.util;

import java.io.File;

public abstract class SerializableObjectBase
{
	private static ObjectSerializer objectSerializer;

	public static void SetSerializer(ObjectSerializer objectSerializer)
	{
		SerializableObjectBase.objectSerializer = objectSerializer;
	}

	public boolean Save()
	{
		return Save(null);
	}

	public boolean Save(String filePath)
	{
		try
		{
			synchronized (this)
			{
				if (filePath == null)
					filePath = this.getClass().getSimpleName() + ".cfg";

				if (objectSerializer != null)
				{
					String xml = objectSerializer.serialize(this);
					Utilities.WriteTextFile(filePath, xml);
				}

				// XMLEncoder encoder = new XMLEncoder(new FileOutputStream(filePath));
				// encoder.writeObject(this);
				// encoder.close();
			}
			return true;
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
		return false;
	}

	public boolean Load()
	{
		return Load(null);
	}

	public boolean Load(String filePath)
	{
		try
		{
			if (filePath == null)
				filePath = this.getClass().getSimpleName() + ".cfg";
			synchronized (this)
			{
				if (!new File(filePath).exists())
					return false;

				if (objectSerializer != null)
				{
					String xml = Utilities.ReadTextFile(filePath);
					objectSerializer.deserialize(xml, this);
				}

				// XMLDecoder decoder = new XMLDecoder(new FileInputStream(filePath));
				// Object obj = decoder.readObject();
				// decoder.close();
				// for (Field sourceField : obj.getClass().getFields())
				// {
				// try
				// {
				// Field targetField = thisclass.getField(sourceField.getName());
				// if (targetField != null && targetField.getType() == sourceField.getType())
				// targetField.set(this, sourceField.get(obj));
				// }
				// catch (Exception ex) { }
				// }
			}
			return true;
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
		return false;
	}
}
