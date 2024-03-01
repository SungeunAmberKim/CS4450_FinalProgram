package cs4450_finalprogram;

public class Block {
    /**
     * The definition of a block, its type and position
     */
    private boolean isActive;
    private BlockType type;
    private float x,y,z;
    
    public enum BlockType {
        BlockType_Grass(0),
        BlockType_Sand(1),
        BlockType_Water(2),
        BlockType_Dirt(3),
        BlockType_Stone(4),
        BlockType_Bedrock(5),
        BlockType_Cactus(6),
        BlockType_Default(7);

        private int blockID;
        BlockType(int i) {
            blockID=i;
        }
        public int getID(){
            return blockID;
        }
        public void setID(int i){
            blockID = i;
        }
    }
    
    public Block(BlockType type){
        /**
         * Constructor of a Block
         */
        this.type = type;
    }
    
    public void setCords(float x, float y, float z){
        /**
         * Set the coordinates of the block
         */
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public float[] getCords(){
        return new float[]{x, y, z};
    }
    
    public boolean isActive(){
        /**
         * Check of a block is active
         */
        return isActive;
    }
    
    public void setActive(boolean active){
        /**
         * Set the active variable of a block
         */
        isActive=active;
    }
    public int getID(){
        /**
         * Get the ID of the type of this block
         */
        return type.getID();
    }
    public void setID(int i){
        /**
         * Set the ID of a block
         */
        type.setID(i);
    }
}
