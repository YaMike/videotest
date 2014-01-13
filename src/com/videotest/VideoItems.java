package com.videotest;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class VideoItems {
	private ArrayList<VideoItem> val = new ArrayList<VideoItem>();
	
	VideoItems(String url) {
		Document doc;
		try {			
			System.out.println("Connecting: " + url);
			doc = Jsoup.connect(url).get();
			Elements links = doc.select("a[href]");
			for (Element link: links) {
				String fullLink = url + "/" + link.attr("href");
				if (!fullLink.contains(".mp4")) continue;
				val.add(new VideoItem(fullLink, link.attr("href")));
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	ArrayList<VideoItem> getItems() {
		return val;
	}
}
