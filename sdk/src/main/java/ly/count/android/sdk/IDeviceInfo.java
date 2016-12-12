package ly.count.android.sdk;

import android.content.Context;

import org.json.JSONObject;

public interface IDeviceInfo {
    String getOS();
    String getOSVersion();
    String getDevice();
    String getResolution(final Context context);
    String getDensity(final Context context);
    String getCarrier(final Context context);
    String getLocale();
    String getAppVersion(final Context context);
    String getStore(final Context context);
    String getMetrics(final Context context);
    void fillJSONIfValuesNotEmpty(final JSONObject json, final String ... objects);
}
