package jiho.whereru.org.ignitednewapplication.Pager;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import jiho.whereru.org.ignitednewapplication.R;

public class Map_Fragment extends Fragment implements OnMapReadyCallback {

    //기호상수 선언
    private static final String CURRENT_LATLNG = "CURRENT_LATLNG";
    private static final String ALARM_LATLNG = "Latlng";

    //etc
    PagerAdapter adapter;
    ViewPager pager;
    Button btn_checkLocation;

    //GoogleMap
    private static final String TAG = "GoogleMap";
    private GoogleMap map;
    SupportMapFragment mapFragment;

    //Marker
    MarkerOptions MymarkerOptions;
    MarkerOptions alarmMarkerOptions;
    private SensorManager mSensorManager;
    private int circleRange = 50;

    //ProximityAlert
    private LocationManager locationManager;
    private PharmacyIntentReceiver mapReceiver;
    ArrayList PendingIntentList;
    String intentKey = "pharmacyProximity";

    //Firebase
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference myRef = database.getReference(user.getUid());

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated( savedInstanceState );

        //근접경보
        locationManager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );
        PendingIntentList = new ArrayList();
        mapReceiver = new PharmacyIntentReceiver( intentKey );
        getActivity().registerReceiver( mapReceiver, mapReceiver.getFilter() );

        FragmentManager fm = getChildFragmentManager();
        mapFragment = (SupportMapFragment)fm.findFragmentById( R.id.map );
        if(mapFragment == null){
            mapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace( R.id.map,mapFragment ).commit();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.map__fragment, container, false);
        btn_checkLocation = layout.findViewById( R.id.btn_checkLocation );
        btn_checkLocation.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestMyLocation();
            }
        } );
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapFragment.getMapAsync( this );
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d( TAG, "GoogleMap is ready." );

        map = googleMap;
        if (ActivityCompat.checkSelfPermission( getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled( true );
        SharedPreferences latitudePref = getActivity().getSharedPreferences( "latitude",Context.MODE_PRIVATE );
        SharedPreferences longtitudePref = getActivity().getSharedPreferences( "longtitude",Context.MODE_PRIVATE );
        Float nLatitude = ( latitudePref.getFloat( "latitude",0 ) );
        Float nLongtitude = ( longtitudePref.getFloat( "longtitude",0 ) );
        //Toast.makeText( getContext(),nLatitude+"+"+nLongtitude,Toast.LENGTH_LONG ).show();
        MymarkerOptions = new MarkerOptions();
        MymarkerOptions.position( new LatLng( nLatitude,nLongtitude ) );
        LatLng markerLatlng = MymarkerOptions.getPosition();
        MymarkerOptions.title( "● 가야할 장소\n" );
        MymarkerOptions.snippet( "● GPS로 확인한 위치" );
        MymarkerOptions.icon( BitmapDescriptorFactory.fromResource( R.drawable.mylocation ) );
        map.addMarker( MymarkerOptions );
        register( markerLatlng.latitude, markerLatlng.longitude,circleRange,-1 );
        //맵 클릭시 발동하는 리스너 인데... 솔직히 지금은 딱히 필요없음
        map.setOnMapClickListener( new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //MarkerOptions options = new MarkerOptions();
                //options.position( latLng );
                //Toast.makeText(MainActivity.this,nlatlng.toString(), Toast.LENGTH_SHORT).show();
                //register( nlatlng.latitude, nlatlng.longitude, radious, -1 );
            }
        } );

        try {
            MapsInitializer.initialize( getContext() );
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 센서 관리자 객체 참조
        mSensorManager = (SensorManager) getActivity().getSystemService( Context.SENSOR_SERVICE );

    }
    private void requestMyLocation() {
        LocationManager manager =
                (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );

        try {
            long minTime = 10000;
            float minDistance = 0;
            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime,
                    minDistance,
                    new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            showCurrentLocation( location );
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {

                        }

                        @Override
                        public void onProviderEnabled(String provider) {

                        }

                        @Override
                        public void onProviderDisabled(String provider) {

                        }
                    }
            );
            manager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTime,
                    minDistance,
                    new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            showCurrentLocation( location );
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {

                        }

                        @Override
                        public void onProviderEnabled(String provider) {

                        }

                        @Override
                        public void onProviderDisabled(String provider) {

                        }
                    }
            );


        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    private void showCurrentLocation(Location location) {
        Intent proximityIntent = new Intent( intentKey );
        /*cLatlng = new LatLng( location.getLatitude(), location.getLongitude() );
        currentLatlng.setValue( cLatlng );
        map.animateCamera( CameraUpdateFactory.newLatLngZoom( cLatlng, 15 ) );
        showMyLocationMarker( location );*/
    }

    private void showMyLocationMarker(Location location) {
        //원 그려주는 메서드 이지만, 현재 알람에서 좌표값을 가져오는 기능을 구현 안했기에 현재는 미구현으로 남겨둠

        //추가 마커를 설정해야하는데, 현재 아무 계획없으니 계획 하는데로 수정하겠음
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onPause() {
        super.onPause();

        if (map != null) {
            if (ActivityCompat.checkSelfPermission( getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            map.setMyLocationEnabled( false );
        }
    }



    /**
     * 센서의 정보를 받기 위한 리스너 객체 생성
     */

    //Proximity 등록
    //데이터베이스 연동하여 아이디, 좌표값 받아옴
    @SuppressLint("MissingPermission")
    private void register(double latitude, double longitude, float radius, long expiration){
        Intent proximityIntent = new Intent(intentKey);
        int id=0;

        proximityIntent.putExtra("id값",id);
        proximityIntent.putExtra("latitude", latitude);
        proximityIntent.putExtra("longitude", longitude);
        proximityIntent.putExtra( "radius",radius );
        proximityIntent.putExtra( "expiration",expiration );
        //펜딩 인텐트 객체를 생성(참조)
        PendingIntent intent = PendingIntent.getBroadcast(getContext(), id, proximityIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        locationManager.addProximityAlert(latitude, longitude, radius, expiration, intent);
        PendingIntentList.add(intent);
    }
    //등록해제
    private void unregister(){
        if(PendingIntentList !=null){
            for(int i=0; i<PendingIntentList.size(); i++){
                PendingIntent curIntent =(PendingIntent)PendingIntentList.get(i);
                locationManager.removeProximityAlert(curIntent);
                PendingIntentList.remove(i);
            }
            SharedPreferences latitudePref = getActivity().getSharedPreferences( "latitude",Context.MODE_PRIVATE );
            SharedPreferences longtitudePref = getActivity().getSharedPreferences( "longtitude",Context.MODE_PRIVATE );
            SharedPreferences.Editor latitudeEditor = latitudePref.edit();
            SharedPreferences.Editor longtitudeEditor = longtitudePref.edit();
            latitudeEditor.remove( "latitude" );
            longtitudeEditor.remove( "longtitude" );
            map.clear();

        }

        if(mapReceiver !=null){
            getActivity().unregisterReceiver(mapReceiver);
            mapReceiver = null;
        }

    }

    //근접경보 리시버
    private class PharmacyIntentReceiver extends BroadcastReceiver {

        private String mExpectedAction;

        public PharmacyIntentReceiver(String expectedAction){
            mExpectedAction = expectedAction;
        }

        public IntentFilter getFilter(){
            IntentFilter filter = new IntentFilter(mExpectedAction);
            return filter;
        }

        //메시지를 받았을 때 호출되는 메소드
        public void onReceive(Context context, Intent intent){
            boolean isEntering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, true);
            if(isEntering){
                unregister();
                Toast.makeText(context,isEntering ? "목적지에 도착하였습니다": "아직 도착하지 않았습니다.",Toast.LENGTH_LONG).show();
            }
        }
    }
}
