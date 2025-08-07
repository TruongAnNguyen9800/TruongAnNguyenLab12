package truongan.nguyen.n01639800.lab12;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
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

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.TruviewPager);
        ArrayList<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new Truo11ngAn());
        fragmentList.add(new Ngu22yen());
        fragmentList.add(new N0163339800());
        fragmentList.add(new TN44());

        ViewPagerAdapter adapter = new ViewPagerAdapter(this, fragmentList);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.TrutabLayout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(getTabName(position))).attach();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted && viewPager.getCurrentItem() == 3) {
                        showFirstContact();
                    } else if (!isGranted) {
                        Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getTabName(int position) {
        switch (position) {
            case 0: return getString(R.string.frag_1);
            case 1: return getString(R.string.frag_2);
            case 2: return getString(R.string.frag_3);
            case 3: return getString(R.string.frag_4);
            default: return "";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_contacts) {
            int currentTab = viewPager.getCurrentItem();
            if (currentTab < 3) {
                viewPager.setCurrentItem(3);
            } else {
                if (hasContactsPermission()) {
                    showFirstContact();
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean hasContactsPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    private void showFirstContact() {
        new Thread(() -> {
            String contactInfo = getFirstContactInfo();
            runOnUiThread(() -> {
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.first_contact)
                        .setMessage(contactInfo)
                        .setPositiveButton(R.string.ok, null)
                        .create();
                dialog.show();
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(R.color.green);
                }
            });
        }).start();
    }

    private String getFirstContactInfo() {
        Cursor cursor = null;
        try {
            String[] projection = {
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };

            cursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    null,
                    null,
                    ContactsContract.Contacts.DISPLAY_NAME + " ASC LIMIT 1");

            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                return String.format("%s: %s\n%s: %s",
                        getString(R.string.name), name == null ? getString(R.string.unknown_name) : name,
                        getString(R.string.phone), phone == null ? getString(R.string.unknown_number) : phone);
            }
            return getString(R.string.no_contacts_found);
        } catch (SecurityException e) {
            return getString(R.string.permission_required);
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
