package com.videotest;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

public class VideoItem {
	private Bitmap videoPreview;
	private String videoLink;
	private String videoName;
	
	VideoItem(String videoLink, String videoName) {
		this.videoLink = videoLink;
		this.videoName = videoName;
		this.videoPreview = ThumbnailUtils.createVideoThumbnail(videoLink,
				MediaStore.Images.Thumbnails.MICRO_KIND);
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
}
