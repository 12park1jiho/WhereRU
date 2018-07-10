package jiho.whereru.org.ignitednewapplication.Pager.AlarmTab;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.HashMap;
import java.util.Map;

import jiho.whereru.org.ignitednewapplication.MainActivity;
import jiho.whereru.org.ignitednewapplication.OptionActivity;
import jiho.whereru.org.ignitednewapplication.R;
import jiho.whereru.org.ignitednewapplication.Util.AlarmItem;
import jiho.whereru.org.ignitednewapplication.Util.MyFirebaseRecyclerAdapter;

import static android.content.Context.NOTIFICATION_SERVICE;

public class MyTab_Fragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = MyTab_Fragment.class.getSimpleName();

    public static final String ALRAM_CHILD = "ALRAM";
    public static final String USER_CODE = "USER_CODE";
    public static final String FAMILY_CODE="FamilyCode";

    private DatabaseReference mFirebaseDatabaseReference;
    private RecyclerView alramRecyclerView;
    private  MyFirebaseRecyclerAdapter myFirebaseRecyclerAdapter;

    // Firebase 인스턴스 변수
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private String uid;
    GestureDetector gestureDetector;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.my_tab__fragment,container,false);
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

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        uid = mFirebaseUser.getUid();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference(uid);
        mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(FAMILY_CODE).exists()) {
                    // 인증이 안 되었다면 인증 화면으로 이동
                    startActivity(new Intent(getActivity(), OptionActivity.class));
                    getActivity().finish();
                    return;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        alramRecyclerView = getActivity().findViewById(R.id.Myalarm_recycler_view);
        // 쿼리 수행 위치
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference(uid);
        Query query = mFirebaseDatabaseReference.child(ALRAM_CHILD);

        // 옵션
        FirebaseRecyclerOptions<AlarmItem> options =
                new FirebaseRecyclerOptions.Builder<AlarmItem>()
                        .setQuery(query, AlarmItem.class)
                        .build();

        // 어댑터
        myFirebaseRecyclerAdapter = new MyFirebaseRecyclerAdapter(options,getContext());
        // 리사이클러뷰에 레이아웃 매니저와 어댑터 설정
        alramRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        alramRecyclerView.setAdapter(myFirebaseRecyclerAdapter);

        // Firebase Remote Config 초기화
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Firebase Remote Config 설정
        FirebaseRemoteConfigSettings firebaseRemoteConfigSettings =
                new FirebaseRemoteConfigSettings.Builder()
                        .setDeveloperModeEnabled(true)
                        .build();

        // 인터넷 연결이 안 되었을 때 기본 값 정의
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put("message_length", 10L);

        // 설정과 기본 값 설정
        mFirebaseRemoteConfig.setConfigSettings(firebaseRemoteConfigSettings);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);

        // 원격 구성 가져오기
        fetchConfig();

        // 새로운 글이 추가되면 제일 하단으로 포지션 이동
        myFirebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {

                /*Intent intent = new Intent(getContext(), MainActivity.class);
                PendingIntent pIntent = PendingIntent.getActivity(getContext(), (int) System.currentTimeMillis(), intent, 0);
                Notification n  = new Notification.Builder(getContext())
                        .setContentTitle("메시지가 도착했습니다")
                        .setSmallIcon(R.drawable.ic_account_circle_black_24dp)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true)
                        .addAction(R.drawable.ic_account_circle_black_24dp, "button1", pIntent).build();
                NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(0, n);*/
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = myFirebaseRecyclerAdapter.getItemCount();
                LinearLayoutManager layoutManager = (LinearLayoutManager) alramRecyclerView.getLayoutManager();
                int lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    alramRecyclerView.scrollToPosition(positionStart);
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    PendingIntent pIntent = PendingIntent.getActivity(getContext(), (int) System.currentTimeMillis(), intent, 0);
                    Notification n  = new Notification.Builder(getContext())
                            .setContentTitle("메시지가 도착했습니다")
                            .setSmallIcon(R.drawable.child)
                            .setContentIntent(pIntent)
                            .setAutoCancel(true)
                            .addAction(R.drawable.child, "button1", pIntent).build();
                    NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(0, n);
                }
            }
        });

        // 키보드 올라올 때 RecyclerView의 위치를 마지막 포지션으로 이동
        alramRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            alramRecyclerView.smoothScrollToPosition(myFirebaseRecyclerAdapter.getItemCount());
                        }
                    }, 100);
                }
            }
        });

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
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings()
                .isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 원격 구성 가져오기 성공
                        mFirebaseRemoteConfig.activateFetched();
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



