package yad.optics.illusion;

/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

/**
 * Activity for testing OpenGL ES drawing speed.  This activity sets up sprites 
 * and passes them off to an OpenGLSurfaceView for rendering and movement.
 */
public class OpenGLTestActivity extends Activity {
    private final static int SPRITE_WIDTH = 64;
    private final static int SPRITE_HEIGHT = 64;
    private final static int BACKGROUND_WIDTH = 512;
    private final static int BACKGROUND_HEIGHT = 512;
    private Activity activity;
    private Context context;

    private GLSurfaceView mGLSurfaceView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mGLSurfaceView = new GLSurfaceView(this);
        SimpleGLRenderer spriteRenderer = new SimpleGLRenderer(this);

        activity = this;
        context = this;
        
        // Clear out any old profile results.
        ProfileRecorder.sSingleton.resetAll();
        
        final Intent callingIntent = getIntent();
        // Allocate our sprites and add them to an array.
        final boolean animate = callingIntent.getBooleanExtra("animate", true);
        final boolean useVerts = 
            callingIntent.getBooleanExtra("useVerts", false);
        final boolean useHardwareBuffers = 
            callingIntent.getBooleanExtra("useHardwareBuffers", false);
        
        Grid spriteGrid = null;
        if (useVerts) {
            // Setup a quad for the sprites to use.  All sprites will use the
            // same sprite grid intance.
            spriteGrid = new Grid(2, 2);
            spriteGrid.set(0, 0,  0.0f, 0.0f, 0.0f, 0.0f , 1.0f);
            spriteGrid.set(1, 0, SPRITE_WIDTH, 0.0f, 0.0f, 1.0f, 1.0f);
            spriteGrid.set(0, 1, 0.0f, SPRITE_HEIGHT, 0.0f, 0.0f, 0.0f);
            spriteGrid.set(1, 1, SPRITE_WIDTH, SPRITE_HEIGHT, 0.0f, 1.0f, 0.0f);
        }
        
        // We need to know the width and height of the display pretty soon,
        // so grab the information now.
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        
        // Now's a good time to run the GC.  Since we won't do any explicit
        // allocation during the test, the GC should stay dormant and not
        // influence our results.
        Runtime r = Runtime.getRuntime();
        r.gc();
        
        spriteRenderer.setVertMode(useVerts, useHardwareBuffers);
        
  /*      
        mGLSurfaceView.setZOrderOnTop(true);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        mGLSurfaceView.setRenderer(spriteRenderer);
        mGLSurfaceView.setRenderMode(
        0//GLSurfaceView.RENDERMODE_WHEN_DIRTY
        );
*/        
        boolean useBackground = false;
        if(useBackground)
        {
	        //mGLSurfaceView.setEGLConfigChooser( 8, 8, 8, 8, 16, 0);
	        mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
	        mGLSurfaceView.setZOrderOnTop(true);
	        //mGLSurfaceView.setZOrderMediaOverlay(true);
        }
        mGLSurfaceView.setRenderer(spriteRenderer);

        /*
        if (animate) {
            Mover simulationRuntime = new Mover();
            simulationRuntime.setRenderables(renderableArray);
            
            simulationRuntime.setViewSize(dm.widthPixels, dm.heightPixels);
            mGLSurfaceView.setEvent(simulationRuntime);
        }
        */
        
        //ставим рекламу
        AdView admobView = new AdView(activity, AdSize.BANNER, "ca-app-pub-3194728467757569/6452305330");
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
		    RelativeLayout.LayoutParams.WRAP_CONTENT, 
		    RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		admobView.setLayoutParams(lp);

		final RelativeLayout layout = new RelativeLayout(context);
		
		layout.addView(mGLSurfaceView);
		layout.addView(admobView);
		admobView.loadAd(new AdRequest());
		
        setContentView(layout);
        //setContentView(mGLSurfaceView);
    }
}
