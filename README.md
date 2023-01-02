# Foxhole-SRPG
Foxhole Simplified Region Path Generation
This tool is used to generate a graph from a simplefied version of a region in foxhole.

The program generate all datas used for generate a graph. This can be used for getting the shortest path from a determinated point of a region to another point. Note that the program does not implement any search or anything else. This program only generate the data.

The map use 2 different colors for moving in the maps and mark nodes:
    Red (255.0, 0.0, 0.0): The red is used only for marking a node. It must be 1 pixel. This mark where a node of the graph should be.
    Green (0.0, 255.0, 0.0): The green is used for marking the path of the node. The line must not excede 5 pixel wide.
    
The map to be used by the algorithm should be simplified, the algorithm use "space around point" for moving, so having 2 near lines could compromise the final result.

To see what the algorithm generate, the world implement a method called "printLinkMap" that generate a map of the region with all the connection created by the algorithm.
