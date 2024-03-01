package cs4450_finalprogram;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;


public class FPCameraController {
    /**
     * Class for the camera controller as well as the game loop
     */
    private Vector3f position = null; //3D vector to store the camera's position in
    private Vector3f lPosition = null; //3D vector to store the light's position in
    private float yaw = 0.0f; //the rotation around the Y axis of the camera
    private float pitch = 0.0f; //the rotation around the X axis of the camera
    private final FloatBuffer lightBuffer;
    
    public FPCameraController(float x, float y, float z){
        /**
         * instantiate position Vector3f to the x y z parameters
         */
        
        position = new Vector3f(x, y, z);
        lPosition = new Vector3f(x,y,z);
        //float[] temp = initialLPosition.get();
        //System.out.println(temp);
        lPosition.x = x;
        lPosition.y = y;
        lPosition.z = z;
        lightBuffer = BufferUtils.createFloatBuffer(4);
        lightBuffer.put(lPosition.x).put(lPosition.y).put(lPosition.z).put(1.0f).flip();
        glLight(GL_LIGHT0, GL_POSITION, lightBuffer); //sets our light’s position
        lightBuffer.clear();
    }
    
    public void yaw(float amount){
        /**
         * increment the yaw by the amount 
         */
        yaw += amount;
    }
    
    public void pitch(float amount){
        /**
         * increment the pitch by the amount
         */
        pitch -= amount;
    }
    
    public void walkForward(float distance){
        /**
         * moves the camera forward relative to its current rotation (yaw)
         */
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw));
        position.x -= xOffset;
        position.z += zOffset;
    }
    
    public void walkBackwards(float distance){
        /**
         * moves the camera backward relative to its current rotation (yaw)
         */
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw));
        position.x += xOffset;
        position.z -= zOffset;
    }
    
    public void strafeLeft(float distance){
        /**
         * strafes the camera left relative to its current rotation (yaw)
         */
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw-90));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw-90));
        position.x -= xOffset;
        position.z += zOffset;
    }
    
    public void strafeRight(float distance){
        /**
         * strafes the camera right relative to its current rotation (yaw)
         */
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw+90));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw+90));
        position.x -= xOffset;
        position.z += zOffset;
    }
    
    public void moveUp(float distance){
        /**
         * moves the camera up relative to its current rotation (yaw)
         */
        position.y -= distance;
    }
    
    public void moveDown(float distance){
        /**
         * moves the camera down
         */
        position.y += distance;
    }
    
    //translates and rotate the matrix so that it looks through the camera
    //this does basically what gluLookAt() does
    public void lookThrough(){
        //roatate the pitch around the X axis
        glRotatef(pitch, 1.0f, 0.0f, 0.0f);
        //roatate the yaw around the Y axis
        glRotatef(yaw, 0.0f, 1.0f, 0.0f);
        //translate to the position vector's location
        glTranslatef(position.x, position.y, position.z);
    }
    
    public void gameLoop(){
        FPCameraController camera = new FPCameraController(position.getX(), position.getY(), position.getZ());
        float dx;
        float dy;
        int chunkX= 6; //Set amount of chunks in the X direction
        int chunkZ= 6; //Set amount of chunks in the Y direction
        Chunk[] chunks = new Chunk[(chunkX*chunkZ)]; //Create an array of chunks
        int index = 0;
        for (int i=0;i<chunkX;i++){ 
            for(int j=0;j<chunkZ;j++){
                chunks[index] = new Chunk(i*Chunk.CHUNK_SIZE*Chunk.CUBE_LENGTH, 0, j*Chunk.CHUNK_SIZE*Chunk.CUBE_LENGTH); //Create a new chunk
                chunks[index].rebuildMesh(); //Build the chunk
                index++;
            }
        }
        float mouseSensitivity = 0.04f;
        float movementSpeed = .7f;
        //hide the mouse
        Mouse.setGrabbed(true);
        // keep looping till the display window is closed the ESC key is down
        double dayRad = 0;
        while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){ //while the window is open and the escape key is not pressed
            
            dayRad += Math.PI/(Chunk.CHUNK_SIZE*Chunk.CUBE_LENGTH*chunkZ*(Math.PI+0.5)); //Increment the dayRad for the day/night cycle
            lPosition.z += Math.sin(dayRad)/2; //Set the light position
            lightBuffer.put(lPosition.x).put(lPosition.y).put(lPosition.z).put(1.0f).flip(); //Put the light position into the buffer
            glLight(GL_LIGHT0, GL_POSITION, lightBuffer); //Set the light position
            lightBuffer.clear(); //Clear the buffer
            
            //distance in mouse movement
            //from the last getDX() call.
            dx = Mouse.getDX();
            //distance in mouse movement
            //from the last getDY() call.
            dy = Mouse.getDY();
            //controll camera yaw from x movement fromt the mouse
            camera.yaw(dx * mouseSensitivity);
            //controll camera pitch from y movement fromt the mouse
            camera.pitch(dy * mouseSensitivity);
            //when passing in the distance to move
            //we times the movementSpeed with dt this is a time scale
            //so if its a slow frame u move more then a fast frame
            //so on a slow computer you move just as fast as on a fast computer
            
            if (Keyboard.isKeyDown(Keyboard.KEY_W)) //FORWARD
                camera.walkForward(movementSpeed);
            
            if (Keyboard.isKeyDown(Keyboard.KEY_S)) //BACKWARD
                camera.walkBackwards(movementSpeed);
            
            if (Keyboard.isKeyDown(Keyboard.KEY_A)) //LEFT
                camera.strafeLeft(movementSpeed);
            
            if (Keyboard.isKeyDown(Keyboard.KEY_D)) //RIGHT
                camera.strafeRight(movementSpeed);
            
            if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) //UP
                camera.moveUp(movementSpeed);
            
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_E)) //DOWN
                camera.moveDown(movementSpeed);
            
            //set the modelview matrix back to the identity
            glLoadIdentity();
            camera.lookThrough();
            //look through the camera before you draw anything
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            //you would draw your scene here.
            //render();
            for(Chunk part : chunks)
                part.render();

            Display.update();
            Display.sync(60);
        }
        Display.destroy();
    }
}