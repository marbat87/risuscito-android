package it.cammino.risuscito.music;

import android.os.AsyncTask;

public class PrepareMusicRetrieverTask extends AsyncTask<Void, Void, Void> {
    MusicRetriever mRetriever;
    MusicRetrieverPreparedListener mListener;
    public PrepareMusicRetrieverTask(MusicRetriever retriever,
                                     MusicRetrieverPreparedListener listener) {
        mRetriever = retriever;
        mListener = listener;
    }
    @Override
    protected Void doInBackground(Void... arg0) {
        mRetriever.prepare();
        return null;
    }
    @Override
    protected void onPostExecute(Void result) {
        mListener.onMusicRetrieverPrepared();
    }
    public interface MusicRetrieverPreparedListener {
        void onMusicRetrieverPrepared();
    }
}