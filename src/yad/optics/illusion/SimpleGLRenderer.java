package yad.optics.illusion;

/*
 * Copyright (C) 2008 The Android Open Source Project
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * An OpenGL ES renderer based on the GLSurfaceView rendering framework.  This
 * class is responsible for drawing a list of renderables to the screen every
 * frame.  It also manages loading of textures and (when VBOs are used) the
 * allocation of vertex buffer objects.
 */
public class SimpleGLRenderer implements GLSurfaceView.Renderer {
	
	private float[] lightDiffuseColor = {0.99f, 0.99f, 0.99f, 0};
	private float[] lightAmbientColor = {0.92f, 0.92f, 0.92f, 0};
	private float[] lightPosition = {30, 30, 30, 30};
	private float lightSpecular[] = new float[] { 0.97f, 0.97f, 0.97f, 1 };
	private float lightDirection[] = new float[] {0.0f, 0.0f, -1.0f};
	private float matAmbient[] = new float[] { 0.93f, 0.93f, 0.93f, 1.0f };
	private float matDiffuse[] = new float[] { 0.96f, 0.96f, 0.96f, 1.0f };
    
	private final int ISOMETRIC_MODE = 0;//X-справа вниз, Y-вверх, Z-слева вниз  
	private final int PERSPECTIVE_MODE = 1;  //with background
	private final int AngleStep = 5;
	
	private int MODE = 0;
	
    // Specifies the format our textures should be converted to upon load.
    private static BitmapFactory.Options sBitmapOptions
        = new BitmapFactory.Options();
    // An array of things to draw every frame.
    // Pre-allocated arrays to use at runtime so that allocation during the
    // test can be avoided.
    private int[] mTextureNameWorkspace;
    private int[] mCropWorkspace;
    // A reference to the application context.
    private Context mContext;
    // Determines the use of vertex arrays.
    private boolean mUseVerts;
    // Determines the use of vertex buffer objects.
    private boolean mUseHardwareBuffers;
    
    private int Width;
    private int Height;
    private CubeColorSides mCube = new CubeColorSides();
    private CubeNoLeft mCubeNoLeft = new CubeNoLeft();
    private CubeNoTop mCubeNoTop = new CubeNoTop();
    
    private float mCubeRotation;

    //private Cube cube;
    private static float angleCube = 0;    
    private static float speedCube = -1.5f;   // Rotational speed for cube (NEW)
    //private Square      square;     // the square
    private FloatBuffer textureBuffer;	// buffer holding the texture coordinates
    private float texture[] = {    		
    		// Mapping coordinates for the vertices
    		0.0f, 1.0f,		// top left		(V2)
    		0.0f, 0.0f,		// bottom left	(V1)
    		1.0f, 1.0f,		// top right	(V4)
    		1.0f, 0.0f		// bottom right	(V3)
    };

    
    public SimpleGLRenderer(Context context) {
        // Pre-allocate and store these objects so we can use them at runtime
        // without allocating memory mid-frame.
        mTextureNameWorkspace = new int[1];
        mCropWorkspace = new int[4];
        
        // Set our bitmaps to 16-bit, 565 format.
        sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        
        mContext = context;
        
        //this.square     = new Square();
    }
    
    public int[] getConfigSpec() {
        // We don't need a depth buffer, and don't care about our
        // color depth.
        //int[] configSpec = { EGL10.EGL_DEPTH_SIZE, 0, EGL10.EGL_NONE };
        int[] configSpec = { EGL10.EGL_DEPTH_SIZE, 1, EGL10.EGL_NONE };
        /*
    	int[] configSpec = {
                EGL10.EGL_RED_SIZE, 5,
                EGL10.EGL_GREEN_SIZE, 6,
                EGL10.EGL_BLUE_SIZE, 5,
                EGL10.EGL_DEPTH_SIZE, 16,
                // Requires that setEGLContextClientVersion(2) is called on the view.
                EGL10.EGL_RENDERABLE_TYPE, 4, // EGL_OPENGL_ES2_BIT
                EGL10.EGL_SAMPLE_BUFFERS, 1, // true
                EGL10.EGL_SAMPLES, 2,
                EGL10.EGL_NONE
        };
        */
        return configSpec;
    }
    
    /** 
     * Changes the vertex mode used for drawing.  
     * @param useVerts  Specifies whether to use a vertex array.  If false, the
     *     DrawTexture extension is used.
     * @param useHardwareBuffers  Specifies whether to store vertex arrays in
     *     main memory or on the graphics card.  Ignored if useVerts is false.
     */
    public void setVertMode(boolean useVerts, boolean useHardwareBuffers) {
        mUseVerts = useVerts;
        mUseHardwareBuffers = useVerts ? useHardwareBuffers : false;
    }

