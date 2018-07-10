package jiho.whereru.org.ignitednewapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OptionActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String FAMILY_CODE="FamilyCode";
    //View
    Button Btn_HomeLocation, Btn_Setting, Btn_SearchName;
    TextView Tv_address,tv_nameCode,myLatitude,myLongtitude;
    EditText et_searchName;

    //Firebase
    FirebaseDatabase database;
    DatabaseReference searchRef;
    DatabaseReference myRef;
    FirebaseAuth auth;
    FirebaseUser user;

    //etc
    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        //액션바 타이틀 변경하기
        getSupportActionBar().setTitle("환경설정");
        //액션바 배경색 변경
        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFFffe301));
        //홈버튼 표시
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myLatitude = (TextView)findViewById(R.id.tv_myLatitude);
        myLongtitude = (TextView)findViewById(R.id.tv_myLongtitude);
        Tv_address = (TextView)findViewById( R.id.tv_searchLocation );
        tv_nameCode = (TextView)findViewById(R.id.tv_nameCode);
        et_searchName = (EditText)findViewById( R.id.et_searchName );
        user = FirebaseAuth.getInstance().getCurrentUser();

        database = FirebaseDatabase.getInstance();
        searchRef = database.getReference("LIST");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uid = user.getUid();
        myRef = database.getReference(uid);

        Btn_HomeLocation = (Button)findViewById( R.id.btn_searchLocation );
        Btn_SearchName = (Button)findViewById(R.id.btn_searchName);
        Btn_Setting = (Button)findViewById( R.id.btn_saveSetting );
        Btn_HomeLocation.setOnClickListener( this );
        Btn_SearchName.setOnClickListener( this );
        Btn_Setting.setOnClickListener( this );

    }

    @Override
    public void onClick(View view) {
        if(view==Btn_HomeLocation){
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult( builder.build( this ), 101 );
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e){
                e.printStackTrace();
            }
        }else if(view==Btn_SearchName){
            searchRef.addValueEventListener( new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if((!dataSnapshot.child( et_searchName.getText().toString() ).exists())||et_searchName.getText().toString().equals( "" )){
                        Toast.makeText( OptionActivity.this,"찾을 수 없습니다" ,Toast.LENGTH_LONG).show();
                    }else {
                        tv_nameCode.setText( dataSnapshot.child( et_searchName.getText().toString() ).getValue(String.class) );
                        Toast.makeText( OptionActivity.this,et_searchName.getText().toString()+"님과 연동되었습니다",Toast.LENGTH_LONG ).show();

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            } );
        }else if(view==Btn_Setting){
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            String uid = user.getUid();
            String email = user.getEmail();
            String name = user.getDisplayName();
            searchRef.child(name).setValue(uid);
            myRef = database.getReference(uid);
            myRef.child("Latlng").child("latitude").setValue(Float.parseFloat((String) myLatitude.getText()));
            myRef.child("Latlng").child("longtitude").setValue(Float.parseFloat((String) myLongtitude.getText()));
            myRef.child("address").setValue( Tv_address.getText() );
            myRef.child("E_mail").setValue(email);
            myRef.child(FAMILY_CODE).setValue(tv_nameCode.getText());
            Intent intent = new Intent( OptionActivity.this,MainActivity.class );
            startActivity( intent );
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if(requestCode == 101){
            if(resultCode == RESULT_OK){
                Place place = PlacePicker.getPlace( this,data );
                Tv_address.setText(  place.getAddress().toString() );
                myLatitude.setText(String.valueOf(place.getLatLng().latitude));
                myLongtitude.setText(String.valueOf(place.getLatLng().longitude));
            }
        }
    }
    //옵션화면에서 제공하는 뒤로가기버튼을 눌렀을 시 메인화면으로 이동한다.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class); intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
}
