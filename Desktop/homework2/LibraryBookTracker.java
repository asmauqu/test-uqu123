import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LibraryBookTracker {
    private static int validRecords = 0;
    private static int searchResults = 0;
    private static int booksAdded = 0;
    private static int errorCount = 0;
    private static String logPath;

    public static void main(String[] args) {
        try {
            // 1. التحقق من المدخلات الأساسية
            if (args.length < 2) throw new InsufficientArgumentsException("Fewer than two arguments provided.");
            if (!args[0].endsWith(".txt")) throw new InvalidFileNameException("File must end with .txt");

            File catalogFile = new File(args[0]);
            logPath = catalogFile.getParent() == null ? "errors.log" : catalogFile.getParent() + "/errors.log";
            
            //إنشاء الملف إذا لم يكن موجود
            if (!catalogFile.exists()) {
                catalogFile.createNewFile();
            }

            List<Book> books = loadAndValidateBooks(catalogFile);
            String operation = args[1];

            // 2. تحديد نوع العملية
            if (operation.matches("\\d{13}")) {
                performISBNSearch(books, operation);
            } else if (operation.contains(":")) {
                addNewBook(books, operation, catalogFile);
            } else {
                performTitleSearch(books, operation);
            }

            // 3. طباعة الإحصائيات
            printStatistics();

        } catch (Exception e) {
            System.err.println("Critical Error: " + e.getMessage());
        } finally {
            System.out.println("Thank you for using the Library Book Tracker.");
        }
    }

    private static List<Book> loadAndValidateBooks(File file) {
        List<Book> list = new ArrayList<>();
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                try {
                    Book b = parseLine(line);
                    list.add(b);
                    validRecords++;
                } catch (BookCatalogException e) {
                    logError(line, e);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file.");
        }
        return list;
    }

    private static Book parseLine(String line) throws BookCatalogException {
        String[] parts = line.split(":");
        if (parts.length != 4) throw new MalformedBookEntryException("Missing fields");
        
        String title = parts[0].trim();
        String author = parts[1].trim();
        String isbn = parts[2].trim();
        int copies;

        if (title.isEmpty() || author.isEmpty()) throw new MalformedBookEntryException("Title/Author empty");
        if (!isbn.matches("\\d{13}")) throw new InvalidISBNException("ISBN must be 13 digits");
        try {
            copies = Integer.parseInt(parts[3].trim());
            if (copies <= 0) throw new MalformedBookEntryException("Copies must be > 0");
        } catch (NumberFormatException e) {
            throw new MalformedBookEntryException("Invalid copies format");
        }

        return new Book(title, author, isbn, copies);
    }

    private static void logError(String offendingText, Exception e) {
        errorCount++;
        try (PrintWriter out = new PrintWriter(new FileWriter(logPath, true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            out.printf("[%s] INVALID INPUT: \"%s\" - %s: %s\n", 
                        timestamp, offendingText, e.getClass().getSimpleName(), e.getMessage());
        } catch (IOException io) {
            System.err.println("Could not write to log file.");
        }
    }

    private static void printHeader() {
        System.out.printf("%-30s %-20s %-15s %5s\n", "Title", "Author", "ISBN", "Copies");
        for(int i = 0; i < 75; i++) System.out.print("-");
          System.out.println();

    }

    private static void printBook(Book b) {
        System.out.printf("%-30.30s %-20.20s %-15s %5d\n", b.title, b.author, b.isbn, b.copies);
    }

    private static void performTitleSearch(List<Book> books, String keyword) {
        printHeader();
        for (Book b : books) {
            if (b.title.toLowerCase().contains(keyword.toLowerCase())) {
                printBook(b);
                searchResults++;
            }
        }
    }

    private static void performISBNSearch(List<Book> books, String isbn) throws DuplicateISBNException {
        List<Book> found = new ArrayList<>();
        for (Book b : books) {
            if (b.isbn.equals(isbn)) found.add(b);
        }
        if (found.size() > 1) throw new DuplicateISBNException("Multiple books with same ISBN found.");
        
        printHeader();
        if (!found.isEmpty()) {
            printBook(found.get(0));
            searchResults = 1;
        }
    }

    private static void addNewBook(List<Book> books, String record, File file) {
        try {
            Book newBook = parseLine(record);
            books.add(newBook);
            Collections.sort(books);
            
            try (PrintWriter pw = new PrintWriter(file)) {
                for (Book b : books) pw.println(b.toFileFormat());
            }
            
            printHeader();
            printBook(newBook);
            booksAdded = 1;
        } catch (BookCatalogException | IOException e) {
            logError(record, e);
            System.out.println("Failed to add book. Check errors.log");
        }
    }

    private static void printStatistics() {
        System.out.println("\n--- Statistics ---");
        System.out.println("Valid records processed: " + validRecords);
        System.out.println("Search results: " + searchResults);
        System.out.println("Books added: " + booksAdded);
        System.out.println("Errors encountered: " + errorCount);
    }
}