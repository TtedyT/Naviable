import cmd, sys
import json
from pathlib import Path

import shortuuid

ADD_EDGE = 'E'
ADD_NODE = 'N'
YES = 'Y'
NO = 'N'

class GraphJsonShell(cmd.Cmd):
    intro = 'Welcome to the GraphJson shell. Type help or ? to list commands.\n'
    prompt = '(graph) '
    file = None
    nodes = {}
    edges = {}

    # --- shell commands ----
    def do_nodes(self, arg):
        'Print the existing nodes'
        print(self.nodes)

    def do_edges(self, arg):
        'Print the existing edges'
        print(self.edges)

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
            new_edge = self.add_edge()
            new_node1 = new_edge["first"]
            new_node2 = new_edge["second"]

            self.edges[shortuuid.uuid()] = new_edge
            self.nodes[new_node1["name"]] = new_node1
            self.nodes[new_node2["name"]] = new_node2

        elif to_add == ADD_NODE:
            new_node = self.add_node()
            self.nodes[new_node["name"]] = new_node

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

            for id, edge in list(self.edges.items()):
                if edge["first"] == node1 and edge["second"] == node2:
                    del self.edges[id]

            delete_nodes = input("Do you want to delete the nodes too? (Y/N): ")
            if delete_nodes == YES:
                del self.nodes[node1_name]
                del self.nodes[node2_name]

        elif to_delete == ADD_NODE:
            while True:
                node_name = input("Name of the node: ")
                try:
                    del self.nodes[node_name]
                    break
                except KeyError:
                    print("Sorry, that name does not exist.")

    def do_save(self, arg):
        'Save the current edges and nodes'
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

        node = {"name" : name, "x" : float(x_coord), "y" : float(y_coord), "mappable" : mappable}
        return node

    def add_edge(self):
        print("Adding the 1st node...")
        node1 = self.add_node()
        print("Adding the 2nd node...")
        node2 = self.add_node()

        print("Adding directions...")
        directions = []
        while True:
            description = input("Add next direction description or type 'end': ")
            if description == 'end':
                break
            else:
                type = input("Is the direction LEFT, RIGHT, or STRAIGHT? ")
                direction = {"description" : description, "type" : type}
                directions.append(direction)

        edge = {"first" : node1, "second" : node2, "directions" : directions}
        return edge

    def read_json_files(self):
        nodes_file = Path('nodes.json')
        edges_file = Path('edges.json')

        if nodes_file.exists():
            f_nodes = open(nodes_file, )
            self.nodes = json.load(f_nodes)
            f_nodes.close()

        if edges_file.exists():
            f_edges = open(edges_file, )
            self.edges = json.load(f_edges)
            f_edges.close()

    def write_json_files(self):
        nodes_json = json.dumps(self.nodes)
        with open("nodes.json", "w") as outfile:
            outfile.write(nodes_json)

        edges_json = json.dumps(self.edges)
        with open("edges.json", "w") as outfile:
            outfile.write(edges_json)


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