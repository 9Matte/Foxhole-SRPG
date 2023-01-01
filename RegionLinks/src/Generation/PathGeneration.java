package Generation;


public class PathGeneration {
    public static World world = new World();

    public static void main(String[] args) {
        String region = "Callahans";

        world.setRegion(region);
        world.generateWorld();

        System.out.println("\n\n\n");
        System.out.println("Nodes found: " + world.getNodesNumber());
        System.out.println("Links generated: " + world.getLinksNumber());

        world.printLinkMap();
    }
}
