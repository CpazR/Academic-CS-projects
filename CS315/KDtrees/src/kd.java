import java.io.*;
import java.util.Arrays;
import java.util.Vector;

public class kd {

    static class KDTree {
        int dimensions;
        int bucketSize;
        Node rootNode;

        public KDTree(int dimensions, Vector<KdPoint> pointBucket, int bucketSize) {
            this.dimensions = dimensions;
            this.bucketSize = bucketSize;
            this.generateTree(pointBucket);
        }

        /**
         * Generate tree using valueBucket
         */
        private void generateTree(Vector<KdPoint> pointBucket) {
            this.rootNode = new Node(this.dimensions, 0, this.bucketSize);
            this.rootNode.splittingDimension = this.rootNode.findWidestDimension(pointBucket);
            Node currentNode = this.rootNode;
            for (int i = 0; i < pointBucket.size() - 1; i++) {
                currentNode.insert(pointBucket.get(i));
            }
        }

        /**
         * From root node, probe tree with given point
         */
        public void probe(KdPoint probePoint) {
            this.rootNode.probe(probePoint);
        }

        static class Node {
            // data for all "k" dimensions
            int dimensions;
            int splittingDimension;
            int bucketSize;
            Vector<KdPoint> pointBucket;
            // child nodes
            Node leftChild;
            Node rightChild;

            public Node(int dimensions, int splittingDimension, int bucketSize) {
                // assign dimensions in allocate data size in memory
                this.dimensions = dimensions;
                this.splittingDimension = splittingDimension;
                this.bucketSize = bucketSize;
                this.pointBucket = new Vector<>();
            }

            private KdPoint getDiscriminant() {
                return this.pointBucket.get(0);
            }

            /**
             * Return the widest dimension given a point bucket
             */
            private int findWidestDimension(Vector<KdPoint> pointBucket) {
                int[] widths = {0, 0, 0};
                int largestWidth = 0;

                // get widths of each dimension
                for (int i = 0; i < this.dimensions; i++) {
                    int min = pointBucket.get(0).coordinates[i];
                    int max = pointBucket.get(0).coordinates[i];
                    for (KdPoint kdPoint : pointBucket) {
                        if (kdPoint.coordinates[i] < min) {
                            min = kdPoint.coordinates[i];
                        }
                        if (kdPoint.coordinates[i] > max) {
                            max = kdPoint.coordinates[i];
                        }
                    }
                    widths[i] = max - min;
                }

                // find largest width
                int widthComp = 0;
                for (int i = 0; i < widths.length; i++) {
                    if (widthComp < widths[i]) {
                        largestWidth = i;
                        widthComp = widths[i];
                    }
                }

                return largestWidth;
            }

            /**
             * make node into an internal node with only a discriminate value
             */
            private void makeInternal() {
                // set splitting dimension for sorting
                this.splittingDimension = this.findWidestDimension(pointBucket);
                // create copy of median
                KdPoint median = (this.quickSelect(new Vector<>(this.pointBucket), this.splittingDimension, 0, this.pointBucket.size() - 1, (pointBucket.size() - 1) / 2));

                // node is internal, find median and split bucket of points into children

                this.rightChild = new Node(this.dimensions, 0, this.bucketSize);
                this.leftChild = new Node(this.dimensions, 0, this.bucketSize);

                for (int i = 0; i < this.bucketSize; i++) {
                    if (this.pointBucket.get(i).coordinates[this.splittingDimension] > median.coordinates[this.splittingDimension]) {
                        this.rightChild.insert(this.pointBucket.get(i));
                    } else {
                        this.leftChild.insert(this.pointBucket.get(i));
                    }
                }
                // clear bucket of all values except for discriminant
                this.pointBucket.clear();
                this.pointBucket.add(median);
            }

            /**
             * Insert depending on splitting dimension
             */
            public void insert(KdPoint insertionPoint) {
                if (this.leftChild == null && this.rightChild == null) {
                    // node is leaf, add to bucket
                    if (this.pointBucket.size() < this.bucketSize) {
                        this.pointBucket.add(insertionPoint);
                    }
                    // node is full, make into an internal node
                    else {
                        this.makeInternal();
                    }
                }

                // node has children, insert into
                if (this.leftChild != null && this.rightChild != null) {
                    // compare to median and insert point into node based on comparison
                    if (insertionPoint.coordinates[this.splittingDimension] > this.getDiscriminant().coordinates[this.splittingDimension]) {
                        this.rightChild.insert(insertionPoint);
                    } else {
                        this.leftChild.insert(insertionPoint);
                    }

                }
            }

            /**
             * Given a point, iterate through tree until an available node is found, and print the bucket's contents
             */
            public void probe(KdPoint probePoint) {
                // if node only has one value, use as median,
                if (this.pointBucket.size() > 1) {
                    System.out.println("Probe: " + probePoint + " reached bucket: " + this.pointBucket.size() + " elements");
                    System.out.println(this);
                } else {
                    // compare to median and probe node based on comparison
                    if (probePoint.coordinates[this.splittingDimension] > this.getDiscriminant().coordinates[this.splittingDimension]) {
                        this.rightChild.probe(probePoint);
                    } else {
                        this.leftChild.probe(probePoint);
                    }
                }
            }

            private void swapPoints(Vector<KdPoint> pointBucket, int pointPosA, int pointPosB) {
                KdPoint tempPoint = new KdPoint(pointBucket.get(pointPosA));
                pointBucket.set(pointPosA, pointBucket.get(pointPosB));
                pointBucket.set(pointPosB, tempPoint);
            }

