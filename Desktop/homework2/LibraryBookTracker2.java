// Homework 3: Multi-threaded Library Book Tracker with Runnable Interface
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class LibraryBookTracker2 {
    static List<String[]> bookList = new ArrayList<>();
    static int validRecordsProcessed = 0;
    static int errorsEncountered = 0;
    static int booksAdded = 0;
    static int searchResultsCount = 0;

    public static void main(String[] args) {
    try {
        if (args.length < 2) {
            String errorMsg = "Missing arguments. Usage: java LibraryBookTrackerV3 <filename> <operation>";
            System.out.println("Error: " + errorMsg);
            logError(errorMsg); 
            return;
        }

        String fileName = args[0];
        String operation = args[1];

        if (!fileName.endsWith(".txt")) {
            String extError = "Invalid file extension: " + fileName;
            System.out.println("Error: " + extError);
            logError(extError);
            return;
        }

        Thread fileThread = new Thread(new FileReaderTask(fileName));
        Thread opThread = new Thread(new OperationAnalyzerTask(operation, fileName));

        fileThread.start();
        fileThread.join(); 

        opThread.start();
        opThread.join(); 

    } catch (InterruptedException e) {
        System.out.println("Thread execution interrupted.");
        logError("Thread Error: " + e.getMessage());
    } catch (Exception e) {
        System.out.println("An unexpected error occurred.");
        logError("General Error: " + e.getMessage());
    }

    System.out.println("\n--- Final Statistics ---");
    System.out.println("Number of valid records processed: " + validRecordsProcessed);
    System.out.println("Number of search results: " + searchResultsCount);
    System.out.println("Number of books added: " + booksAdded);
    System.out.println("Number of errors encountered: " + errorsEncountered);
    
    System.out.println("\nThank you for using the Library Book Tracker.");
}

private static void logError(String msg) {
    try (PrintWriter out = new PrintWriter(new FileWriter("errors.log", true))) {
        out.println("[" + java.time.LocalDateTime.now() + "] " + msg);
    } catch (java.io.IOException e) {
        System.out.println("Failed to write to log file.");
    }
}
}

class FileReaderTask implements Runnable {
    private String fileName;

    public FileReaderTask(String fileName) { this.fileName = fileName; }

    @Override
    public void run() {
          System.out.println("[Thread 1] Starting to read the catalog file...");
        File file = new File(fileName);
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] parts = line.split(":");
                if (parts.length == 4) {
                    LibraryBookTracker2.bookList.add(parts);
                    LibraryBookTracker2.validRecordsProcessed++;
                } else {
                    LibraryBookTracker2.errorsEncountered++;
                    logError("Malformed record: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Critical Error: File not found.");
        }
    }

    private void logError(String msg) {
        try (PrintWriter out = new PrintWriter(new FileWriter("errors.log", true))) {
            out.println("[" + LocalDateTime.now() + "] " + msg);
        } catch (IOException e) { e.printStackTrace(); }
    }
}

class OperationAnalyzerTask implements Runnable {
    private String arg;
    private String fileName;

    public OperationAnalyzerTask(String arg, String fileName) { 
        this.arg = arg; 
        this.fileName = fileName;
    }

    @Override
    public void run() {
        System.out.println("[Thread 2] Reading complete. Starting operation: " + arg);
        if (arg.contains(":")) {
            addBook(arg);
        } else {
            searchBooks(arg);
        }
    }

    private void searchBooks(String query) {
        System.out.printf("%-30s %-20s %-15s %5s%n", "Title", "Author", "ISBN", "Copies");
        System.out.println("----------------------------------------------------------------------");
        for (String[] book : LibraryBookTracker2.bookList) {
            if (book[0].toLowerCase().contains(query.toLowerCase()) || book[2].equals(query)) {
                System.out.printf("%-30s %-20s %-15s %5s%n", book[0], book[1], book[2], book[3]);
                LibraryBookTracker2.searchResultsCount++;
            }
        }
    }

    private void addBook(String data) {
try (PrintWriter pw = new PrintWriter(new FileWriter(fileName, true))) {
            pw.println(data);
            LibraryBookTracker2.booksAdded = 1;
            System.out.println("Book added successfully to " + fileName);
        } catch (IOException e) {
            System.out.println("Error writing to file.");
        }
    }
}