	private void DrawRotatingAxes(GL10 gl){
	    float scale = 1.0f;
	    if(MODE == ISOMETRIC_MODE) scale = 100; 
	
	    float a = Scene.Instance().GetAngleHorizontal();
	    float b = Scene.Instance().GetAngleVertical();
	    
	    //вращение платформы по часовой стрелке
	    //Y
		gl.glLoadIdentity();
	    gl.glTranslatef((100-Width/2)/100.0f, (100-Height/2)/100.0f, -9.0f);
	    gl.glRotatef(45.0f, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
	    gl.glRotatef(a, 0.0f, 1.0f, 0.0f);
	    gl.glScalef(scale*0.3f, scale*1.0f, scale*0.3f);
	    mCube.draw(gl);
	
		//Z
		gl.glLoadIdentity();
	    gl.glTranslatef((100-Width/2)/100.0f, (100-Height/2)/100.0f, -9.0f);
	    gl.glRotatef(45.0f, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
	    gl.glRotatef(a, 0.0f, 0.0f, 1.0f);
	    gl.glScalef(scale*0.3f, scale*0.3f, scale*1.0f);
	    mCube.draw(gl);
	
	    //X
		gl.glLoadIdentity();
	    gl.glTranslatef((100-Width/2)/100.0f, (100-Height/2)/100.0f, -9.0f);
	    gl.glRotatef(45.0f, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
	    gl.glRotatef(a, 1.0f, 0.0f, 0.0f);
	    gl.glScalef(scale*1.0f, scale*0.3f, scale*0.3f);
	    mCube.draw(gl);
	    
	}
	
	private void DrawLevel2(GL10 gl){ 
	    float scale = 1.0f;
	    if(MODE == ISOMETRIC_MODE) scale = 50;
	
	    float a = Scene.Instance().GetAngleHorizontal();
	    float b = Scene.Instance().GetAngleVertical();

	    //Y
		gl.glLoadIdentity();
	    gl.glRotatef(45.0f, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
	    gl.glRotatef(a, 0.0f, 0.0f, 1.0f);
	    gl.glTranslatef(0.0f, 0.0f, 0.0f);
	    gl.glScalef(scale*0.3f, scale*2.0f, scale*0.3f);
	    mCube.draw(gl);

	    gl.glLoadIdentity();
	    gl.glRotatef(45.0f, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(45.0f, 0.0f, 0.0f, 1.0f);
	    gl.glRotatef(a, 0.0f, 1.0f, 0.0f);
	    gl.glTranslatef(50.0f, 85.0f, 0.0f);
	    gl.glScalef(scale*1.0f, scale*0.3f, scale*0.3f);
	    mCube.draw(gl);
	
	    
	}
	
	private void DrawLevel1(GL10 gl){
	    float scale = 1.0f;
	    if(MODE == ISOMETRIC_MODE) scale = 100;
	
	    float a = Scene.Instance().GetAngleHorizontal();
	    float b = Scene.Instance().GetAngleVertical();

	    float m = 0.25f;

	    //http://en.wikipedia.org/wiki/Isometric_projection
	    //X-справа, Y-вверх, Z-слева
	    float angleX = 35.264f;//-60.0f;//35.264f
	    float angleY = -45.0f;//0.0f;//-45.0
	    float angleZ = 0.0f;//-135.0f;
	    
	    //angleX = 0.0f;
	    //angleZ = 0.0f;
	    
	    //0,0,0
	    gl.glLoadIdentity();
	    gl.glTranslatef(0, 0, 0);
	    gl.glRotatef(-45.0f, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(-45.0f, 0.0f, 0.0f, 1.0f);
	    gl.glScalef(0.07f * scale, 0.07f * scale, 0.07f * scale);
	    mCube.draw(gl);

	    //isometric
	    gl.glLoadIdentity();
	    gl.glTranslatef(0, 0, 0);
	    gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
	    gl.glRotatef(angleZ, 0.0f, 0.0f, 1.0f);
	    gl.glPushMatrix();
	    
	    //100,0,0
	    gl.glTranslatef(100, 0, 0);
	    gl.glRotatef(b, 1.0f, 0.0f, 0.0f);
	    gl.glScalef(100, 0.1f * scale, 0.1f * scale);
	    mCube.draw(gl);

	    //0,100,0
	    gl.glPopMatrix();
	    gl.glRotatef(a, 0.0f, 1.0f, 0.0f);
	    gl.glTranslatef(0, 100, 0);
	    gl.glScalef(0.1f * scale, 100, 0.1f * scale);
	    mCube.draw(gl);
	    
	    //0,0,100	
	    //gl.glPopMatrix() не срабатывает
	    gl.glLoadIdentity();
	    gl.glTranslatef(0, 0, 0);
	    gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
	    gl.glRotatef(angleZ, 0.0f, 0.0f, 1.0f);
	    gl.glTranslatef(0, 0, 100);
	    gl.glScalef(0.1f * scale, 0.1f * scale, 100);
	    mCube.draw(gl);
	}

	private void DrawLevel3(GL10 gl){
	    float scale = 1.0f;
	    if(MODE == ISOMETRIC_MODE) scale = 100;
	
	    float a = Scene.Instance().GetAngleHorizontal();
	    float b = Scene.Instance().GetAngleVertical();

	    a = (int)(a/AngleStep) * AngleStep;
	    float m = 0.25f;

	    //http://en.wikipedia.org/wiki/Isometric_projection
	    //X-справа, Y-вверх, Z-слева
	    float angleX = 35.264f;
	    float angleY = -45;
	    
	    //isometric

	    //A	
	    gl.glLoadIdentity();
	    gl.glTranslatef(0, 0, 0);
	    gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
	    gl.glTranslatef(0, 0, 60);
	    gl.glScalef(10, 10, 20);
	    mCube.draw(gl);
	    //G
	    gl.glLoadIdentity();
	    gl.glTranslatef(0, 0, 0);
	    gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
	    if(a<-90)
	    	gl.glTranslatef(180, 180, 200);
	    else
	    	gl.glTranslatef(180, 180, 200);
	    gl.glScalef(10, 10, 20);
	    mCubeNoLeft.draw(gl);

	    //B
	    gl.glLoadIdentity();
	    gl.glTranslatef(0, 0, 0);
	    gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
	    gl.glTranslatef(70, 0, 80);
	    gl.glScalef(80, 10, 10);
	    mCube.draw(gl);

	    //C
	    gl.glLoadIdentity();
	    gl.glTranslatef(0, 0, 0);
	    gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
	    gl.glTranslatef(140, 40, 80);
	    gl.glScalef(10, 30, 10);
	    mCube.draw(gl);

	    //D
	    gl.glLoadIdentity();
	    gl.glTranslatef(0, 0, 0);
	    gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
	    gl.glTranslatef(30, 140, 80);
	    gl.glScalef(40, 10, 10);
	    mCube.draw(gl);

	    //E
	    gl.glLoadIdentity();
	    gl.glTranslatef(0, 0, 0);
	    gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
	    gl.glTranslatef(110, 140, 80);
	    gl.glRotatef(a, 1.0f, 0.0f, 0.0f);
	    gl.glScalef(40, 10, 10);
	    mCube.draw(gl);
	    
	    //F
	    gl.glLoadIdentity();
	    gl.glTranslatef(0, 0, 0);
	    gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
	    gl.glTranslatef(140, 140, 80);
	    gl.glRotatef(a, 1.0f, 0.0f, 0.0f);
	    gl.glTranslatef(0, -30, 0);//точка вращеня - назад на 35
	    gl.glScalef(10, 40, 10);
	    mCube.draw(gl);
	    
	    //колонны
	    //1
	    gl.glLoadIdentity();
	    gl.glTranslatef(0, 0, 0);
	    gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
	    gl.glTranslatef(0, 75, 90);
	    gl.glScalef(1, 60, 1);
	    mCube.draw(gl);

	    //2-1 верхняя часть средней колонны
	    gl.glLoadIdentity();
	    gl.glTranslatef(0, 0, 0);
	    gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
	    gl.glTranslatef(0, 100, 75);
	    gl.glScalef(1, 50, 1);
	    mCube.draw(gl);
	    //2-2 - нижняя часть 2-й колонны
	    gl.glLoadIdentity();
	    gl.glTranslatef(0, 0, 0);
	    gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
	    gl.glTranslatef(20, 60, 95);
	    gl.glScalef(1, 40, 1);
	    mCubeNoTop.draw(gl);

	    //3
	    gl.glLoadIdentity();
	    gl.glTranslatef(0, 0, 0);
	    gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
	    gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
	    gl.glTranslatef(15, 75, 75);
	    gl.glScalef(1, 60, 1);
	    mCube.draw(gl);
	}
	
	public void drawFrame(GL10 gl) {
    	gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);        
        
        //mCubeRotation = Scene.Instance().GetAngle();

        gl.glLoadIdentity(); 
        //DrawSprites(gl);
        //DrawRotatingAxes(gl);

        DrawLevel3(gl);
        
        gl.glLoadIdentity(); 
            
    }
    /* Called when the size of the window changes. */
    public void sizeChanged(GL10 gl, int width, int height) {
    	//http://androidcookbook.com/Recipe.seam;jsessionid=C0EC047F8349134EE99E8F376C828E3F?recipeId=1529
    	Width = width;
    	Height = height;

    	gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

    	//3D, с перспективой
    	if(MODE == PERSPECTIVE_MODE)
    	{
	        GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);
	        gl.glViewport(0, 0, width, height);
    	}
    	else
    	{
	    	//плоский вид
	        gl.glOrthof(-width/2, width/2, -height/2, height/2, -1000.0f, 1000.0f);
	        gl.glShadeModel(GL10.GL_SMOOTH);
	        if(true)
	        {
		        gl.glEnable(GL10.GL_BLEND);
		        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		        gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);
		        gl.glEnable(GL10.GL_TEXTURE_2D);
		        gl.glEnable(GL10.GL_DEPTH_TEST);
	        }
	        
	        gl.glEnable(GL10.GL_LIGHTING);
	    	gl.glEnable(GL10.GL_LIGHT0); 
	    	gl.glEnable(GL10.GL_COLOR_MATERIAL);
	    	gl.glEnable(GL10.GL_BLEND);
	    	    	
	    	gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, matAmbient, 0);
	    	gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, matDiffuse, 0);
	    	         
	    	gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbientColor, 0);
	    	gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuseColor, 0);
	    	gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0);
	    	
