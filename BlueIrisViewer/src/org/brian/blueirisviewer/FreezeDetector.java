package org.brian.blueirisviewer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class FreezeDetector
{
	private long lastTime = 0;
	private double positionRunning = 0;
	private Sprite spriteRed;

	public FreezeDetector()
	{
		spriteRed = new Sprite(BlueIrisViewer.texOpaqueRed);
	}

	public void render(SpriteBatch batch)
	{
		if (BlueIrisViewer.bivSettings.freezeDetectionAnimation == 1 || BlueIrisViewer.bivSettings.freezeDetectionAnimation == 2)
		{
			float x = 0;
			float y = 0;
			float s = BlueIrisViewer.bivSettings.freezeDetectionAnimationSize;

			long timeNow = GameTime.getGameTime();
			long timePassedSinceLastFrame = lastTime == 0 ? 0 : GameTime.getGameTime() - lastTime;
			float timePerMoveStep = 100f;
			float moveAmount = (timePassedSinceLastFrame / timePerMoveStep) * BlueIrisViewer.bivSettings.freezeDetectionAnimationSpeed;
			positionRunning += moveAmount;

			if (BlueIrisViewer.bivSettings.freezeDetectionAnimation == 1)
			{
				// Across the Top
				long distancePerEdge = 2000;
				long position = (long) Math.abs(positionRunning % distancePerEdge);
				// Position is now a number between 0 and 999. Calculate X and Y position based on that.
				float relativePositionAlongEdge = position / (float) distancePerEdge;
				// top left to top right
				x = relativePositionAlongEdge * (BlueIrisViewer.fScreenWidth - s);
				y = BlueIrisViewer.fScreenHeight - s;
			}
			else if (BlueIrisViewer.bivSettings.freezeDetectionAnimation == 2)
			{
				// Around the Perimeter
				float aspectRatio = Math.max(1, BlueIrisViewer.fScreenWidth) / Math.max(1, BlueIrisViewer.fScreenHeight);
				long distanceHeight = 1000;
				long distanceWidth = (long) (distanceHeight * aspectRatio);
				long distancePerimeter = distanceHeight + distanceHeight + distanceWidth + distanceWidth;
				long position = (long) Math.abs(positionRunning % distancePerimeter);
				// BlueIrisViewer.ui.DrawText(batch, "position: " + position, 30, BlueIrisViewer.fScreenHeight - 30);
				// Position is now a number between 0 and 999. Calculate X and Y position based on that.
				if (position < distanceWidth)
				{
					// top left to top right
					float relativePositionAlongEdge = (position % distanceWidth) / (float) distanceWidth;
					x = relativePositionAlongEdge * (BlueIrisViewer.fScreenWidth - s);
					y = BlueIrisViewer.fScreenHeight - s;
				}
				else if (position < (distanceWidth + distanceHeight))
				{
					// top right to bottom right
					long positionRelative = position - distanceWidth;
					float relativePositionAlongEdge = 1 - ((positionRelative % distanceHeight) / (float) distanceHeight);
					x = BlueIrisViewer.fScreenWidth - s;
					y = relativePositionAlongEdge * (BlueIrisViewer.fScreenHeight - s);
				}
				else if (position < (distancePerimeter - distanceHeight))
				{
					// bottom right to bottom left
					long positionRelative = position - (distanceWidth + distanceHeight);
					float relativePositionAlongEdge = 1 - ((positionRelative % distanceWidth) / (float) distanceWidth);
					x = relativePositionAlongEdge * (BlueIrisViewer.fScreenWidth - s);
					y = 0;
				}
				else
				{
					// bottom left to top left
					long positionRelative = position - (distancePerimeter - distanceHeight);
					float relativePositionAlongEdge = (positionRelative % distanceHeight) / (float) distanceHeight;
					x = 0;
					y = relativePositionAlongEdge * (BlueIrisViewer.fScreenHeight - s);
				}
			}

			// BlueIrisViewer.ui.DrawText(batch, "x: " + (int) x + ", y: " + (int) y, 30, BlueIrisViewer.fScreenHeight - 60);
			spriteRed.setAlpha(BlueIrisViewer.bivSettings.freezeDetectionAnimationOpacity / 255f);
			spriteRed.setPosition(x, y);
			spriteRed.setSize(s, s);
			spriteRed.draw(batch);
			lastTime = timeNow;
		}
		if (BlueIrisViewer.bivSettings.warnOfStalledImageLoading)
		{
			long timeSinceLastFrame = GameTime.getGameTime() - BlueIrisViewer.images.getLastFrameTime();
			if (timeSinceLastFrame > (BlueIrisViewer.bivSettings.imageRefreshDelayMS * 2) + 5000)
			{
				String text = "Image loading stall detected!";
				TextBounds textSize = BlueIrisViewer.ui.MeasureText(text);
				float hMargin = 60;
				float vMargin = 100;
				float x = (BlueIrisViewer.fScreenWidth - textSize.width) / 2;
				float y = (BlueIrisViewer.fScreenHeight - textSize.height) / 2;
				Texture boxBackground = BlueIrisViewer.texDarkGray;
				if (GameTime.getGameTime() % 2000 < 1000)
					boxBackground = BlueIrisViewer.texRed;
				batch.draw(boxBackground, x - hMargin, y - vMargin, textSize.width + hMargin + hMargin, textSize.height + vMargin + vMargin);
				BlueIrisViewer.ui.DrawText(batch, text, x, y + textSize.height);
			}
//			BlueIrisViewer.ui.DrawText(batch, String.valueOf(timeSinceLastFrame) + " / " + ((BlueIrisViewer.bivSettings.imageRefreshDelayMS * 2) + 5000), 30, 30);
		}
	}
}
