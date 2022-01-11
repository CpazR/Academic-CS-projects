import java.util.Scanner;
import java.util.Vector;

public class wordCount {

    static class HashTable {
        HashNode[] nodes; // keys relating to values
        int size;

        HashTable(int size) {
            this.size = size;
            this.nodes = new HashNode[size];
        }

        /**
         * Calculate an index for the given string
         */
        int hash(String value) {
            int valueAsciiSize = 0;
            for (int i = 0; i < value.length(); i++) {
                valueAsciiSize += value.charAt(i) * 31 ^ (size - i);
            }

            return (valueAsciiSize % size);
        }

        /**
         * Get node given a string
         */
        HashNode get(String value) {
            int index = hash(value);

            if (!value.equals(this.nodes[index].value)) {
                // did not find value at index, check adjacent indices
                if (value.equals(this.nodes[index - 1].value))
                    index = index - 1;

                if (value.equals(this.nodes[index + 1].value))
                    index = index + 1;

            }

            return this.nodes[index];
        }

        /**
         * Insert value into a new node or add to collision count of node if it already exists
         */
        void put(String value) {
            // get index by hashing value
            int index = hash(value);

            if (!insert(value, index)) {
                // insertion was met with collision, try adjacent indices
                boolean adjacentSuccessful = false;

                if (index > 0) {
                    adjacentSuccessful = insert(value, index - 1);
                }

                if (index < this.size - 1 && !adjacentSuccessful) {
                    adjacentSuccessful = insert(value, index + 1);
                }
            }
        }

        /**
         * Insert a value into the table at a given index
         * Return whether or not a collision was found
         */
        boolean insert(String value, int index) {
            boolean noCollisions = true;
            if (this.nodes[index] == null) {
                // value does not exist in hash table, insert at index
                this.nodes[index] = new HashNode(index, value);
            } else {
                // value already exists!
                if (!value.equals(this.nodes[index].value)) {
                    // is a collision add collision to count
                    this.nodes[index].collisions += 1;
                    noCollisions = false;
                }
                if (value.equals(this.nodes[index].value)) {
                    // is a duplicate, add to count
                    this.nodes[index].occurrences += 1;
                }
            }

            return noCollisions;
        }

        enum SortingCriteria {
            ASCIIALPHABET,
            OCCURRENCES,
        }

        /**
         * Using heapsort, sort values in hash table and return the resulting array of nodes.
         * Tha table itself will not be sorted since a hashtable cannot be traditionally sorted.
         */
        HashNode[] getSortedNodes(SortingCriteria sortCriteria) {
            // get copy of nodes as a vector
            Vector<HashNode> sortedNodes = new Vector<>();
            for (HashNode node : this.nodes) {
                if (node != null) {
                    sortedNodes.add(node);
                }
            }

            // heapify vector
            HashNode tempNode;
            int heapSize = sortedNodes.size() - 1;
            for (int i = (heapSize / 2) - 1; i >= 0; i--) {
                this.heapify(sortedNodes, i, heapSize, sortCriteria);
            }

            // swap nodes
            for (int i = heapSize - 1; i >= 0; i--) {
                tempNode = new HashNode(sortedNodes.get(0));
                sortedNodes.set(0, sortedNodes.get(i));
                sortedNodes.set(i, tempNode);

                // heapify smaller heap at 'i'
                heapify(sortedNodes, 0, i, sortCriteria);
            }

            // convert back to an array for simplicity's sake
            return sortedNodes.toArray(new HashNode[0]);
        }

