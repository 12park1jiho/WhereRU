package jiho.whereru.org.ignitednewapplication.Pager.AlarmTab;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jiho.whereru.org.ignitednewapplication.AlarmInsertActivity;
import jiho.whereru.org.ignitednewapplication.R;
import jiho.whereru.org.ignitednewapplication.Util.AlarmItem;
import jiho.whereru.org.ignitednewapplication.Util.MyFirebaseRecyclerAdapter;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class YourTab_Fragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = YourTab_Fragment.class.getSimpleName();
    private static final int REQUEST_INVITE = 1000;

    public static final String ALRAM_CHILD = "ALRAM";
    public static final String USER_CODE = "USER_CODE";
    public static final String FAMILY_CODE="FamilyCode";

    private DatabaseReference fFirebaseDatabaseReference;
    private MyFirebaseRecyclerAdapter myFirebaseRecyclerAdapter;
    private RecyclerView alarmRecyclerView;

    //조건 상수 선언
    private static final int IMAGE_GALLERY_REQUEST = 1;
    private static final int IMAGE_CAMERA_REQUEST = 2;
    private static final int PLACE_PICKER_REQUEST = 3;

    // Firebase 인스턴스 변수
    private FirebaseRemoteConfig fFirebaseRemoteConfig;
    // Google
    private GoogleApiClient mGoogleApiClient;

    // 사용자 이름과 사진
    private String mUsername;
    private String alertName;
    private String alerttitle;
    private String alertDate;
    private String alertLatlng;
    private String alertContent;
    private String mPhotoUrl;
    private String uid;
    private String f_uid;
    private String temp;

    TextView tv_nameCode;

    SharedPreferences pref;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.your_tab__fragment,container,false);
        return layout;
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getContext(), "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tv_nameCode = (TextView)getActivity().findViewById( R.id.tv_nameCode );
        pref = getActivity().getSharedPreferences("f_uid",MODE_PRIVATE);
        uid = pref.getString( "f_uid", "null");
        alarmRecyclerView = getActivity().findViewById(R.id.Youralarm_recycler_view);
        // 쿼리 수행 위치
        fFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference(uid);
        Query query = fFirebaseDatabaseReference.child(ALRAM_CHILD);

        // 옵션
        FirebaseRecyclerOptions<AlarmItem> options =
                new FirebaseRecyclerOptions.Builder<AlarmItem>()
                        .setQuery(query, AlarmItem.class)
                        .build();

        // 어댑터
        myFirebaseRecyclerAdapter = new MyFirebaseRecyclerAdapter(options,getContext());

        // 리사이클러뷰에 레이아웃 매니저와 어댑터 설정
        alarmRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        alarmRecyclerView.setAdapter(myFirebaseRecyclerAdapter);

        // Firebase Remote Config 초기화
        fFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Firebase Remote Config 설정
        FirebaseRemoteConfigSettings firebaseRemoteConfigSettings =
                new FirebaseRemoteConfigSettings.Builder()
                        .setDeveloperModeEnabled(true)
                        .build();

        // 인터넷 연결이 안 되었을 때 기본 값 정의
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put("message_length", 10L);

        // 설정과 기본 값 설정
        fFirebaseRemoteConfig.setConfigSettings(firebaseRemoteConfigSettings);
        fFirebaseRemoteConfig.setDefaults(defaultConfigMap);

        // 원격 구성 가져오기
        fetchConfig();

        // 새로운 글이 추가되면 제일 하단으로 포지션 이동
        myFirebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = myFirebaseRecyclerAdapter.getItemCount();
                LinearLayoutManager layoutManager = (LinearLayoutManager) alarmRecyclerView.getLayoutManager();
                int lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    alarmRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        // 키보드 올라올 때 RecyclerView의 위치를 마지막 포지션으로 이동
        alarmRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            alarmRecyclerView.smoothScrollToPosition(myFirebaseRecyclerAdapter.getItemCount());
                        }
                    }, 100);
                }
            }
        });
        Button btn_Insert = getActivity().findViewById( R.id.btn_YourAlarm_Insert );
        btn_Insert.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( getActivity(),AlarmInsertActivity.class );
                startActivity( intent );
            }
        } );
    }

    @Override
    public void onStart() {
        super.onStart();
        // FirebaseRecyclerAdapter 실시간 쿼리 시작
        myFirebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        // FirebaseRecyclerAdapter 실시간 쿼리 중지
        myFirebaseRecyclerAdapter.stopListening();
    }

    // 원격 구성 가져오기
    public void fetchConfig() {
        long cacheExpiration = 3600; // 1시간
        // 개발자 모드라면 0초로 하기
        if (fFirebaseRemoteConfig.getInfo().getConfigSettings()
                .isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        fFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 원격 구성 가져오기 성공
                        fFirebaseRemoteConfig.activateFetched();
                        applyRetrievedLengthLimit();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 원격 구성 가져오기 실패
                        Log.w(TAG, "Error fetching config: " +
                                e.getMessage());
                        applyRetrievedLengthLimit();
                    }
                });
    }


    /**
     * 서버에서 가져 오거나 캐시된 값을 가져 옴
     */
    private void applyRetrievedLengthLimit() {
    }
}


