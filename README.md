# Foxhole-SRPG
Foxhole Simplified Region Path Generation.
This tool is used to generate a graph from a simplefied image of a region in foxhole.

The program use images of foxhole's regions to generate a graph that connect all the main points of the marked region. This can be later used for getting the shortest path from a determinated point of a region to another point. Note that the program does not implement any search algorithm. This program only generate the data of the graph behind the region.

The program use 2 different colors for moving along the paths and marking nodes:
    Red (255.0, 0.0, 0.0): The red is used only for marking a node. It must be 1 pixel. This mark where a node of the graph should be.
    Green (0.0, 255.0, 0.0): The green is used for marking the path of the node. The line must not excede 5 pixel wide.
    
The map that is used by the algorithm should be simplified (not all the roads can be marked, only the main ones), because the algorithm use "space around point" for moving, so having 2 near lines could compromise the final result.

To see what the algorithm generate, the world implement a method called "printLinkMap" that generate a map of the region with all the connection created by the algorithm.