	    	gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, lightSpecular, 0);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0);   
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPOT_DIRECTION, lightDirection, 0);
			gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_CUTOFF, 180f);  
			gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_EXPONENT, 100f);
			gl.glEnable(GL10.GL_DEPTH_TEST);   
			gl.glDepthFunc(GL10.GL_LESS);
			gl.glDisable(GL10.GL_DITHER);
    	}
    	
		gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
	
        //GLU.gluLookAt(gl, 0.0f, 0.0f, -1000.0f, 0.0f, 0.0f, 0.0f, 0, 0, 1);
	}
    
    @Override
    public void surfaceCreated(GL10 gl) {
    	
    	// Load the texture for the square
    	//square.loadGLTexture(gl, mContext, R.drawable.level03);
    	
    	gl.glEnable(GL10.GL_TEXTURE_2D);			//Enable Texture Mapping ( NEW )
    	gl.glShadeModel(GL10.GL_SMOOTH); 			//Enable Smooth Shading
    	//gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 	//Black Background
    	//gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
    	gl.glEnable(GL10.GL_DEPTH_TEST); 			//Enables Depth Testing
    	gl.glDepthFunc(GL10.GL_LEQUAL); 			//The Type Of Depth Testing To Do
    	
    	//Really Nice Perspective Calculations
    	gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST); 
    	/*
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 
            
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);

        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                  GL10.GL_NICEST);*/
    }

    /**
     * Called when the rendering thread shuts down.  This is a good place to
     * release OpenGL ES resources.
     * @param gl
     */
    public void shutdown(GL10 gl) {
        
    }
 
    /** 
     * Loads a bitmap into OpenGL and sets up the common parameters for 
     * 2D texture maps. 
     */
    protected int loadBitmap(Context context, GL10 gl, int resourceId) {
        int textureName = -1;
        if (context != null && gl != null) {
            gl.glGenTextures(1, mTextureNameWorkspace, 0);

            textureName = mTextureNameWorkspace[0];
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textureName);

            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

            gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);

            InputStream is = context.getResources().openRawResource(resourceId);
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(is, null, sBitmapOptions);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    // Ignore.
                }
            }

            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

            mCropWorkspace[0] = 0;
            mCropWorkspace[1] = bitmap.getHeight();
            mCropWorkspace[2] = bitmap.getWidth();
            mCropWorkspace[3] = -bitmap.getHeight();
            
            bitmap.recycle();

            ((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D, 
                    GL11Ext.GL_TEXTURE_CROP_RECT_OES, mCropWorkspace, 0);

            
            int error = gl.glGetError();
            if (error != GL10.GL_NO_ERROR) {
                Log.e("SpriteMethodTest", "Texture Load GLError: " + error);
            }
        
        }

        return textureName;
    }
}
