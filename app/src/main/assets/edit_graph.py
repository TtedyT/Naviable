# -*- coding: utf-8 -*-
from __future__ import print_function, unicode_literals

import math
import copy
import regex
import json
from pathlib import Path
from pprint import pprint

from PyInquirer import prompt, Separator
from prompt_toolkit.validation import Validator, ValidationError
from examples import custom_style_2

# ---- Main Options ----
ADD_EDGE = 'Add an edge'
ADD_NODE = 'Add a single node'
DELETE_EDGE = 'Delete an edge'
DELETE_NODE = 'Delete a node'
EDIT_EDGE = 'Edit an edge'
SAVE = 'Save changes'
UNDO = 'Undo last change'
REVERT = 'Revert to last save'
QUIT = 'Quit'
# ---- Changes ----
ADD_CHANGE = 0
DEL_CHANGE = 1
NO_NODES_CHANGED = 0
SRC_NODE_CHANGED = 1
DEST_NODE_CHANGED = 2
BOTH_NODES_CHANGED = 3
# ---- MapNode Fields ----
NODE_ID = 'name'
X_COORD = 'x'
Y_COORD = 'y'
MAPPABLE = 'mappable'
CATEGORY = 'category'
ADJACENCY_LIST = 'adjacency'
# ---- EdgeInfo Fields ----
DEST_ID = 'destId'
DIRECTIONS = 'directions'
DISTANCE = 'distance'
# ---- Direction Fields ----
DIRECTION_DESC = 'description'
DIRECTION_TYPE = 'type'

class CoordinatesValidator(Validator):
    def validate(self, document):
        ok = regex.match('^[+-]?([0-9]+\.?[0-9]*|\.[0-9]+)\s*,\s*[+-]?([0-9]+\.?[0-9]*|\.[0-9]+)$', document.text)
        if not ok:
            raise ValidationError(
                message='Please enter a two valid numbers separated by a comma',
                cursor_position=len(document.text))  # Move cursor to end


class DistanceValidator(Validator):
    def validate(self, document):
        ok = regex.match('^[+-]?([0-9]+\.?[0-9]*|\.[0-9]+)$', document.text)
        if not ok:
            raise ValidationError(
                message='Please enter a valid number',
                cursor_position=len(document.text))  # Move cursor to end



