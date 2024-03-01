package cs4450_finalprogram;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.glu.GLU;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

public class Basic3D {
    private FPCameraController fp;
    private DisplayMode displayMode;
    private FloatBuffer whiteLight;
    
    
    public static void main(String[] args) {
        Basic3D basic = new Basic3D();
        basic.start();
    }
    
    
    public void start() {
        try {
            createWindow();
            initGL();
            fp = new FPCameraController(-30f,-30f,-30f);
            //chunk = new Chunk(0, 0, 0); // Create a chunk at position (0, 0, 0) 
            fp.gameLoop();//render();
        } catch (Exception e) {
            System.out.println("The game loop could not be started");
        }
    }
    
    private void createWindow() throws Exception{
        Display.setFullscreen(false);
        DisplayMode d[] = Display.getAvailableDisplayModes();
        for (DisplayMode d1 : d) {
            if (d1.getWidth() == 640 && d1.getHeight() == 480 && d1.getBitsPerPixel() == 32) {
                displayMode = d1;
                break;
            }
        }
        Display.setDisplayMode(displayMode);
        Display.setTitle("Final Project");
        Display.create();
    }
    
    private void initLightArrays() {
        whiteLight = BufferUtils.createFloatBuffer(4);
        whiteLight.put(2.0f).put(2.0f).put(2.0f).put(0.0f).flip();
    }
    
    private void initGL() {
        initLightArrays();
        glLight(GL_LIGHT0, GL_SPECULAR, whiteLight);//sets our specular light
        glLight(GL_LIGHT0, GL_DIFFUSE, whiteLight);//sets our diffuse light
        glLight(GL_LIGHT0, GL_AMBIENT, whiteLight);//sets our ambient light
        glEnable(GL_LIGHTING);//enables our lighting
        glEnable(GL_LIGHT0);//enables light0

        glClearColor(0.2f, 0.3f, 0.6f, 0.0f);
        glMatrixMode(GL_PROJECTION);
        glEnable(GL_DEPTH_TEST); //add this line at the end of the initL() method
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glLoadIdentity();
        GLU.gluPerspective(100.0f, (float)displayMode.getWidth()/(float)displayMode.getHeight(), 0.1f, 300.0f);
        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        glEnable(GL_TEXTURE_2D);
        glEnableClientState (GL_TEXTURE_COORD_ARRAY);
    }
    
    
}
