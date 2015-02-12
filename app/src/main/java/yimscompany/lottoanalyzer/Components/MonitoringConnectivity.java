package yimscompany.lottoanalyzer.Components;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;

/**
 * monitoring connectivity
 * author: shyim
 */
public class MonitoringConnectivity {
    ConnectivityManager _cManager;
    NetworkInfo _activeNetwork;
    Context _ctx;


    public MonitoringConnectivity(Context c) {
        _cManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        _activeNetwork = _cManager.getActiveNetworkInfo();
        _ctx = c;
    }

    public boolean IsConnected() {
        if (_activeNetwork != null &&
                (GetType() == _cManager.TYPE_WIFI || GetType() == _cManager.TYPE_MOBILE) &&
                _activeNetwork.isConnected()) {
            try {
                Runtime runtime = Runtime.getRuntime();
                Process proc = runtime.exec("ping -c 1 google.com");
                proc.waitFor();
                int exitCode = proc.exitValue();
                if (exitCode == 0) {
                    return true;
                }
            } catch (IOException e) {
            } catch (InterruptedException e) {
            }
        }
        return false;
    }

    public int GetType() {
        return _activeNetwork.getType();
    }

    //TODO: implementing monitor for changing connectivity
}
