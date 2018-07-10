package jiho.whereru.org.ignitednewapplication.Util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.ObservableSnapshotArray;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import jiho.whereru.org.ignitednewapplication.MainActivity;
import jiho.whereru.org.ignitednewapplication.R;

public class MyFirebaseRecyclerAdapter extends FirebaseRecyclerAdapter<AlarmItem,MyFirebaseRecyclerAdapter.AlarmViewHolder>{
    public static final String ALRAM_CHILD = "ALRAM";
    String AlarmDate,AlarmName,AlarmTitle,AlarmLatitude,AlarmLongtitude,AlarmTime,AlarmContext;
    ObservableSnapshotArray<AlarmItem> snapshotArray;
    FirebaseRecyclerOptions<AlarmItem> options;
    Context context;
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public MyFirebaseRecyclerAdapter(FirebaseRecyclerOptions<AlarmItem> options,Context context) {
        super( options );
        this.options = options;
        this.context = context;
        snapshotArray = options.getSnapshots();
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView TV_AlarmDate,TV_AlarmName,TV_AlarmTitle,TV_AlarmLatitude,TV_AlarmLongtitude,TV_AlarmTime,TV_AlarmContext;

        public AlarmViewHolder(View v) {
            super(v);
            TV_AlarmDate = itemView.findViewById(R.id.TV_AlarmDate);
            TV_AlarmName = itemView.findViewById( R.id.TV_AlarmName);
            TV_AlarmTitle = itemView.findViewById(R.id.TV_AlarmTitle);
            TV_AlarmLatitude = itemView.findViewById( R.id.TV_AlarmLatitude );
            TV_AlarmLongtitude = itemView.findViewById( R.id.TV_AlarmLongtitude );
            TV_AlarmTime = itemView.findViewById(R.id.TV_AlarmTime);
            TV_AlarmContext = itemView.findViewById(R.id.TV_AlarmContext);

        }
    }
    @Override
    protected void onBindViewHolder(final AlarmViewHolder holder, final int position, final AlarmItem model) {
        holder.TV_AlarmDate.setText(model.getDate());
        AlarmDate = holder.TV_AlarmDate.toString();
        holder.TV_AlarmName.setText(model.getName());
        AlarmName = holder.TV_AlarmName.toString();
        holder.TV_AlarmTitle.setText(model.getTitle());
        AlarmTitle = holder.TV_AlarmTitle.toString();
        holder.TV_AlarmLatitude.setText(model.getLatitude());
        AlarmLatitude = holder.TV_AlarmLatitude.toString();
        holder.TV_AlarmLongtitude.setText(model.getLongtitude());
        AlarmLongtitude = holder.TV_AlarmLongtitude.toString();
        holder.TV_AlarmTime.setText(model.getTime());
        AlarmTime = holder.TV_AlarmTime.toString();
        holder.TV_AlarmContext.setText(model.getContent());
        AlarmContext = holder.TV_AlarmContext.toString();

        holder.TV_AlarmTitle.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder( view.getContext() );
                builder.setPositiveButton( "지도에 표시하기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences latitudePref = context.getSharedPreferences( "latitude", Activity.MODE_PRIVATE );
                        SharedPreferences longtitudePref = context.getSharedPreferences( "longtitude",Activity.MODE_PRIVATE);
                        SharedPreferences.Editor latitudeEditor = latitudePref.edit();
                        SharedPreferences.Editor longtitudeEditor = longtitudePref.edit();
                        latitudeEditor.putFloat( "latitude",Float.parseFloat( String.valueOf( holder.TV_AlarmLatitude.getText() ) ) );
                        longtitudeEditor.putFloat( "longtitude",Float.parseFloat( String.valueOf( holder.TV_AlarmLongtitude.getText() ) ) );
                        latitudeEditor.commit(); longtitudeEditor.commit();
                    }
                });
                builder.setNeutralButton( "알람 삭제하기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(user.getUid());
                        myRef.addListenerForSingleValueEvent( new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.child( ALRAM_CHILD ).child( holder.TV_AlarmDate.getText().toString() ).exists()){
                                    myRef.child( ALRAM_CHILD ).child( holder.TV_AlarmDate.getText().toString() ).removeValue();
                                    Toast.makeText( context,"삭제되었습니다",Toast.LENGTH_LONG ).show();
                                }else Toast.makeText( context,"실패하였습니다",Toast.LENGTH_LONG ).show();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        } );
                    }
                });
                builder.setNegativeButton( "확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.setMessage( "옵션" );
                builder.show();
            }
        } );
    }
    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }
}
