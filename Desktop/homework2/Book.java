class Book implements Comparable<Book> {
    String title, author, isbn;
    int copies;

    public Book(String title, String author, String isbn, int copies) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.copies = copies;
    }

    @Override
    public int compareTo(Book other) {
        return this.title.compareToIgnoreCase(other.title);
    }
    
    public String toFileFormat() {
        return title + ":" + author + ":" + isbn + ":" + copies;
    }
}