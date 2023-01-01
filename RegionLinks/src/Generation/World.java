package Generation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class World {
    private List<Node> nodes = new ArrayList<>();
    private List<Link> links = new ArrayList<>();
    private BufferedImage image = null;
    private String region;

    private WorldGeneration generation = new WorldGeneration();

    public void addNode(Node n) {
        boolean found = false;
        for (Node nodeEx: nodes)
            if(nodeEx.toString().equals(n.toString())) //same node!
                found = true;
        if(!found)
            nodes.add(n);
    }
    public boolean nodeAlreadyParsed(Node n) {
        for (Node nodeEx: nodes)
            if(nodeEx.toString().equals(n.toString())) //same node!
                return nodeEx.alreadyMapped;
        return false;
    }
    public void parseNode(Node n) {
        for (Node nodeEx: nodes)
            if(nodeEx.toString().equals(n.toString())) //same node!
                nodeEx.alreadyMapped = true;
    }
    public void createLink(Node n1, Node n2) {
        for (Link link: links) {
            if(link.getMainNode().toString().equals(n1.toString())) {
                link.addConnection(n2);
                return;
            }
        }
        //in case we don't get any match with the link, we have to create a new one
        links.add(new Link(n1, n2));
    }

    public int getNodesNumber() {
        return nodes.size();
    }
    public int getLinksNumber() {
        return links.size();
    }

    public void mergeLinks() {
        //we have to check for all duplicated methods
        //n1 -> n2 is equal to n2 -> n1
        List<Link> removeTheseLinks = new ArrayList<>();

        for(int i = 0; i < links.size(); i++) {
            Link verifyLink = links.get(i);
            for(int j = 0; j < links.size(); j++) {
                if(j > i) { //we need to take in consideration only the nodes after the ones that we have already verified
                    Link fixedLink = links.get(j);
                    //now that we have the one to be checked and the fidex one, we have to check all the nodes
                    for (Node n: verifyLink.getLinkedNodes())
                        if(n.toString().equals(fixedLink.getMainNode().toString())) //check if a node is the mainnode of fixedLink
                            if(fixedLink.isConnectedToNode(verifyLink.getMainNode()))//if yes, then check if mainnode is present in fixedling
                                removeTheseLinks.add(new Link(verifyLink.getMainNode(), n));

                }
            }
        }

        System.out.println("\nmerged links: " + removeTheseLinks.size());

        for (Link removed: removeTheseLinks) //we have to cicle all the removed links to remove them
            for (Link link: links)
                if(link.getMainNode() == removed.getMainNode())
                    link.removeConnection(removed.getLinkedNodes());


    }


    /**
     * Set the image of the region that need to be generated
     * @param region Region of the world
     */
    public void setRegion(String region) {
        this.region = region.toUpperCase().replace(" ", "-");
        File file = new File("src/resources/maps/" + this.region + ".png");
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            System.out.println("World is unable to load a not existing image!\nBe sure to have putted the image in folder src/resources/maps/ and retry.");
            System.exit(1);
        }
    }

    public void generateWorld() {
        generation.parse();
        mergeLinks();
    }
    
    public void printNodes() {
        System.out.println("Nodes: ");
        for (Node n: nodes)
            System.out.println(n);
    }
    public void printLinks() {
        System.out.println("Links: ");
        for (Link l: links)
            System.out.println(l);
    }

    public void printPathMap() {
        if(image == null) { //first, check if the region has been setted before.
            System.out.println("You need to set the region first, before printing the map");
            System.exit(1);
        }

        //initialize
        BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        File f = null;

        //get the values of all pixels. then match if they are part of the path
        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++)
            {
                // generating values less than 256
                int clr = image.getRGB(x, y);
                int red = (clr & 0x00ff0000) >> 16;
                int green = (clr & 0x0000ff00) >> 8;
                int blue = clr & 0x000000ff;

                //pixel
                int p = (200<<24) | (red<<16) | (green<<8) | blue;
                if(generation.isPathPixel(clr))
                    img.setRGB(x, y, p);
            }

        // write image
        try
        {
            new File("src/resources/generated_maps/" + region).mkdirs();
            f = new File("src/resources/generated_maps/" + region + "/" + region + "_PATH.png");
            ImageIO.write(img, "png", f);
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e);
        }
    }

    public void printLinkMap() {
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        try {
            bufferedImage = ImageIO.read(new File("src/resources/maps/" + region + ".png"));
        } catch (IOException e) {
            System.out.println("cannot read main image");
            System.exit(1);
        }

        Graphics g2d = bufferedImage.getGraphics();

        g2d.setColor(Color.BLACK);

        for (Link l: links)
            for (Node linked: l.getLinkedNodes()) {
                g2d.drawLine(l.getMainNode().x, l.getMainNode().y, linked.x, linked.y);
            }


        g2d.dispose();
        try {
            new File("src/resources/generated_maps/" + region).mkdirs();
            File f = new File("src/resources/generated_maps/" + region + "/" + region + "_LINKS.png");
            ImageIO.write(bufferedImage, "png", f);
        }
        catch (Exception e) {
            System.out.println("Unable to print image with links: " + e.getMessage());
        }
    }




