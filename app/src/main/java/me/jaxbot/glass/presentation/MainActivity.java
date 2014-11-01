package me.jaxbot.glass.presentation;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link Activity} showing a tuggable "Hello World!" card.
 * <p>
 * The main content view is composed of a one-card {@link CardScrollView} that provides tugging
 * feedback to the user when swipe gestures are detected.
 * If your Glassware intends to intercept swipe gestures, you should set the content view directly
 * and use a {@link com.google.android.glass.touchpad.GestureDetector}.
 * @see <a href="https://developers.google.com/glass/develop/gdk/touch">GDK Developer Guide</a>
 */
public class MainActivity extends Activity {
    final String url = "http://192.168.1.104:9810/";

    private List<CardBuilder> mCards;
    private CardScrollView mCardScrollView;
    private MyCardScrollAdapter mAdapter;

    int lastPosition = 0;

    void getSlideData() {
        final Activity activity = this;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mCards = new ArrayList<CardBuilder>();

                    DefaultHttpClient httpclient = new DefaultHttpClient();
                    HttpGet httpget = new HttpGet(url + "slides");

                    HttpResponse response = httpclient.execute(httpget);

                    InputStream inputStream = response.getEntity().getContent();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if (line.equals("")) continue;
                        mCards.add(new CardBuilder(activity, CardBuilder.Layout.TEXT)
                                        .setText(line)
                        );
                    }

                    inputStream.close();
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            initView();
                        }
                    });

                } catch (Exception e) {
                    System.out.println(e);
                }
                return null;
            }
        }.execute(null, null, null);
    }

    void initView() {
        mCardScrollView = new CardScrollView(this);
        mAdapter = new MyCardScrollAdapter();
        mCardScrollView.setAdapter(mAdapter);
        mCardScrollView.activate();

        mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audio.playSoundEffect(Sounds.TAP);
                openOptionsMenu();
            }
        });
        mCardScrollView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i < lastPosition) {
                    Log.i("T", "Previous");
                    doHTTPRequest("prev");
                }
                if (i > lastPosition) {
                    Log.i("T", "Next");
                    doHTTPRequest("next");
                }
                lastPosition = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        setContentView(mCardScrollView);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getSlideData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCardScrollView != null)
            mCardScrollView.activate();
    }

    @Override
    protected void onPause() {
        if (mCardScrollView != null)
            mCardScrollView.deactivate();
        super.onPause();
    }

    private class MyCardScrollAdapter extends CardScrollAdapter {

        @Override
        public int getPosition(Object item) {
            return mCards.indexOf(item);
        }

        @Override
        public int getCount() {
            return mCards.size();
        }

        @Override
        public Object getItem(int position) {
            return mCards.get(position);
        }

        @Override
        public int getViewTypeCount() {
            return CardBuilder.getViewTypeCount();
        }

        @Override
        public int getItemViewType(int position){
            return mCards.get(position).getItemViewType();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mCards.get(position).getView(convertView, parent);
        }
    }

    void doHTTPRequest(final String cmd)
    {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    DefaultHttpClient httpclient = new DefaultHttpClient();
                    HttpGet httpget = new HttpGet(url + cmd);

                    httpclient.execute(httpget);
                } catch (Exception e) {
                    System.out.println(e);
                }
                return null;
            }
        }.execute(null, null, null);
    }

}