            /**
             * Calculate the median from a bucket of points of a given dimension using quickSelect with lomuto's partitioning
             */
            private KdPoint quickSelect(Vector<KdPoint> pointBucket, int dimension, int startingPosition, int endingPosition, int index) {
                KdPoint result = pointBucket.firstElement();
                boolean skipSorting = false;

                if (startingPosition == endingPosition) {
                    skipSorting = true;
                }

                if (!skipSorting) {
                    // partition with lomuto's method
                    int pivotIndex = lomutoPartitioning(pointBucket, dimension, startingPosition, endingPosition);

                    // validate index position
                    if (index == pivotIndex) {
                        result = pointBucket.get(pivotIndex);
                    } else {
                        if (index < pivotIndex) {
                            result = quickSelect(new Vector<>(pointBucket), dimension, startingPosition, pivotIndex - 1, index);
                        } else {
                            result = quickSelect(new Vector<>(pointBucket), dimension, pivotIndex + 1, endingPosition, index);
                        }
                    }
                }

                return result;
            }

            /**
             * Partition using lomuto's method
             */
            private int lomutoPartitioning(Vector<KdPoint> pointBucket, int dimension, int startingPosition, int endingPosition) {
                int pivotIndex = startingPosition;
                KdPoint pivot = pointBucket.get(pivotIndex); // pivot with starting element
                this.swapPoints(this.pointBucket, pivotIndex, endingPosition);

                for (int i = startingPosition; i < endingPosition; i++) {
                    var kdPoint = pointBucket.get(i);
                    if (kdPoint.coordinates[dimension] <= pivot.coordinates[dimension]) {
                        this.swapPoints(this.pointBucket, i, pivotIndex);
                        pivotIndex++;
                    }
                }
                this.swapPoints(this.pointBucket, pivotIndex, endingPosition);

                return pivotIndex;
            }

            public String toString() {
                StringBuilder returnString = new StringBuilder();

                for (KdPoint point : pointBucket) {
                    returnString.append(Arrays.toString(point.coordinates)).append("\n");
                }

                return returnString.toString();
            }
        }
    }


    /**
     * Generically sized point
     */
    static class KdPoint {
        int dimensions;
        Integer[] coordinates;

        KdPoint(int dimensions) {
            this.dimensions = dimensions;
            this.coordinates = new Integer[dimensions];
        }

        /**
         * Constructor for duplicating point
         */
        public KdPoint(KdPoint duplicateKdPoint) {
            this.dimensions = duplicateKdPoint.dimensions;
            this.coordinates = duplicateKdPoint.coordinates;
        }

        void setCoordinate(int dimension, Integer coordinate) {
            this.coordinates[dimension] = coordinate;
        }

        @Override
        public String toString() {
            return Arrays.toString(coordinates);
        }
    }

    /**
     * Generates KdPoints from an array of values and a dimension
     */
    public static Vector<KdPoint> generatePoints(int dimensions, int[] values) {
        // allocate array and initialize first point
        var points = new Vector<KdPoint>();
        points.add(new KdPoint(dimensions));

        // iterate through data and insert
        int currentDimension = 0;
        int currentPoint = 0;
        for (int i = 0; i < values.length; i++) {
            if (currentDimension >= dimensions) {
                currentDimension = 0;
                currentPoint++;
                // check if there are enough values to fit into a new point
                if ((values.length) - i >= dimensions) {
                    points.add(new KdPoint(dimensions));
                } else {
                    break; // NOT ENOUGH VALUES FOR ANOTHER POINT; EXIT LOOP (not ideal approach...)
                }
            }

            if (currentDimension < dimensions) {
                points.get(currentPoint).setCoordinate(currentDimension, values[i]);
                currentDimension++;
            }
        }

        return points;
    }

    /**
     * Mostly generic method for getting input
     */
    static String getInput(String promptMessage, BufferedReader reader) throws IOException {
        System.out.print(promptMessage);
        return reader.readLine();
    }

    public static void main(String[] args) throws IOException {
        // parse argument array into integers (blame java for all the extra steps
        int dimensions = Integer.parseInt(args[0]);
        int totalPoints = Integer.parseInt(args[1]); // only needed for collecting values through the offline method
        int probes = Integer.parseInt(args[2]);
        int bucketSize = 10;

        /* PIPED INPUT STREAMING COPIED FROM TERNARY ASSIGNMENT */

        // Initialize reader for collecting inputs from piped input
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        int totalValues = dimensions * totalPoints;
        int[] valueBucket = new int[totalValues];
        for (int i = 0; i < totalValues; i++) {
            String input = getInput("", reader);
            try {
                valueBucket[i] = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Error when parsing input: " + input + "\nPlease enter a valid input.");
            }
        }

        // parse random values into
        Vector<KdPoint> pointBucket = generatePoints(dimensions, valueBucket);

        // Based on given information, generate K-D tree
        KDTree tree = new KDTree(dimensions, pointBucket, bucketSize);

        // Probe into tree

        // get probe points
        int totalProbeValues = dimensions * probes;
        int[] probeValues = new int[totalProbeValues];
        for (int i = 0; i < totalProbeValues; i++) {
            String input = getInput("", reader);
            try {
                probeValues[i] = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Error when parsing input: " + input + "\nPlease enter a valid input.");
            }
        }

        Vector<KdPoint> probePoints = generatePoints(dimensions, probeValues);

        // with probe points, probe tree ***using lambda expressions instead of for loop to be cleaner***
        probePoints.forEach(tree::probe);
    }
}
