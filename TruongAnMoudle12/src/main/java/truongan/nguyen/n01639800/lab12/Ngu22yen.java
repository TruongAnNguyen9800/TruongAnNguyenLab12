package truongan.nguyen.n01639800.lab12;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Typeface;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
                    enableMyLocation();
                } else {
                    Snackbar.make(requireView(), getString(R.string.location_denied), Snackbar.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ngu22yen_fragment, container, false);
        infoTextView = view.findViewById(R.id.infoTextView);
        infoTextView.setText(getString(R.string.full_name_and_number));
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
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(humber)
                .title(getString(R.string.school_name))
                .snippet(getString(R.string.school_name_name)));

        if (marker != null) {
            marker.showInfoWindow();
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(humber, 15f));

        mMap.setOnMapClickListener(this::handleMapClick);

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
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(getString(R.string.full_name))
                        .snippet(addressStr)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                if (marker != null) marker.showInfoWindow();

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));

                infoTextView.setText(addressStr);

                Snackbar.make(requireView(), addressStr, Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.dismiss), v -> {})
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
            enableMyLocation();
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void sendNotification(String address) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentTitle(getString(R.string.address_change))
                .setContentText(address)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 500, 500})
                .setSound(MediaStore.Audio.Media.INTERNAL_CONTENT_URI);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            notificationManager.notify(101, builder.build());
        }
    }

    private void createNotificationChannel() {
        CharSequence name = "Location Channel";
        String description = "Notifications for clicked map locations";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        channel.enableVibration(true);
        channel.enableLights(true);

        NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}
