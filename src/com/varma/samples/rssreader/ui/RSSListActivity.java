package com.varma.samples.rssreader.ui;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.test.rssreader.R;
import com.varma.samples.rssreader.data.RSSItem;
import com.varma.samples.rssreader.xmlparser.RSSParser;



public class RSSListActivity extends ListActivity {
	private ArrayList<RSSItem> itemlist = null;
	private RSSListAdaptor rssadaptor = null;
	
	private boolean checkInternetConnection() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    // test for connection
	    if (cm.getActiveNetworkInfo() != null
	            && cm.getActiveNetworkInfo().isAvailable()
	            && cm.getActiveNetworkInfo().isConnected()) {
	        return true;
	    } else {
	        Log.v("rss", "Internet Connection Not Present");
	        return false;
	    }
	}
	
	private boolean haveNetworkConnection() {
	    boolean haveConnectedWifi = false;
	    boolean haveConnectedMobile = false;

	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
	            if (ni.isConnected())
	                haveConnectedWifi = true;
	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
	            if (ni.isConnected())
	                haveConnectedMobile = true;
	    }
	    return haveConnectedWifi || haveConnectedMobile;
	}

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (haveNetworkConnection()== false) crea_connessione();
        
        itemlist = new ArrayList<RSSItem>();
        
        new RetrieveRSSFeeds().execute();
    }
    
    private void crea_connessione()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Connesione Internet non disponibile. Vuoi attivarla?")
               .setCancelable(false)
               .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                   public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                	   //mostra la schermata delle impostazioni del telefono relative al Networking
                	   startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                       
                   }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        finish();
                   }
               });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		RSSItem data = itemlist.get(position);
		
		Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(data.link));
		
		startActivity(intent);
	}

	private void retrieveRSSFeed(String urlToRssFeed,ArrayList<RSSItem> list)
    {
        try
        {
           URL url = new URL(urlToRssFeed);
           SAXParserFactory factory = SAXParserFactory.newInstance();
           SAXParser parser = factory.newSAXParser();
           XMLReader xmlreader = parser.getXMLReader();
           RSSParser theRssHandler = new RSSParser(list);

           xmlreader.setContentHandler(theRssHandler);

           InputSource is = new InputSource(url.openStream());

           xmlreader.parse(is);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private class RetrieveRSSFeeds extends AsyncTask<Void, Void, Void>
    {
    	private ProgressDialog progress = null;
    	
		@Override
		protected Void doInBackground(Void... params) {
			retrieveRSSFeed("http://www.eventiintoscana.it/feed",itemlist);
			
			rssadaptor = new RSSListAdaptor(RSSListActivity.this, R.layout.rssitemview,itemlist);
			
			return null;
		}
    	
		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
		
		@Override
		protected void onPreExecute() {
			progress = ProgressDialog.show(
					RSSListActivity.this, null, "Loading RSS Feeds...");
			
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(Void result) {
			setListAdapter(rssadaptor);
			
			progress.dismiss();
			
			super.onPostExecute(result);
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}
    }
    
    private class RSSListAdaptor extends ArrayAdapter<RSSItem>{
    	private List<RSSItem> objects = null;
    	
		public RSSListAdaptor(Context context, int textviewid, List<RSSItem> objects) {
			super(context, textviewid, objects);
			
			this.objects = objects;
		}
		
		@Override
		public int getCount() {
			return ((null != objects) ? objects.size() : 0);
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public RSSItem getItem(int position) {
			return ((null != objects) ? objects.get(position) : null);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			
			if(null == view)
			{
				LayoutInflater vi = (LayoutInflater)RSSListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = vi.inflate(R.layout.rssitemview, null);
			}
			
			RSSItem data = objects.get(position);
			
			if(null != data)
			{
				TextView title = (TextView)view.findViewById(R.id.txtTitle);
				TextView date = (TextView)view.findViewById(R.id.txtDate);
				TextView description = (TextView)view.findViewById(R.id.txtDescription);
				
				title.setText(data.title);
				date.setText("on " + data.date);
				String prova = android.text.Html.fromHtml(data.description).toString();
				//description.setText(data.description);
				description.setText(prova);
			}
			
			return view;
		}
    }
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
		menu.add(1,1,0,"Credits");
		return true;
    }
    // gestione degli eventi dell'option menu
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    		{
    	case 1:
        	
        	AlertDialog.Builder conferma_canc = new AlertDialog.Builder(this);
        	conferma_canc.setTitle("Credits");
        	conferma_canc.setMessage("Applicazione sviluppata da Luca Innocenti (lucainnoc@gmail.com) modificando il codice reperibile a http://code.google.com/p/krvarma-android-samples/. Il codice sorgente e' disponibile su http://github.com/c1p81");
        	conferma_canc.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        	  public void onClick(DialogInterface dialog, int id) {
        	  }
        	});
        	AlertDialog alert = conferma_canc.create();
	        alert.show();
     		return true;
    		}
		return false;    	   	
    } 
    
    
}