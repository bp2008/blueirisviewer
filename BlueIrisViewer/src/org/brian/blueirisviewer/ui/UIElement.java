package org.brian.blueirisviewer.ui;

import org.brian.blueirisviewer.BlueIrisViewer;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public abstract class UIElement
{
	private Table parent;
	private Window window;	
	private Table table;
	
	public UIElement(Skin skin)
	{
		UI.uiElements.add(this);
		parent = new Table();
		parent.setFillParent(true);
		window = new Window("Unnamed Window", skin);
		window.setModal(BlueIrisViewer.bivSettings.modalUI);
		table = new Table(skin);
		window.add(table);
		
		hide();
		
		parent.add(window);
		onCreate(skin, window, table);
	}

	public void show()
	{
		if (parent != null && !parent.hasParent())
			UI.root.add(parent);
	}

	public void hide()
	{
		if (parent != null)
			parent.remove();
	}
	
	public boolean isShowing()
	{
		return parent != null && parent.hasParent();
	}

	public void onUpdate()
	{
		onUpdate(window, table);
	}
	
	public void setModal(boolean isModal)
	{
		window.setModal(isModal);
	}
	
	public abstract void onCreate(final Skin skin, final Window window, final Table table);

	protected abstract void onUpdate(final Window window, final Table table);

	public abstract void onDestroy();
}
