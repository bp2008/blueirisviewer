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

public class CameraLayoutWnd extends UIElement
{
	public CameraLayoutWnd(Skin skin)
	{
		super(skin);
	}

	@Override
	public void onCreate(final Skin skin, final Window window, final Table table)
	{
		table.columnDefaults(0).align(Align.right).padRight(10);
		table.columnDefaults(1).align(Align.left);
		table.pad(10, 10, 10, 10);

		window.setTitle("Camera Layout");

		final CheckBox cbGridSizeOverride = new CheckBox("  Grid Size Override", skin);
		cbGridSizeOverride.setChecked(BlueIrisViewer.bivSettings.bOverrideGridLayout);
		table.add(cbGridSizeOverride).colspan(2).padBottom(10).align(Align.center);
		table.row();

		final Label lblGridX = new Label("Grid Width:", skin);
		table.add(lblGridX).align(Align.right).padRight(5);

		final TextField txtGridX = new TextField(String.valueOf(BlueIrisViewer.bivSettings.overrideGridLayoutX), skin);
		txtGridX.setTextFieldListener(new TextField.TextFieldListener()
		{
			@Override
			public void keyTyped(TextField textField, char key)
			{
				int val = Utilities.ParseInt(textField.getText(), -1);
				if (val < 0)
				{
					textField.setColor(1, 0, 0, 1);
					BlueIrisViewer.bivSettings.overrideGridLayoutX = 0;
				}
				else
				{
					textField.setColor(1, 1, 1, 1);
					BlueIrisViewer.bivSettings.overrideGridLayoutX = val;
				}
				BlueIrisViewer.images.GridSettingsChanged();
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(txtGridX).align(Align.left).width(50);
		table.row();

		final Label lblGridY = new Label("Grid Height:", skin);
		table.add(lblGridY).align(Align.right).padRight(5);

		final TextField txtGridY = new TextField(String.valueOf(BlueIrisViewer.bivSettings.overrideGridLayoutY), skin);
		txtGridY.setTextFieldListener(new TextField.TextFieldListener()
		{
			@Override
			public void keyTyped(TextField textField, char key)
			{
				int val = Utilities.ParseInt(textField.getText(), -1);
				if (val < 0)
				{
					textField.setColor(1, 0, 0, 1);
					BlueIrisViewer.bivSettings.overrideGridLayoutY = 0;
				}
				else
				{
					textField.setColor(1, 1, 1, 1);
					BlueIrisViewer.bivSettings.overrideGridLayoutY = val;
				}
				BlueIrisViewer.images.GridSettingsChanged();
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(txtGridY).align(Align.left).width(50);
		table.row();

		if (!BlueIrisViewer.bivSettings.bOverrideGridLayout)
		{
			lblGridX.setColor(0.5f, 0.5f, 0.5f, 1);
			lblGridY.setColor(0.5f, 0.5f, 0.5f, 1);
		}
		txtGridX.setDisabled(!BlueIrisViewer.bivSettings.bOverrideGridLayout);
		txtGridY.setDisabled(!BlueIrisViewer.bivSettings.bOverrideGridLayout);
		cbGridSizeOverride.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				BlueIrisViewer.bivSettings.bOverrideGridLayout = cbGridSizeOverride.isChecked();
				txtGridX.setDisabled(!BlueIrisViewer.bivSettings.bOverrideGridLayout);
				txtGridY.setDisabled(!BlueIrisViewer.bivSettings.bOverrideGridLayout);
				if (!BlueIrisViewer.bivSettings.bOverrideGridLayout)
				{
					lblGridX.setColor(0.5f, 0.5f, 0.5f, 1);
					lblGridY.setColor(0.5f, 0.5f, 0.5f, 1);
				}
				else
				{
					lblGridX.setColor(1, 1, 1, 1);
					lblGridY.setColor(1, 1, 1, 1);
				}
				BlueIrisViewer.bivSettings.Save();
				BlueIrisViewer.images.GridSettingsChanged();
			}
		});

		table.add().height(10);
		table.row();

		table.add(new Label("Image Fill Mode:", skin));

		final SelectBox sbImageFillMode = new SelectBox(new Object[] { "Preserve Aspect Ratio", "Stretch to Fill" },
				skin);
		sbImageFillMode.setSelection(BlueIrisViewer.bivSettings.imageFillMode);
		sbImageFillMode.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				BlueIrisViewer.bivSettings.imageFillMode = sbImageFillMode.getSelectionIndex();
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(sbImageFillMode);
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
		table.add(btnClose).colspan(2).align(Align.center);
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
