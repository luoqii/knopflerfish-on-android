package org.bangbang.song.knopflerfish;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.knopflerfish.framework.FrameworkFactoryImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.service.startlevel.StartLevel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * 
 * totally inspired by http://nilvec.com/embedding-osgi-into-an-android-application-part-1.html
 * 
 * @author bangbang.song@gmail.com
 *
 */
public class KFWrapper {
	private static final String TAG = KFWrapper.class.getSimpleName();
	private static KFWrapper sInstance;
	private static final String OSGI_BUNDLE_CACHE_DIR = "osgi_bundlecache";
	private Framework mFramework;
	private Application mApp;
	private String mCacheDir;

	public static KFWrapper getInstance(Application application) {
		if (null == sInstance) {
			sInstance = new KFWrapper(application);
		}
		return sInstance;
	}
	
	private KFWrapper(Application application) {
		mApp = application;
		initKF(application);
	}

	private void initKF(Application application) {
		mCacheDir = application.getDir(OSGI_BUNDLE_CACHE_DIR, Context.MODE_WORLD_WRITEABLE).toString();

		Map parameters = new HashMap();
		parameters.put(Constants.FRAMEWORK_STORAGE, mCacheDir);
		FrameworkFactory ff = new FrameworkFactoryImpl();
		mFramework = ff.newFramework(parameters);
		try {
			mFramework.init();
		} catch (BundleException be) {
			be.printStackTrace();
		}
		
		setInitlevel(1);
		installBundle("event_all-4.0.1.jar");
		startBundle("event_all-4.0.1.jar");
		// install/start other bundles...

		setStartLevel(10);

		try {
		    mFramework.start();
		} catch (BundleException be) {
		    Log.e(TAG, be.toString());
		    // framework start failed
		}

		Log.d(TAG, "OSGi framework running, state: " + mFramework.getState());
		
		Bundle[] bundles = mFramework.getBundleContext().getBundles();
		for (Bundle b : bundles) {
			Log.d(TAG, "b: " + toString(b));
		}
	}
	
	private String toString(Bundle b) {
		return "bundle ID: " + b.getBundleId() + " L: " + b.getLocation();
	}

	private void startBundle(String bundle) {
	    Log.d(TAG, "starting bundle " + bundle);
	    InputStream bs;
	    try {
	        bs = mApp.getAssets().open("felix/preloadbundle/" + bundle);
	    } catch (IOException e) {
	        Log.e(TAG, e.toString());
	        return;
	    }

	    long bid = -1;
	    Bundle[] bl = mFramework.getBundleContext().getBundles();
	    for (int i = 0; bl != null && i < bl.length; i++) {
	        if (bundle.equals(bl[i].getLocation())) {
	            bid = bl[i].getBundleId();
	        }
	    }

	    Bundle b = mFramework.getBundleContext().getBundle(bid);
	    if (b == null) {
	        Log.e(TAG, "can't start bundle " + bundle);
	        return;
	    }

	    try {
	        b.start(Bundle.START_ACTIVATION_POLICY);
	        Log.d(TAG, "bundle " + b.getSymbolicName() + "/" + b.getBundleId() + "/"
	                + b + " started");
	    } catch (BundleException be) {
	        Log.e(TAG, be.toString());
	    }

	    try {
	        bs.close();
	    } catch (IOException e) {
	        Log.e(TAG, e.toString());
	    }
	}
	
	private void installBundle(String bundle) {
	    Log.d(TAG, "installing bundle " + bundle);
	    InputStream bs;
	    try {
	        bs = mApp.getAssets().open("felix/preloadbundle/" + bundle);
	    } catch (IOException e) {
	        Log.e(TAG, e.toString());
	        return;
	    }

	    try {
	        mFramework.getBundleContext().installBundle(bundle, bs);
	        Log.d(TAG, "bundle " + bundle + " installed");
	    } catch (BundleException be) {
	        Log.e(TAG, be.toString());
	    }

	    try {
	        bs.close();
	    } catch (IOException e) {
	        Log.e(TAG, e.toString());
	    }
	}

	private void setStartLevel(int startLevel) {
	    ServiceReference sr = mFramework.getBundleContext()
	        .getServiceReference(StartLevel.class.getName());
	    if (sr != null) {
	        StartLevel ss =
	            (StartLevel)mFramework.getBundleContext().getService(sr);
	        ss.setStartLevel(startLevel);
	        mFramework.getBundleContext().ungetService(sr);
	    } else {
	        Log.e(TAG, "No start level service " + startLevel);
	    }
	}

	private void setInitlevel(int level) {
	    ServiceReference sr = mFramework.getBundleContext()
	        .getServiceReference(StartLevel.class.getName());
	    if (sr != null) {
	        StartLevel ss =
	            (StartLevel)mFramework.getBundleContext().getService(sr);
	        ss.setInitialBundleStartLevel(level);
	        mFramework.getBundleContext().ungetService(sr);
	        Log.d(TAG, "initlevel " + level + " set");
	    } else {
	        Log.e(TAG, "No start level service " + level);
	    }
	}
}
