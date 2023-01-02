package main.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Link {
    private Node mainNode;
    private List<Node> linkedNodes = new ArrayList<>();

    public Link(Node mainNode) {
        this.mainNode = mainNode;
    }
    public Link(Node mainNode, List<Node> linkedNodes) {
        this.mainNode = mainNode;
        this.linkedNodes = linkedNodes;
    }
    public Link(Node mainNode, Node secondLink) {
        this.mainNode = mainNode;
        linkedNodes.add(secondLink);
    }

    public Node getMainNode() {
        return mainNode;
    }
    public List<Node> getLinkedNodes() {
        return linkedNodes;
    }

    public void addConnection(Node n) {
        if(!isConnectedToNode(n))
            linkedNodes.add(n);
    }
    public boolean isConnectedToNode(Node node) {
        boolean found = false;
        for (Node c: linkedNodes) {
            found = Objects.equals(node.toString(), c.toString());
        }
        return found;
    }

    public void removeConnection(Node n) {
        for (Node n2: linkedNodes) {
            if(n2.toString().equals(n.toString())) {
                linkedNodes.remove(n2);
                return;
            }
        }
    }
    public void removeConnection(List<Node> n) {
        linkedNodes.removeAll(n);
    }

    @Override
    public String toString() {
        String retString = "Main node: " + mainNode + " linked to: ";
        for (Node n: linkedNodes)
            retString += n + " ";
        return retString;
    }
}
