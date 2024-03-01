package cs4450_finalprogram;
import java.util.Random;

public class PerlinNoiseGenerator {

    private final int P = 512;
    private int[] permutation;

    PerlinNoiseGenerator(){
        permutation = new int[P * 2];
        // Generate permutation table
        Random random = new Random();
        for (int i = 0; i < P; i++) {
            permutation[i] = i;
        }

        // Shuffle the array
        for (int i = P - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = temp;
        }
            
        // Duplicate the permutation table to avoid overflow
        System.arraycopy(permutation, 0, permutation, P, P);
    }

    public double noise(double x, double y) {
        // Determine grid cell coordinates
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;

        // Relative coordinates in grid cell
        x -= Math.floor(x);
        y -= Math.floor(y);

        // Compute fade curves for x and y
        double u = fade(x);
        double v = fade(y);

        // Hash coordinates of the 4 cube corners
        int a = permutation[X] + Y;
        int b = permutation[X + 1] + Y;

        // And add blended results from 2 corners of the cube
        return lerp(v, lerp(u, grad(permutation[a], x, y), grad(permutation[b], x - 1, y)),
                lerp(u, grad(permutation[a + 1], x, y - 1), grad(permutation[b + 1], x - 1, y - 1)));
    }

    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private double grad(int hash, double x, double y) {
        int h = hash & 15;
        // Convert lower 4 bits of hash into 12 gradient directions
        double grad = 1 + (h & 7); // Gradient value 1-8
        if ((h & 8) != 0) grad = -grad; // Randomly invert half of them
        // Use gradients to get dot product of gradient and input vector
        return (grad * x + grad * y);
    }

    public void main(String[] args) {
        // Example usage
        int width = 10;
        int height = 10;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double value = noise(i * 0.1, j * 0.1); // Adjust frequency by multiplying with a constant
                System.out.printf("%.2f ", value);
            }
            System.out.println();
        }
    }
}
