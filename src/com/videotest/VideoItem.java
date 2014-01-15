package com.videotest;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

public class VideoItem {
	private final int Height = 100;
	private final int Width  = 100;
	
	private Bitmap videoPreview;
	private String videoLink;
	private String videoName;
	
	VideoItem(String videoLink, String videoName) {
		this.videoLink = videoLink;
		this.videoName = videoName;
//		this.videoPreview = ThumbnailUtils.createVideoThumbnail(videoLink,
//				MediaStore.Images.Thumbnails.MICRO_KIND);
		this.videoPreview = Bitmap.createBitmap(Width, Height, Config.ARGB_8888);
		getLinkPreview(videoLink, Width, Height, this.videoPreview);
		System.out.println("Video preview: " + videoPreview);
	}
	
	String getLink() {
		return videoLink;
	}
	
	String getFileName() {
		return videoName;
	}
	
	Bitmap getPreview() {
		return videoPreview;
	}
	
	public native int getLinkPreview(String link, int w, int h, Bitmap bitmap);
}
