package com.example.travelmantrics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class DealListActivity extends AppCompatActivity {

    @Override
    protected void onCreate ( Bundle savedInstanceState ) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );
    }

    public void showMenu() {
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu ( Menu menu ) {

        getMenuInflater ().inflate ( R.menu.list_deal_menu, menu );

        MenuItem insert = menu.findItem ( R.id.create_deal );

        if(FirebaseUtils.isAdmin) {
            insert.setVisible ( true );
        }else {
           insert.setVisible ( false );
        }
        return true;
        //return super.onCreateOptionsMenu ( menu );
    }

    @Override
    public boolean onOptionsItemSelected ( @NonNull MenuItem item ) {

        switch(item.getItemId ()) {
            case R.id.create_deal:
                Intent intent = new Intent (this, DealActivity.class);
                startActivity ( intent );
                return true;

            case R.id.sign_out:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void> () {
                            public void onComplete(@NonNull Task<Void> task) {
                                FirebaseUtils.attachListener();
                            }
                        });

                FirebaseUtils.removeListener ();

                return true;

            default:
                return super.onOptionsItemSelected ( item );
        }

    }

    @Override
    protected void onResume () {
        super.onResume ( );

        FirebaseUtils.openFirebaseReference ( "traveldeal", this );
        RecyclerView rv = findViewById(R.id.rvDeals);
        DealAdapter adapter = new DealAdapter();
        rv.setAdapter(adapter);

        //Define the layout manager: linearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                RecyclerView.VERTICAL,
                false);

        rv.setLayoutManager(layoutManager);

        FirebaseUtils.attachListener ();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtils.removeListener ();
    }
}
