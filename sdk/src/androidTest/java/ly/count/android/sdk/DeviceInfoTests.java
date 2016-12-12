/*
Copyright (c) 2012, 2013, 2014 Countly

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package ly.count.android.sdk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.telephony.TelephonyManager;
import android.test.AndroidTestCase;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class DeviceInfoTests extends AndroidTestCase {

    public void testGetOS() {
        IDeviceInfo info = new DeviceInfo();
        assertEquals("Android", info.getOS());
    }

    public void testGetOSVersion() {
        IDeviceInfo info = new DeviceInfo();
        assertEquals(android.os.Build.VERSION.RELEASE, info.getOSVersion());
    }

    public void testGetDevice() {
        IDeviceInfo info = new DeviceInfo();
        assertEquals(android.os.Build.MODEL, info.getDevice());
    }

    public void testGetResolution() {
        IDeviceInfo info = new DeviceInfo();
        final DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        final String expected = metrics.widthPixels + "x" + metrics.heightPixels;
        assertEquals(expected,  info.getResolution(getContext()));
    }

    public void testGetResolution_getWindowManagerReturnsNull() {
        IDeviceInfo info = new DeviceInfo();
        final Context mockContext = mock(Context.class);
        when(mockContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(null);
        assertEquals("", info.getResolution(mockContext));
    }

    public void testGetResolution_getDefaultDisplayReturnsNull() {
        IDeviceInfo info = new DeviceInfo();
        final WindowManager mockWindowMgr = mock(WindowManager.class);
        when(mockWindowMgr.getDefaultDisplay()).thenReturn(null);
        final Context mockContext = mock(Context.class);
        when(mockContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(mockWindowMgr);
        assertEquals("", info.getResolution(mockContext));
    }

    private Context mockContextForTestingDensity(final int density) {
        IDeviceInfo info = new DeviceInfo();
        final DisplayMetrics metrics = new DisplayMetrics();
        metrics.densityDpi = density;
        final Resources mockResources = mock(Resources.class);
        when(mockResources.getDisplayMetrics()).thenReturn(metrics);
        final Context mockContext = mock(Context.class);
        when(mockContext.getResources()).thenReturn(mockResources);
        return mockContext;
    }

    public void testGetDensity() {
        IDeviceInfo info = new DeviceInfo();
        Context mockContext = mockContextForTestingDensity(DisplayMetrics.DENSITY_LOW);
        assertEquals("LDPI", info.getDensity(mockContext));
        mockContext = mockContextForTestingDensity(DisplayMetrics.DENSITY_MEDIUM);
        assertEquals("MDPI", info.getDensity(mockContext));
        mockContext = mockContextForTestingDensity(DisplayMetrics.DENSITY_TV);
        assertEquals("TVDPI", info.getDensity(mockContext));
        mockContext = mockContextForTestingDensity(DisplayMetrics.DENSITY_HIGH);
        assertEquals("HDPI", info.getDensity(mockContext));
        mockContext = mockContextForTestingDensity(DisplayMetrics.DENSITY_XHIGH);
        assertEquals("XHDPI", info.getDensity(mockContext));
        mockContext = mockContextForTestingDensity(DisplayMetrics.DENSITY_XXHIGH);
        assertEquals("XXHDPI", info.getDensity(mockContext));
        mockContext = mockContextForTestingDensity(DisplayMetrics.DENSITY_XXXHIGH);
        assertEquals("XXXHDPI", info.getDensity(mockContext));
        mockContext = mockContextForTestingDensity(DisplayMetrics.DENSITY_400);
        assertEquals("XMHDPI", info.getDensity(mockContext));
        mockContext = mockContextForTestingDensity(0);
        assertEquals("", info.getDensity(mockContext));
    }

    public void testGetCarrier_nullTelephonyManager() {
        IDeviceInfo info = new DeviceInfo();
        final Context mockContext = mock(Context.class);
        when(mockContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(null);
        assertEquals("", info.getCarrier(mockContext));
    }

    public void testGetCarrier_nullNetOperator() {
        IDeviceInfo info = new DeviceInfo();
        final TelephonyManager mockTelephonyManager = mock(TelephonyManager.class);
        when(mockTelephonyManager.getNetworkOperatorName()).thenReturn(null);
        final Context mockContext = mock(Context.class);
        when(mockContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mockTelephonyManager);
        assertEquals("", info.getCarrier(mockContext));
    }

    public void testGetCarrier_emptyNetOperator() {
        IDeviceInfo info = new DeviceInfo();
        final TelephonyManager mockTelephonyManager = mock(TelephonyManager.class);
        when(mockTelephonyManager.getNetworkOperatorName()).thenReturn("");
        final Context mockContext = mock(Context.class);
        when(mockContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mockTelephonyManager);
        assertEquals("", info.getCarrier(mockContext));
    }

    public void testGetCarrier() {
        IDeviceInfo info = new DeviceInfo();
        final TelephonyManager mockTelephonyManager = mock(TelephonyManager.class);
        when(mockTelephonyManager.getNetworkOperatorName()).thenReturn("Verizon");
        final Context mockContext = mock(Context.class);
        when(mockContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mockTelephonyManager);
        assertEquals("Verizon", info.getCarrier(mockContext));
    }

    public void testGetLocale() {
        IDeviceInfo info = new DeviceInfo();
        final Locale defaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("ab", "CD"));
            assertEquals("ab_CD", info.getLocale());
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    public void testGetAppVersion() throws PackageManager.NameNotFoundException {
        IDeviceInfo info = new DeviceInfo();
        final PackageInfo pkgInfo = new PackageInfo();
        pkgInfo.versionName = "42.0";
        final String fakePkgName = "i.like.chicken";
        final PackageManager mockPkgMgr = mock(PackageManager.class);
        when(mockPkgMgr.getPackageInfo(fakePkgName, 0)).thenReturn(pkgInfo);
        final Context mockContext = mock(Context.class);
        when(mockContext.getPackageName()).thenReturn(fakePkgName);
        when(mockContext.getPackageManager()).thenReturn(mockPkgMgr);
        assertEquals("42.0", info.getAppVersion(mockContext));
    }

    public void testGetAppVersion_pkgManagerThrows() throws PackageManager.NameNotFoundException {
        IDeviceInfo info = new DeviceInfo();
        final String fakePkgName = "i.like.chicken";
        final PackageManager mockPkgMgr = mock(PackageManager.class);
        when(mockPkgMgr.getPackageInfo(fakePkgName, 0)).thenThrow(new PackageManager.NameNotFoundException());
        final Context mockContext = mock(Context.class);
        when(mockContext.getPackageName()).thenReturn(fakePkgName);
        when(mockContext.getPackageManager()).thenReturn(mockPkgMgr);
        assertEquals("1.0", info.getAppVersion(mockContext));
    }

    public void testGetMetrics() throws UnsupportedEncodingException, JSONException {
        IDeviceInfo info = new DeviceInfo();
        final JSONObject json = new JSONObject();
        json.put("_device", info.getDevice());
        json.put("_os", info.getOS());
        json.put("_os_version", info.getOSVersion());
        if (!"".equals(info.getCarrier(getContext()))) { // ensure tests pass on non-cellular devices
            json.put("_carrier", info.getCarrier(getContext()));
        }
        json.put("_resolution", info.getResolution(getContext()));
        json.put("_density", info.getDensity(getContext()));
        json.put("_locale", info.getLocale());
        json.put("_app_version", info.getAppVersion(getContext()));

        final String expected = URLEncoder.encode(json.toString(), "UTF-8");
        assertNotNull(expected);
        assertEquals(expected, info.getMetrics(getContext()));
    }

    public void testFillJSONIfValuesNotEmpty_noValues() {
        IDeviceInfo info = new DeviceInfo();
        final JSONObject mockJSON = mock(JSONObject.class);
        info.fillJSONIfValuesNotEmpty(mockJSON);
        verifyZeroInteractions(mockJSON);
    }

    public void testFillJSONIfValuesNotEmpty_oddNumberOfValues() {
        IDeviceInfo info = new DeviceInfo();
        final JSONObject mockJSON = mock(JSONObject.class);
        info.fillJSONIfValuesNotEmpty(mockJSON, "key1", "value1", "key2");
        verifyZeroInteractions(mockJSON);
    }

    public void testFillJSONIfValuesNotEmpty() throws JSONException {
        IDeviceInfo info = new DeviceInfo();
        final JSONObject json = new JSONObject();
        info.fillJSONIfValuesNotEmpty(json, "key1", "value1", "key2", null, "key3", "value3", "key4", "", "key5", "value5");
        assertEquals("value1", json.get("key1"));
        assertFalse(json.has("key2"));
        assertEquals("value3", json.get("key3"));
        assertFalse(json.has("key4"));
        assertEquals("value5", json.get("key5"));
    }
}
