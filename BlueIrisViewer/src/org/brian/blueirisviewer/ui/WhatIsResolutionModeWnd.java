package org.brian.blueirisviewer.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class WhatIsResolutionModeWnd extends UIElement
{
	public WhatIsResolutionModeWnd(Skin skin)
	{
		super(skin);
	}

	@Override
	public void onCreate(Skin skin, Window window, Table table)
	{
		window.setTitle("About Resolution Modes");

		Table scrollTable = new Table(skin);
		scrollTable.columnDefaults(0).align(Align.left);
		scrollTable.pad(10, 10, 10, 30);

		ScrollPane scrollPane = new ScrollPane(scrollTable, skin);
		scrollPane.setFadeScrollBars(false);
		scrollPane.setScrollingDisabled(true, false);
		table.add(scrollPane).width(550);
		table.row();

		scrollTable.add(
				GetWrapTrueLabel(
						"Blue Iris allows 3rd party programs to limit the resolution of images that are sent over the network."
								+ "  BlueIrisViewer takes advantage of this to reduce the amount of CPU time and"
								+ " network bandwidth consumed.", skin)).width(500);
		;
		scrollTable.row();

		scrollTable.add().height(10);
		scrollTable.row();

		scrollTable
				.add(GetWrapTrueLabel(
						"In the Performance Options window, you can select one of the following resolution modes depending on your needs:",
						skin)).width(500);
		scrollTable.row();

		scrollTable.add().height(10);
		scrollTable.row();

		scrollTable.add(GetWrapTrueLabel("High Efficiency", skin)).width(500);
		scrollTable.row();
		scrollTable
				.add(GetWrapTrueLabel(
						"BlueIrisViewer will request images no larger than the area they are to be displayed in.  "
								+ "You may notice significant image quality degradation and softer edges.  "
								+ "Image quality is temporarily poor after full-screening a high definition camera"
								+ ", lasting until a higher-resolution frame is downloaded.", skin)).padLeft(20)
				.width(500);
		scrollTable.row();

		scrollTable.add().height(10);
		scrollTable.row();

		scrollTable.add(GetWrapTrueLabel("Balanced", skin)).width(500);
		scrollTable.row();
		scrollTable
				.add(GetWrapTrueLabel(
						"Images are requested at sizes up to double the width and height of the area "
								+ "they are to be displayed in, but never larger than the resolution of your BlueIrisViewer window.  "
								+ "Camera grid images appear sharper, and there is less temporary quality loss when full-screening a camera.",
						skin)).padLeft(20).width(500);
		scrollTable.row();
		
		scrollTable.add().height(10);
		scrollTable.row();

		scrollTable.add(GetWrapTrueLabel("High Quality", skin)).width(500);
		scrollTable.row();
		scrollTable
				.add(GetWrapTrueLabel(
						"(Recommended Mode)\nImages are always requested at sizes up to double the width and height of the area "
								+ "they are to be displayed in, even when a camera is full-screened.  If your cameras are higher "
								+ "resolution than the BlueIrisViewer window, then a full-screened camera will appear sharper than "
								+ "it would in Balanced mode.", skin)).padLeft(20).width(500);
		scrollTable.row();

		scrollTable.add().height(10);
		scrollTable.row();

		scrollTable.add(GetWrapTrueLabel("Maximum Quality", skin)).width(500);
		scrollTable.row();
		scrollTable
				.add(GetWrapTrueLabel(
						"Images are always requested at double the width and height of the BlueIrisViewer window. "
								+ "This mode is like High Quality mode, but without temporary quality loss when full-screening a camera.  "
								+ "An efficiency benefit is only seen if your cameras are very high resolution.", skin))
				.padLeft(20).width(500);
		scrollTable.row();

		scrollTable.add().height(10);
		scrollTable.row();

		scrollTable.add(GetWrapTrueLabel("No Optimizations", skin)).width(500);
		scrollTable.row();
		scrollTable
				.add(GetWrapTrueLabel(
						"Images are requested at their default resolution, as configured in Blue Iris under the Webcast/Jpeg options.  "
								+ "This typically results in the sharpest possible images with little regard for efficiency.",
						skin)).padLeft(20).width(500);
		scrollTable.row();

		scrollTable.add().height(10);
		scrollTable.row();

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

	private Label GetWrapTrueLabel(String str, Skin skin)
	{
		Label lbl = new Label(str, skin);
		lbl.setWrap(true);
		return lbl;
	}

	@Override
	protected void onUpdate(Window window, Table table)
	{
	}

	@Override
	public void onDestroy()
	{
	}
}