        void heapify(Vector<HashNode> nodes, int i, int heapSize, SortingCriteria sortCriteria) {
            // get bounds for heap
            int leftBound = 2 * i + 1;
            int rightBound = 2 * i + 2;
            int largestNodeIndex = i;
            // heap based on node duplicate counts

            // verify that value at i is not null
            if (nodes.get(i) != null) {
                // is left bound larger??
                if (leftBound < heapSize && nodes.get(leftBound) != null) {
                    // account for different possible sorting criteria
                    boolean criteria = (sortCriteria == SortingCriteria.ASCIIALPHABET ?
                            (nodes.get(leftBound).value.compareTo(nodes.get(largestNodeIndex).value) > 0) :
                            (nodes.get(leftBound).occurrences < nodes.get(largestNodeIndex).occurrences));
                    if (criteria) {
                        largestNodeIndex = leftBound;
                    }
                }

                // is right bound larger??
                if (rightBound < heapSize && nodes.get(rightBound) != null) {
                    // account for different possible sorting criteria
                    boolean criteria = (sortCriteria == SortingCriteria.ASCIIALPHABET ?
                            (nodes.get(rightBound).value.compareTo(nodes.get(largestNodeIndex).value) > 0) :
                            (nodes.get(rightBound).occurrences < nodes.get(largestNodeIndex).occurrences));
                    if (criteria) {
                        largestNodeIndex = rightBound;
                    }
                }

                // perform a swap if a larger node was determined
                if (largestNodeIndex != i) {
                    HashNode tempNode = new HashNode(nodes.get(i));
                    nodes.set(i, nodes.get(largestNodeIndex));
                    nodes.set(largestNodeIndex, tempNode);
                    heapify(nodes, largestNodeIndex, heapSize, sortCriteria);
                }
            }
        }

        /**
         * Print all non-null values stored in hash table with collisions as-is
         */
        void print(SortingCriteria criteria) {
            for (HashNode node : getSortedNodes(criteria)) {
                if (node != null)
                    if (node.occurrences > 0)
                        printNode(node);
            }
        }

        /**
         * Print all non-null values stored in hash table with collisions as-is
         */
        void printNode(String value) {
            HashNode node = this.nodes[this.hash(value)];
            printNode(node);
        }

        void printNode(HashNode node) {
            System.out.println(node.value + " " + node.occurrences);
        }

        /**
         * Nodes pointed to by the hash-table
         */
        static class HashNode {
            int occurrences;
            int key;            // point in hash table node exists in   **is this needed?**
            String value;       // value contained in node
            int collisions; // collisions made with node

            HashNode(int key, String value) {
                this.key = key;
                this.value = value;
                this.collisions = 0;
                this.occurrences = 1; // initial occurrence
            }

            // duplicate a node
            public HashNode(HashNode hashNode) {
                this.key = hashNode.key;
                this.value = hashNode.value;
                this.collisions = hashNode.collisions;
                this.occurrences = hashNode.occurrences;
            }
        }
    }

    public static void main(String[] args) {
        String[] data;
        Vector<String> wordBuffer = new Vector<>();

        // Scanner for file input
        Scanner fileScanner = null;
        fileScanner = new Scanner(System.in);

        // parse file word by word
        while (fileScanner.hasNext()) {
            Scanner lineScanner = new Scanner(fileScanner.nextLine());
            while (lineScanner.hasNext()) {
                // split words from hyphen if present
                String[] words = lineScanner.next().split("-");
                // remove punctuation from string, if it exists
                for (String word : words) {
                    word = word.replaceAll("\\p{P}", "");
                    // check if string wasn't *just* punctuation
                    if (!word.isEmpty()) {
                        wordBuffer.add(word);
                    }
                }
            }
        }

        // move to array
        data = new String[wordBuffer.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = wordBuffer.get(i);
        }

        HashTable table = new HashTable(data.length);

        for (String string : data) {
            table.put(string);
        }

        System.out.println("----- ASCII alphabetical order: -----");
        table.print(HashTable.SortingCriteria.ASCIIALPHABET);
        System.out.println("\n\n"); // line breaks for readability
        System.out.println("----- Occurrence count order: -----");
        table.print(HashTable.SortingCriteria.OCCURRENCES);
    }
}

