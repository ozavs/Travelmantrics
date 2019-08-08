package com.example.travelmantrics;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtils {

    private static final int RC_SIGN_IN = 20;
    public static FirebaseDatabase mDatabase;
    public static DatabaseReference mDatabaseReference;
    public static FirebaseAuth mAuth;
    public static FirebaseAuth.AuthStateListener mAuthListener;
    public static FirebaseStorage mStorage;
    public static StorageReference mStorageReference;
    public static ArrayList<TravelDeal> mDeals;

    private static FirebaseUtils firebaseUtils;
    private static DealListActivity caller;
    public static boolean isAdmin;

    private FirebaseUtils() {

    }

    public static void openFirebaseReference(String ref) {
    }

    public static void openFirebaseReference( String ref, DealListActivity callerActivity) {
        if(firebaseUtils == null) {
            firebaseUtils = new FirebaseUtils ();

            mDatabase = FirebaseDatabase.getInstance();
            mAuth = FirebaseAuth.getInstance();

            caller = callerActivity;
            mAuthListener = new FirebaseAuth.AuthStateListener ( ) {
                @Override
                public void onAuthStateChanged ( @NonNull FirebaseAuth firebaseAuth ) {
                    if(firebaseAuth.getCurrentUser () == null) {

                        //Show the user a login screen
                        FirebaseUtils.signIn();
                    }else {
                        //Get the user id
                        String  userId = firebaseAuth.getUid ();
                        checkAdmin(userId);
                    }
                }
            };

            mStorage = FirebaseStorage.getInstance ();
            mStorageReference = mStorage.getReference ().child ( "deal_image" );
        }

        mDatabaseReference = mDatabase.getReference().child( ref );
        mDeals = new ArrayList<TravelDeal>();
    }

    private static void checkAdmin ( String userId ) {
        isAdmin = false;

        DatabaseReference sRef = FirebaseUtils.mDatabase.getReference ().child ( "administrator" ).child ( userId );
        sRef.addChildEventListener ( new ChildEventListener ( ) {
            @Override
            public void onChildAdded ( @NonNull DataSnapshot dataSnapshot , @Nullable String s ) {
                FirebaseUtils.isAdmin = true;
                caller.showMenu();
            }

            @Override
            public void onChildChanged ( @NonNull DataSnapshot dataSnapshot , @Nullable String s ) {

            }

            @Override
            public void onChildRemoved ( @NonNull DataSnapshot dataSnapshot ) {

            }

            @Override
            public void onChildMoved ( @NonNull DataSnapshot dataSnapshot , @Nullable String s ) {

            }

            @Override
            public void onCancelled ( @NonNull DatabaseError databaseError ) {

            }
        } );
    }

    private static void signIn() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());


// Create and launch sign-in intent
        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme ( R.style.FullScreenTheme )
                        .build(),
                RC_SIGN_IN);
    }


    public static void attachListener() {
        mAuth.addAuthStateListener ( mAuthListener );
    }

    public static void removeListener () {
        mAuth.removeAuthStateListener ( mAuthListener );
    }
}