//============================================================================================================================================================
//============================================================================================================================================================
//============================================================================================================================================================
//============================================================================================================================================================



    class WorldGeneration {
        private int nodeSearchLenght = 35; //size of the box that search for other nodes
        private int boxSearchLenght = 20; //size of the box that search for other parts of the path
        private double aggregatePointDistance = 5;


        public void parse() {
            Node startingPoint = getStartingPoints();
            watchAround(startingPoint.x, startingPoint.y, startingPoint ,null);
        }

        private void watchAround(int centerX, int centerY, Node lastNode, Node lastWorldNode) {
            boolean blockExecution = false;
            Node maxDistanceNode = null;
            double maxDistance = -1;
            List<Node> points = new ArrayList<>();


            //First things first, let's check if there is a red near my position
            for(int x = -nodeSearchLenght/2; x < nodeSearchLenght/2; x++) {
                for(int y = -nodeSearchLenght/2; y < nodeSearchLenght/2; y++) {
                    try {
                        int clr = image.getRGB(centerX + x, centerY + y);
                        if(isNodePixel(clr)) {
                            //When a connection has been found, we have to add it to the links.
                            //also, we have to do a search all around to determinate all the possible ways we can take
                            //To do that, we have to look like the previus ones and then aggregate the nearest ones, based on distance
                            Node n = new Node(centerX + x, centerY + y);
                            addNode(n); //we add the new node to the world. The world will handle it
                            if(!nodeAlreadyParsed(n)) { //we have to check if the node has been already parsed (maybe another root have already found it)
                                parseNode(n); //if not, it's time to parse it
                                detectDirections(n.x, n.y);
                                blockExecution = true;
                            }
                            else {
                                //System.out.println("è stato trovato un nodo rosso che è già stato analizzato");
                                if(lastWorldNode != null) {
                                    if(!lastWorldNode.toString().equals(n.toString())) {
                                        blockExecution = true;
                                        createLink(lastWorldNode, n);
                                    }
                                }
                            }
                        }
                    }
                    catch (Exception ignored) {
                        //got a pixel outside image. Ignore it
                    }
                }
            }


            if(!blockExecution) {
                //cicle all the box around the center. You don't know the direction to choose, so the known point is in the middle
                points = getAllPathNodesInBox(new Node(centerX, centerY), boxSearchLenght);

                //after collecting all points, we now get the furthest
                for (Node c: points) {
                    double distance = distance(c, lastNode);
                    if(distance > maxDistance) {
                        maxDistance = distance;
                        maxDistanceNode = c;
                    }
                }
                //System.out.println("furthest: " + maxDistanceNode.x + " " + maxDistanceNode.y + "\n");
                watchAround(maxDistanceNode.x, maxDistanceNode.y, new Node(centerX, centerY), lastWorldNode);
            }
        }


        private void detectDirections(int centerX, int centerY) {
            List<Node> points = checkPathBoxAroundCenter(new Node(centerX, centerY), nodeSearchLenght);
            //now, with all points, it must aggregate them based on a distance threshold
            List<Node> expandWays = new ArrayList<>();
            for (Node newCoord: points) {
                if (expandWays.size() != 0) {
                    //if aggregated have already some elements, we have to check the distance between all elements inside aggregated and the coordiante
                    //if is more that the distance, then it's a new path
                    boolean less = false;
                    for (Node alreadyAg: expandWays) {
                        if(distance(newCoord, alreadyAg) < aggregatePointDistance) {
                            alreadyAg.x = (alreadyAg.x + newCoord.x) / 2;
                            alreadyAg.y = (alreadyAg.y + newCoord.y) / 2;
                            less = true;
                            break;
                        }
                    }
                    if(!less)
                        expandWays.add(newCoord);
                }
                else //in case it's the first element, add it
                    expandWays.add(newCoord);
            }

            for (Node n : expandWays)
                watchAround(n.x, n.y, new Node(centerX, centerY), new Node(centerX, centerY));

        }

        /**
         * Method used for getting the starting point
         * @return Starting point
         */
        private Node getStartingPoints() {
            //starting taking the point in the middle
            int midImageX = image.getWidth() / 2;
            int midImageY = image.getHeight() / 2;
            Node center = new Node(midImageX, midImageY);

            //now we have to find 1 point that is in the path
            boolean found = false;
            int size = 6; //just a random starting number
            do {
                List<Node> points = checkPathBoxAroundCenter(center, size);
                if (points.size() > 0)
                    return points.get(0); //Just take the first one
                else
                    size += 4; //if not found, increase the size
            } while (true);
        }


        /**
         * Method used for getting all the path points that stay on the borders of a box.
         * @param center Center of the box.
         * @param boxSize Size of the box side.
         * @return List containing all the points of the path on the border.
         */
        private List<Node> checkPathBoxAroundCenter(Node center, int boxSize) {
            List<Node> points = new ArrayList<>();
            int midSize = boxSize/2;
            //all the try-catch are required because if point is outside the box, can throw exception
            for(int x = -midSize; x < midSize; x++) {
                try {
                    int clr = image.getRGB(center.x + x, center.y + midSize);
                    if(isPathPixel(clr))
                        points.add(new Node(center.x + x, center.y + midSize));
                }
                catch (Exception ignored) {}
            }
            //get all points on bottom side
            for(int x = -midSize; x < midSize; x++) {
                try {
                    int clr = image.getRGB(center.x + x, center.y - midSize);
                    if(isPathPixel(clr))
                        points.add(new Node(center.x + x, center.y - midSize));
                }
                catch (Exception ignored) {}
            }
            //get all points on left side
            for(int y = -midSize; y < midSize; y++) {
                try {
                    int clr = image.getRGB(center.x - midSize, center.y + y);
                    if(isPathPixel(clr))
                        points.add(new Node(center.x  - midSize, center.y + y));
                }
                catch (Exception ignored) {}
            }
            //get all points on right side
            for(int y = -midSize; y < midSize; y++) {
                try {
                    int clr = image.getRGB(center.x + midSize, center.y + y);
                    if(isPathPixel(clr))
                        points.add(new Node(center.x + midSize, center.y + y));
                }
                catch (Exception ignored) {}
            }
            return points;
        }


        /**
         * Method is used for getting all the path points that stay inside a box
         * @param center Center of the box.
         * @param boxSize Size of the box side
         * @return All the path points
         */
        private List<Node> getAllPathNodesInBox(Node center, int boxSize) {
            List<Node> points = new ArrayList<>();
            for(int x = -boxSize/2; x < boxSize/2; x++) {
                for(int y = -boxSize/2; y < boxSize/2; y++) {
                    //get the color of the pixel
                    try {
                        int clr = image.getRGB(center.x + x, center.y + y);
                        if(isPathPixel(clr))
                            points.add(new Node(center.x + x, center.y + y));
                    }
                    catch (Exception e) {
                        //out of bounds of image. not a problem, ignore this point
                    }
                }
            }
            return points;
        }



        public boolean isNodePixel(int clr) {
            int red = (clr & 0x00ff0000) >> 16;
            int green = (clr & 0x0000ff00) >> 8;
            int blue = clr & 0x000000ff;
            return (red == 255 && green == 0 && blue == 0);
        }
        public boolean isPathPixel(int clr) {
            int red = (clr & 0x00ff0000) >> 16;
            int green = (clr & 0x0000ff00) >> 8;
            int blue = clr & 0x000000ff;
            return (red == 0 && blue == 0 && green == 255);
            //return (red > 180 && green > 160 && blue > 115) && ((red < 220 && green < 200 && blue < 155));
        }
        private double distance(Node a, Node b) {
            return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
        }
    }
}
