/*
 * Copyright 2016 Alejandro Alcalde
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elbauldelprogramador.photogesture.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Alejandro Alcalde (elbauldelprogramador.com) on 2/8/16.
 */
// http://stackoverflow.com/questions/4157724/dynamically-add-pictures-to-gallery-widget
public class MediaScannerWrapper implements
        MediaScannerConnection.MediaScannerConnectionClient {
    private MediaScannerConnection mConnection;
    private String mPath;
    private String mMimeType;

    // filePath - where to scan;
    // mime type of media to scan i.e. "image/jpeg".
    // use "*/*" for any media
    public MediaScannerWrapper(Context ctx, String filePath, String mime) {
        mPath = filePath;
        mMimeType = mime;
        mConnection = new MediaScannerConnection(ctx, this);
    }

    // do the scanning
    public void scan() {
        mConnection.connect();
    }

    // start the scan when scanner is ready
    public void onMediaScannerConnected() {
        mConnection.scanFile(mPath, mMimeType);
        Log.w("MediaScannerWrapper", "media file scanned: " + mPath);
    }

    public void onScanCompleted(String path, Uri uri) {
        // when scan is completes, update media file tags
    }
}
