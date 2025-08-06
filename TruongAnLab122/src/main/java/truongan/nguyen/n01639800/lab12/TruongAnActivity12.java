//  Name: Truong An Nguyen, Student ID: N01639800, Section: 0CA

package truongan.nguyen.n01639800.lab12;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class TruongAnActivity12 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new TruongAnFragment());
        fragmentList.add(new NguyenFragment());
        fragmentList.add(new N01639800Fragment());
        fragmentList.add(new TNFragment());

        ViewPager2 viewPager = findViewById(R.id.TruviewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, fragmentList);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.TrutabLayout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    tab.setText(getString(R.string.tab) + (position + 1));
                }
        ).attach();
    }
}