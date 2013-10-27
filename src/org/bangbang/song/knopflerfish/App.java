package org.bangbang.song.knopflerfish;

import android.app.Application;

public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		KFWrapper.getInstance(this);
	}

}
