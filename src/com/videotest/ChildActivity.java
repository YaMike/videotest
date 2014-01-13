package com.videotest;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class ChildActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.child_activity);

		Uri uri=Uri.parse(getIntent().getExtras().getString("open"));
		MediaController mc = new MediaController(this);
		VideoView videoView=(VideoView)findViewById(R.id.videoView);
		videoView.setVideoURI(uri);
		videoView.setMediaController(mc);
		videoView.start();
	}
}
