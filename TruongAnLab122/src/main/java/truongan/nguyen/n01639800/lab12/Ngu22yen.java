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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.snackbar.Snackbar;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Ngu22yen extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView textView;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String CHANNEL_ID = "location_channel";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ngu22yen_fragment, container, false);
        textView = view.findViewById(R.id.nameTextView);

        textView.setText("Truong An Nguyen - N01639800");
        textView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        textView.setTextSize(16);
        textView.setTypeface(textView.getTypeface(), android.graphics.Typeface.BOLD_ITALIC);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        createNotificationChannel();

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);

        LatLng humber = new LatLng(43.7275, -79.6077);
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(humber)
                .title("Humber Polytechnic")
                .snippet("Etobicoke - Truong An"));

        marker.showInfoWindow();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(humber, 15));

        enableLocation();

        mMap.setOnMapClickListener(latLng -> {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        latLng.latitude, latLng.longitude, 1);
                if (!addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String addressText = address.getAddressLine(0);

                    mMap.clear();
                    Marker newMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Selected Location")
                            .snippet(addressText));
                    if (newMarker != null) newMarker.showInfoWindow();

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                    textView.setText(addressText);

                    Snackbar snackbar = Snackbar.make(requireView(), addressText, Snackbar.LENGTH_INDEFINITE)
                            .setAction("Dismiss", v -> {});
                    snackbar.show();

                    sendNotification(addressText);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void enableLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocation();
            }
        }
    }

    private void createNotificationChannel() {
        CharSequence name = "Location Updates";
        String description = "Channel for map address notifications";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String address) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.location_icon)
                .setContentTitle("Selected Location")
                .setContentText(address)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{100, 200, 300})
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        notificationManager.notify(1, builder.build());
    }
}
