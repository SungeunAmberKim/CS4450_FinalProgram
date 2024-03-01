package cs4450_finalprogram;


import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;


public class Chunk {
    static final int CHUNK_SIZE = 30;
    static final int CUBE_LENGTH = 2;
    private final Block[][][] Blocks;
    private int VBOVertexHandle;
    private int VBOColorHandle;
    private final int StartX, StartY, StartZ;
    private Random r = new Random();
    private int VBOTextureHandle;
    private Texture texture;

    public Chunk(int startX, int startY, int startZ){
        try{
            texture = TextureLoader.getTexture("PNG",ResourceLoader.getResourceAsStream("src/terrain.png"));
        }
        catch(IOException e){
            System.out.print("Error: PNG not found for textures");
        }
        r = new Random();
        Blocks = new
        Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Stone);
                }
            }
        }
        VBOTextureHandle = glGenBuffers(); 
        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        this.StartX = startX;
        this.StartY = startY;
        this.StartZ = startZ;
    }
    
    public void render(){
        /**
         * Render an entire chunk
         */
        glPushMatrix();
        glBindBuffer(GL_ARRAY_BUFFER,VBOVertexHandle);
        glVertexPointer(3, GL_FLOAT, 0, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
        glColorPointer(3,GL_FLOAT, 0, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBindTexture(GL_TEXTURE_2D, 1);
        glTexCoordPointer(2,GL_FLOAT,0,0L);
        glDrawArrays(GL_QUADS, 0,CHUNK_SIZE *CHUNK_SIZE*CHUNK_SIZE * 24);
        glPopMatrix();
    }
    
    public void rebuildMesh() {
        SimplexNoise simplex = new SimplexNoise(30,.027f,r.nextInt(100));
        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        VBOTextureHandle = glGenBuffers();
        FloatBuffer VertexTextureData = BufferUtils.createFloatBuffer((CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE)* 6 * 12);
        FloatBuffer VertexPositionData = BufferUtils.createFloatBuffer((CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);
        FloatBuffer VertexColorData = BufferUtils.createFloatBuffer((CHUNK_SIZE* CHUNK_SIZE *CHUNK_SIZE) * 6 * 12);
        PerlinNoiseGenerator perlin = new PerlinNoiseGenerator();
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int height = (int)(StartY + (100 * simplex.getNoise(x,z)) * CUBE_LENGTH)+5;
                double value = perlin.noise(x * 0.07, z * 0.05);
                
                for(int y = 0; y <= height; y++){
                    if(y==0){
                        Blocks[x][y][z].setID(5);
                    }else if(value >= 0.06){
                        if (y==1)
                            Blocks[x][y][z].setID(2);
                        else
                            Blocks[x][y][z].setActive(false);
                    }else if (y!=height){
                        if(r.nextFloat()>0.7f){
                            Blocks[x][y][z].setID(3);
                        }else{
                            Blocks[x][y][z].setID(4);
                        }
                    }else{
                        if(r.nextFloat()>0.9f){
                            Blocks[x][y][z].setID(0);
                            for(int i=0;i<r.nextInt(6);i++){
                                Blocks[x][y+i][z].setID(6);
                                VertexPositionData.put(createCube((float) (StartX + x * CUBE_LENGTH),(float) (StartY+(y+i)*CUBE_LENGTH),(float) (StartZ + z * CUBE_LENGTH)));
                                VertexColorData.put(createCubeVertexCol(getCubeColor(Blocks[x][y+i][z])));
                                VertexTextureData.put(createTexCube(0,0,Blocks[x][y+i][z]));
                            }
                        }else{
                             Blocks[x][y][z].setID(1);
                        }
                            
                    }
                    if(value < 0.06 || y<=1){
                        VertexPositionData.put(createCube((float) (StartX + x * CUBE_LENGTH),(float) (StartY+y*CUBE_LENGTH),(float) (StartZ + z * CUBE_LENGTH)));
                        VertexColorData.put(createCubeVertexCol(getCubeColor(Blocks[x][y][z])));
                        VertexTextureData.put(createTexCube(0,0,Blocks[x][y][z]));
                    }
                }
            }
        }
        
     
        
        VertexTextureData.flip();
        VertexColorData.flip();
        VertexPositionData.flip();
        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexTextureData,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER,VBOVertexHandle);
        glBufferData(GL_ARRAY_BUFFER,VertexPositionData,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER,VBOColorHandle);
        glBufferData(GL_ARRAY_BUFFER,VertexColorData,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    
    
    private float[] createCubeVertexCol(float[] CubeColorArray) {
        float[] cubeColors = new float[CubeColorArray.length * 4 * 6];
        for (int i = 0; i < cubeColors.length; i++) {
            cubeColors[i] = CubeColorArray[i %CubeColorArray.length];
        }
        return cubeColors;
    }
    
    public static float[] createCube(float x, float y, float z) {
        int offset = CUBE_LENGTH / 2;
        return new float[] {
            // TOP QUAD
            x + offset, y + offset, z,
            x - offset, y + offset, z,
            x - offset, y + offset, z - CUBE_LENGTH,
            x + offset, y + offset, z - CUBE_LENGTH,
            // BOTTOM QUAD
            x + offset, y - offset, z - CUBE_LENGTH,
            x - offset, y - offset, z - CUBE_LENGTH,
            x - offset, y - offset, z,
            x + offset, y - offset, z,
            // FRONT QUAD
            x + offset, y + offset, z - CUBE_LENGTH,
            x - offset, y + offset, z - CUBE_LENGTH,
            x - offset, y - offset, z - CUBE_LENGTH,
            x + offset, y - offset, z - CUBE_LENGTH,
            // BACK QUAD
            x + offset, y - offset, z,
            x - offset, y - offset, z,
            x - offset, y + offset, z,
            x + offset, y + offset, z,
            // LEFT QUAD
            x - offset, y + offset, z - CUBE_LENGTH,
            x - offset, y + offset, z,
            x - offset, y - offset, z,
            x - offset, y - offset, z - CUBE_LENGTH,
            // RIGHT QUAD
            x + offset, y + offset, z,
            x + offset, y + offset, z - CUBE_LENGTH,
            x + offset, y - offset, z - CUBE_LENGTH,
            x + offset, y - offset, z 
        };
    }
    
    private float[] getCubeColor(Block block) {
        return switch (block.getID()) {
            case 6 -> new float[] { 0, 1, 0 };
            default -> new float[] { 1, 1, 1 };
        };
    }
    
    public static float[] createTexCube(float x, float y, Block block) {
        float offset = (1024f/16)/1024f;

        return switch (block.getID()) {
            case 1 -> new float[] { //SAND
                // TOP QUAD(DOWN=+Y)
                x + offset*3, y + offset*2,
                x + offset*2, y + offset*2,
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1,
                // BOTTOM!
                x + offset*3, y + offset*2,
                x + offset*2, y + offset*2,
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1,
                // FRONT QUAD
                x + offset*3, y + offset*2,
                x + offset*2, y + offset*2,
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1,
                // BACK QUAD
                x + offset*3, y + offset*2,
                x + offset*2, y + offset*2,
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1,
                // LEFT QUAD
                x + offset*3, y + offset*2,
                x + offset*2, y + offset*2,
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1,
                // RIGHT QUAD
                x + offset*3, y + offset*2,
                x + offset*2, y + offset*2,
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1,
            };
            case 2 -> new float[] {//WATER
                // TOP QUAD(DOWN=+Y)
                x + offset*15, y + offset*1,
                x + offset*14, y + offset*1,
                x + offset*14, y + offset*0,
                x + offset*15, y + offset*0,
                // BOTTOM!
                x + offset*15, y + offset*1,
                x + offset*14, y + offset*1,
                x + offset*14, y + offset*0,
                x + offset*15, y + offset*0,
                // FRONT QUAD
                x + offset*15, y + offset*1,
                x + offset*14, y + offset*1,
                x + offset*14, y + offset*0,
                x + offset*15, y + offset*0,
                // BACK QUAD
                x + offset*15, y + offset*1,
                x + offset*14, y + offset*1,
                x + offset*14, y + offset*0,
                x + offset*15, y + offset*0,
                // LEFT QUAD
                x + offset*15, y + offset*1,
                x + offset*14, y + offset*1,
                x + offset*14, y + offset*0,
                x + offset*15, y + offset*0,
                // RIGHT QUAD
                x + offset*15, y + offset*1,
                x + offset*14, y + offset*1,
                x + offset*14, y + offset*0,
                x + offset*15, y + offset*0,
            };
            case 3 -> new float[] {//DIRT
                // TOP QUAD(DOWN=+Y)
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // BOTTOM!
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // FRONT QUAD
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // BACK QUAD
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // LEFT QUAD
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // RIGHT QUAD
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
            };
            case 4 -> new float[] {//STONE
                // TOP QUAD(DOWN=+Y)
                x + offset*2, y + offset*1,
                x + offset*1, y + offset*1,
                x + offset*1, y + offset*0,
                x + offset*2, y + offset*0,
                // BOTTOM!
                x + offset*2, y + offset*1,
                x + offset*1, y + offset*1,
                x + offset*1, y + offset*0,
                x + offset*2, y + offset*0,
                // FRONT QUAD
                x + offset*2, y + offset*1,
                x + offset*1, y + offset*1,
                x + offset*1, y + offset*0,
                x + offset*2, y + offset*0,
                // BACK QUAD
                x + offset*2, y + offset*1,
                x + offset*1, y + offset*1,
                x + offset*1, y + offset*0,
                x + offset*2, y + offset*0,
                // LEFT QUAD
                x + offset*2, y + offset*1,
                x + offset*1, y + offset*1,
                x + offset*1, y + offset*0,
                x + offset*2, y + offset*0,
                // RIGHT QUAD
                x + offset*2, y + offset*1,
                x + offset*1, y + offset*1,
                x + offset*1, y + offset*0,
                x + offset*2, y + offset*0,
            };
            case 5 -> new float[] {//BEDROCK
                // TOP QUAD(DOWN=+Y)
                x + offset*2, y + offset*2,
                x + offset*1, y + offset*2,
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                // BOTTOM!
                x + offset*2, y + offset*2,
                x + offset*1, y + offset*2,
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                // FRONT QUAD
                x + offset*2, y + offset*2,
                x + offset*1, y + offset*2,
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                // BACK QUAD
                x + offset*2, y + offset*2,
                x + offset*1, y + offset*2,
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                // LEFT QUAD
                x + offset*2, y + offset*2,
                x + offset*1, y + offset*2,
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                // RIGHT QUAD
                x + offset*2, y + offset*2,
                x + offset*1, y + offset*2,
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
            };
            case 6 -> new float[] {//CACTUS
                x + offset*6, y + offset*5,
                x + offset*5, y + offset*5,
                x + offset*5, y + offset*4,
                x + offset*6, y + offset*4,
                // BOTTOM!
                x + offset*6, y + offset*5,
                x + offset*5, y + offset*5,
                x + offset*5, y + offset*4,
                x + offset*6, y + offset*4,
                // FRONT QUAD
                x + offset*7, y + offset*5,
                x + offset*6, y + offset*5,
                x + offset*6, y + offset*4,
                x + offset*7, y + offset*4,
                // BACK QUAD
                x + offset*7, y + offset*5,
                x + offset*6, y + offset*5,
                x + offset*6, y + offset*4,
                x + offset*7, y + offset*4,
                // LEFT QUAD
                x + offset*7, y + offset*5,
                x + offset*6, y + offset*5,
                x + offset*6, y + offset*4,
                x + offset*7, y + offset*4,
                // RIGHT QUAD
                x + offset*7, y + offset*5,
                x + offset*6, y + offset*5,
                x + offset*6, y + offset*4,
                x + offset*7, y + offset*4,
            };
            case 7 -> new float[] {//PUMPKIN
                // TOP QUAD(DOWN=+Y)
                x + offset*7, y + offset*7,
                x + offset*6, y + offset*7,
                x + offset*6, y + offset*6,
                x + offset*7, y + offset*6,
                // BOTTOM!
                x + offset*7, y + offset*8,
                x + offset*6, y + offset*8,
                x + offset*6, y + offset*7,
                x + offset*7, y + offset*7,
                // FRONT QUAD
                x + offset*8, y + offset*8,
                x + offset*7, y + offset*8,
                x + offset*7, y + offset*7,
                x + offset*8, y + offset*7,
                // BACK QUAD
                x + offset*7, y + offset*8,
                x + offset*6, y + offset*8,
                x + offset*6, y + offset*7,
                x + offset*7, y + offset*7,
                // LEFT QUAD
                x + offset*7, y + offset*8,
                x + offset*6, y + offset*8,
                x + offset*6, y + offset*7,
                x + offset*7, y + offset*7,
                // RIGHT QUAD
                x + offset*7, y + offset*8,
                x + offset*6, y + offset*8,
                x + offset*6, y + offset*7,
                x + offset*7, y + offset*7,
            };
            default -> new float[] {
                //TOP
                //  QUAD(DOWN=+Y)
                x + offset*3, y + offset*10,
                x + offset*2, y + offset*10,
                x + offset*2, y + offset*9,
                x + offset*3, y + offset*9,
                // BOTTOM!
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // FRONT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // BACK QUAD
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                // LEFT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // RIGHT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1
            };
        }; 
    }

    public int getStartX() {
        return StartX;
    }

    public int getStartY() {
        return StartY;
    }

    public int getStartZ() {
        return StartZ;
    }
    
}   

