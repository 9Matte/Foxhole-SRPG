package main.generation;

public class PathGeneration {
    public static World world = new World();

    public static void main(String[] args) {
        String region = "westgate";

        world.setRegion(region);
        world.generateRegion();

        world.printLinkMap();
    }
}
