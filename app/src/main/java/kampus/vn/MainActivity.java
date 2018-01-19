package kampus.vn;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kampus.vn.loader.EarthquakeLoader;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<EarthQuake>> {
    private String TAG = MainActivity.class.getName();

    private ListView listView;
    /** TextView that is displayed when the list is empty */
    private TextView emptyView;
    private ProgressBar progressBar;
    /** Adapter for the list of earthquakes */
    private EarthquakeAdapter mAdapter;

    /**
     * Constant value for the earthquake loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int EARTHQUAKE_LOADER_ID = 1;
    private static final String USGS_REQUEST_URL= "https://earthquake.usgs.gov/fdsnws/event/1/query?" +
            "format=geojson&starttime=2017-11-25&endtime=2017-11-28&minmag=4.5&limit=10";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);
        emptyView = findViewById(R.id.emptyView);
        listView.setEmptyView(emptyView);
        progressBar = findViewById(R.id.progressBar);
        //Create a empty list
        List<EarthQuake> list = new ArrayList<>();
        //Create a Adapter
        mAdapter = new EarthquakeAdapter(this, list);
        //Set adapter for List
        listView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //In this case has connect to internet
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getSupportLoaderManager();
            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);
        } else {
            //Hide Loading indicator
            progressBar.setVisibility(View.GONE);
            //Update EmptyView
            emptyView.setText(R.string.no_connect_internet);
        }

        //Handle event when item in ListView was clicked
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                EarthQuake earthQuake = mAdapter.getItem(i);
                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri earthquakeUri = Uri.parse(earthQuake.getUrl());

                // Create a new intent to view the earthquake URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });
    }

    @Override
    public Loader<List<EarthQuake>> onCreateLoader(int id, Bundle args) {
        return new EarthquakeLoader(this, USGS_REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<List<EarthQuake>> loader, List<EarthQuake> data) {
        // Hide loading indicator because the data has been loaded
        progressBar.setVisibility(View.GONE);
        //set emptyview display as string was defined no_earthquakes
        emptyView.setText(R.string.no_earthquakes);
        // Clear the adapter of previous earthquake data
        mAdapter.clear();
        // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (data != null && !data.isEmpty()) {
            mAdapter.addAll(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<EarthQuake>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }
}
