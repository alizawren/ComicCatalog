package com.alizawren.comiccatalog;

/**
 * Created by Alisa Ren on 4/17/2019.
 */
public class ComicBook {
    private String isbn;
    private String title;
    private String recordUrl;
    private String imageUrl;

    public ComicBook() {

    }

    public ComicBook(String isbn, String title, String recordUrl, String imageUrl) {
        this.isbn = isbn;
        this.title = title;
        this.recordUrl = recordUrl;
        this.imageUrl = imageUrl;
    }

    public String getIsbn() {
        return isbn;
    }

}
