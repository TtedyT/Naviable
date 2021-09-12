import cmd, sys
import json
from math import sqrt
from pathlib import Path

ADD_EDGE = 'E'
ADD_NODE = 'N'
YES = 'Y'
NO = 'N'

class GraphJsonShell(cmd.Cmd):
    intro = 'Welcome to the GraphJson shell. Type help or ? to list commands.\n'
    prompt = '(graph) '
    file = None
    nodes = {}  # id -> {"name" : str, "x" : float, "y" : float, "mappable" : boolean, "adjacency" : set}

    # --- shell commands ----
    def do_nodes(self, arg):
        'Print the existing nodes'
        print(self.nodes)

    def do_add(self, arg):
        'Add an edge or node to the graph'
        to_add = None
        while True:
            print("Would you like to add an edge (and its nodes) or a single node?")
            to_add = input("Edge (%s) / Node (%s): " % (ADD_EDGE, ADD_NODE))
            if to_add == ADD_EDGE or to_add == ADD_NODE:
                break
            else:
                print("Sorry, I didn't understand that.")

        if to_add == ADD_EDGE:
            new_node1, new_node2 = self.add_edge()

            self.nodes[new_node1["name"]] = new_node1
            self.nodes[new_node2["name"]] = new_node2

        elif to_add == ADD_NODE:
            new_node = self.add_node()
            self.nodes[new_node["name"]] = new_node

    @staticmethod
    def filter_edge_with_name(edge, name):
        return edge["dest_id"] != name

    def do_delete(self, arg):
        'Delete an edge or node from the graph'
        to_delete = None
        while True:
            print("Would you like to delete an edge or a node?")
            to_delete = input("Edge (%s) / Node (%s): " % (ADD_EDGE, ADD_NODE))
            if to_delete == ADD_EDGE or to_delete == ADD_NODE:
                break
            else:
                print("Sorry, I didn't understand that.")

        if to_delete == ADD_EDGE:
            while True:
                node1_name = input("Name of the edge's 1st node: ")
                node1 = self.nodes.get(node1_name)
                if node1:
                    break
                else:
                    print("Sorry, that name does not exist.")

            while True:
                node2_name = input("Name of the edge's 2nd node: ")
                node2 = self.nodes.get(node2_name)
                if node2:
                    break
                else:
                    print("Sorry, that name does not exist.")

            # Remove the edge from the src node's adjacency set
            adjacency_list = self.nodes[node1_name]["adjacency"]
            adjacency_list = list(filter(lambda item: filter_edge_with_name(item, node2_name), adjacency_list))
            self.nodes[node1_name]["adjacency"] = adjacency_list

            # delete_nodes = input("Do you want to delete the nodes too? (Y/N): ")
            # if delete_nodes == YES:
            #     del self.nodes[node1_name]
            #     del self.nodes[node2_name]

        elif to_delete == ADD_NODE:
            while True:
                node_name = input("Name of the node: ")
                try:
                    del self.nodes[node_name]
                    # TODO: go over all the nodes and remove from adjacency list
                    break
                except KeyError:
                    print("Sorry, that name does not exist.")

    def do_save(self, arg):
        'Save the current nodes'
        self.write_json_files()

    def do_quit(self, arg):
        'Exit the shell'
        print('Thank you for using the GraphJson shell')
        self.close()
        return True

    # --- data management methods ----
    def add_node(self):
        name = input("Location name: ")
        existing_node = self.nodes.get(name)
        if existing_node:
            print(existing_node)
            edit = input("The node already exists. Do you want to overwrite? (Y/N): ")
            if edit == NO:
                return existing_node

        while True:
            try:
                coords = input("Coordinates (x, y): ")
                x_coord, y_coord = coords.split(',')
                break
            except ValueError:
                print("Sorry, please enter coordinates separated by a comma.")

        while True:
            response = input("Is it mappable? (Y/N): ")
            if response == YES:
                mappable = True
                break
            elif response == NO:
                mappable = False
                break
            else:
                print("Sorry, please enter 'Y' or 'N'.")

        node = {"name": name, "x": float(x_coord), "y": float(y_coord), "mappable": mappable, "adjacency": []}
        return node

    def add_edge(self):
        print("Adding the 1st node...")
        src = self.add_node()
        print("Adding the 2nd node...")
        dest = self.add_node()

        while True:
            distance_type = input("Enter E for Euclidean distance or your own: ")
            if distance_type == 'E':
                x_dist = src["x"] - dest["x"]
                y_dist = src["y"] - dest["y"]
                distance = sqrt(x_dist ** 2 + y_dist ** 2)
                break
            else:
                try:
                    distance = float(distance_type)
                    break
                except ValueError:
                    print("Please enter a number or E")

        print("Adding directions...")
        directions = []
        while True:
            description = input("Add next direction description or type 'end': ")
            if description == 'end':
                break
            else:
                type = input("What is the type? ")
                direction = {"description": description, "type": type}
                directions.append(direction)

        edge = {"dest_id": dest["name"], "directions": directions, "distance": distance}
        src["adjacency"].append(edge)
        return src, dest

    def read_json_files(self):
        nodes_file = Path('nodes.json')

        if nodes_file.exists():
            f_nodes = open(nodes_file, )
            self.nodes = json.load(f_nodes)
            f_nodes.close()

    def write_json_files(self):
        nodes_json = json.dumps(self.nodes, indent=4, sort_keys=True)
        with open("nodes.json", "w") as outfile:
            outfile.write(nodes_json)

    # ----- record and playback -----
    def preloop(self):
        self.read_json_files()

    def postloop(self):
        self.write_json_files()

    def precmd(self, line):
        line = line.lower()
        if self.file and 'playback' not in line:
            print(line, file=self.file)
        return line

    def close(self):
        pass

def parse(arg):
    'Convert a series of zero or more numbers to an argument tuple'
    return tuple(map(int, arg.split()))

if __name__ == '__main__':
    GraphJsonShell().cmdloop()