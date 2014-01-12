package com.videotest;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

public class VideoItems {
	private ArrayList<String> ll = new ArrayList<String>();
	private ArrayList<Bitmap> il = new ArrayList<Bitmap>();
	
	VideoItems(String url) {
		Document doc;
		try {			
			doc = Jsoup.connect(url).get();
			Elements links = doc.select("a[href]");
			for (Element link: links) {
				String fullLink = url + link.attr("href");
				System.out.println("\nlink: " + fullLink);
				ll.add(fullLink);
				il.add(ThumbnailUtils.createVideoThumbnail(fullLink, 
						MediaStore.Images.Thumbnails.MINI_KIND));
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	ArrayList<String> getLinks() {
		return ll;
	}
	
	ArrayList<Bitmap> getPreviewImages() {
		return il;
	}
}
