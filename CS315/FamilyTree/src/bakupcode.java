//public class bakupcode {
//    void findRelation(int personNumA, int personNumB) {
//        // perform a union find to establish if there is a relation at all
//        // family number for person N for union find
//        boolean isConnected = false;
//        int[] parents = new int[100];
//        int[] families = new int[100];
//
//        for (int i = 1; i < 100; i++) {
//            genealogy.FamilyTree.Person person = this.persons[i];
//            // skip iteration if person is null
//            if (person == null) {
//                continue;
//            }
//
//            int familyA = find(families, person.marriage);
//            int familyB = find(families, person.parentage);
//
//            if (familyA == familyB) {
//                isConnected = true;
//                break;
//            }
//
////                if (familyA == 0 || familyB == 0) {
////                    // person has no relation and no way to connect to other person, exit early
////                    break;
////                }
//
//            union(parents, families, familyA, familyB);
//        }
//
//        if (isConnected) {
//            // use BFS to find shortest path
//            breadthFirstSearch(personNumA, personNumB);
//        } else {
//            System.out.println("Person " + personNumA + " and " + personNumB + " are not related.");
//        }
//    }
//
//    private int find(int[] families, genealogy.FamilyTree.Family family) {
//        int familyNum = 0;
//        if (family != null) {
//            for (genealogy.FamilyTree.Person person : family.getPersons()) {
//                if (families[person.personNum] != 0) {
//                    if (person.parentage != null && families[person.personNum] != person.parentage.familyNum) {
//                        familyNum = find(families, person.parentage);
//                    } else if (person.marriage != null && families[person.personNum] != person.marriage.familyNum) {
//                        familyNum = find(families, person.marriage);
//                    }
//                }
//
//                if (families[person.personNum] == 0
//                        || (person.parentage != null && families[person.personNum] != person.parentage.familyNum)
//                        || (person.marriage != null && families[person.personNum] != person.marriage.familyNum)) {
//                    if (person.parentage != null) {
//                        familyNum = person.parentage.familyNum;
//                        families[person.personNum] = familyNum;
//                    } else if (person.marriage != null) {
//                        familyNum = person.marriage.familyNum;
//                        families[person.personNum] = familyNum;
//                    }
//                }
//            }
//        }
//        return familyNum;
//    }
//
//    private void union(int[] parents, int[] families, int personNumA, int personNumB) {
//        int personAFamilyNum = find(families, this.persons[personNumA].parentage);
//        int personBFamilyNum = find(families, this.persons[personNumB].marriage);
//        families[personAFamilyNum] = personBFamilyNum;
//    }
//}
