import java.time.LocalDateTime;

class BookCatalogException extends Exception {
    public BookCatalogException(String message) { super(message); }
}

class InvalidISBNException extends BookCatalogException {
    public InvalidISBNException(String message) { super(message); }
}

class DuplicateISBNException extends BookCatalogException {
    public DuplicateISBNException(String message) { super(message); }
}

class MalformedBookEntryException extends BookCatalogException {
    public MalformedBookEntryException(String message) { super(message); }
}

class InsufficientArgumentsException extends Exception {
    public InsufficientArgumentsException(String message) { super(message); }
}

class InvalidFileNameException extends Exception {
    public InvalidFileNameException(String message) { super(message); }
}