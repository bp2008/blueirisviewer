package org.brian.blueirisviewer.ui;

import org.brian.blueirisviewer.BlueIrisViewer;
import org.brian.blueirisviewer.util.Utilities;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class PerformanceWnd extends UIElement
{
	public PerformanceWnd(Skin skin)
	{
		super(skin);
	}

	@Override
	public void onCreate(final Skin skin, final Window window, final Table table)
	{
		table.columnDefaults(0).align(Align.right).padRight(10);
		table.columnDefaults(1).align(Align.left);
		table.pad(10, 10, 10, 10);

		window.setTitle("Performance");
		
		Label lblRefreshTime = new Label("Milliseconds between\nimage refreshes:", skin);
		lblRefreshTime.setAlignment(Align.right);
		table.add(lblRefreshTime).align(Align.right);
		
		TextField txtImageRefreshDelayMS = new TextField(String.valueOf(BlueIrisViewer.bivSettings.imageRefreshDelayMS), skin);
		txtImageRefreshDelayMS.setTextFieldListener(new TextField.TextFieldListener()
		{
			@Override
			public void keyTyped(TextField textField, char key)
			{
				int val = Utilities.ParseInt(textField.getText(), -1);
				if(val < 0)
				{
					textField.setColor(1, 0, 0, 1);
					BlueIrisViewer.bivSettings.imageRefreshDelayMS = 250;
				}
				else
				{
					textField.setColor(1, 1, 1, 1);
					BlueIrisViewer.bivSettings.imageRefreshDelayMS = val;
				}
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(txtImageRefreshDelayMS).align(Align.left);
		table.row();

		table.add().height(10);
		table.row();
		
		final TextButton btnClose = new TextButton("Close", skin);
		btnClose.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				hide();
				BlueIrisViewer.images.Initialize();
			}
		});
		table.add(btnClose).colspan(4);
		table.row();
	}

	@Override
	public void onUpdate(final Window window, final Table table)
	{
	}

	@Override
	public void onDestroy()
	{
	}
}
