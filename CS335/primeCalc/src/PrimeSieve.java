import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class PrimeSieve {

    private final Boolean[] isPrime;

    PrimeSieve(int size) {
        this.isPrime = new Boolean[size];
        Arrays.fill(isPrime, 2, size, true);
    }

    void run() {
        for (int i = 2; i < this.isPrime.length; i++) {
            sieveMethod(i);

        }
    }

    /**
     * Helper method for the iteration over multiples of `divideBy`
     */
    void sieveMethod(int divideBy) {
        for (int i = divideBy; i < this.isPrime.length; i += divideBy) {
            // if there is a remainder, label at index i, not a prime
            if (i != divideBy && i % divideBy == 0) {
                this.isPrime[i] = false;
            }
        }
    }

    void histogram(int numberOfIntervals) {
        System.out.println("The CS335 Prime Number Histogram (# of Primes in each interval)");

        // Use collections to cleanly count the number of primes
        List<Boolean> primeList = Arrays.stream(isPrime).collect(Collectors.toList());

        printHistogram(numberOfIntervals, primeList);
    }

    /**
     * Given a list of booleans, print out the number of prime numbers in intervals of 100
     */
    private void printHistogram(int numberOfIntervals, List<Boolean> primeList) {
        // Set initial interval
        int intervalSize = primeList.size() / numberOfIntervals;
        int startOfInterval = 0;
        int endOfInterval = intervalSize;
        int intervalPrimeCount = 0;

        for (int currentInterval = 0; currentInterval < primeList.size(); currentInterval++) {
            if (Objects.nonNull(primeList.get(currentInterval)) && primeList.get(currentInterval)) {
                intervalPrimeCount++;
            }

            if (currentInterval == endOfInterval - 1) {
                // Print out current interval information
                System.out.println("Interval " + startOfInterval + " - " + currentInterval + " count: " + intervalPrimeCount);
                // Reset for next interval
                startOfInterval += intervalSize;
                endOfInterval += intervalSize;
                if (endOfInterval >= primeList.size()) {
                    endOfInterval = primeList.size();
                }
                intervalPrimeCount = 0;
            }
        }
    }
}