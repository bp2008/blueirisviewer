package org.brian.blueirisviewer.ui;

import org.brian.blueirisviewer.BlueIrisViewer;
import org.brian.blueirisviewer.images.Images;
import org.brian.blueirisviewer.util.Utilities;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
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
	String[] AllCameras = null;
	List<String> listAllCams;

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

		final SelectBox<String> sbImageFillMode = new SelectBox<String>(skin);
		sbImageFillMode.setItems(new String[] { "Preserve Aspect Ratio", "Stretch to Fill" });
		sbImageFillMode.setSelectedIndex(BlueIrisViewer.bivSettings.imageFillMode);
		sbImageFillMode.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				BlueIrisViewer.bivSettings.imageFillMode = sbImageFillMode.getSelectedIndex();
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(sbImageFillMode);
		table.row();

		table.add().height(10);
		table.row();

		table.add(new Label("All Cameras", skin)).align(Align.center);
		table.add(new Label("Hidden Cameras", skin)).align(Align.center);
		table.row();

		listAllCams = new List<String>(skin);
		ScrollPane spListAllCams = new ScrollPane(listAllCams, skin);
		spListAllCams.setFadeScrollBars(false);
		spListAllCams.setScrollingDisabled(true, false);

		table.add(spListAllCams).maxHeight(200).align(Align.center);

		final List<String> listHiddenCams = new List<String>(skin);
		listHiddenCams.setItems(BlueIrisViewer.bivSettings.getHiddenCamsStringArray());
		ScrollPane spListHiddenCams = new ScrollPane(listHiddenCams, skin);
		spListHiddenCams.setFadeScrollBars(false);
		spListHiddenCams.setScrollingDisabled(true, false);

		table.add(spListHiddenCams).maxHeight(200).align(Align.center);
		table.row();

		table.add().height(7);
		table.row();

		final TextButton btnHideSelected = new TextButton("Hide Selected", skin);
		btnHideSelected.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				String selected = listAllCams.getSelected();
				if (selected != null && !BlueIrisViewer.bivSettings.hiddenCams.contains(selected))
				{
					BlueIrisViewer.bivSettings.hiddenCams.add(selected);
					BlueIrisViewer.bivSettings.Save();
					listHiddenCams.setItems(BlueIrisViewer.bivSettings.getHiddenCamsStringArray());
				}
			}
		});

		table.add(btnHideSelected).align(Align.center);

		final TextButton btnShowSelected = new TextButton("Show Selected", skin);
		btnShowSelected.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				String selected = listHiddenCams.getSelected();
				if (selected != null)
				{
					BlueIrisViewer.bivSettings.hiddenCams.remove(selected);
					BlueIrisViewer.bivSettings.Save();
					listHiddenCams.setItems(BlueIrisViewer.bivSettings.getHiddenCamsStringArray());
				}
			}
		});

		table.add(btnShowSelected).align(Align.center);
		table.row();

		table.add().height(10);
		table.row();

		final TextButton btnApplyChanges = new TextButton("Restart Imaging Engine to Apply Changes", skin);
		btnApplyChanges.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				if (BlueIrisViewer.images != null)
					BlueIrisViewer.images.dispose();
				BlueIrisViewer.images = new Images();
				BlueIrisViewer.images.Initialize();
			}
		});

		table.add(btnApplyChanges).colspan(2).align(Align.center);
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
	public void onUpdate(final Window window, final Table table)
	{
		if (AllCameras == null && BlueIrisViewer.images != null)
		{
			AllCameras = BlueIrisViewer.images.GetCameraNamesStringArray();
			if (AllCameras != null)
				listAllCams.setItems(AllCameras);
		}
	}

	@Override
	public void onDestroy()
	{
	}
}
