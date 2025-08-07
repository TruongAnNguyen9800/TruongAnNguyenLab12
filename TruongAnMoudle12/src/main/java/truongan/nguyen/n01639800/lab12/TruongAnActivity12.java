//  Name: Truong An Nguyen, Student ID: N01639800, Section: 0CA

package truongan.nguyen.n01639800.lab12;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class TruongAnActivity12 extends AppCompatActivity {

    private ActivityResultLauncher<Intent> contactsLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {}
        );

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openContacts();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                });

        ArrayList<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new Truo11ngAn());
        fragmentList.add(new Ngu22yen());
        fragmentList.add(new N0163339800());
        fragmentList.add(new TN44());

        ViewPager2 viewPager = findViewById(R.id.TruviewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, fragmentList);
        viewPager.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = findViewById(R.id.TrutabLayout);
        String[] tabNames = new String[] {"Truong An", "Nguyen", "N01639800", "TN"};
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabNames[position])
        ).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_contacts) {
            openContacts();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            new Thread(() -> {
                int contactCount = getContactCount();
                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                getString(R.string.contacts_count, contactCount),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }).start();

            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            contactsLauncher.launch(intent);
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    private int getContactCount() {
        Cursor cursor = null;
        try {
            String[] projection = {ContactsContract.Contacts._ID};
            cursor = getContentResolver().query(
                    ContactsContract.Contacts.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);

            return cursor != null ? cursor.getCount() : 0;
        } catch (SecurityException e) {
            Toast.makeText(this, "Permission needed to read contacts", Toast.LENGTH_SHORT).show();
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
