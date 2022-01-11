import java.util.Scanner;
import java.util.Vector;


public class genealogy {
    /**
     * Repurposed linked list class from project 1 to act as queue
     */
    static class Node<t> {
        // default next and previous node to null
        Node<t> next;
        Node<t> prev;

        t data;

        Node(t data) {
            this.next = null;
            this.data = data;
        }

        public Node<t> getNext() {
            return next;
        }

        public Node<t> getPrev() {
            return prev;
        }

        public void setNext(Node<t> next) {
            this.next = next;
        }

        public void setPrev(Node<t> prev) {
            this.prev = prev;
        }
    }

    /**
     * Doubly linked lists
     * Generalized to allow for easy application of persons
     */
    static class Queue<t> {

        private final HeaderNode header;

        // create an empty list
        Queue() {
            // create reference to header node
            this.header = new HeaderNode();
        }

        class HeaderNode {
            // reference to front node
            Node<t> front;

            // reference to rear node
            Node<t> rear;

            // count of all nodes
            int count;

            public Node<t> getFront() {
                return front;
            }

            public void setFront(Node<t> front) {
                this.front = front;
            }

            public Node<t> getRear() {
                return rear;
            }

            public void setRear(Node<t> rear) {
                this.rear = rear;
            }
        }

        boolean isEmpty() {
            return this.header.count == 0;
        }

        void addFrontNode(t data) {
            Node<t> currentFront = this.header.getFront();
            Node<t> newNode = new Node<>(data);
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

        t removeRearNode() {
            Node<t> currentRear = this.header.getRear();
            if (currentRear != null) {
                // has rear node, dereference for garbage collection
                boolean onlyNode = (this.header.getFront() == this.header.getRear());
                Node<t> newRear = currentRear.getPrev();
                this.header.setRear(newRear);
                if (onlyNode) {
                    this.header.setFront(currentRear.getNext());
                }
                this.header.count--;
            }
            assert currentRear != null;
            return currentRear.data;
        }
    }

    /**
     * A family tree dedicated to tracking disjointed families
     */
    static class UnionFindFamilyTree {
        // "Root" might not be entirely accurate, given the use case; but in the context of a union find, it works.
        int[] roots = new int[100];

        UnionFindFamilyTree() {
            // Initialize each persons root as itself. Or each person as its own set.
            for (int i = 0; i < roots.length; i++) {
                roots[i] = i;
            }
        }

        /**
         * Relate "personB" to "personA"
         */
        void union(int personA, int personB) {
            int rootA = find(personA);
            int rootB = find(personB);

            roots[rootA] = rootB;
        }

        /**
         * Find root of "personNum"
         */
        int find(int personNum) {
            int root = 0;
            if (roots[personNum] == personNum) { // found root!
                root = personNum;
            } else { // haven't found root yet, find parent's root recursively
                root = find(roots[personNum]);
            }
            return root;
        }

        /**
         * Determine if two persons are in the same family tree by checking if they have the same root
         */
        public boolean isConnected(int personNumA, int personNumB) {
            return find(personNumA) == find(personNumB);
        }
    }

    static class FamilyTree {

        // leave  0 - index null for unknown families/persons
        Family[] families = new Family[100];
        Person[] persons = new Person[100];
        UnionFindFamilyTree unionFindFamilyTree = new UnionFindFamilyTree();

        FamilyTree() {
            // unknown person
            this.persons[0] = new Person(0);
        }

        private void insertPerson(Person personToInsert) {
            this.persons[personToInsert.personNum] = personToInsert;
        }

        private void insertPersons(Vector<Person> peopleToInsert) {
            for (Person personToInsert : peopleToInsert)
                this.persons[personToInsert.personNum] = personToInsert;
        }

        private boolean personExists(Person personToInsert) {
            return (this.persons[personToInsert.personNum] != null);
        }

        private Vector<Person> peopleThatExist(Vector<Person> peopleToInsert) {
            Vector<Person> personsThatExist = new Vector<>();
            for (Person personToInsert : peopleToInsert)
                if (this.persons[personToInsert.personNum] != null) {
                    // persons exists! likely a relation exists
                    personsThatExist.add(personToInsert);
                }
            return personsThatExist;
        }

