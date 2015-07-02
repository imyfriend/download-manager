package com.novoda.downloadmanager.demo.extended;

import android.database.Cursor;
import android.os.AsyncTask;

import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.lib.Query;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryForDownloadsAsyncTask extends AsyncTask<Query, Void, List<Download>> {

    private final DownloadManager downloadManager;
    private final WeakReference<Callback> weakCallback;

    static QueryForDownloadsAsyncTask newInstance(DownloadManager downloadManager, Callback callback) {
        return new QueryForDownloadsAsyncTask(downloadManager, new WeakReference<>(callback));
    }

    QueryForDownloadsAsyncTask(DownloadManager downloadManager, WeakReference<Callback> weakCallback) {
        this.downloadManager = downloadManager;
        this.weakCallback = weakCallback;
    }

    @Override
    protected List<Download> doInBackground(Query... params) {
        Cursor cursor = downloadManager.query(params[0]);
        List<Download> downloads = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE));
                String fileName = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_FILENAME));
                int downloadStatus = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                long id = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BATCH_ID));
                downloads.add(new Download(id, title, fileName, downloadStatus));
            }
        } finally {
            cursor.close();
        }
        return downloads;
    }

    @Override
    protected void onPostExecute(List<Download> downloads) {
        Map<Long, DownloadBatch> downloadBatchMap = new HashMap<>();
        for (Download download : downloads) {
            long batchId = download.getBatchId();
            DownloadBatch downloadBatch = getDownloadBatch(downloadBatchMap, batchId);
            downloadBatch.add(download);
            downloadBatchMap.put(batchId, downloadBatch);
        }

        Callback callback = weakCallback.get();
        if (callback == null) {
            return;
        }
        callback.onQueryResult(new ArrayList<DownloadBatch>(downloadBatchMap.values()));
    }

    private DownloadBatch getDownloadBatch(Map<Long, DownloadBatch> downloadBatchMap, long batchId) {
        if (downloadBatchMap.containsKey(batchId)) {
            return downloadBatchMap.get(batchId);
        }
        return new DownloadBatch(batchId);
    }

    interface Callback {
        void onQueryResult(List<DownloadBatch> downloads);
    }
}
