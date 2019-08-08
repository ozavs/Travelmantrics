package com.example.travelmantrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {

    public static final int PICTURE_SELECT = 300;
    FirebaseDatabase mDatabase;
    DatabaseReference mReference;

    TextView title, price,description;
    Button btn;
    ImageView imageView;
    TravelDeal deal;

    @Override
    protected void onCreate ( Bundle savedInstanceState ) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_deal );

        mDatabase = FirebaseUtils.mDatabase;
        mReference = FirebaseUtils.mDatabaseReference;
        mReference = mDatabase.getReference ().child ( "traveldeal" );

        title = findViewById ( R.id.txtTitle );
        price = findViewById ( R.id.txtPrice );
        description = findViewById ( R.id.txtDescription );
        btn = findViewById ( R.id.btnImage );
        imageView = findViewById ( R.id.image );
        Intent intent = getIntent ();

        TravelDeal tv = (TravelDeal) intent.getSerializableExtra ("deal");

        if(tv == null) {
            tv = new TravelDeal();
        }

        this.deal = tv;

        title.setText ( tv.getTitle () );
        description.setText ( tv.getDescription () );
        price.setText ( tv.getPrice () );
        showImage ( deal.getImageUrl () );

        btn.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public void onClick ( View view ) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType ( "image/jpeg" );
                intent.putExtra ( Intent.EXTRA_LOCAL_ONLY, true );
                startActivityForResult ( Intent.createChooser ( intent,"Picture select"), PICTURE_SELECT );
            }
        } );
    }

    @Override
    protected void onActivityResult ( int requestCode , int resultCode , @Nullable Intent data ) {
        super.onActivityResult ( requestCode , resultCode , data );
        if(requestCode == PICTURE_SELECT && resultCode == RESULT_OK){
            Uri imageUri = data.getData ();

            StorageReference sRef = FirebaseUtils.mStorageReference.child ( System.currentTimeMillis () + ".jpg" );
            sRef.putFile ( imageUri ).addOnSuccessListener ( new OnSuccessListener<UploadTask.TaskSnapshot> ( ) {
                @Override
                public void onSuccess ( UploadTask.TaskSnapshot taskSnapshot ) {
                    if(taskSnapshot.getMetadata () != null) {
                        String imageName = taskSnapshot.getStorage ().getName ();
                        deal.setImageName ( imageName );

                        //Next get URL of uploaded image
                        Task<Uri> task = taskSnapshot.getStorage ().getDownloadUrl ();
                        task.addOnSuccessListener ( new OnSuccessListener<Uri> ( ) {
                            @Override
                            public void onSuccess ( Uri uri ) {
                                String url = uri.toString();
                                deal.setImageUrl ( url );
                                showImage(url);
                            }
                        } );
                    }
                }
            } );




        }
    }

    private void showImage (String url) {

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        Picasso.with ( getApplicationContext () )
                .load ( url )
                .resize ( width, width * 2/3 )
                .centerCrop ()
                .into ( imageView );

    }

    private void saveDeal() {
        deal.setTitle(title.getText().toString().trim());
        deal.setPrice(price.getText().toString().trim());
        deal.setDescription(description.getText().toString().trim ());

        if(deal.getId() == null) {
            mReference.push().setValue(deal);
        }
        else {
            mReference.child(deal.getId()).setValue(deal);
        }

        Toast.makeText(this.getApplicationContext(), "Deal saved successfully", Toast.LENGTH_LONG).show();
        clean();
        backToList();
    }

    private void deleteDeal() {
        if(deal == null) {
            Toast.makeText ( this.getBaseContext (),
                    "You have to save a deal before deleting it",
                    Toast.LENGTH_LONG ).show ();
            return;
        }

        mReference.child ( deal.getId () ).removeValue ();

        if(deal.getImageName() != null && !deal.getImageName().isEmpty()) {
            StorageReference ref = FirebaseUtils.mStorage.getReference().child(deal.getImageName());

            //Delete the file
            ref.delete().addOnSuccessListener(new OnSuccessListener<Void> () {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getApplicationContext(),
                            "Image removed from cloud storage", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener () {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getApplicationContext(),
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void backToList () {
        Intent intent = new Intent(this, DealListActivity.class);
        intent.setFlags ( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity ( intent );
    }

    private void clean () {
        title.setText ( "" );
        description.setText ( "" );
        price.setText ( "" );
        title.requestFocus ();
    }

    @Override
    public boolean onCreateOptionsMenu ( Menu menu ) {
        getMenuInflater().inflate( R.menu.list_save_menu, menu );

        MenuItem save = menu.findItem ( R.id.save_menu );
        MenuItem delete = menu.findItem ( R.id.delete_deal );

        if(FirebaseUtils.isAdmin) {
            save.setVisible ( true );
            delete.setVisible ( true );
            enableEditText ( true);
            findViewById(R.id.btnImage).setEnabled(true);

        }else {
            save.setVisible ( false );
            delete.setVisible ( false );
            enableEditText ( false );
            findViewById(R.id.btnImage).setEnabled(false);
        }

        return true;
        //return super.onCreateOptionsMenu ( menu );
    }

    private void enableEditText (boolean isEnabled) {
        title.setEnabled ( isEnabled );
        description.setEnabled ( isEnabled );
        price.setEnabled ( isEnabled );
    }

    @Override
    public boolean onOptionsItemSelected ( @NonNull MenuItem item ) {

        switch (item.getItemId()) {

            case(R.id.save_menu):
                saveDeal();
                return true;

            case R.id.delete_deal:
                deleteDeal();
                Toast.makeText(this, "Deal deleted successfully", Toast.LENGTH_LONG).show();
                backToList();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