        boolean insertFamily(Family familyToInsert) {
            // flag that will be used as a reference for each step of insertion process
            boolean canInsert = true;

            boolean husbandExists = personExists(familyToInsert.husband);
            boolean wifeExists = personExists(familyToInsert.wife);
            Vector<Person> childrenThatAlreadyExist = peopleThatExist(familyToInsert.children);

            if (this.families[familyToInsert.familyNum] == null) {
                // perform validation before doing any insertion


                // Check if persons already exist; if they do, update relations
                if (familyToInsert.husband.personNum != 0 && husbandExists) {
                    Person husband = this.persons[familyToInsert.husband.personNum];
                    boolean isMarried = husband.marriage != null;
                    if (isMarried) {
                        // is currently married, cannot insert family
                        softThrow("ERROR", "Husband " + familyToInsert.husband.personNum + " is already married in \"" + husband.marriage.toString(true) + "\"");
                        canInsert = false;
                    }
                }
                if (familyToInsert.wife.personNum != 0 && wifeExists) {
                    Person wife = this.persons[familyToInsert.wife.personNum];
                    boolean isMarried = wife.marriage != null;
                    if (isMarried) {
                        // is currently married, cannot insert family
                        softThrow("ERROR", "Wife " + familyToInsert.wife.personNum + " is already married in \"" + wife.marriage.toString(true) + "\"");
                        canInsert = false;
                    }
                }
                if (!childrenThatAlreadyExist.isEmpty()) {
                    // must be a husband or wife elsewhere; link parentage to family
                    for (Person existingChildInFamily : childrenThatAlreadyExist) {
                        Person child = this.persons[existingChildInFamily.personNum];
                        boolean isChild = child.parentage != null;
                        if (isChild) {
                            // is already a child, cannot insert family
                            softThrow("ERROR", "Child \"" + child.personNum + "\" has parents in \"" + child.parentage.toString(true) + "\"");
                            canInsert = false;
                        }
                    }
                }

                // validation has been passed, insert family or update relations
                if (canInsert) {
                    this.families[familyToInsert.familyNum] = familyToInsert;

                    if (!husbandExists) {
                        insertPerson(familyToInsert.husband);
                    } else {
                        Person husband = this.persons[familyToInsert.husband.personNum];
                        boolean isMarried = husband.marriage != null;
                        if (!isMarried) {
                            // must be children elsewhere; link marriage to family
                            husband.marriage = familyToInsert;
                            // update family to reference existing person
                            this.families[familyToInsert.familyNum].husband = husband;
                        }
                    }
                    if (!wifeExists) {
                        insertPerson(familyToInsert.wife);
                    } else {
                        Person wife = this.persons[familyToInsert.wife.personNum];
                        boolean isMarried = wife.marriage != null;
                        if (!isMarried) {
                            // must be children elsewhere; link marriage to family
                            wife.marriage = familyToInsert;
                            // update family to reference existing person
                            this.families[familyToInsert.familyNum].wife = wife;
                        }
                    }
                    if (personExists(familyToInsert.husband) && personExists(familyToInsert.wife)) {
                        unionFindFamilyTree.union(familyToInsert.husband.personNum, familyToInsert.wife.personNum);
                    }
                    if (childrenThatAlreadyExist.isEmpty()) {
                        insertPersons(familyToInsert.children);
                    } else {
                        for (int i = 0; i < familyToInsert.children.size(); i++) {
                            if (personExists(familyToInsert.children.get(i))) {
                                Person child = this.persons[familyToInsert.children.get(i).personNum];
                                boolean isChild = child.parentage != null;
                                if (!isChild) {
                                    child.parentage = familyToInsert;
                                    // update family to reference existing person
                                    this.families[familyToInsert.familyNum].children.set(i, child);
                                }
                            } else {
                                insertPerson(familyToInsert.children.get(i));
                            }

                            Person child = this.persons[familyToInsert.children.get(i).personNum];
                            if (personExists(familyToInsert.husband)) {
                                unionFindFamilyTree.union(child.personNum, familyToInsert.husband.personNum);
                            }
                            if (personExists(familyToInsert.wife)) {
                                unionFindFamilyTree.union(child.personNum, familyToInsert.wife.personNum);
                            }
                        }
                    }
                }
            } else {
                // Family exists? Throw error.
                canInsert = false;
                softThrow("ERROR", "Family \"" + familyToInsert.familyNum + "\" already exists");
            }

            return canInsert;
        }

