package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener, PermissionsListener {

    private MapView mView;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location myCurrentLocation;

    private ArrayList<Landmark> myLandmarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this,getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        //loadData();
        mView = findViewById(R.id.mapView);
        mView.onCreate(savedInstanceState);
        mView.getMapAsync(this);
    }

    private void loadData() {
        //Load các landmarks từ file vào arraylist & chuyển landmark -> markers, show markers lên bản đồ.
        //Neu arraylist ko rỗng, xóa hết item.
        if(myLandmarks.size()!=0)
            myLandmarks.clear();
        //load dữ liệu (title, description, ảnh, id..) từ file vào arraylist
        //.... làm sau!!
    }

    //Khoi tao buttons menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_bar,menu);
        return super.onCreateOptionsMenu(menu);
    }
    //Bat su kien cho menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.addPlaceMenu:
                actionAddPlace();
                //refreshMarkers();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshMarkers() {
        map.clear();
        loadData();
        for(int i =0; i<myLandmarks.size(); i++)
            displayLandMark(i);
    }

    private void displayLandMark(int i) {
        // số thứ tự của landmark cần chuyển thành marker và hiển thị lên map.
        Landmark landmarkI = myLandmarks.get(i);
        Icon icon = null;
        if(landmarkI.getLogoID()!= 0) {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), landmarkI.getLogoID());
            bmp = Bitmap.createScaledBitmap(bmp, bmp.getWidth() / 4, bmp.getHeight() / 4, false);
            IconFactory iconFactory = IconFactory.getInstance(this);
            icon = iconFactory.fromBitmap(bmp);
        }
        map.addMarker(new MarkerOptions()
                .position(landmarkI.getLatlong())
                .title(landmarkI.getTitle())
                .snippet(landmarkI.getDescription())
                .icon(icon)
        );
    }

    private void actionAddPlace() {
        String saySomething = "Tạo activity khác, chứa 1 Textview, cho ng dùng nhập Title, 1 text View cho người dùng nhập Discription, 1 button load ảnh (Nếu ng dùng nhấn btn,thì xin quyền truy cập ảnh & load ảnh của ng dùng vào grid view), 1 GridView để chứa ảnh người dùng, 1 btn xác nhận, 1 btn cancel";
        Toast.makeText(getApplicationContext(),saySomething,Toast.LENGTH_SHORT).show();
        //Khi nhấn xác nhận or cancel, thì quay lại màn hình map, Nếu là xác nhận thì thêm marker lên bản đồ.
        // Hiện tại mình làm chỉ lấy tọa độ của ng dùng thôi --> Ko cho người dùng nhập tọa độ
        //Thêm landmark này vào file chứa landmark và update data lên server luôn.
        Intent intent = new Intent(MainActivity.this,ActivityAddALandMark.class);
        startActivityForResult(intent,1);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        enableLocation();
    }
    private void enableLocation(){
        if(PermissionsManager.areLocationPermissionsGranted(this)){
            initializeLocationEngine();
            initializeLocationLayer();
        } else{
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }
    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine(){
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();
        Location lastLocation = locationEngine.getLastLocation();
        if(lastLocation != null){
            myCurrentLocation = lastLocation;
            setCameraLocation(lastLocation);
        }else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer(){
        locationLayerPlugin = new LocationLayerPlugin(mView,map,locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
        locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
    }

    private void setCameraLocation(Location location){
        //make the camera follow the current location
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),10));
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location!= null){
            myCurrentLocation = location;
            setCameraLocation(location);
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //Show a Toask text why needed
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if(granted)
            enableLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState!= null)
            mView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mView.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                Landmark newLandmark = new Landmark();
                newLandmark.setLatlong(new LatLng(myCurrentLocation.getLatitude(),myCurrentLocation.getLongitude()));
                newLandmark.setTitle(data.getStringExtra("tit"));
                newLandmark.setDescription(data.getStringExtra("des"));
                String uri = data.getStringExtra("uri");
                Toast.makeText(getApplicationContext(),uri+"------",Toast.LENGTH_SHORT).show();
                Uri uriLink =Uri.parse(uri);
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uriLink);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    bitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth()/4,bitmap.getHeight()/4,false);
                    IconFactory iconFactory =IconFactory.getInstance(MainActivity.this);
                    Icon icon = iconFactory.fromBitmap(bitmap);
                    map.addMarker(new MarkerOptions()
                        .position(newLandmark.getLatlong())
                        .title(newLandmark.getTitle())
                        .snippet(newLandmark.getDescription())
                        .icon(icon));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
//                Bitmap bmp = BitmapFactory.decodeResource(getResources(),);
//                bmp = Bitmap.createScaledBitmap(bmp, bmp.getWidth() / 4, bmp.getHeight() / 4, false);
//
//                IconFactory iconFactory = IconFactory.getInstance(this);
//                Icon icon = iconFactory.fromBitmap(bmp);
//                }
//                map.addMarker(new MarkerOptions()
//                        .position(landmarkI.getLatlong())
//                        .title(landmarkI.getTitle())
//                        .snippet(landmarkI.getDescription())
//                        .icon(icon)
//                );
            }
        }
    }
}