package truongan.nguyen.n01639800.lab12;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;


public class N0163339800 extends Fragment {

    private WebView webView;
    private Spinner websiteSpinner;
    private AdView adView;
    private int adClickCount = 0;

    private final String[] websites = {
            "Select URL from list",
            "https://www.youtube.com/",
            "https://arcaea.lowiro.com/en",
            "https://www.leagueoflegends.com/en-us/"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.n0163339800_fragment, container, false);

        TextView title = view.findViewById(R.id.labTitle);
        websiteSpinner = view.findViewById(R.id.websiteSpinner);
        webView = view.findViewById(R.id.webView);
        adView = view.findViewById(R.id.adView);

        setupSpinner();
        setupWebView();
        setupAd();

        websiteSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    webView.loadUrl("file:///android_asset/welcome.html");
                } else {
                    webView.loadUrl(websites[position]);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        return view;
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                websites);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        websiteSpinner.setAdapter(adapter);
    }

    private void setupWebView() {
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/welcome.html");
    }

    private void setupAd() {
        MobileAds.initialize(requireContext(), initializationStatus -> {});
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                adClickCount++;
                Toast.makeText(requireContext(), "Truong An Nguyen: " + adClickCount, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroyView();
    }
}
