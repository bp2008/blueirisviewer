package org.brian.blueirisviewer.ui;

import org.brian.blueirisviewer.BlueIrisViewer;
import org.brian.blueirisviewer.util.Utilities;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class FreezeDetectionWnd extends UIElement
{
	public FreezeDetectionWnd(Skin skin)
	{
		super(skin);
	}

	@Override
	protected void onCreate(Skin skin, Window window, Table table)
	{
		table.columnDefaults(0).align(Align.right).padRight(10);
		table.columnDefaults(1).align(Align.left);
		table.pad(10, 10, 10, 10);

		window.setTitle("Freeze Detection Options");

		table.add(new Label("Animation:", skin)).align(Align.right).padRight(5);
		
		final SelectBox<String> sbPerimeterAnimation = new SelectBox<String>(skin);
		sbPerimeterAnimation.setItems(new String[] { "None", "Across the Top", "Around the Perimeter" });
		sbPerimeterAnimation.setSelectedIndex(BlueIrisViewer.bivSettings.freezeDetectionAnimation);
		sbPerimeterAnimation.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				BlueIrisViewer.bivSettings.freezeDetectionAnimation = sbPerimeterAnimation.getSelectedIndex();
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(sbPerimeterAnimation);
		table.row();

		table.add().height(10);
		table.row();

		table.add(new Label("Speed (1-1000):", skin)).align(Align.right).padRight(5);

		final TextField txtPerimeterAnimationSpeed = new TextField(String.valueOf(BlueIrisViewer.bivSettings.freezeDetectionAnimationSpeed), skin);
		txtPerimeterAnimationSpeed.setTextFieldListener(new TextField.TextFieldListener()
		{
			@Override
			public void keyTyped(TextField textField, char key)
			{
				int val = Utilities.ParseInt(textField.getText(), -1);
				if (val < 1 || val > 1000)
				{
					textField.setColor(1, 0, 0, 1);
					BlueIrisViewer.bivSettings.freezeDetectionAnimationSpeed = 50;
				}
				else
				{
					textField.setColor(1, 1, 1, 1);
					BlueIrisViewer.bivSettings.freezeDetectionAnimationSpeed = val;
				}
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(txtPerimeterAnimationSpeed).align(Align.left);
		table.row();
		
		table.add(new Label("Size (1-1000):", skin)).align(Align.right).padRight(5);

		final TextField txtPerimeterAnimationSize = new TextField(String.valueOf(BlueIrisViewer.bivSettings.freezeDetectionAnimationSize), skin);
		txtPerimeterAnimationSize.setTextFieldListener(new TextField.TextFieldListener()
		{
			@Override
			public void keyTyped(TextField textField, char key)
			{
				int val = Utilities.ParseInt(textField.getText(), -1);
				if (val < 1 || val > 1000)
				{
					textField.setColor(1, 0, 0, 1);
					BlueIrisViewer.bivSettings.freezeDetectionAnimationSize = 50;
				}
				else
				{
					textField.setColor(1, 1, 1, 1);
					BlueIrisViewer.bivSettings.freezeDetectionAnimationSize = val;
				}
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(txtPerimeterAnimationSize).align(Align.left);
		table.row();

		table.add(new Label("Opacity (1-255):", skin)).align(Align.right).padRight(5);

		final TextField txtPerimeterAnimationOpacity = new TextField(String.valueOf(BlueIrisViewer.bivSettings.freezeDetectionAnimationOpacity), skin);
		txtPerimeterAnimationOpacity.setTextFieldListener(new TextField.TextFieldListener()
		{
			@Override
			public void keyTyped(TextField textField, char key)
			{
				int val = Utilities.ParseInt(textField.getText(), -1);
				if (val < 1 || val > 255)
				{
					textField.setColor(1, 0, 0, 1);
					BlueIrisViewer.bivSettings.freezeDetectionAnimationOpacity = 127;
				}
				else
				{
					textField.setColor(1, 1, 1, 1);
					BlueIrisViewer.bivSettings.freezeDetectionAnimationOpacity = val;
				}
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(txtPerimeterAnimationOpacity).align(Align.left);
		table.row();
		
		table.add(new Label("----------------------------------------------------", skin)).colspan(2).align(Align.center);
		table.row();
		

		final CheckBox cbWarnOfStalledImageLoading = new CheckBox("  Warn if video\n  is stalled", skin);
		cbWarnOfStalledImageLoading.setChecked(BlueIrisViewer.bivSettings.warnOfStalledImageLoading);
		cbWarnOfStalledImageLoading.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				BlueIrisViewer.bivSettings.warnOfStalledImageLoading = cbWarnOfStalledImageLoading.isChecked();
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(cbWarnOfStalledImageLoading).colspan(2).padBottom(10).align(Align.center);
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
		table.add(btnClose).colspan(2).align(Align.right);
		table.row();
	}

	@Override
	protected void onUpdate(Window window, Table table)
	{
	}

	@Override
	protected void onDestroy()
	{
	}
}
