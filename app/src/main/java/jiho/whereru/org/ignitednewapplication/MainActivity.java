package jiho.whereru.org.ignitednewapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import jiho.whereru.org.ignitednewapplication.Util.MainPagerAdapter;

public class MainActivity extends AppCompatActivity {
    //기호상수 선언
    private static final String CURRENT_LATLNG = "CURRENT_LATLNG";
    private static final String ALARM_LATLNG = "ALARM_LATLNG";
    public static final String FAMILY_CODE="FamilyCode";

    MainPagerAdapter adapter;
    ViewPager pager;
    Button btnAlarm, btnSOS, btnMap;

    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference myRef,currentLatlng,alarmLatlng;
    String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Firebase
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        myUid = user.getUid();
        myRef = database.getReference(myUid);
        currentLatlng = myRef.child( CURRENT_LATLNG );
        alarmLatlng = myRef.child( ALARM_LATLNG );

        myRef = database.getReference(myUid);
        currentLatlng = myRef.child( CURRENT_LATLNG );
        alarmLatlng = myRef.child( ALARM_LATLNG );
        myRef.addValueEventListener( new ValueEventListener() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SharedPreferences pref=getSharedPreferences("f_uid", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("f_uid",dataSnapshot.child( FAMILY_CODE ).getValue(String.class));
                editor.commit();

                /*if(dataSnapshot.child( "hour" ).exists()&&dataSnapshot.child( "minute" ).exists()) {
                    Calendar calendar = Calendar.getInstance();
                    Long cHour = Long.valueOf( calendar.get( Calendar.HOUR) )*60*60*1000;
                    Long cMinute = Long.valueOf( calendar.get( Calendar.MINUTE) )*60*1000;
                    AlarmManager manager = (AlarmManager) getSystemService( Context.ALARM_SERVICE );
                    Intent intent = new Intent( MainActivity.this, AlarmBroadcast.class );
                    PendingIntent broadcast = PendingIntent.getBroadcast( MainActivity.this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT );
                    Long alarmHour = ( Long.parseLong( dataSnapshot.child( "hour" ).getValue(String.class) )) * 60 * 60 * 1000;
                    Long alarmMinute = ( Long.parseLong( dataSnapshot.child( "minute" ).getValue(String.class) )) * 60 * 1000;
                    manager.setRepeating( AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),alarmHour+alarmMinute,broadcast );
                }*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        adapter = new MainPagerAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        //액션바 설정하기//
        //액션바 타이틀 변경하기
        getSupportActionBar().setTitle("어디로 가야하오");
        //액션바 배경색 변경
        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFFffe301));

        btnAlarm = (Button)findViewById(R.id.btnAlarm);
        btnSOS = (Button)findViewById(R.id.btn_SOS);
        btnMap = (Button)findViewById( R.id.btnMap );
        btnSOS.setOnClickListener( onClickListener );
        btnMap.setOnClickListener(onClickListener);
        btnAlarm.setOnClickListener(onClickListener);
    }


    View.OnClickListener onClickListener;

    {
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnAlarm:
                        pager.setCurrentItem(0);
                        break;
                    case R.id.btn_SOS:
                        pager.setCurrentItem(1);
                        break;
                    case R.id.btnMap:
                        pager.setCurrentItem(2);
                        break;


                }
            }
        };
    }

    //액션버튼 메뉴 액션바에 집어 넣기
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    //액션버튼을 클릭했을때의 동작
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        //or switch문을 이용하면 될듯 하다.
        switch (id){
            case R.id.action_button1:
                Intent i = new Intent( this,IntroduceActivity.class );
                startActivity( i );
                return true;
            case R.id.action_button2:
                Intent intent = new Intent(this, OptionActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}


