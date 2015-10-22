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

public class WhatIsPixelManipulationWnd extends UIElement
{
	public WhatIsPixelManipulationWnd(Skin skin)
	{
		super(skin);
	}

	@Override
	public void onCreate(Skin skin, Window window, Table table)
	{
		window.setTitle("About Image Filters");

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
						"Blue Iris Viewer includes some image filters to make night viewing more comfortable.", skin))
				.width(500);
		;
		scrollTable.row();

		scrollTable.add().height(10);
		scrollTable.row();

		scrollTable.add(GetWrapTrueLabel("Here are descriptions of the image filters:", skin)).width(500);
		scrollTable.row();

		scrollTable.add().height(10);
		scrollTable.row();

		scrollTable.add(GetWrapTrueLabel("Normal", skin)).width(500);
		scrollTable.row();
		scrollTable.add(GetWrapTrueLabel("Images are displayed normally.", skin)).padLeft(20).width(500);
		scrollTable.row();

		scrollTable.add().height(10);
		scrollTable.row();

		scrollTable.add(GetWrapTrueLabel("Red 1", skin)).width(500);
		scrollTable.row();
		scrollTable
				.add(GetWrapTrueLabel(
						"The green and blue color channels are removed from images, leaving the"
								+ " red channel unchanged.  This is the most efficient Red method."
								+ "  Red is easier on the eyes when they are used to the dark.",
						skin)).padLeft(20).width(500);
		scrollTable.row();

		scrollTable.add().height(10);
		scrollTable.row();

		scrollTable.add(GetWrapTrueLabel("Red 2", skin)).width(500);
		scrollTable.row();
		scrollTable
				.add(GetWrapTrueLabel("The red channel value is replaced with the brightest of the"
						+ " three color channels, while the green and blue color channels are removed.", skin))
				.padLeft(20).width(500);
		scrollTable.row();

		scrollTable.add().height(10);
		scrollTable.row();

		scrollTable.add(GetWrapTrueLabel("Red 3", skin)).width(500);
		scrollTable.row();
		scrollTable
				.add(GetWrapTrueLabel("The red channel value is replaced with the average of the"
						+ " three color channels, while the green and blue color channels are removed.", skin))
				.padLeft(20).width(500);
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
