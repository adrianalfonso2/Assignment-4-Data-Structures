import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;

// Interface for Hash Functions
interface HashFunctionInterface {
    int computeHash(String key);
}

// Hash Function 1: Early Java hashCode implementation
class HashFunction1 implements HashFunctionInterface {
    public int computeHash(String key) {
        int hash = 0;
        int skip = Math.max(1, key.length() / 8);
        for (int i = 0; i < key.length(); i += skip) {
            hash = (hash * 37) + key.charAt(i);
        }
        return hash;
    }
}

// Hash Function 2: Current Java hashCode implementation
class HashFunction2 implements HashFunctionInterface {
    public int computeHash(String key) {
        int hash = 0;
        for (int i = 0; i < key.length(); i++) {
            hash = (hash * 31) + key.charAt(i);
        }
        return hash;
    }
}

// Class to hold search results
class SearchResult {
    boolean found;
    int comparisons;

    SearchResult(boolean found, int comparisons) {
        this.found = found;
        this.comparisons = comparisons;
    }
}

// Separate Chaining Hash Table
class SeparateChainingHashTable {
    private int M;
    private LinkedList<Entry>[] table;
    private HashFunctionInterface hashFunction;

    public SeparateChainingHashTable(int size, HashFunctionInterface hashFunction) {
        this.M = size;
        this.hashFunction = hashFunction;
        // Suppressing unchecked warning for generic array creation
        @SuppressWarnings("unchecked")
        LinkedList<Entry>[] tempTable = new LinkedList[M];
        table = tempTable;
        for (int i = 0; i < M; i++) {
            table[i] = new LinkedList<>();
        }
    }

    // Entry class to store key and value
    class Entry {
        String key;
        int value;

        Entry(String key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    // Insert key with value into the hash table
    public void insert(String key, int value) {
        int hash = Math.abs(hashFunction.computeHash(key)) % M;
        for (Entry entry : table[hash]) {
            if (entry.key.equals(key)) {
                // Update value if key already exists
                entry.value = value;
                return;
            }
        }
        table[hash].add(new Entry(key, value));
    }

    // Search for a key and return the number of comparisons
    public SearchResult search(String key) {
        int hash = Math.abs(hashFunction.computeHash(key)) % M;
        int comparisons = 0;
        for (Entry entry : table[hash]) {
            comparisons++;
            if (entry.key.equals(key)) {
                return new SearchResult(true, comparisons);
            }
        }
        return new SearchResult(false, comparisons);
    }
}

// Linear Probing Hash Table
class LinearProbingHashTable {
    private int M;
    private Entry[] table;
    private HashFunctionInterface hashFunction;

    public LinearProbingHashTable(int size, HashFunctionInterface hashFunction) {
        this.M = size;
        this.hashFunction = hashFunction;
        table = new Entry[M];
    }

    // Entry class to store key and value
    class Entry {
        String key;
        int value;

        Entry(String key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    // Insert key with value into the hash table using linear probing
    public void insert(String key, int value) {
        int hash = Math.abs(hashFunction.computeHash(key)) % M;
        while (table[hash] != null) {
            if (table[hash].key.equals(key)) {
                // Update value if key already exists
                table[hash].value = value;
                return;
            }
            hash = (hash + 1) % M;
        }
        table[hash] = new Entry(key, value);
    }

    // Search for a key and return the number of comparisons
    public SearchResult search(String key) {
        int hash = Math.abs(hashFunction.computeHash(key)) % M;
        int comparisons = 0;
        int startHash = hash; // To detect if we've looped around

        while (true) {
            comparisons++;
            if (table[hash] == null) {
                // Key not found
                return new SearchResult(false, comparisons);
            }
            if (table[hash].key.equals(key)) {
                // Key found
                return new SearchResult(true, comparisons);
            }
            hash = (hash + 1) % M;
            if (hash == startHash) {
                // Searched the entire table
                return new SearchResult(false, comparisons);
            }
        }
    }
}

public class StrongPasswordChecker {
    // URLs and constants
    private static final String DICTIONARY_URL = "https://www.mit.edu/~ecprice/wordlist.10000";
    private static final int SEPARATE_CHAINING_SIZE = 1000;
    private static final int LINEAR_PROBING_SIZE = 20000;

