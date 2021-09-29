# Naviable
Naviable is an android application that helps people with disabilities navigate through
the campuses of The Hebrew University of Jerusalem

## Installation
Feel free to clone and edit our project using
```bash
git clone https://github.com/TtedyT/Naviable.git
```

## Usage
The application uses a user-made directed graph in order to navigate the campuses, the graph can be
extended further/modified using the file edit_graph.py found in `app/src/main/assets/edit_graph.py`
which will in turn generate a graph.json file that will be used by our app, run it using the command
```bash
python3 edit_graph.py
```

### Using App
Simple choose your destination and source, and you'll get a list of directions to get to that area.
you can also choose to scan a QR code that updates your source destination if there's one bearby
available in your campus. You can also choose to read the directions using Text-To-Speech.
You can also change your starting position using the settings menu.

### Editing Graph 

#### Creating nodes/edges ####
You can choose the command ```> Add Edge``` to add an edge between two existing nodes, or
create the two nodes. Using the command ```> Add Node``` adds a node without any edges connecting
to or from it.

#### Deleting nodes/edges ####
You can choose the command ```> Delete Edge``` to add an edge between a src and dest node existing nodes, or
create the two nodes. Using the command ```> Delete Node``` removes the nodes and all edges connecting to or
from it.

#### Node Attributes ####
- Coordinates: coordinate of point on google map, used to calculate distance and draw path
- Mappable: whether the node should be searched/seen on the google map (for example stairs can be a node
  but they should not be shown on map)
- Category: what the node represents, used to show the node on map in different way (default node has NONE category)

#### Edge Attributes ####
- Distance: can either be euclidean (automatically calculated from coordinates) or custom (determined by user)
  used by algorithm to find best path
- Directions: hand-written directions that tell you how to get from node A to B (directed)
- Type: Accessible type of direction (ramp, elevator, go straight.. etc), used to show relevant icon on app

## Contributors
This App was made by Tadassa Tafarra, Ahmad Okosh and Jason Greenspan. Special thanks to Reem Halamish and
Amnon Dekel for their support in the creation of this application.
