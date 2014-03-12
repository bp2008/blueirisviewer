package org.brian.blueirisviewer.ui;

import org.brian.blueirisviewer.BlueIrisViewer;
import org.brian.blueirisviewer.images.Images;
import org.brian.blueirisviewer.util.Encryption;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class ServerSetupWnd extends UIElement
{
	public ServerSetupWnd(Skin skin)
	{
		super(skin);
	}

	@Override
	public void onCreate(final Skin skin, final Window window, final Table table)
	{
		table.columnDefaults(0).align(Align.right).padRight(10);
		table.columnDefaults(1).align(Align.left);
		table.pad(10, 10, 10, 10);

		window.setTitle("Server Setup");

		table.add(new Label("Server Address: ", skin));
		TextField txtHostName = new TextField(BlueIrisViewer.bivSettings.serverURL, skin);
		txtHostName.setTextFieldListener(new TextField.TextFieldListener()
		{
			@Override
			public void keyTyped(TextField textField, char key)
			{
				BlueIrisViewer.bivSettings.serverURL = textField.getText();
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(txtHostName).width(250);
		table.row();

		table.add().height(10);
		table.row();

		table.add(new Label("User Name: ", skin));
		TextField txtUsername = new TextField(BlueIrisViewer.bivSettings.username, skin);
		txtUsername.setTextFieldListener(new TextField.TextFieldListener()
		{
			@Override
			public void keyTyped(TextField textField, char key)
			{
				BlueIrisViewer.bivSettings.username = textField.getText();
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(txtUsername).width(250);
		table.row();

		table.add().height(10);
		table.row();

		table.add(new Label("Password: ", skin));
		TextField txtPassword = new TextField("", skin);
		txtPassword.setPasswordMode(true);
		txtPassword.setPasswordCharacter('*');
		txtPassword.setTextFieldListener(new TextField.TextFieldListener()
		{
			@Override
			public void keyTyped(TextField textField, char key)
			{
				BlueIrisViewer.bivSettings.password = Encryption.Encrypt(textField.getText());
				BlueIrisViewer.bivSettings.Save();
			}
		});
		table.add(txtPassword).width(250);
		table.row();

		table.add().height(10);
		table.row();

		final TextButton btnTryNewAddress = new TextButton("Reconnect with new configuration", skin);
		btnTryNewAddress.addListener(new ChangeListener()
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
		table.add(btnTryNewAddress).colspan(2).align(Align.center);
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
	}

	@Override
	public void onDestroy()
	{
	}
}
