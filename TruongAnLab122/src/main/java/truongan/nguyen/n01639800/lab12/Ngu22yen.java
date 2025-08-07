package truongan.nguyen.n01639800.lab12;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Typeface;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Ngu22yen extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView infoTextView;
    private static final String CHANNEL_ID = "location_channel";

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Snackbar.make(requireView(), "Location permission denied.", Snackbar.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ngu22yen_fragment, container, false);
        infoTextView = view.findViewById(R.id.infoTextView);
        infoTextView.setText("Truong An Nguyen - N01639800");
        infoTextView.setTypeface(null, Typeface.BOLD_ITALIC);
        infoTextView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark));

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        createNotificationChannel();

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.mMap = googleMap;

        LatLng humber = new LatLng(43.7305, -79.6086);
        MarkerOptions marker = new MarkerOptions()
                .position(humber)
                .title("Humber Polytechnic")
                .snippet("Etobicoke - Truong An");

        googleMap.addMarker(marker);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(humber, 15f));
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(@NonNull Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(@NonNull Marker marker) {
                return null;
            }
        });

        googleMap.setOnMapClickListener(this::handleMapClick);

        requestLocationPermission();
    }

    private void handleMapClick(LatLng latLng) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressStr = address.getAddressLine(0);

                mMap.clear();
                mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title("Selected Location")
                                .snippet(addressStr)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                        .showInfoWindow();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));

                infoTextView.setText(addressStr);

                Snackbar.make(requireView(), addressStr, Snackbar.LENGTH_INDEFINITE)
                        .setAction("Dismiss", v -> {
                        })
                        .show();

                sendNotification(addressStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void sendNotification(String address) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentTitle("Map Location Clicked")
                .setContentText(address)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{0, 250, 250, 250})
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        notificationManager.notify(101, builder.build());
    }

    private void createNotificationChannel() {
        CharSequence name = "Location Channel";
        String description = "Shows location click notifications";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