class GraphJsonShell():
    last_saved_nodes = {}
    current_nodes = {}
    last_change_type = None
    last_nodes_changed = None
    last_change_src = None
    last_change_dest = None

    def main_prompt(self):
        # Read in old json
        self.read_json_files()

        actions = {
            'type': 'list',
            'name': 'current_action',
            'message': 'What do you want to do?',
            'choices': [
                ADD_EDGE,
                ADD_NODE,
                DELETE_EDGE,
                DELETE_NODE,
                EDIT_EDGE,
                SAVE,
                {
                    'name': UNDO,
                    'disabled': 'Unavailable at this time'
                },
                REVERT,
                QUIT
            ]
        }

        answers = prompt(actions, style=custom_style_2)
        while answers['current_action'] != QUIT:
            self.do_action(answers['current_action'])
            answers = prompt(actions, style=custom_style_2)

        save_before_quit = prompt({
            'type': 'confirm',
            'message': 'Do you want to save before exiting?',
            'name': 'save',
            'default': True,
        }, style=custom_style_2)

        if save_before_quit['save']:
            self.save()

    def do_action(self, action):
        if action == ADD_EDGE:
            self.last_change_type = ADD_CHANGE
            self.last_nodes_changed = NO_NODES_CHANGED
            new_src, new_dest = self.add_edge()
            if new_src is None or new_dest is None:
                return
            print("Edge added")
            self.last_change_src = new_src[NODE_ID]
            self.last_change_dest = new_dest[NODE_ID]
            self.current_nodes[self.last_change_src] = new_src
            self.current_nodes[self.last_change_dest] = new_dest
        elif action == ADD_NODE:
            self.last_change_type = ADD_CHANGE
            self.last_nodes_changed = NO_NODES_CHANGED
            new_node = self.add_node()
            if new_node is None:
                return
            print("Node added")
            self.last_change_src = new_node[NODE_ID]
            self.last_change_dest = None
            self.current_nodes[self.last_change_src] = new_node
        elif action == DELETE_EDGE:
            self.last_change_type = DEL_CHANGE
            self.last_nodes_changed = NO_NODES_CHANGED
            src_id, dest_id = self.delete_edge()
            self.last_change_src = src_id
            self.last_change_dest = dest_id
        elif action == DELETE_NODE:
            self.last_change_type = DEL_CHANGE
            src_id = self.delete_node()
            self.last_change_src = src_id
            self.last_change_dest = None
            if src_id != "not":
                self.last_nodes_changed = NO_NODES_CHANGED
            print("Node %s deleted" % src_id)
        elif action == EDIT_EDGE:
            self.edit_edge()
        elif action == SAVE:
            self.save()
        elif action == UNDO:
            self.undo()
        elif action == REVERT:
            self.revert_changes()

    def add_edge(self):
        print("Adding the 1st node...")
        src = self.add_node()
        if src is None:
            return None, None
        print()
        print("Adding the 2nd node...")
        dest = self.add_node()
        if dest is None:
            return None, None
        print()

        edge_questions = [
            {
                'type': 'rawlist',
                'name': 'distance_type',
                'message': 'Do you want Euclidean distance or to enter one?',
                'choices': [
                    'Euclidean distance',
                    'Enter a distance',
                ]
            },
            {
                'type': 'input',
                'message': 'Distance: ',
                'name': DISTANCE,
                'when': lambda answers: answers['distance_type'] == 'Enter a distance',
                'validate': DistanceValidator,
                'filter': lambda val : float(val)
            },
            {
                'type': 'confirm',
                'message': 'Do you want to add this edge in the other direction?',
                'name': "bi_directional"
            }
        ]

        edge_answers = prompt(edge_questions, style=custom_style_2)
        print()
        if edge_answers['distance_type'] == 'Euclidean distance':
            x_dist = src["x"] - dest["x"]
            y_dist = src["y"] - dest["y"]
            distance = math.sqrt(x_dist ** 2 + y_dist ** 2)
        else:
            distance = edge_answers[DISTANCE]

        print("Adding directions from %s to %s..." % (src[NODE_ID], dest[NODE_ID]))
        directions = self.get_directions()
        edge = {DEST_ID: dest[NODE_ID], DIRECTIONS: directions, DISTANCE: distance}
        src[ADJACENCY_LIST].append(edge)

        if edge_answers['bi_directional']:
            print("Adding directions from %s to %s..." % (dest[NODE_ID], src[NODE_ID]))
            directions = self.get_directions()
            edge = {DEST_ID: src[NODE_ID], DIRECTIONS: directions, DISTANCE: distance}
            dest[ADJACENCY_LIST].append(edge)

        return src, dest

    def get_directions(self):
        directions = []
        while True:
            direction_questions = [
                {
                    'type': 'input',
                    'name': DIRECTION_DESC,
                    'message': 'Description: ',
                    'filter': lambda val: val.strip()
                },
                {
                    'type': 'rawlist',
                    'name': DIRECTION_TYPE,
                    'message': 'Type: ',
                    'choices': [
                        'STRAIGHT',
                        'RIGHT',
                        'LEFT',
                        'RAMP',
                        'ELEVATOR'
                    ],
                    'filter': lambda val: val.lower()
                },
                {
                    'type': 'confirm',
                    'message': 'Would you like to add another direction?',
                    'name': 'add_another',
                }
            ]
            direction_answers = prompt(direction_questions, style=custom_style_2)

            direction = {DIRECTION_DESC: direction_answers[DIRECTION_DESC],
                         DIRECTION_TYPE: direction_answers[DIRECTION_TYPE]}
            directions.append(direction)
            if not direction_answers['add_another']:
                break

        return directions

    def add_node(self):
        node_questions = [
            {
                'type': 'input',
                'name': NODE_ID,
                'message': 'Location name: ',
                'filter': lambda val: val.strip()
            },
            {
                'type': 'confirm',
                'message': 'The node already exists. Do you want to use it?',
                'name': 'use_existing_node',
                'default': False,
                'when': lambda answers: answers[NODE_ID] in self.current_nodes
            },
            {
                'type': 'confirm',
                'message': 'Do you want to add a different node?',
                'name': 'get_new_input',
                'when': lambda answers: answers[NODE_ID] in self.current_nodes and not answers['use_existing_node']
            },
            {
                'type': 'input',
                'name': 'coords',
                'message': 'Coordinates (x, y): ',
                'validate': CoordinatesValidator,
                'when': lambda answers: answers[NODE_ID] not in self.current_nodes
            },
            {
                'type': 'confirm',
                'name': MAPPABLE,
                'message': 'Is it mappable? ',
                'when': lambda answers: answers[NODE_ID] not in self.current_nodes
            },
            {
                'type': 'list',
                'name': CATEGORY,
                'message': 'SPECIAL CATEGORY: ',
                'choices': [
                    'NONE',
                    'TOILET',
                    'RESTAURANT',
                    'CAFE',
                    'LIBRARY'
                ],
                'filter': lambda val: val.lower(),
                'when': lambda answers: answers[NODE_ID] not in self.current_nodes
            },
            ]

        while True:
            node_answers = prompt(node_questions, style=custom_style_2)
            existing_node = self.current_nodes.get(node_answers[NODE_ID])
            if existing_node:
                if node_answers['use_existing_node']:
                    return existing_node
                elif node_answers['get_new_input']:
                    continue
                elif not node_answers['get_new_input']:
                    return None
            else:
                break

        x_coord, y_coord = node_answers['coords'].split(',')
        node = {NODE_ID: node_answers[NODE_ID],
                X_COORD: float(x_coord), Y_COORD: float(y_coord), MAPPABLE: node_answers[MAPPABLE],
                CATEGORY: node_answers[CATEGORY], ADJACENCY_LIST: []}
        self.last_nodes_changed += 1
        return node

    def edit_edge(self):
        edit_questions = [
            {
                'type': 'input',
                'name': 'src',
                'message': 'What is the name of the src node? ',
                'validate': lambda val: 'That name does not exist ' if val.strip() not in self.current_nodes else True,
                'filter': lambda val: val.strip()
            },
            {
                'type': 'input',
                'name': 'dest',
                'message': 'What is the name of the dest node? ',
                'validate': lambda val: 'That name does not exist ' if val.strip() not in self.current_nodes else True,
                'filter': lambda val: val.strip()
            },
            {
                'type': 'expand',
                'message': 'Do you want to edit the directions or the distance of the edge?',
                'name': 'edit',
                'default': 'd',
                'choices': [
                    {
                        'key': 'd',
                        'name': 'directions',
                        'value': 'dir'
                    },
                    {
                        'key': 'w',
                        'name': 'distance',
                        'value': 'dist'
                    }
                ]
            },
            {
                'type': 'rawlist',
                'name': 'distance_type',
                'message': 'Do you want Euclidean distance or to enter one?',
                'when': lambda answers: answers['edit'] == 'dist',
                'choices': [
                    'Euclidean distance',
                    'Enter a distance',
                ]
            },
            {
                'type': 'input',
                'message': 'Distance: ',
                'name': DISTANCE,
                'when': lambda answers: answers['edit'] == 'dist' and answers['distance_type'] == 'Enter a distance',
                'validate': DistanceValidator,
                'filter': lambda val: float(val)
            },
        ]

        edit_answers = prompt(edit_questions, style=custom_style_2)
        src_id = edit_answers['src']
        dest_id = edit_answers['dest']
        src = self.current_nodes[src_id]
        dest = self.current_nodes[dest_id]

        if edit_answers['edit'] == 'dist':
            if edit_answers['distance_type'] == 'Euclidean distance':
                x_dist = src["x"] - dest["x"]
                y_dist = src["y"] - dest["y"]
                distance = math.sqrt(x_dist ** 2 + y_dist ** 2)
            else:
                distance = edit_answers[DISTANCE]

            adjacency_list = src[ADJACENCY_LIST]
            for i, edge in enumerate(adjacency_list):
                if edge[DEST_ID] == dest_id:
                    adjacency_list[i][DISTANCE] = distance

        if edit_answers['edit'] == 'dir':
            print("Editing directions from %s to %s..." % (src[NODE_ID], dest[NODE_ID]))
            directions = self.get_directions()

            adjacency_list = src[ADJACENCY_LIST]
            for i, edge in enumerate(adjacency_list):
                if edge[DEST_ID] == dest_id:
                    adjacency_list[i][DIRECTIONS] = directions



    def delete_edge(self):
        delete_questions = [
            {
                'type': 'input',
                'name': 'src',
                'message': 'What is the name of the src node? ',
                'validate': lambda val: 'That name does not exist ' if val.strip() not in self.current_nodes else True,
                'filter': lambda val: val.strip()
            },
            {
                'type': 'input',
                'name': 'dest',
                'message': 'What is the name of the dest node? ',
                'validate': lambda val: 'That name does not exist ' if val.strip() not in self.current_nodes else True,
                'filter': lambda val: val.strip()
            },
            {
                'type': 'expand',
                'message': 'Do you want to delete any of the nodes?',
                'name': 'nodes',
                'default': 'n',
                'choices': [
                    {
                        'key': 'n',
                        'name': 'none of the nodes',
                        'value': 'none'
                    },
                    {
                        'key': 's',
                        'name': 'src node',
                        'value': 'src'
                    },
                    {
                        'key': 'd',
                        'name': 'dest node',
                        'value': 'dest'
                    },
                    {
                        'key': 'b',
                        'name': 'both nodes',
                        'value': 'both'
                    }
                ]
            }
            ]


        delete_answers = prompt(delete_questions, style=custom_style_2)
        src_id = delete_answers['src']
        dest_id = delete_answers['dest']

        # Remove edge
        adjacency_list = self.current_nodes[src_id][ADJACENCY_LIST]
        adjacency_list[:] = [edge for edge in adjacency_list if edge[DEST_ID] != dest_id]

        # Remove nodes
        if delete_answers['nodes'] != 'none':
            if delete_answers['nodes'] == 'src':
                self.delete_node(src_id)
                self.last_nodes_changed = SRC_NODE_CHANGED
            elif delete_answers['nodes'] == 'dest':
                self.delete_node(dest_id)
                self.last_nodes_changed = DEST_NODE_CHANGED
            elif delete_answers['nodes'] == 'both':
                self.delete_node(src_id)
                self.delete_node(dest_id)
                self.last_nodes_changed = BOTH_NODES_CHANGED

        return src_id, dest_id


    def delete_node(self, node_id=None):
        if node_id is None:
            answer = prompt({
                'type': 'input',
                'name': 'node_id',
                'message': 'Which node do you want to remove? ',
                'filter': lambda val: val.strip()
            })
            node_id = answer['node_id']

            if node_id not in self.current_nodes:
                print('That name does not exist.')
                return "not"

        # Remove node from adjacency lists
        for id, node in self.current_nodes.items():
            adjacency_list = node[ADJACENCY_LIST]
            adjacency_list[:] = [edge for edge in adjacency_list if edge[DEST_ID] != node_id]

        # Then remove the node itself
        del self.current_nodes[node_id]
        return node_id

    def save(self):
        nodes_json = json.dumps(self.current_nodes, indent=4, sort_keys=True)
        with open("nodes.json", "w") as outfile:
            outfile.write(nodes_json)
            self.last_saved_nodes = copy.deepcopy(self.current_nodes)

    def undo(self):
        if self.last_change_type == ADD_CHANGE:
            # last added was a single node
            if self.last_change_dest is None and self.last_nodes_changed == SRC_NODE_CHANGED:
                self.last_change_type = DEL_CHANGE
                self.delete_node(self.last_change_src)
            # last added was an edge
            elif self.last_change_dest is not None:
                self.last_change_type = DEL_CHANGE
                self.last_nodes_changed = NO_NODES_CHANGED
                # Remove edge
                adjacency_list = self.current_nodes[self.last_change_src][ADJACENCY_LIST]
                adjacency_list[:] = [edge for edge in adjacency_list if edge[DEST_ID] != self.last_change_dest]

                # Remove any added nodes
                if self.last_nodes_changed == SRC_NODE_CHANGED:
                    self.delete_node(self.last_change_src)
                    self.last_nodes_changed = SRC_NODE_CHANGED
                elif self.last_nodes_changed == DEST_NODE_CHANGED:
                    self.delete_node(self.last_change_dest)
                    self.last_nodes_changed = DEST_NODE_CHANGED
                elif self.last_nodes_changed == BOTH_NODES_CHANGED:
                    self.delete_node(self.last_change_src)
                    self.delete_node(self.last_change_dest)
                    self.last_nodes_changed = BOTH_NODES_CHANGED

        elif self.last_change_type == DEL_CHANGE:
            # TODO: undo delete
            pass
            # # last deleted was a single node
            # if self.last_change_dest is None and self.last_nodes_changed == SRC_NODE_CHANGED:
            #     self.last_change_type = ADD_CHANGE
            #     self.last_nodes_changed = NO_NODES_CHANGED
            #     self.add_node(self.last_change_src)
            # # last added was an edge
            # elif self.last_change_dest is not None:
            #     self.last_change_type = DEL_CHANGE
            #     self.last_nodes_changed = NO_NODES_CHANGED
            #     # Remove edge
            #     adjacency_list = self.current_nodes[self.last_change_src][ADJACENCY_LIST]
            #     adjacency_list[:] = [edge for edge in adjacency_list if edge[DEST_ID] != self.last_change_dest]
            #
            #     # Remove any added nodes
            #     if self.last_nodes_changed == SRC_NODE_CHANGED:
            #         self.delete_node(self.last_change_src)
            #         self.last_nodes_changed = SRC_NODE_CHANGED
            #     elif self.last_nodes_changed == DEST_NODE_CHANGED:
            #         self.delete_node(self.last_change_dest)
            #         self.last_nodes_changed = DEST_NODE_CHANGED
            #     elif self.last_nodes_changed == BOTH_NODES_CHANGED:
            #         self.delete_node(self.last_change_src)
            #         self.delete_node(self.last_change_dest)
            #         self.last_nodes_changed = BOTH_NODES_CHANGED

    def revert_changes(self):
        self.current_nodes = copy.deepcopy(self.last_saved_nodes)

    def read_json_files(self):
        nodes_file = Path('nodes.json')

        if nodes_file.exists():
            f_nodes = open(nodes_file, )
            self.current_nodes = json.load(f_nodes)
            self.last_saved_nodes = copy.deepcopy(self.current_nodes)
            f_nodes.close()


if __name__ == '__main__':
    GraphJsonShell().main_prompt()
