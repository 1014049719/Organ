/**
 * Copyright 2014 Joan Zapata
 *
 * This file is part of Android-pdfview.
 *
 * Android-pdfview is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Android-pdfview is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Android-pdfview.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.joanzapata.pdfview;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import org.vudroid.core.DecodeService;
import org.vudroid.core.DecodeServiceBase;
import org.vudroid.pdfdroid.codec.PdfContext;

class DecodingAsyncTask extends AsyncTask<Void, Void, Void> {

    /** The decode service used for decoding the PDF */
    private DecodeService decodeService;

    private boolean cancelled;

    private Uri uri;

    private PDFView pdfView;

    private Context context;

    private boolean isError;

    private OnDecodingErrorListener onDecodingErrorListener;


    public DecodingAsyncTask(Uri uri, PDFView pdfView) {
        this.cancelled = false;
        this.pdfView = pdfView;
        this.uri = uri;
        context = pdfView.getContext();
    }

    @Override
    protected Void doInBackground(Void... params) {
        decodeService = new DecodeServiceBase(new PdfContext(context));
        decodeService.setContentResolver(context.getContentResolver());
        try{
            decodeService.open(uri);
        }catch (Exception e){
            cancelled = true;
            isError = true;
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(Void result) {
        if (!cancelled) {
            pdfView.loadComplete(decodeService);
        }else {
            if (isError){
                if (onDecodingErrorListener != null){
                    onDecodingErrorListener.OnDecodingError();
                }
            }
        }
    }

    protected void onCancelled() {
        cancelled = true;
    }

    public interface OnDecodingErrorListener{
        void OnDecodingError();
    }

    public OnDecodingErrorListener getOnDecodingErrorListener() {
        return onDecodingErrorListener;
    }

    public void setOnDecodingErrorListener(OnDecodingErrorListener onDecodingErrorListener) {
        this.onDecodingErrorListener = onDecodingErrorListener;
    }
}