    public static void main(String[] args) {
        try {
            // Initialize hash functions
            HashFunctionInterface hashFunc1 = new HashFunction1();
            HashFunctionInterface hashFunc2 = new HashFunction2();

            // Initialize hash tables
            SeparateChainingHashTable scHash1 = new SeparateChainingHashTable(SEPARATE_CHAINING_SIZE, hashFunc1);
            SeparateChainingHashTable scHash2 = new SeparateChainingHashTable(SEPARATE_CHAINING_SIZE, hashFunc2);
            LinearProbingHashTable lpHash1 = new LinearProbingHashTable(LINEAR_PROBING_SIZE, hashFunc1);
            LinearProbingHashTable lpHash2 = new LinearProbingHashTable(LINEAR_PROBING_SIZE, hashFunc2);

            // Load dictionary from URL
            System.out.println("Loading dictionary...");
            URL url = new URL(DICTIONARY_URL);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            int lineNumber = 1;
            while ((line = br.readLine()) != null) {
                String word = line.trim().toLowerCase();
                if (!word.isEmpty()) {
                    scHash1.insert(word, lineNumber);
                    scHash2.insert(word, lineNumber);
                    lpHash1.insert(word, lineNumber);
                    lpHash2.insert(word, lineNumber);
                    lineNumber++;
                }
            }
            br.close();
            System.out.println("Dictionary loaded with " + (lineNumber - 1) + " words.\n");

            // Define test passwords
            String[] testPasswords = {
                "account8",
                "accountability",
                "9a$D#qW7!uX&Lv3zT",
                "B@k45*W!c$Y7#zR9P",
                "X$8vQ!mW#3Dz&Yr4K5"
            };

            // Alternatively, read password from command line
            /*
            if (args.length != 1) {
                System.out.println("Usage: java StrongPasswordChecker <password>");
                return;
            }
            String password = args[0];
            String[] testPasswords = { password };
            */

            // Process each password
            for (String password : testPasswords) {
                System.out.println("Checking password: " + password);
                boolean isStrong = checkStrongPassword(password, scHash1, scHash2, lpHash1, lpHash2);
                if (isStrong) {
                    System.out.println("Result: The password is STRONG.\n");
                } else {
                    System.out.println("Result: The password is NOT STRONG.\n");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to check if a password is strong
    private static boolean checkStrongPassword(String password,
                                               SeparateChainingHashTable scHash1,
                                               SeparateChainingHashTable scHash2,
                                               LinearProbingHashTable lpHash1,
                                               LinearProbingHashTable lpHash2) {
        boolean isStrong = true;

        // (i) Check length
        if (password.length() < 8) {
            System.out.println("Password length is less than 8 characters.");
            isStrong = false;
        }

        // Prepare variations for checks
        String lowerPassword = password.toLowerCase();
        String passwordBaseDigit = null;

        // Check if password ends with a digit
        if (Character.isDigit(password.charAt(password.length() - 1))) {
            passwordBaseDigit = lowerPassword.substring(0, lowerPassword.length() - 1);
        }

        // (ii) Check if it's a word in the dictionary using all hash tables and hash functions

        // Separate Chaining with Hash Function 1
        SearchResult scHash1Result = scHash1.search(lowerPassword);
        boolean foundInScHash1 = scHash1Result.found;
        int comparisonsScHash1 = scHash1Result.comparisons;

        // Separate Chaining with Hash Function 2
        SearchResult scHash2Result = scHash2.search(lowerPassword);
        boolean foundInScHash2 = scHash2Result.found;
        int comparisonsScHash2 = scHash2Result.comparisons;

        // Linear Probing with Hash Function 1
        SearchResult lpHash1Result = lpHash1.search(lowerPassword);
        boolean foundInLpHash1 = lpHash1Result.found;
        int comparisonsLpHash1 = lpHash1Result.comparisons;

        // Linear Probing with Hash Function 2
        SearchResult lpHash2Result = lpHash2.search(lowerPassword);
        boolean foundInLpHash2 = lpHash2Result.found;
        int comparisonsLpHash2 = lpHash2Result.comparisons;

        // If the password is found in any of the hash tables, it's not strong
        if (foundInScHash1 || foundInScHash2 || foundInLpHash1 || foundInLpHash2) {
            System.out.println("Password is a dictionary word.");
            isStrong = false;
        }

        // (iii) Check if it's a word followed by a digit
        if (passwordBaseDigit != null) {
            // Separate Chaining with Hash Function 1
            SearchResult scHash1DigitResult = scHash1.search(passwordBaseDigit);
            boolean foundDigitInScHash1 = scHash1DigitResult.found;
            int comparisonsDigitScHash1 = scHash1DigitResult.comparisons;

            // Separate Chaining with Hash Function 2
            SearchResult scHash2DigitResult = scHash2.search(passwordBaseDigit);
            boolean foundDigitInScHash2 = scHash2DigitResult.found;
            int comparisonsDigitScHash2 = scHash2DigitResult.comparisons;

            // Linear Probing with Hash Function 1
            SearchResult lpHash1DigitResult = lpHash1.search(passwordBaseDigit);
            boolean foundDigitInLpHash1 = lpHash1DigitResult.found;
            int comparisonsDigitLpHash1 = lpHash1DigitResult.comparisons;

            // Linear Probing with Hash Function 2
            SearchResult lpHash2DigitResult = lpHash2.search(passwordBaseDigit);
            boolean foundDigitInLpHash2 = lpHash2DigitResult.found;
            int comparisonsDigitLpHash2 = lpHash2DigitResult.comparisons;

            // If the base password is found in any of the hash tables, it's not strong
            if (foundDigitInScHash1 || foundDigitInScHash2 || foundDigitInLpHash1 || foundDigitInLpHash2) {
                System.out.println("Password is a dictionary word followed by a digit.");
                isStrong = false;
            }

            // Display additional comparison costs for "Digit Base"
            System.out.println("Additional Search Costs for Digit Base:");
            System.out.println("Separate Chaining with Hash Function 1 (Digit Base): " + comparisonsDigitScHash1 + " comparisons");
            System.out.println("Separate Chaining with Hash Function 2 (Digit Base): " + comparisonsDigitScHash2 + " comparisons");
            System.out.println("Linear Probing with Hash Function 1 (Digit Base): " + comparisonsDigitLpHash1 + " comparisons");
            System.out.println("Linear Probing with Hash Function 2 (Digit Base): " + comparisonsDigitLpHash2 + " comparisons");
        }

        // Display comparison costs for the main password
        System.out.println("Search Costs:");
        System.out.println("Separate Chaining with Hash Function 1: " + comparisonsScHash1 + " comparisons");
        System.out.println("Separate Chaining with Hash Function 2: " + comparisonsScHash2 + " comparisons");
        System.out.println("Linear Probing with Hash Function 1: " + comparisonsLpHash1 + " comparisons");
        System.out.println("Linear Probing with Hash Function 2: " + comparisonsLpHash2 + " comparisons");

        return isStrong;
    }
}