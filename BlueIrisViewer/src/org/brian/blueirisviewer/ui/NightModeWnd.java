package org.brian.blueirisviewer.ui;

import org.brian.blueirisviewer.BlueIrisViewer;
import org.brian.blueirisviewer.util.NightModeManager;
import org.brian.blueirisviewer.util.ScreenBrightness;
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

public class NightModeWnd extends UIElement
{
	WhatIsPixelManipulationWnd whatIsPixelManipulationWnd;

	public NightModeWnd(Skin skin)
	{
		super(skin);
	}

	@Override
	public void onCreate(final Skin skin, final Window window, final Table table)
	{
		table.columnDefaults(0).align(Align.right).padRight(10);
		table.columnDefaults(1).align(Align.left);
		table.pad(10, 10, 10, 10);

		window.setTitle("Night Mode Options");

		final CheckBox cbNightModeEnabled = new CheckBox("  Enable Night Mode during\n  time specified below", skin);
		cbNightModeEnabled.setChecked(BlueIrisViewer.bivSettings.nightModeEnabled);
		cbNightModeEnabled.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				BlueIrisViewer.bivSettings.nightModeEnabled = cbNightModeEnabled.isChecked();
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(cbNightModeEnabled).colspan(2).padBottom(10).align(Align.center);
		table.row();

		table.add().height(10);
		table.row();

		final Label lblNightModeStart = new Label("Night Mode Begins at:", skin);
		table.add(lblNightModeStart).align(Align.right).padRight(5);

		final TextField txtNightModeStart = new TextField(
				String.valueOf(BlueIrisViewer.bivSettings.nightModeStartTime), skin);
		txtNightModeStart.setTextFieldListener(new TextField.TextFieldListener()
		{
			@Override
			public void keyTyped(TextField textField, char key)
			{
				String val = textField.getText();
				if (!NightModeManager.isValidTimeString(val))
				{
					textField.setColor(1, 0, 0, 1);
					BlueIrisViewer.bivSettings.nightModeStartTime = "20:00";
				}
				else
				{
					textField.setColor(1, 1, 1, 1);
					BlueIrisViewer.bivSettings.nightModeStartTime = val;
				}
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(txtNightModeStart).align(Align.left);
		table.row();

		table.add().height(10);
		table.row();

		final Label lblNightModeEnd = new Label("Night Mode Ends at:", skin);
		table.add(lblNightModeEnd).align(Align.right).padRight(5);

		final TextField txtNightModeEnd = new TextField(String.valueOf(BlueIrisViewer.bivSettings.nightModeEndTime),
				skin);
		txtNightModeEnd.setTextFieldListener(new TextField.TextFieldListener()
		{
			@Override
			public void keyTyped(TextField textField, char key)
			{
				String val = textField.getText();
				if (!NightModeManager.isValidTimeString(val))
				{
					textField.setColor(1, 0, 0, 1);
					BlueIrisViewer.bivSettings.nightModeEndTime = "8:00";
				}
				else
				{
					textField.setColor(1, 1, 1, 1);
					BlueIrisViewer.bivSettings.nightModeEndTime = val;
				}
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(txtNightModeEnd).align(Align.left);
		table.row();

		table.add().height(10);
		table.row();

		Label lblDateHints1 = new Label("Use 24-hour time, providing hour and minute.", skin);
		table.add(lblDateHints1).colspan(2).align(Align.center);
		table.row();

		Label lblDateHints2 = new Label("Example: For 8:35 PM, enter 20:35", skin);
		table.add(lblDateHints2).colspan(2).align(Align.center);
		table.row();

		table.add(new Label("----------------------------------------------------", skin)).colspan(2)
				.align(Align.center);
		table.row();

		if (BlueIrisViewer.sScreenBrightnessProgramPath != null)
		{
			final CheckBox cbDayModeBrightness = new CheckBox("  Set screen brightness during day mode", skin);
			cbDayModeBrightness.setChecked(BlueIrisViewer.bivSettings.setDayModeBrightness);
			cbDayModeBrightness.addListener(new ChangeListener()
			{
				@Override
				public void changed(ChangeEvent event, Actor actor)
				{
					BlueIrisViewer.bivSettings.setDayModeBrightness = cbDayModeBrightness.isChecked();
					BlueIrisViewer.bivSettings.Save();
					SetScreenBrightness();
				}
			});
			table.add(cbDayModeBrightness).colspan(2).padBottom(10).align(Align.center);
			table.row();

			table.add(new Label("Brightness (0-100):", skin)).align(Align.right).padRight(5);

			final TextField txtDayModeBrightness = new TextField(
					String.valueOf(BlueIrisViewer.bivSettings.dayModeBrightness), skin);
			txtDayModeBrightness.setTextFieldListener(new TextField.TextFieldListener()
			{
				@Override
				public void keyTyped(TextField textField, char key)
				{
					int val = Utilities.ParseInt(textField.getText(), -1);
					if (val < 0 || val > 100)
					{
						textField.setColor(1, 0, 0, 1);
						BlueIrisViewer.bivSettings.dayModeBrightness = 100;
					}
					else
					{
						textField.setColor(1, 1, 1, 1);
						BlueIrisViewer.bivSettings.dayModeBrightness = val;
					}
					BlueIrisViewer.bivSettings.Save();
					SetScreenBrightness();
				}
			});
			table.add(txtDayModeBrightness).align(Align.left);
			table.row();

			table.add().height(10);
			table.row();

			final CheckBox cbNightModeBrightness = new CheckBox("  Set screen brightness during night mode", skin);
			cbNightModeBrightness.setChecked(BlueIrisViewer.bivSettings.setNightModeBrightness);
			cbNightModeBrightness.addListener(new ChangeListener()
			{
				@Override
				public void changed(ChangeEvent event, Actor actor)
				{
					BlueIrisViewer.bivSettings.setNightModeBrightness = cbNightModeBrightness.isChecked();
					BlueIrisViewer.bivSettings.Save();
					SetScreenBrightness();
				}
			});
			table.add(cbNightModeBrightness).colspan(2).padBottom(10).align(Align.center);
			table.row();

			table.add(new Label("Brightness (0-100):", skin)).align(Align.right).padRight(5);

			final TextField txtNightModeBrightness = new TextField(
					String.valueOf(BlueIrisViewer.bivSettings.nightModeBrightness), skin);
			txtNightModeBrightness.setTextFieldListener(new TextField.TextFieldListener()
			{
				@Override
				public void keyTyped(TextField textField, char key)
				{
					int val = Utilities.ParseInt(textField.getText(), -1);
					if (val < 0 || val > 100)
					{
						textField.setColor(1, 0, 0, 1);
						BlueIrisViewer.bivSettings.nightModeBrightness = 10;
					}
					else
					{
						textField.setColor(1, 1, 1, 1);
						BlueIrisViewer.bivSettings.nightModeBrightness = val;
					}
					BlueIrisViewer.bivSettings.Save();
					SetScreenBrightness();
				}
			});
			table.add(txtNightModeBrightness).align(Align.left);
			table.row();

			table.add(new Label("Brightness changing does not work on all systems.", skin)).colspan(2)
					.align(Align.center);
			table.row();

			table.add(new Label("----------------------------------------------------", skin)).colspan(2)
					.align(Align.center);
			table.row();
		}

		table.add(new Label("Day Mode Image Filter:", skin)).colspan(2).align(Align.center);
		table.row();

		final SelectBox<String> sbDayModeImageFilter = new SelectBox<String>(skin);
		sbDayModeImageFilter.setItems(new String[] { "Normal", "Red 1", "Red 2", "Red 3" });
		sbDayModeImageFilter.setSelectedIndex(BlueIrisViewer.bivSettings.pixelManipulationDayMode);
		sbDayModeImageFilter.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				BlueIrisViewer.bivSettings.pixelManipulationDayMode = sbDayModeImageFilter.getSelectedIndex();
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(sbDayModeImageFilter).align(Align.right);

		whatIsPixelManipulationWnd = new WhatIsPixelManipulationWnd(skin);
		final TextButton btnWhatIsResolutionMode = new TextButton("   ?   ", skin);
		btnWhatIsResolutionMode.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				if (whatIsPixelManipulationWnd.isShowing())
					whatIsPixelManipulationWnd.hide();
				else
					whatIsPixelManipulationWnd.show();
			}
		});
		table.add(btnWhatIsResolutionMode).align(Align.left);
		table.row();

		table.add().height(10);
		table.row();

		table.add(new Label("Night Mode Image Filter:", skin)).colspan(2).align(Align.center);
		table.row();

		final SelectBox<String> sbNightModeImageFilter = new SelectBox<String>(skin);
		sbNightModeImageFilter.setItems(new String[] { "Normal", "Red 1", "Red 2", "Red 3" });
		sbNightModeImageFilter.setSelectedIndex(BlueIrisViewer.bivSettings.pixelManipulationNightMode);
		sbNightModeImageFilter.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				BlueIrisViewer.bivSettings.pixelManipulationNightMode = sbNightModeImageFilter.getSelectedIndex();
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(sbNightModeImageFilter).align(Align.right);

		whatIsPixelManipulationWnd = new WhatIsPixelManipulationWnd(skin);
		final TextButton btnWhatIsResolutionMode2 = new TextButton("   ?   ", skin);
		btnWhatIsResolutionMode2.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				if (whatIsPixelManipulationWnd.isShowing())
					whatIsPixelManipulationWnd.hide();
				else
					whatIsPixelManipulationWnd.show();
			}
		});
		table.add(btnWhatIsResolutionMode2).align(Align.left);
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

	private void SetScreenBrightness()
	{
		boolean nightMode = BlueIrisViewer.nightModeManager.isNightModeNow();

		if (nightMode && BlueIrisViewer.bivSettings.setNightModeBrightness)
			ScreenBrightness.SetBrightness(BlueIrisViewer.bivSettings.nightModeBrightness);
		else if (!nightMode && BlueIrisViewer.bivSettings.setDayModeBrightness)
			ScreenBrightness.SetBrightness(BlueIrisViewer.bivSettings.dayModeBrightness);
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
