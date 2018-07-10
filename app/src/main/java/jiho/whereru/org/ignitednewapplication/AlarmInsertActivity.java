package jiho.whereru.org.ignitednewapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jiho.whereru.org.ignitednewapplication.Util.AlarmItem;
import jiho.whereru.org.ignitednewapplication.Util.MyFirebaseRecyclerAdapter;
import jiho.whereru.org.ignitednewapplication.Util.SetAlramCycleFragment;

public class AlarmInsertActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = AlarmInsertActivity.class.getSimpleName();

    private static final String LOG_CODE = "1";
    private static final String PLOG_CODE = "2";

    private static final int REQUEST_INVITE = 1000;

    public static final String USER_CODE = "USER_CODE";
    public static final String ALRAM_CHILD = "ALRAM";
    public static final String FAMILY_CODE="FamilyCode";
    private DatabaseReference mFirebaseDatabaseReference;

    private MyFirebaseRecyclerAdapter myFirebaseAdapter;

    //조건 상수 선언
    private static final int IMAGE_GALLERY_REQUEST = 1;
    private static final int IMAGE_CAMERA_REQUEST = 2;
    private static final int PLACE_PICKER_REQUEST = 3;

    // Firebase 인스턴스 변수
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;


    // 사용자 이름과 사진
    private String mUsername;
    private String uid;

    TextView insertName;
    EditText insertTitle;
    EditText insertContext;
    TextView insertDate;
    TextView insertTime;
    TextView insertLatitude;
    TextView insertLongtitude;
    CircleImageView insertImage;
    Button btn_insertLocation,btn_insertTime;
    LatLng latLng;


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText( this, "Google Play Services error.", Toast.LENGTH_SHORT ).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_alarm_insert );

        insertTime = findViewById( R.id.TV_insertTime );
        insertName = findViewById( R.id.TV_insertName );
        insertTitle = findViewById( R.id.ET_insertTitle );
        insertDate = findViewById( R.id.TV_insertDate );
        insertName.setText( mUsername );
        Calendar mCalendar = Calendar.getInstance();
        Date curDate = new Date();
        mCalendar.setTime(curDate);
        int year = mCalendar.get( Calendar.YEAR);
        int monthOfYear = mCalendar.get(Calendar.MONTH);
        int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
        int hourOfDay = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);
        int second = mCalendar.get(Calendar.SECOND);
        insertDate.setText(year+"년"+monthOfYear+"월"+dayOfMonth+"일"+hourOfDay+"시"+minute+"분"+second+"초");
        insertContext = findViewById( R.id.ET_insertContext );
        insertImage = findViewById( R.id.IV_insert );
        insertLatitude = findViewById( R.id.TV_insertLatitude );
        insertLongtitude = findViewById( R.id.TV_insertLongtitude );

        btn_insertLocation = findViewById( R.id.btn_searchLocation );
        btn_insertTime = findViewById( R.id.btn_SearchTime );
        btn_insertLocation.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLocation();
            }
        });
        btn_insertTime.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetAlramCycleFragment setAlramCycleFragment = new SetAlramCycleFragment();
                setAlramCycleFragment.show( getFragmentManager(), "TimePicker");
            }
        } );
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        uid = mFirebaseUser.getUid();
        // Firebase 리얼타임 데이터 베이스 초기화
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference(uid);
        mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(FAMILY_CODE).exists()) {
                    // 인증이 안 되었다면 인증 화면으로 이동
                    startActivity(new Intent(AlarmInsertActivity.this, OptionActivity.class));
                    finish();
                    return;
                } else if(dataSnapshot.child(FAMILY_CODE).exists()){
                    mUsername = mFirebaseUser.getDisplayName();
                    uid = dataSnapshot.child(FAMILY_CODE).getValue(String.class);
                    mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference(uid);
                }else startActivity(new Intent(AlarmInsertActivity.this, OptionActivity.class));
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference(uid);

        // 보내기 버튼
        findViewById( R.id.btn_insertAlarm ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmItem alarmItem = new AlarmItem(
                        insertDate.getText().toString(),
                        mUsername,
                        insertTitle.getText().toString(),
                        insertLatitude.getText().toString(),
                        insertLongtitude.getText().toString(),
                        insertTime.getText().toString(),
                        insertContext.getText().toString()
                        );
                mFirebaseDatabaseReference.child( ALRAM_CHILD )
                        .child( insertDate.getText().toString() )
                        .setValue( alarmItem );

                Intent intent = new Intent( AlarmInsertActivity.this, MainActivity.class );
                startActivity( intent );
            }
        } );
        findViewById( R.id.btn_insertBack ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( AlarmInsertActivity.this, MainActivity.class );
                startActivity( intent );
            }
        } );
        // Firebase Remote Config 초기화
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Firebase Remote Config 설정
        FirebaseRemoteConfigSettings firebaseRemoteConfigSettings =
                new FirebaseRemoteConfigSettings.Builder()
                        .setDeveloperModeEnabled( true )
                        .build();

        // 인터넷 연결이 안 되었을 때 기본 값 정의
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put( "message_length", 10L );

        // 설정과 기본 값 설정
        mFirebaseRemoteConfig.setConfigSettings( firebaseRemoteConfigSettings );
        mFirebaseRemoteConfig.setDefaults( defaultConfigMap );

        // 원격 구성 가져오기
        fetchConfig();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void sendLocation() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build( this ) , PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    // 원격 구성 가져오기
    public void fetchConfig() {
        long cacheExpiration = 3600; // 1시간
        // 개발자 모드라면 0초로 하기
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings()
                .isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch( cacheExpiration )
                .addOnSuccessListener( new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 원격 구성 가져오기 성공
                        mFirebaseRemoteConfig.activateFetched();
                        applyRetrievedLengthLimit();
                    }
                } )
                .addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 원격 구성 가져오기 실패
                        Log.w( TAG, "Error fetching config: " +
                                e.getMessage() );
                        applyRetrievedLengthLimit();
                    }
                } );
    }
    /**
     * 서버에서 가져 오거나 캐시된 값을 가져 옴
     */
    private void applyRetrievedLengthLimit() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        Log.d( TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode );
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace( this,data);
                String toastMsg = String.format( "Place: %s", place.getName() );
                Toast.makeText( this, toastMsg, Toast.LENGTH_LONG ).show();
                latLng = place.getLatLng();
                insertLatitude.setText( String.valueOf( latLng.latitude ) );
                insertLongtitude.setText( String.valueOf( latLng.longitude ) );
            }
            else {
                // 실패함
                Log.d( TAG, "Failed to send invitation." );
            }
        }
    }
}
