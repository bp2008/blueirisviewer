package org.brian.blueirisviewer.ui;

import org.brian.blueirisviewer.BlueIrisViewer;
import org.brian.blueirisviewer.util.IntRectangle;
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

public class WindowOptionsWnd extends UIElement
{
	TextField txtWindowX, txtWindowY, txtWindowW, txtWindowH;
	IntRectangle previousPosition = BlueIrisViewer.windowHelper.GetWindowRectangle();

	public WindowOptionsWnd(Skin skin)
	{
		super(skin);
	}

	@Override
	public void onCreate(final Skin skin, final Window window, final Table table)
	{
		table.columnDefaults(0).align(Align.right).padRight(10);
		table.columnDefaults(1).align(Align.left);
		table.pad(10, 10, 10, 10);

		window.setTitle("Window Options");

		// Borderless window option
		table.add(new Label("Borderless Window:", skin));
		final TextButton btnBorderless = new TextButton("Toggle", skin);
		btnBorderless.align(Align.left);
		btnBorderless.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				BlueIrisViewer.bivSettings.borderless = !BlueIrisViewer.bivSettings.borderless;
				BlueIrisViewer.bivSettings.restartBorderlessToggle = true;
				// The save will be performed in the render thread.
			}
		});
		table.add(btnBorderless);
		table.row();

		table.add().height(10);
		table.row();

		// Drag to move window option
		table.add(new Label("Move window by dragging anywhere:", skin));
		final TextButton btnDragMove = new TextButton(BlueIrisViewer.bivSettings.disableWindowDragging ? "No" : "Yes",
				skin);
		btnDragMove.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				BlueIrisViewer.bivSettings.disableWindowDragging = !BlueIrisViewer.bivSettings.disableWindowDragging;
				btnDragMove.setText(BlueIrisViewer.bivSettings.disableWindowDragging ? "No" : "Yes");
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(btnDragMove);
		table.row();

		table.add().height(10);
		table.row();

		// Preserve size when dragging option
		Label lblRememberWindowPositionAndSize = new Label("Remember window position/size\nbetween app sessions:", skin);
		lblRememberWindowPositionAndSize.setAlignment(Align.right);
		table.add(lblRememberWindowPositionAndSize);
		final TextButton btnRememberWindowPositionAndSize = new TextButton(
				BlueIrisViewer.bivSettings.loadStartPositionAndSizeUponAppStart ? "Yes" : "No", skin);
		btnRememberWindowPositionAndSize.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				BlueIrisViewer.bivSettings.loadStartPositionAndSizeUponAppStart = !BlueIrisViewer.bivSettings.loadStartPositionAndSizeUponAppStart;
				btnRememberWindowPositionAndSize
						.setText(BlueIrisViewer.bivSettings.loadStartPositionAndSizeUponAppStart ? "Yes" : "No");
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(btnRememberWindowPositionAndSize);
		table.row();

		table.add().height(20);
		table.row();

		Table tbl = new Table(skin);
		tbl.add("Manual Window Positioning Options").colspan(4).padBottom(10);
		tbl.row();

		IntRectangle currentPosition = previousPosition = BlueIrisViewer.windowHelper.GetWindowRectangle();
		txtWindowX = new TextField(String.valueOf(currentPosition.x), skin);
		txtWindowY = new TextField(String.valueOf(currentPosition.y), skin);
		txtWindowW = new TextField(String.valueOf(currentPosition.width), skin);
		txtWindowH = new TextField(String.valueOf(currentPosition.height), skin);

		tbl.add("Left:").align(Align.right).pad(0, 0, 10, 10);
		tbl.add(txtWindowX).align(Align.left).padBottom(10).width(75);
		tbl.add("Top:").align(Align.right).pad(0, 0, 10, 10);
		tbl.add(txtWindowY).align(Align.left).padBottom(10).width(75).padRight(30);
		tbl.row();
		tbl.add("Width:").align(Align.right).pad(0, 0, 10, 10);
		tbl.add(txtWindowW).align(Align.left).padBottom(10).width(75);
		tbl.add("Height:").align(Align.right).pad(0, 0, 10, 10);
		tbl.add(txtWindowH).align(Align.left).padBottom(10).width(75).padRight(30);
		tbl.row();

		final TextButton btnCommitPositionAndSize = new TextButton("Apply above window position and size", skin);
		btnCommitPositionAndSize.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				IntRectangle i = new IntRectangle(Utilities.ParseInt(txtWindowX.getText().toString(), 0), Utilities
						.ParseInt(txtWindowY.getText().toString(), 0), Utilities.ParseInt(txtWindowW.getText()
						.toString(), 1280), Utilities.ParseInt(txtWindowH.getText().toString(), 720));
				BlueIrisViewer.windowHelper.SetWindowRectangle(i);
			}
		});
		tbl.add(btnCommitPositionAndSize).colspan(4).align(Align.center);
		tbl.row();

		table.add(tbl).colspan(2);
		table.row();

		table.add().height(20);
		table.row();

		final CheckBox cbModalUI = new CheckBox("  Modal Option Windows\n  (Topmost window absorbs all input)", skin);
		cbModalUI.setChecked(BlueIrisViewer.bivSettings.modalUI);
		cbModalUI.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				BlueIrisViewer.bivSettings.modalUI = cbModalUI.isChecked();
				BlueIrisViewer.bivSettings.Save();
				UI.setModal(BlueIrisViewer.bivSettings.modalUI);
			}
		});
		table.add(cbModalUI).colspan(2).padBottom(10).align(Align.center);
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
			}
		});
		table.add(btnClose).colspan(2).align(Align.center);
		table.row();
	}

	@Override
	public void onUpdate(final Window window, final Table table)
	{
		IntRectangle currentPosition = BlueIrisViewer.windowHelper.GetWindowRectangle();
		if (currentPosition.x != previousPosition.x)
			txtWindowX.setText(String.valueOf(currentPosition.x));
		if (currentPosition.y != previousPosition.y)
			txtWindowY.setText(String.valueOf(currentPosition.y));
		if (currentPosition.width != previousPosition.width)
			txtWindowW.setText(String.valueOf(currentPosition.width));
		if (currentPosition.height != previousPosition.height)
			txtWindowH.setText(String.valueOf(currentPosition.height));
		previousPosition = currentPosition;
	}

	@Override
	public void onDestroy()
	{
	}
}
