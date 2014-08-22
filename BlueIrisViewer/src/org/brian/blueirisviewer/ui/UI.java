package org.brian.blueirisviewer.ui;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class UI
{
	private static Skin skin;
	public Stage stage;
	public static ArrayList<UIElement> uiElements;
	public static WidgetGroup root;

	public UI()
	{
		uiElements = new ArrayList<UIElement>();

		stage = new Stage(new ScreenViewport());
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		root = new WidgetGroup();
		root.setFillParent(true);
		stage.addActor(root);

		new MainOptionsWnd(skin);
	}

	public void render()
	{
		for (UIElement ele : uiElements)
			if (ele.isShowing())
				ele.doUpdate();
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
		//Table.drawDebug(stage);
	}

	public void resize(int width, int height)
	{
		stage.getViewport().update(width, height, true);
	}

	public void dispose()
	{
		stage.dispose();
		skin.dispose();
	}

	public void DrawText(SpriteBatch batch, String text, float x, float y)
	{
		BitmapFont font = skin.getFont("default-font");
		font.draw(batch, text, x, y);
	}

	public void openWindow(Class<?> windowClass)
	{
		for (UIElement ele : uiElements)
			if (ele.getClass().getSimpleName().equals(windowClass.getSimpleName()))
				ele.show();
	}
	public boolean isAnyWindowOpen()
	{
		for (UIElement ele : uiElements)
			if(ele.isShowing())
				return true;
		return false;
	}

	public void closeAllUIWindows()
	{
		for (UIElement ele : uiElements)
			ele.hide();
	}

	public static void setModal(boolean isModal)
	{
		for (UIElement ele : uiElements)
			ele.setModal(isModal);
	}

	/**
	 * Shows a modal dialog in the center of the display window, asking the user a question with "Yes" and "No" answer
	 * buttons at the bottom of the dialog.
	 * 
	 * @param question
	 *            The question to ask the user.
	 * @param title
	 *            A short title to show in the dialog box's title bar.
	 * @param actionOnYes
	 *            A Runnable to execute if the user's answer is Yes. May be null.
	 * @param actionOnNo
	 *            A Runnable to execute if the user's answer is No. May be null.
	 */
	public static void showYesNoQuestionDialog(String question, String title, final Runnable actionOnYes,
			final Runnable actionOnNo)
	{
		showYesNoQuestionDialog(question, title, actionOnYes, actionOnNo, true);
	}

	/**
	 * Shows a dialog in the center of the display window, asking the user a question with "Yes" and "No" answer buttons
	 * at the bottom of the dialog.
	 * 
	 * @param question
	 *            The question to ask the user.
	 * @param title
	 *            A short title to show in the dialog box's title bar.
	 * @param actionOnYes
	 *            A Runnable to execute if the user's answer is Yes. May be null.
	 * @param actionOnNo
	 *            A Runnable to execute if the user's answer is No. May be null.
	 */
	public static void showYesNoQuestionDialog(String question, String title, final Runnable actionOnYes,
			final Runnable actionOnNo, boolean modal)
	{
		final Table dialogWrapperTable = new Table(skin);
		dialogWrapperTable.setFillParent(true);

		final Window dialog = new Window(title, skin);
		dialog.setModal(modal);

		Table dialogTable = new Table(skin);
		dialogTable.setFillParent(true);
		dialog.add(dialogTable);

		dialogTable.add(new Label(question, skin)).colspan(2).pad(20);
		dialogTable.row();

		TextButton btn = new TextButton("Yes", skin);
		btn.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				if (actionOnYes != null)
					actionOnYes.run();
				dialog.remove();
				dialogWrapperTable.remove();
			}
		});
		dialogTable.add(btn).align(Align.right).padRight(5);

		btn = new TextButton("No", skin);
		btn.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				if (actionOnNo != null)
					actionOnNo.run();
				dialog.remove();
				dialogWrapperTable.remove();
			}
		});
		dialogTable.add(btn).align(Align.left).padLeft(5);
		dialogTable.row();

		dialogTable.add().height(10);
		dialogTable.row();

		dialogWrapperTable.add(dialog);

		root.addActor(dialogWrapperTable);
	}
}
