# BlueIrisViewer

BlueIrisViewer is a Java application which connects to a Blue Iris Video Security server and displays live camera views in high resolution.

This is a 3rd-party tool designed to be used with Blue Iris Video Security software.  Blue Iris can be found at http://blueirissoftware.com/

BlueIrisViewer is intended to be used from the same LAN as the Blue Iris server.  It uses a lot of bandwidth to deliver high resolution images from all your cameras at once, such that most internet connections and even some WiFi may not be able to keep up.

![screenshot](http://i.imgur.com/ADaeMgXm.jpg)

## Instant Replay

BlueIrisViewer's most unique feature is Instant Replay.  This allows you to quickly and easily scan through the last few minutes of video and re-watch events as if they were live. Please note that this feature makes heavy, constant use of your hard drive for caching video frames, and as such it is recommended that you **do not run this app from a solid state drive (SSD)** if you have Instant Replay enabled, as this would significantly shorten the life of the SSD. It is fine to run the app from an SSD if Instant Replay is **disabled**.

## Installation

Download from the releases section, then extract and run the .jar file.  If double-clicking the .jar file does not work, you will need to run it with the command `java -jar BlueIrisViewer.jar`.

https://github.com/bp2008/blueirisviewer/releases

For more information, check the discussion thread on IP Cam Talk:

http://www.ipcamtalk.com/showthread.php?191-Standalone-Java-app-for-dedicated-live-view-with-Instant-Replay