        void findRelation(int personNumA, int personNumB) {
            // use BFS to find shortest path
            if (unionFindFamilyTree.isConnected(personNumA, personNumB)) {
                breadthFirstSearch(personNumA, personNumB);
            } else {
                System.out.println("Person " + personNumA + " is NOT related to person " + personNumB + "\n");
            }
        }

        /**
         * Search graph using BFS Return Vector of a path of people to "personToFind"
         */
        void breadthFirstSearch(int startingPersonNum, int personToFindNum) {
            Person startingPerson = this.persons[startingPersonNum];
            Person personToFind = this.persons[personToFindNum];

            Queue<Person> personWorkQueue = new Queue<>();

            boolean[] visited = new boolean[100];
            int[] parents = new int[100];
            int[] families = new int[100]; // family person N is related to; used for adjacency

            boolean hasFoundPerson = false;

            visited[startingPersonNum] = true;
            personWorkQueue.addFrontNode(startingPerson);

            if (startingPerson != null && personToFind != null) {
                while (!personWorkQueue.isEmpty() && !hasFoundPerson) {
                    Person currentPerson = personWorkQueue.removeRearNode();

                    // build list of relatives to look through
                    Vector<Family> familyRelations = new Vector<>();
                    familyRelations.add(currentPerson.parentage);
                    familyRelations.add(currentPerson.marriage);

                    // look through relatives and add to queue if not yet visited effectively making families an adjacency matrix
                    for (Family relation : familyRelations) {
                        if (relation != null) {
                            for (Person person : relation.getPersons()) {
                                if (!visited[person.personNum]) {
                                    visited[person.personNum] = true;
                                    families[person.personNum] = relation.familyNum;
                                    parents[person.personNum] = currentPerson.personNum;
                                    personWorkQueue.addFrontNode(person);
                                }

                                // found person, stop!
                                if (personToFind.equals(person)) {
                                    hasFoundPerson = true;
                                    break;
                                }
                            }
                        }
                    }
                    visited[currentPerson.personNum] = true;
                }
            }

            if (hasFoundPerson) {
                System.out.println("\nPerson " + startingPerson.personNum + " is related to person " + personToFind.personNum);
                // print out relation tree
                int currentPersonNum = personToFindNum;
                String relationString = "";
                while (currentPersonNum != 0) {

                    // add person to string
                    relationString = this.persons[currentPersonNum].toString() + (currentPersonNum == personToFindNum ? "" : " -> " + relationString);
                    // add adjacent family to string
                    int adjacentFamilyNum = families[currentPersonNum];
                    if (this.families[adjacentFamilyNum] != null) {
                        relationString = this.families[adjacentFamilyNum].toString(true) + " -> " + relationString;
                    }

                    // update information
                    currentPersonNum = parents[currentPersonNum];

                    if (currentPersonNum == startingPersonNum) {
                        // mark as final iteration of loop
                        currentPersonNum = 0;
                    }
                }
                System.out.println("Relation: " + this.persons[startingPersonNum].toString() + " -> " + relationString);
            }
        }

        /**
         * **EXTRA CREDIT**
         * List all Descendants from a given person number using BFS
         */
        void descendants(int personNum) {
            Queue<Family> familyWorkQueue = new Queue<>();
            Queue<Person> descendantQueue = new Queue<>();

            boolean[] visited = new boolean[100];

            if (this.persons[personNum] != null) {
                familyWorkQueue.addFrontNode(this.persons[personNum].marriage);

                while (!familyWorkQueue.isEmpty()) {
                    Family currentFamily = familyWorkQueue.removeRearNode();
                    if (currentFamily != null && !visited[currentFamily.familyNum]) {
                        // look through children and add marriages to queue if not yet visited
                        for (Person child : currentFamily.children) {
                            // all children count as descendants
                            descendantQueue.addFrontNode(child);
                            if (child.marriage != null) {
                                // add marriages of children to queue
                                familyWorkQueue.addFrontNode(child.marriage);
                            }
                        }
                        visited[currentFamily.familyNum] = true;
                    }
                }
            }

            if (!descendantQueue.isEmpty()) {
                System.out.println("Descendants of Person " + personNum + ":");
                while (!descendantQueue.isEmpty()) {
                    Person descendant = descendantQueue.removeRearNode();
                    System.out.println(descendant.toString());
                }
                System.out.println(""); // print empty line since println already add newline
            } else {
                System.out.println("Person " + personNum + " has no descendants.");
            }
        }

