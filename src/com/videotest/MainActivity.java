package com.videotest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.view.Menu;
import android.widget.ListView;


public class MainActivity extends Activity {

	private ListView mainListView = null;
	private VideoItems videoItems = null;
	private final String url = "http://tangohacks.com/videofeed";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mainListView = (ListView)findViewById(R.id.listview);
		new VideoListLoader().execute(url);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	class VideoListLoader extends AsyncTask<String, Void, VideoItems> {

		private ProgressDialog videoReadProgress = null;

		@Override
		protected VideoItems doInBackground(String... params) {
			return new VideoItems(url);
		}

		@Override
		protected void onPostExecute(VideoItems result) {
			videoReadProgress.dismiss();
			videoReadProgress = null;
			videoItems = result;
		}
		
		@Override
		protected void onPreExecute() {
			videoReadProgress = ProgressDialog.show(MainActivity.this, "Please wait...", "Updating list of video...", true);
		}
	}
}
