 package com.example.firebase;

 import android.content.Intent;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.Toast;

 import androidx.annotation.NonNull;
 import androidx.appcompat.app.AlertDialog;
 import androidx.appcompat.app.AppCompatActivity;

 import com.google.firebase.database.DataSnapshot;
 import com.google.firebase.database.DatabaseError;
 import com.google.firebase.database.DatabaseReference;
 import com.google.firebase.database.FirebaseDatabase;
 import com.google.firebase.database.ValueEventListener;

 import java.util.ArrayList;
 import java.util.List;

 public class MainActivity extends AppCompatActivity {

     public static final String ARTIST_NAME = "artistname";
     public static final String ARTIST_ID = "artistid";
     EditText editTextName;
     Button buttonAdd;
     Spinner spinnerGenres;

     DatabaseReference databaseArtist;
     ListView listViewArtist;
     List<Artist> artistList;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);

         databaseArtist = FirebaseDatabase.getInstance().getReference("artist");

         editTextName = (EditText) findViewById(R.id.editTextName);
         buttonAdd = (Button) findViewById(R.id.buttonAddArtist);
         spinnerGenres = (Spinner) findViewById(R.id.spinnerGenres);

         listViewArtist = (ListView) findViewById(R.id.listViewArtist);
         artistList = new ArrayList<>();
         buttonAdd.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 addArtist();
             }
         });

         listViewArtist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 Artist artist = artistList.get(i);

                 Intent intent = new Intent(getApplicationContext(), AddTrackActivity.class);
                 intent.putExtra(ARTIST_ID, artist.getArtistId());
                 intent.putExtra(ARTIST_NAME, artist.getArtistName());

                 startActivity(intent);
             }
         });

         listViewArtist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
             @Override
             public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                 Artist artist = artistList.get(i);
                 showUpdateDialog(artist.getArtistId(), artist.getArtistName());
                 return false;
             }
         });
     }

     @Override
     protected void onStart() {
         super.onStart();

         databaseArtist.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 artistList.clear();
                 for (DataSnapshot artistSanpshot : dataSnapshot.getChildren()) {
                     Artist artist = artistSanpshot.getValue(Artist.class);
                     artistList.add(artist);
                 }
                 ArtistList adapter = new ArtistList(MainActivity.this, artistList);
                 listViewArtist.setAdapter(adapter);
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });
     }

     private void showUpdateDialog(final String artistId, String artistName) {
         AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
         LayoutInflater inflater = getLayoutInflater();

         final View dialogView = inflater.inflate(R.layout.update_dialog, null);
         dialogBuilder.setView(dialogView);

         final EditText editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
         final Button buttonUpdate = (Button) dialogView.findViewById(R.id.bottonUpdate);
         final Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinnergen);
         final Button buttonDelete = (Button) dialogView.findViewById(R.id.buttonDelete);

         dialogBuilder.setTitle("Update Artist " + artistId);

         final AlertDialog alertDialog = dialogBuilder.create();
         alertDialog.show();

         buttonUpdate.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 String name = editTextName.getText().toString().trim();
                 String genre = spinner.getSelectedItem().toString();

                 if (TextUtils.isEmpty(name)) {
                     editTextName.setError("Perlu Nama");
                     return;
                 }

                 updateArtist(artistId, name, genre);
                 alertDialog.dismiss();
             }
         });

         buttonDelete.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 deleteArtist(artistId);
             }
         });
     }

     private void deleteArtist(String artistId){
         DatabaseReference drArtist = FirebaseDatabase.getInstance().getReference("artist").child(artistId);
         DatabaseReference drTrack = FirebaseDatabase.getInstance().getReference("artist").child(artistId);

         drArtist.removeValue();
         drTrack.removeValue();

         Toast.makeText(this, "Artist Dihapus", Toast.LENGTH_LONG).show();
     }

     private boolean updateArtist(String id, String name, String genre) {
         DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("artist").child(id);
         Artist artist = new Artist(id, name, genre);
         databaseReference.setValue(artist);

         Toast.makeText(this, "Update Artist Berhasil ", Toast.LENGTH_LONG).show();
         return true;
     }

     private void addArtist(){
         String name = editTextName.getText().toString().trim();
         String genre = spinnerGenres.getSelectedItem().toString();

         if (!TextUtils.isEmpty(name)){

             String id = databaseArtist.push().getKey();

             Artist artist = new Artist(id, name , genre);

             databaseArtist.child(id).setValue(artist);

             Toast.makeText(this, "Tambah Artist", Toast.LENGTH_LONG);

         }else{
             Toast.makeText(this, "Masukan Nama Anda", Toast.LENGTH_LONG).show();
         }
     }
 }