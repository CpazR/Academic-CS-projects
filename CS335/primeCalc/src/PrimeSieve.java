import java.util.Arrays;

class PrimeSieve {

    private final Boolean[] isPrimeArray;

    PrimeSieve(int size) {
        this.isPrimeArray = new Boolean[size];
        // Autofill and calculate prime numbers given range
        Arrays.fill(isPrimeArray, 2, size, true);
        run();
    }

    void run() {
        for (int i = 2; i < this.isPrimeArray.length; i++) {
            sieveMethod(i);

        }
    }

    /**
     * Helper method for the iteration over multiples of `divideBy`
     */
    private void sieveMethod(int divideBy) {
        for (int i = divideBy; i < this.isPrimeArray.length; i += divideBy) {
            // if there is a remainder, label at index i, not a prime
            if (i != divideBy && i % divideBy == 0) {
                this.isPrimeArray[i] = false;
            }
        }
    }

    /**
     * Checks if number is prime based on mapped array from sieve
     */
    private boolean isPrime(int numberToCheck) throws ArrayIndexOutOfBoundsException {
        if (numberToCheck < 2) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return isPrimeArray[numberToCheck];
    }

    /**
     * Given a number, check if it's prime based on sieve; return message based on check
     */
    public String lookForPrimeMessage(int numberToCheck) {
        var returnMessage = "Uncaught internal error!";
        try {
            returnMessage = numberToCheck + (isPrime(numberToCheck) ? " is a prime number!" : " is not a prime number!");
        } catch (ArrayIndexOutOfBoundsException e) {
            returnMessage = "Invalid input: Only checks values between 2 - 1,000,000";
        }
        return returnMessage;
    }

}