        /**
         * **EXTRA CREDIT**
         * List all Ancestors from a given person number using BFS.
         */
        void ancestors(int personNum) {
            Queue<Family> familyWorkQueue = new Queue<>();
            Queue<Person> descendantQueue = new Queue<>();

            boolean[] visited = new boolean[100];

            if (this.persons[personNum] != null) {
                visited[personNum] = true;
                familyWorkQueue.addFrontNode(this.persons[personNum].parentage);

                while (!familyWorkQueue.isEmpty()) {
                    Family currentFamily = familyWorkQueue.removeRearNode();
                    // look through husband and wife to add parentage's to queue if not yet visited
                    if (currentFamily != null && !visited[currentFamily.familyNum]) {
                        if (currentFamily.husband != null && currentFamily.husband.personNum != 0) {
                            descendantQueue.addFrontNode(currentFamily.husband);
                            if (currentFamily.husband.parentage != null) {
                                familyWorkQueue.addFrontNode(currentFamily.husband.parentage);
                            }
                        }
                        if (currentFamily.wife != null && currentFamily.wife.personNum != 0) {
                            descendantQueue.addFrontNode(currentFamily.wife);
                            if (currentFamily.wife.parentage != null) {
                                familyWorkQueue.addFrontNode(currentFamily.wife.parentage);
                            }
                        }
                        visited[currentFamily.familyNum] = true;
                    }
                }
            }

            if (!descendantQueue.isEmpty()) {
                System.out.println("Ancestors of Person " + personNum + ":");
                while (!descendantQueue.isEmpty()) {
                    Person descendant = descendantQueue.removeRearNode();
                    System.out.println(descendant.toString());
                }
                System.out.println(""); // print empty line since println already add newline
            } else {
                System.out.println("Person " + personNum + " has no ancestors.");
            }
        }

        static class Person {
            int personNum = 0;
            Family parentage = null;
            Family marriage = null;

            Person() {
            }

            Person(int personNum) {
                this.personNum = personNum;
            }

            @Override
            public String toString() {
                return "Person " + this.personNum;
            }
        }

        static class Family {
            int familyNum = 0;
            Person husband = new Person();
            Person wife = new Person();
            Vector<Person> children = new Vector<>();

            Family() {
            }

            Family(int familyNum, Person husband, Person wife, Vector<Person> children) {

                if (familyNum == 0)
                    softThrow("ERROR", "Family number 0 is invalid!");

                for (Person child : children)
                    if (child.personNum == 0)
                        softThrow("ERROR", "For child, person number 0 is invalid!");

                this.familyNum = familyNum;
                this.husband = husband;
                this.wife = wife;

                // throw a fit if trying to add more than 10 children
                if (children.size() >= 10) {
                    softThrow("ERROR", "Family can't have more than 10 children!");
                }
                this.children = children;

                // establish relations between family; only if known
                if (this.husband.personNum != 0)
                    this.husband.marriage = this;
                if (this.wife.personNum != 0)
                    this.wife.marriage = this;

                for (Person child : this.children) {
                    child.parentage = this;
                }
            }

            /**
             * Gathers all people in a family
             */
            public Vector<Person> getPersons() {
                Vector<Person> persons = new Vector<>();
                persons.add(this.husband);
                persons.add(this.wife);
                persons.addAll(this.children);
                return persons;
            }

            public String toString(boolean simplified) {
                String toString = "Family " + this.familyNum;
                if (!simplified) {
                    String husbandString = this.husband.personNum != 0 ? "Husband " + this.husband.personNum : "has unknown husband";
                    String wifeString = this.wife.personNum != 0 ? "Wife " + this.wife.personNum : "has unknown wife";
                    String childrenString = "no children";
                    if (this.children.size() != 0) {
                        childrenString = "Children";
                        for (Person child : this.children) {
                            if (child.personNum != 0) {
                                childrenString = childrenString.concat(" " + child.personNum);
                            }
                        }
                    }
                    toString = "Family " + this.familyNum + " " + husbandString + ", " + wifeString + ", and " + childrenString;
                }
                return toString;
            }
        }

    }

