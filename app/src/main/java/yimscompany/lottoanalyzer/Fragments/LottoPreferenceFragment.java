package yimscompany.lottoanalyzer.Fragments;


import android.os.Bundle;
import android.preference.PreferenceFragment;

import yimscompany.lottoanalyzer.R;


public class LottoPreferenceFragment extends PreferenceFragment {
    public LottoPreferenceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

    }

}
