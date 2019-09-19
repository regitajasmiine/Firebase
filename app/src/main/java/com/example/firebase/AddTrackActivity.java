package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddTrackActivity extends AppCompatActivity {

    TextView textViewArtistName;
    EditText editTextTrackName;
    SeekBar seekBarRating;
    ListView listViewTrack;
    Button buttonAddTrack;

    DatabaseReference databaseTrack;

    List<Track> tracks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_track);

        textViewArtistName = (TextView)findViewById(R.id.textViewArtistName);
        editTextTrackName = (EditText)findViewById(R.id.editTextTrackName);
        seekBarRating = (SeekBar)findViewById(R.id.seekBarRating);
        listViewTrack = (ListView)findViewById(R.id.listViewTrack);
        buttonAddTrack = (Button)findViewById(R.id.buttonAddTrack);

        Intent intent = getIntent();

        tracks = new ArrayList<>();
        String id = intent.getStringExtra(MainActivity.ARTIST_ID);
        String name = intent.getStringExtra(MainActivity.ARTIST_NAME);

        textViewArtistName.setText(name);
        databaseTrack = FirebaseDatabase.getInstance().getReference("track").child(id);

        buttonAddTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savetTrack();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        databaseTrack.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tracks.clear();

                for (DataSnapshot trackSnapshot : dataSnapshot.getChildren()){
                    Track track = trackSnapshot.getValue(Track.class);
                    tracks.add(track);
                }
                TrackList trackListAdapter = new TrackList(AddTrackActivity.this, tracks);
                listViewTrack.setAdapter(trackListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private  void savetTrack(){
        String trackName = editTextTrackName.getText().toString().trim();
        int rating = seekBarRating.getProgress();
        if (!TextUtils.isEmpty(trackName)){
            String id = databaseTrack.push().getKey();

            Track track = new Track(id, trackName, rating);
            databaseTrack.child(id).setValue(track);
            Toast.makeText(this, "Track Berhasil Disimpan", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "Track Gagal Disimpan", Toast.LENGTH_LONG).show();
        }
    }
}