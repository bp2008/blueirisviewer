package org.brian.blueirisviewer.ui;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.ArrayList;

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
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.esotericsoftware.tablelayout.Cell;

public class WindowOptionsWnd extends UIElement
{
	TextField txtWindowX, txtWindowY, txtWindowW, txtWindowH;
	IntRectangle previousPosition = BlueIrisViewer.windowHelper.GetWindowRectangle();
	WidgetGroup monitorButtons;

	@SuppressWarnings("rawtypes")
	Cell monitorButtonsCell;

	Skin defaultSkin;

	public WindowOptionsWnd(Skin skin)
	{
		super(skin);
	}

	@Override
	protected void onCreate(final Skin skin, final Window window, final Table table)
	{
		defaultSkin = skin;

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

		if (BlueIrisViewer.bivSettings.borderless)
		{
			table.add("Choose a monitor to fullscreen on").colspan(4).padBottom(10).align(Align.center);
			table.row();

			monitorButtons = new WidgetGroup();
			monitorButtonsCell = table.add(monitorButtons).colspan(4).align(Align.center).width(320).height(240);
			table.row();

			table.add().height(20);
			table.row();
		}

		Table tbl = new Table(skin);
		tbl.add("Manual Window Positioning Options").colspan(4).padBottom(10).align(Align.center);
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
				BlueIrisViewer.bivSettings.startPositionX = i.x;
				BlueIrisViewer.bivSettings.startPositionY = i.y;
				BlueIrisViewer.bivSettings.startSizeW = i.width;
				BlueIrisViewer.bivSettings.startSizeH = i.height;
				BlueIrisViewer.bivSettings.Save();
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
		table.add(btnClose).colspan(2).align(Align.right);
		table.row();
	}

	@Override
	protected void onUpdate(final Window window, final Table table)
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
	protected void onDestroy()
	{
	}

	@Override
	protected void onShow()
	{
		if (BlueIrisViewer.bivSettings.borderless)
		{
			GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] allScreens = env.getScreenDevices();
			Rectangle leftMost = null, rightMost = null, topMost = null, bottomMost = null;
			ArrayList<Rectangle> screenRects = new ArrayList<Rectangle>();
			for (int i = 0; i < allScreens.length; i++)
			{
				GraphicsDevice screen = allScreens[i];
				Rectangle rect = screen.getDefaultConfiguration().getBounds();
				if (rect.width > 0 && rect.height > 0)
				{
					screenRects.add(rect);
					if (leftMost == null)
						leftMost = rightMost = topMost = bottomMost = rect;
					else
					{
						if (rect.x < leftMost.x)
							leftMost = rect;
						if (rect.y < topMost.y)
							topMost = rect;
						if (rect.x + rect.width > rightMost.x + rightMost.width)
							rightMost = rect;
						if (rect.y + rect.height > bottomMost.y + bottomMost.height)
							bottomMost = rect;
					}
				}
			}
			// Calculate real desktop width and height in pixels.
			int desktopPxWide = (rightMost.x + rightMost.width) - leftMost.x;
			int desktopPxTall = (bottomMost.y + bottomMost.height) - topMost.y;

			// Scale it down into a 320x240 box
			int w = desktopPxWide;
			int h = desktopPxTall;
			int availableWidth = 320;
			int availableHeight = 240;

			if (w > availableWidth)
			{
				double diff = w / availableWidth;
				w = availableWidth;
				h = (int) (h / diff);
			}
			if (h > availableHeight)
			{
				double diff = h / availableHeight;
				h = availableHeight;
				w = (int) (w / diff);
			}

			// w and h now represent the width and height of the entire desktop, scaled to fit in a 320x240 box.
			monitorButtons.clear();
			monitorButtonsCell.height(h);
			for (int i = 0; i < screenRects.size(); i++)
			{
				final Rectangle screen = screenRects.get(i);
				TextButton btn = new TextButton(String.valueOf(i), defaultSkin);
				// To calculate the button width, we determine what fraction of
				// the desktop width this screen provides. Then we give the button
				// the same fraction of the 320 pixels we have allocated for this
				// monitor selector (w)
				double fractionOfWholeWidth = ((double) screen.width / (double) desktopPxWide);
				int bw = (int) (w * fractionOfWholeWidth);
				// Do the same thing for height
				double fractionOfWholeHeight = ((double) screen.height / (double) desktopPxTall);
				int bh = (int) (h * fractionOfWholeHeight);
				// To calculate the X position, we determine what fraction of
				// the desktop is to the left of this screen. We then multiply
				// this fraction times the "shrunken" width (w).
				int leftfScreen = screen.x - leftMost.x;
				double fractionOfWholeLeft = ((double) leftfScreen / (double) desktopPxWide);
				int bx = (int) (fractionOfWholeLeft * w);
				// Repeat for Y position
				int bottomOfScreen = ((screen.y - topMost.y) + screen.height);
				double fractionOfWholeTop = 1.0d - ((double) bottomOfScreen / (double) desktopPxTall);
				int by = (int) (fractionOfWholeTop * h);
				btn.setBounds(bx, by, bw, bh);

				btn.addListener(new ChangeListener()
				{
					@Override
					public void changed(ChangeEvent event, Actor actor)
					{
						IntRectangle i = new IntRectangle(screen.x, screen.y, screen.width, screen.height);
						BlueIrisViewer.bivSettings.startPositionX = i.x;
						BlueIrisViewer.bivSettings.startPositionY = i.y;
						BlueIrisViewer.bivSettings.startSizeW = i.width;
						BlueIrisViewer.bivSettings.startSizeH = i.height;
						BlueIrisViewer.bivSettings.Save();
						BlueIrisViewer.windowHelper.SetWindowRectangle(i);
					}
				});

				monitorButtons.addActor(btn);
			}
		}
	}
}
