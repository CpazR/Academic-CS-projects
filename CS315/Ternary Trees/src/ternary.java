import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ternary {
    // Intended to try and generalize. Could not find a good enough solution
    static class Tree {

        Node rootNode;

        class Node {
            Integer valueA = null;
            Integer valueB = null;
            Node leftChild = null;
            Node middleChild = null;
            Node rightChild = null;

            @Override
            public String toString() {
                // user ternary operators to cleanly print the ternary tree
                return "(" +
                        (leftChild == null ? "" : leftChild + " ") +
                        (valueA == null ? "" : valueA) +
                        (middleChild == null ? "" : " " + middleChild) +
                        (valueB == null ? "" : " " + valueB) +
                        (rightChild == null ? "" : " " + rightChild) + ")";
            }

            // recursively insert value into tree
            private void insertValue(int value) {
                // node's values are filled, find which child node to send to and recurse
                // valueA is filled
                if (this.valueA != null) {
                    // valueB is filled
                    if (this.valueB != null) {
                        // all values are filled, insert node at child nodes
                        if (value <= this.valueA) {
                            // value is less than or equal to value A, insert at left child
                            this.leftChild = this.insertAtNode(this.leftChild, value);
                        }
                        if (value > this.valueA && value <= this.valueB) {
                            // value is between value A and B, insert at middle child
                            this.middleChild = this.insertAtNode(this.middleChild, value);
                        }
                        if (value > this.valueA && value > this.valueB) {
                            // value is greater than B, insert at right child
                            this.rightChild = this.insertAtNode(this.rightChild, value);
                        }
                    } else {
                        // valueB is not filled, determine if to insert or swap values
                        if (value < this.valueA) {
                            // value is less than valueA, swap set valueA to valueB, and insert value at valueA
                            this.valueB = valueA;
                            this.valueA = value;
                        } else {
                            // value is inserted at A, is greater than A, and no value is at B yet, insert value at B
                            this.valueB = value;
                        }
                    }

                } else {
                    // valueA is null, insert value
                    this.valueA = value;
                }
            }

            // inserts a value at a node; creates if node does not exist yet
            private Node insertAtNode(Node childNode, int value) {
                if (childNode == null) {
                    childNode = new Node();
                }
                childNode.insertValue(value);
                return childNode;
            }

            /**
             * EXTRA CREDIT MATERIAL
             */
            // recursively search for a node with a given value; returns null if node cannot be found
            public Node search(int searchValue) {
                // reference to node being searched for
                Node resultingNode = null;

                // test values first
                if ((this.valueA != null && this.valueA == searchValue) || (this.valueB != null && this.valueB == searchValue)) {
                    resultingNode = this;
                } else {
                    if (searchValue <= this.valueA) {
                        // value is less than or equal to value A, search in left child
                        if (this.leftChild != null) {
                            resultingNode = this.leftChild.search(searchValue);
                        }
                    }
                    if (searchValue > this.valueA && searchValue <= this.valueB) {
                        // value is between value A and B, search in middle child
                        if (this.middleChild != null) {
                            resultingNode = this.middleChild.search(searchValue);
                        }
                    }
                    if (searchValue > this.valueA && searchValue > this.valueB) {
                        // value is greater than B, search in right child
                        if (this.rightChild != null) {
                            resultingNode = this.rightChild.search(searchValue);
                        }
                    }
                }
                return resultingNode;
            }
        }

        Tree(int[] values) {
            sort(values);
        }

        @Override
        public String toString() {
            return "Tree{ " + rootNode + " }";
        }

        void sort(int[] values) {
            rootNode = new Node();
            for (int i = 0; i < values.length; i++) {
                // set initial values in root node
                rootNode.insertValue(values[i]);
            }
        }

        /**
         * EXTRA CREDIT MATERIAL
         */
        // returns node with a given value
        Node search(int searchValue) {
            Node resultingNode = rootNode.search(searchValue);
            System.out.println("\n\nTree from first found node with value '" + searchValue + "' (null if not found): " + resultingNode);
            return resultingNode;
        }
    }

    static String getInput(String promptMessage, BufferedReader reader) throws IOException {
        System.out.print(promptMessage);
        return reader.readLine();
    }

    public static void main(String[] args) throws IOException {
        // Initialize reader for collecting inputs
        BufferedReader reader = new BufferedReader(new InputStreamReader((System.in)));

        // parse argument array into integers (blame java for all the extra steps)
        int maxInts = Integer.parseInt(args[0]);

        int[] unsorted = new int[maxInts];
        for (int i = 0; i < maxInts; i++) {
            String input = getInput("", reader);
            try {
                unsorted[i] = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Error when parsing input: " + input + "\nPlease enter a valid input.");
            }

        }
        Tree ternaryTree = new Tree(unsorted);
        System.out.println(ternaryTree.toString());

        /** EXTRA CREDIT MATERIAL (YET TO BE IMPLEMENTED)*/

        boolean isSearching = args.length > 1;

        if (isSearching) {
            try {
                ternaryTree.search(Integer.parseInt(args[1]));
            } catch (NumberFormatException e) {
                System.out.println("Error when parsing input: " + args[1] + "\nPlease enter a valid input.");
            }
        }

    }
}
