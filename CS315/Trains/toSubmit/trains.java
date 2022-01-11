import java.lang.reflect.Array;
import java.util.Random;

class Node {
    // default next and previous node to null
    Node next;
    Node prev;

    int data;

    Node(int data) {
        this.next = null;
        this.data = data;
    }

    Node(Node nextNode, Node prevNode, int data) {
        this.next = nextNode;
        this.prev = prevNode;
        this.data = data;
    }

    public Node getNext() {
        return next;
    }

    public Node getPrev() {
        return prev;
    }

    public int getData() {
        return data;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public void setData(int data) {
        this.data = data;
    }
}

/**
 * Doubly linked lists
 */
class LinkedList {

    private HeaderNode header;

    // create an empty list
    LinkedList() {
        // create reference to header node
        this.header = new HeaderNode();
    }

    class HeaderNode {
        // reference to front node
        Node front;

        // reference to rear node
        Node rear;

        // count of all nodes
        int count;

        public Node getFront() {
            return front;
        }

        public void setFront(Node front) {
            this.front = front;
        }

        public Node getRear() {
            return rear;
        }

        public void setRear(Node rear) {
            this.rear = rear;
        }
    }

    HeaderNode getHeader() {
        return header;
    }

    boolean isEmpty() {
        return this.header.count == 0;
    }

    void addFrontNode(int data) {
        Node currentFront = this.header.getFront();
        Node newNode = new Node(data);
        if (currentFront == null) {
            // does not have a front, assign to rear as well
            this.header.setRear(newNode);
        } else {
            // has front, create one and update previous front
            currentFront.setPrev(newNode);
            newNode.setNext(currentFront);
        }
        this.header.setFront(newNode);
        this.header.count++;
    }

    void addRearNode(int data) {
        Node currentRear = this.header.getRear();
        Node newNode = new Node(data);
        if (currentRear == null) {
            // does not have a rear, assign to front as well
            this.header.setFront(newNode);
        } else {
            // has rear node, create one and update previous rear node
            currentRear.setNext(newNode);
            newNode.setPrev(currentRear);
        }
        this.header.setRear(newNode);
        this.header.count++;
    }

    Node removeFrontNode() {
        Node currentFront = this.header.getFront();
        if (currentFront != null) {
            // has front node, dereference for garbage collection
            Node newFront = currentFront.getNext();
            this.header.setFront(newFront);
            this.header.count--;
        }
        return currentFront;
    }

    Node removeRearNode() {
        Node currentRear = this.header.getRear();
        if (currentRear != null) {
            // has rear node, dereference for garbage collection
            Node newRear = currentRear.getPrev();
            this.header.setRear(newRear);
            this.header.count--;
        }
        return currentRear;
    }
}

public class trains {

    public static void main(String[] args) {

        // create new instance of random class to generate random numbers with
        Random rand = new Random();

        boolean isArgumentsValid = true;

        int trainCount = Integer.parseInt(args[0]);
        int carsPerTrain = Integer.parseInt(args[1]);
        int steps = Integer.parseInt(args[2]);

        // validate arguments
        if (trainCount < 1 && carsPerTrain < 1) {
            isArgumentsValid = false;
        }

        // only run main part of program is arguments are valid
        if (isArgumentsValid) {
            // create linked lists for trains
            LinkedList[] trains = new LinkedList[trainCount];
            for (int i = 0; i < trainCount; i++) {
                // initialize train
                trains[i] = new LinkedList();
                // populate train with cars
                for (int j = 1; j < carsPerTrain; j++) {
                    trains[i].addRearNode(j);
                }
            }

            boolean isStepping = true;
            int currentSteps = 0;
            while (isStepping) {

                System.out.println("Step: " + currentSteps);

                // "Dice rolls" / "Coin flips"
                int trainDonorIndex = rand.nextInt(trainCount - 1);
                boolean removeFromFront = rand.nextBoolean();
                int trainReceiverIndex = rand.nextInt(trainCount - 1);

                // remove car, assigning the returned value to an independent node
                Node movedCar = removeFromFront ?
                        trains[trainDonorIndex].removeFrontNode() : trains[trainDonorIndex].removeRearNode();

                // assign moved car to the receiving train and print information to console
                trains[trainReceiverIndex].addFrontNode(movedCar.getData());
                System.out.println("\tTrain #" + trainReceiverIndex + " received a "
                        + (removeFromFront ? "front" : "rear")
                        + " car of size " + movedCar.getData() + " from Train #" + trainDonorIndex);
                printTrainValues(trains);

                // exit conditions
                currentSteps++;
                // check if current steps are past the number of steps provided
                if (currentSteps > steps) {
                    isStepping = false;
                    System.out.println("Steps past " + steps + " exiting program.");
                } else {
                    // if current steps are valid, check if any of the trains are empty
                    for (int i = 0; i < trainCount; i++) {
                        if (trains[i].isEmpty()) {
                            // a train is empty, end the program
                            System.out.println("Train #" + i + " is empty, exiting program");
                            isStepping = false;
                            break;
                        }
                    }
                }
            }
            printTrainValues(trains);
        }
    }

    // print values for each train (linked list)
    static void printTrainValues(LinkedList[] trains) {
        // List value of all trains
        for(int i = 0; i < trains.length; i++) {
            System.out.println("\tThe value of train #" + i + ": " + calculateTrainValue(trains[i]));
        }
    }

    // iterate through linked list and calcualte value for train
    static int calculateTrainValue(LinkedList train) {
        Node carIterator = train.getHeader().getFront();
        int trainTotalValue = 0;
        int position = 0;
        while (carIterator != null) {
            position++;
            trainTotalValue += position + carIterator.getData();
            carIterator = carIterator.getNext();
        }
        return trainTotalValue;
    }
}