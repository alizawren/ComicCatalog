package com.alizawren.comiccatalog;

import android.support.annotation.NonNull;
import android.util.Log;

import com.alizawren.comiccatalog.util.Callback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Alisa Ren on 4/17/2019.
 */

public class FirebaseUtil {

    public static final String COMIC_BOOK_COLLECTION_KEY = "comicBooks";
    public static final String USER_COLLECTION_KEY = "users";

    static public Callback<List<ComicBook>> getComicBooks() {
        final Callback<List<ComicBook>> callback = new Callback<>();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            callback.reject();
            return callback;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(COMIC_BOOK_COLLECTION_KEY)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<ComicBook> result = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                result.add(document.toObject(ComicBook.class));

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                        callback.resolve(result);
                    }
                });

        return callback;
    }

    static public Callback<ComicBook> addComicBook(User user, final ComicBook comicBook) {
        final Callback<ComicBook> callback = new Callback<>();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            callback.reject();
            return callback;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // NOTE: THIS NEEDS TO CHANGE TO HAVE COMIC BOOKS BE STORED UNDER USER
        // Store more than just ISBN
        firestore.collection(COMIC_BOOK_COLLECTION_KEY)
                .document(comicBook.getIsbn())
                .set(comicBook)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Comic book successfully written!");
                        callback.resolve(comicBook);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing comic book", e);
                        callback.reject();
                    }
                });

        return callback;
    }

    static public void removeComicBook(ComicBook comicBook) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(COMIC_BOOK_COLLECTION_KEY)
                .document(comicBook.getIsbn())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Comic book successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting comic book", e);
                    }
                });
    }

    static public Callback<User> getUser(final FirebaseUser firebaseUser) {
        System.out.println("Get user is called");
        final Callback<User> callback = new Callback<>();

        if (firebaseUser == null) {
            System.out.println("User was null, returning");
            callback.reject();
            return callback;
        }

        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        final DocumentReference userReference = firestore.collection(USER_COLLECTION_KEY)
                .document(firebaseUser.getUid());

        userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot userDocument = task.getResult();
                    if (!userDocument.exists()) {
                        User newUser = new User(firebaseUser.getUid(), firebaseUser.getDisplayName(),
                                firebaseUser.getEmail());
                        userReference.set(newUser);
                        firestore.document("emails/" + newUser.getEmail()).set(newUser);
                        callback.resolve(newUser);

                        System.out.println("MADE A USER");
                    } else {
                        User newUser = userDocument.toObject(User.class);
                        callback.resolve(newUser);

                        System.out.println("had A USER");
                    }
                } else {
                    System.out.println("Task wasn't successful");
                }
            }
        });

        return callback;
    }
}
