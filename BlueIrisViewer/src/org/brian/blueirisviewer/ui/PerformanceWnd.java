package org.brian.blueirisviewer.ui;

import org.brian.blueirisviewer.BlueIrisViewer;
import org.brian.blueirisviewer.util.OSDetection;
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

public class PerformanceWnd extends UIElement
{
	WhatIsResolutionModeWnd whatIsResolutionModeWnd;

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

		TextField txtImageRefreshDelayMS = new TextField(
				String.valueOf(BlueIrisViewer.bivSettings.imageRefreshDelayMS), skin);
		txtImageRefreshDelayMS.setTextFieldListener(new TextField.TextFieldListener()
		{
			@Override
			public void keyTyped(TextField textField, char key)
			{
				int val = Utilities.ParseInt(textField.getText(), -1);
				if (val < 0)
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

		final SelectBox<String> sbImageResolutionMode = new SelectBox<String>(skin);
		sbImageResolutionMode.setItems(new String[] { "High Efficiency", "Balanced", "High Quality", "Maximum Quality",
				"No Optimizations" });
		sbImageResolutionMode.setSelectedIndex(BlueIrisViewer.bivSettings.imageResolutionMode);
		sbImageResolutionMode.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				BlueIrisViewer.bivSettings.imageResolutionMode = sbImageResolutionMode.getSelectedIndex();
				BlueIrisViewer.bivSettings.Save();
			}
		});
		sbImageResolutionMode.setDisabled(BlueIrisViewer.bivSettings.useMjpegStream);
		if (BlueIrisViewer.bivSettings.useMjpegStream)
			sbImageResolutionMode.setColor(0.5f, 0.5f, 0.5f, 1);
		else
			sbImageResolutionMode.setColor(1, 1, 1, 1);
		table.add(sbImageResolutionMode).colspan(4).align(Align.center);
		table.row();

		table.add().height(10);
		table.row();

		whatIsResolutionModeWnd = new WhatIsResolutionModeWnd(skin);
		final TextButton btnWhatIsResolutionMode = new TextButton("^ What is this? ^", skin);
		btnWhatIsResolutionMode.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				if (whatIsResolutionModeWnd.isShowing())
					whatIsResolutionModeWnd.hide();
				else
					whatIsResolutionModeWnd.show();
			}
		});
		table.add(btnWhatIsResolutionMode).colspan(4).align(Align.center);
		table.row();

		table.add().height(10);
		table.row();

		final CheckBox cbOverrideDefaultCompression = new CheckBox(
				"  Override Blue Iris' Jpeg Quality\n  setting with the value below", skin);
		cbOverrideDefaultCompression.setChecked(BlueIrisViewer.bivSettings.overrideJpegQuality);
		table.add(cbOverrideDefaultCompression).colspan(2).padBottom(10).align(Align.center);
		table.row();

		table.add().height(10);
		table.row();

		final Label lblJpegCompressionQuality = new Label("Jpeg Quality\n(1-100):", skin);
		lblJpegCompressionQuality.setAlignment(Align.right);
		table.add(lblJpegCompressionQuality).align(Align.right);

		final TextField txtJpegCompressionQuality = new TextField(
				String.valueOf(BlueIrisViewer.bivSettings.jpegQuality), skin);
		txtJpegCompressionQuality.setDisabled(!BlueIrisViewer.bivSettings.overrideJpegQuality);
		if (!BlueIrisViewer.bivSettings.overrideJpegQuality)
			lblJpegCompressionQuality.setColor(0.5f, 0.5f, 0.5f, 1);
		txtJpegCompressionQuality.setTextFieldListener(new TextField.TextFieldListener()
		{
			@Override
			public void keyTyped(TextField textField, char key)
			{
				int val = Utilities.ParseInt(textField.getText(), -1);
				if (val < 1 || val > 100)
				{
					textField.setColor(1, 0, 0, 1);
					BlueIrisViewer.bivSettings.jpegQuality = 60;
				}
				else
				{
					textField.setColor(1, 1, 1, 1);
					BlueIrisViewer.bivSettings.jpegQuality = val;
				}
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(txtJpegCompressionQuality).align(Align.left);
		table.row();

		cbOverrideDefaultCompression.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				BlueIrisViewer.bivSettings.overrideJpegQuality = cbOverrideDefaultCompression.isChecked();
				txtJpegCompressionQuality.setDisabled(!BlueIrisViewer.bivSettings.overrideJpegQuality);
				if (!BlueIrisViewer.bivSettings.overrideJpegQuality)
					lblJpegCompressionQuality.setColor(0.5f, 0.5f, 0.5f, 1);
				else
					lblJpegCompressionQuality.setColor(1, 1, 1, 1);
				BlueIrisViewer.bivSettings.Save();
			}
		});

		table.add().height(10);
		table.row();

		final CheckBox cbUseMjpeg = new CheckBox(
				"  Use MJPEG video\n   - Images load faster\n   - Increases CPU usage\n   - Disables image resolution modes",
				skin);
		cbUseMjpeg.setChecked(BlueIrisViewer.bivSettings.useMjpegStream);
		table.add(cbUseMjpeg).colspan(2).padBottom(10).align(Align.center);
		table.row();

		table.add().height(10);
		table.row();
		
		cbUseMjpeg.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				BlueIrisViewer.bivSettings.useMjpegStream = cbUseMjpeg.isChecked();
				BlueIrisViewer.bivSettings.Save();
				sbImageResolutionMode.setDisabled(BlueIrisViewer.bivSettings.useMjpegStream);
				if (BlueIrisViewer.bivSettings.useMjpegStream)
					sbImageResolutionMode.setColor(0.5f, 0.5f, 0.5f, 1);
				else
					sbImageResolutionMode.setColor(1, 1, 1, 1);
			}
		});
		
		if (OSDetection.isWindows())
		{
			final CheckBox cbUseLibjpegTurbo = new CheckBox(
					"  Use libjpeg-turbo for image decompression\n   - Reduces CPU usage\n   - Increases memory usage",
					skin);
			cbUseLibjpegTurbo.setChecked(BlueIrisViewer.bivSettings.useLibjpegTurbo);
			table.add(cbUseLibjpegTurbo).colspan(2).padBottom(10).align(Align.center);
			table.row();

			table.add().height(10);
			table.row();
			
			cbUseLibjpegTurbo.addListener(new ChangeListener()
			{
				@Override
				public void changed(ChangeEvent event, Actor actor)
				{
					BlueIrisViewer.bivSettings.useLibjpegTurbo = cbUseLibjpegTurbo.isChecked();
					BlueIrisViewer.bivSettings.Save();
				}
			});
		}

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
		table.add(btnClose).colspan(4).align(Align.right);
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
