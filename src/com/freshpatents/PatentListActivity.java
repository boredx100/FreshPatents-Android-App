package com.freshpatents;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class PatentListActivity extends ListActivity {

    /**
     * Area list
     */
    ArrayList<HashMap<String,String>> areaList = new ArrayList<HashMap<String,String>>();
    
    /**
     * Progress dialog for loading
     */
    private ProgressDialog dialog;
	
    /**
     * Text to display if no areas are found
     */
    private String noneText = "No patents found";
    
    /**
     * The empty area view
     */
    private TextView emptyView;
    
    /**
     * Context
     */
    private Context mContext;

    /**
     * On create
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
      
        // Grab passed in info
        Bundle extras = getIntent().getExtras();
        
        setContentView(R.layout.list);
        
        ListView lv = getListView();
        
        // Inflate header row so we can set some custom text
        LayoutInflater inflater = getLayoutInflater();
        TextView headerView = (TextView) inflater.inflate(R.layout.header_row, null);
        headerView.setText("Patent Applications");
        lv.addHeaderView(headerView);
        
        // Clear out empty row
        emptyView = ((TextView) lv.getEmptyView());
        emptyView.setText("");
        
        // Show loading dialog
        dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
        
        mContext = this;
        
        // async task
        String url = "/search.php?s=" + extras.getString("srch");
        new GetJsonTask().execute(url);
        

    }
    
    /**
     * Asynchronous get JSON task
     */
    private class GetJsonTask extends AsyncTask<String, Void, String> {
        
        /**
         * Execute in background
         */
        protected String doInBackground(String... args) {
            
              FpApi api = new FpApi(mContext);
              return api.getJson(args[0]);

        }
        
        /**
         * After execute (in UI thread context)
         */
        protected void onPostExecute(String result)
        {
            loadPatents(result);
        }
    }
    
    /**
     * Load areas from JSON string result
     */
    public void loadPatents(String result) {
    
        try {
          
        	Log.i("FP", "result");
        	Log.i("FP", result);
            // Convert result into JSONArray
            JSONArray json = new JSONArray(result);
          
            // Loop over JSONarray
            for (int i = 0; i < json.length(); i++) {
            
                // Get JSONObject from current array element
                JSONObject areaObject = json.getJSONObject(i);
                
                String patentId = areaObject.getString("ApplicationNumber");
                Log.i("FP", patentId);
                String name = areaObject.getString("ApplicationTitle");
                Log.i("FP", name);
                
                // Add state to name, if present
                if (areaObject.has("state")) {
                    name += " (" + areaObject.getString("state") +")";
                }
                
                // Add state to hashmap
                HashMap<String, String> map = new HashMap<String,String>();
                map.put("patentId", patentId);
                map.put("name", name);
                areaList.add(map);
            }
          
        } catch (JSONException e) {
          
        	Toast.makeText(mContext, "An error occurred while retrieving area data", Toast.LENGTH_SHORT).show();
          
        }
    
        // Create simple adapter using hashmap
        SimpleAdapter areas = new SimpleAdapter(
            this,
            areaList,
            R.layout.list_row,
            new String[] { "name"},
            new int[] { R.id.name }
        );
      
      setListAdapter(areas);
    
      ListView lv = getListView();
      lv.setTextFilterEnabled(true);
      
      // Populate empty row in case we didn't find any areas
      emptyView.setText(noneText);
      
      // Set on item click listener for states
      lv.setOnItemClickListener(new OnItemClickListener() {
          
          /**
           * On item click action, open area activity
           */
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              
              // Get object from item position
              Object item = parent.getItemAtPosition(position);
              HashMap<String, String> hashMap = (HashMap<String, String>) item;
              String str = hashMap.get("patentId"); // id
              String patentName = hashMap.get("name");
        
              Intent i = new Intent(getApplicationContext(), PatentDetailActivity.class);
              i.putExtra("patentId", str);
              i.putExtra("name", patentName);
              startActivity(i);
          }
          
      });
      
      dialog.hide();
      
    }
    
}
