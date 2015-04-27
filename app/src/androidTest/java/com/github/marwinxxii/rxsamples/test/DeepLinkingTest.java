package com.github.marwinxxii.rxsamples.test;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.test.AndroidTestCase;

public class DeepLinkingTest extends AndroidTestCase {
    private static final String SCHEME = "sample", HOST = "com.github.marwinxxii.rxsamples";
    
    public void testEditTextResolved() {
        assertTrue(isActivityResolved("edittexts"));
    }

    public void testEmailResolved() {
        assertTrue(isActivityResolved("email"));
    }

    public void testIOCombineResolved() {
        assertTrue(isActivityResolved("iocombine"));
    }
    
    private boolean isActivityResolved(String path) {
        Uri uri = new Uri.Builder().scheme(SCHEME).encodedAuthority(HOST).appendPath(path).build();
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        return getContext().getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null;
    }
}
