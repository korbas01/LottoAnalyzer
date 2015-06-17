package yimscompany.lottoanalyzer;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import yimscompany.lottoanalyzer.Fragments.LottoPreferenceFragment;
import yimscompany.lottoanalyzer.UIComponent.LottoUIHelper;


public class LottoPreferenceActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.action_settings));
        LottoUIHelper.ActionBarTitleCenter(this);

        LottoPreferenceFragment f = new LottoPreferenceFragment();

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new LottoPreferenceFragment())
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return LottoPreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_out_down, R.anim.pull_in_up);
    }


}
