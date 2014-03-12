package org.brian.blueirisviewer.ui;

import java.text.DecimalFormat;

import org.brian.blueirisviewer.BlueIrisViewer;
import org.brian.blueirisviewer.images.Images;
import org.brian.blueirisviewer.util.Utilities;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class InstantReplayWnd extends UIElement
{
	Label lblInstantReplayEstimatedCacheSize;

	DecimalFormat twoPlacePrecision;

	public InstantReplayWnd(Skin skin)
	{
		super(skin);
	}

	@Override
	public void onCreate(final Skin skin, final Window window, final Table table)
	{
		twoPlacePrecision = new DecimalFormat("#.##");

		table.columnDefaults(0).align(Align.right).padRight(10);
		table.columnDefaults(1).align(Align.left);
		table.pad(10, 10, 10, 10);

		window.setTitle("Instant Replay Options");

		final CheckBox cbInstantReplayEnabled = new CheckBox("  Enable Instant Replay", skin);
		cbInstantReplayEnabled.setChecked(BlueIrisViewer.bivSettings.instantReplayEnabled);
		cbInstantReplayEnabled.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				BlueIrisViewer.bivSettings.instantReplayEnabled = cbInstantReplayEnabled.isChecked();
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(cbInstantReplayEnabled).colspan(2).padBottom(10).align(Align.center);
		table.row();

		table.add().height(10);
		table.row();

		final Label lblInstantReplayDiskSizeMB = new Label("History Length in Minutes:", skin);
		table.add(lblInstantReplayDiskSizeMB).align(Align.right).padRight(5);

		final TextField txtInstantReplayHistoryLengthMinutes = new TextField(
				String.valueOf(BlueIrisViewer.bivSettings.instantReplayHistoryLengthMinutes), skin);
		txtInstantReplayHistoryLengthMinutes.setTextFieldListener(new TextField.TextFieldListener()
		{
			@Override
			public void keyTyped(TextField textField, char key)
			{
				int val = Utilities.ParseInt(textField.getText(), -1);
				if (val < 1 || val > 1440)
				{
					textField.setColor(1, 0, 0, 1);
					BlueIrisViewer.bivSettings.instantReplayHistoryLengthMinutes = 5;
				}
				else
				{
					textField.setColor(1, 1, 1, 1);
					BlueIrisViewer.bivSettings.instantReplayHistoryLengthMinutes = val;
				}
				BlueIrisViewer.images.GridSettingsChanged();
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(txtInstantReplayHistoryLengthMinutes).align(Align.left);
		table.row();

		table.add().height(10);
		table.row();

		lblInstantReplayEstimatedCacheSize = new Label("Estimated cache size\nwith current settings:\n"
				+ EstimateCacheSize(), skin);
		table.add(lblInstantReplayEstimatedCacheSize).colspan(2).align(Align.center);

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

	private String EstimateCacheSize()
	{
		return twoPlacePrecision
				.format((Utilities.getCurrentBytesPer3Seconds() * 20 * BlueIrisViewer.bivSettings.instantReplayHistoryLengthMinutes) / 1000000.0)
				+ " MB";
	}

	@Override
	public void onUpdate(final Window window, final Table table)
	{
		lblInstantReplayEstimatedCacheSize.setText("Estimated cache size with current levels\nof network usage: "
				+ EstimateCacheSize());
	}

	@Override
	public void onDestroy()
	{
	}
}
