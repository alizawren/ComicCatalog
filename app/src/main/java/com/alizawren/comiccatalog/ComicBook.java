package com.alizawren.comiccatalog;

/**
 * Created by Alisa Ren on 4/17/2019.
 */
public class ComicBook {
    private String title;
    private String isbn;
    private String recordUrl;

    public ComicBook(String title, String isbn, String recordUrl) {
        this.title = title;
        this.isbn = isbn;
        this.recordUrl = recordUrl;
    }

    public String getIsbn() {
        return isbn;
    }

}
