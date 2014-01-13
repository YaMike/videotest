package com.videotest;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener {
	
	private final String url = "http://tangohacks.com/videofeed";
	private ListView mainListView;
	private ArrayList<VideoItem> videoItems = new ArrayList<VideoItem>();
	private VideoItemsAdapter videoAdapter;
	private ProgressDialog videoReadProgress;
	
	public Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mainListView = (ListView)findViewById(R.id.listview);
		videoAdapter = new VideoItemsAdapter(this, videoItems);
		mainListView.setAdapter(videoAdapter);
		mainListView.setClickable(true);
		mainListView.setOnItemClickListener(this);
		videoReadProgress = ProgressDialog.show(MainActivity.this, 
				"Please wait...", "Updating list of video...", true);
		new Thread(new VideoListLoader()).start();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		VideoItem vi = (VideoItem)mainListView.getItemAtPosition(position);
		Intent i = new Intent(this, ChildActivity.class);
		i.putExtra("open", vi.getLink());
		startActivityForResult(i, 1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class VideoListLoader implements Runnable {
		@Override
		public void run() {
			final VideoItems result = new VideoItems(url);
			final ArrayList<VideoItem> items = result.getItems(); 
			videoAdapter.setNewItems(items);
		}
	}
	
	private class VideoItemsAdapter extends BaseAdapter implements ListAdapter {
		private final Context context;
		private ArrayList<VideoItem> items;
		public final Handler handler = new Handler();

		public VideoItemsAdapter(Context context, ArrayList<VideoItem> items) {
			this.context = context;
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			View rowView = inflater.inflate(R.layout.video_item_layout, parent, false);
			
			TextView  nameText = (TextView)rowView.findViewById(R.id.videoItemName);
			TextView  linkText = (TextView)rowView.findViewById(R.id.videoItemLink);
			ImageView imView = (ImageView)rowView.findViewById(R.id.icon);
			
			VideoItem item = items.get(position);			
			nameText.setText(item.getFileName());
			linkText.setText(item.getLink());
			imView.setImageBitmap(item.getPreview());
			
			return rowView;	
		}

		@Override
		public int getCount() {
			return items != null ? items.size() : 0;
		}

		@Override
		public VideoItem getItem(int position) {
			return items != null ? items.get(position) : null;
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}
		
		public void setNewItems(final ArrayList<VideoItem> newItems) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					items = newItems;
					notifyDataSetChanged();
					videoReadProgress.dismiss();
				}
			});
		}
		
	}
}
