package main.generation;

public class Node {
    public int x;
    public int y;
    public boolean alreadyMapped = false;

    public Node(double x, double y) {
        this.x = (int)x;
        this.y = (int)y;
    }

    @Override
    public String toString() {
        return x + " " + y;
    }
}