    /**
     * "Throw" an error to the user, but don't kill the program.
     * Only to be used when a non-destructive error occurs.
     */
    public static void softThrow(String type, String message) {
        System.out.println("\t" + type + ": " + message);
    }

    public static void main(String[] args) throws Exception {

        int a = 381;
        int b = 141;

        while (b != 0) {
            int remainder = a % b;
            a = b;
            b = remainder;

            System.out.println(a + ", " + b);
        }
        System.out.println("Result! " + a);

//        FamilyTree familyTree = new FamilyTree();
//
//        // Scanner for file input
//        Scanner fileScanner;
//
//        fileScanner = new Scanner(System.in);
//
//
//        // parse file word by word
//        while (fileScanner.hasNext()) {
//            Scanner lineScanner = new Scanner(fileScanner.nextLine());
//            while (lineScanner.hasNext()) {
//                // split words to be easily readable
//                String line = lineScanner.nextLine();
//                String[] words = line.split(" ");
//
//                boolean isAddingFamily = words[0].equals("Family");
//                int familyToAdd = 0;
//                int husbandToAdd = 0;
//                int wifeToAdd = 0;
//                Vector<Integer> childrenToAdd = new Vector<>();
//                if (isAddingFamily) {
//                    familyToAdd = Integer.parseInt(words[1]);
//                }
//
//                boolean isCheckingRelation = words[0].equals("Relate");
//                int personRelateA = 0;
//                int personRelateB = 0;
//                if (isCheckingRelation) {
//                    personRelateA = Integer.parseInt(words[1]);
//                    personRelateB = Integer.parseInt(words[2]);
//                }
//
//                boolean isCheckingDescendants = words[0].equals("Descendants");
//                int personDescend = 0;
//                if (isCheckingDescendants) {
//                    personDescend = Integer.parseInt(words[1]);
//                }
//
//                boolean isCheckingAncestry = words[0].equals("Ancestors");
//                int personAscend = 0;
//                if (isCheckingAncestry) {
//                    personAscend = Integer.parseInt(words[1]);
//                }
//
//                // start at 2, since the 0th token is for determining the function and 1st token is assumed to be family number
//                if (isAddingFamily) {
//                    for (int i = 2; i < words.length; i += 2) {
//                        String word = words[i];
//                        if (!word.isEmpty()) {
//                            // adding a family, look for family parameters
//                            if (word.equals("Husband"))
//                                husbandToAdd = Integer.parseInt(words[i + 1]);
//                            if (word.equals("Wife"))
//                                wifeToAdd = Integer.parseInt(words[i + 1]);
//                            if (word.equals("Child"))
//                                childrenToAdd.add(Integer.parseInt(words[i + 1]));
//                        }
//                    }
//                    Vector<FamilyTree.Person> children = new Vector<>();
//                    for (Integer childNum : childrenToAdd) {
//                        children.add(new FamilyTree.Person(childNum));
//                    }
//                    FamilyTree.Family newFamily = new FamilyTree.Family(familyToAdd, new FamilyTree.Person(husbandToAdd), new FamilyTree.Person(wifeToAdd), children);
//                    if (familyTree.insertFamily(newFamily))
//                        System.out.println(newFamily.toString(false));
//                }
//
//                // is checking a relation, look for relate parameters
//                if (isCheckingRelation) {
//                    familyTree.findRelation(personRelateA, personRelateB);
//                }
//
//                if (isCheckingDescendants) {
//                    familyTree.descendants(personDescend);
//                }
//
//                if (isCheckingAncestry) {
//                    familyTree.ancestors(personAscend);
//                }
//
//                if (!isAddingFamily && !isCheckingRelation && !isCheckingDescendants && !isCheckingAncestry) {
//                    // BAD COMMAND, alert user to fix
//                    throw new Exception("ERROR: BAD COMMAND; \"" + words[0] + "\" Please check input and try again.");
//                }
//            }
//        }

    }
}
