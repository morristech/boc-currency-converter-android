package com.example.boccurrencyconverter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	JSONArray rates;
	public static String PACKAGE_NAME;
	List ratesList;
	
	ListView fromList;
	ListView toList;
	String[] ratesArray;
	
	BigDecimal fromAmount;
	BigDecimal toAmount;
	
	BigDecimal fromRate;
	BigDecimal toRate;
	
	double value;
	
	int fromSelected;
	int toSelected;
	
	Dialog fromDialog;
	Dialog toDialog;
	
	RateAdapter fromAdapter;
	RateAdapter toAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_main);
		
		PACKAGE_NAME = getApplicationContext().getPackageName();
		
		fromSelected = 0;
		toSelected = 1;

		View view = this.findViewById(android.R.id.content);
		final Activity activity = this;
		
		// Setup event to dismiss keyboard when background is touched
		view.setOnTouchListener(new View.OnTouchListener() {  
			@Override
		    public boolean onTouch (View v, MotionEvent event)
		    {
		        hideSoftKeyboard(activity);
		        return false;
		    }
		});
		
		// Setup event to pick currency from
		final Button fromButton = (Button) findViewById(R.id.selectConvertFrom);
        fromButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	showSelectFrom(v);
            }
        });
        
        // Setup event to pick currency to
        final Button toButton = (Button) findViewById(R.id.selectConvertTo);
        toButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	showSelectTo(v);
            }
        });
        
        // Setup event to pick currency to
        final ImageButton swapButton = (ImageButton) findViewById(R.id.swapButton);
        swapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	swapCurrencies(v);
            }
        });
        
        // Setup on change event for from value
        EditText fromInput = (EditText)findViewById(R.id.fromInput);
        fromInput.addTextChangedListener(new TextWatcher() {
        	@Override
        	public void afterTextChanged(Editable s) {
        		try {
        			value = Double.parseDouble(s.toString());
        			calculateExchangeRate();
        		} catch(NumberFormatException e) {
        			// TODO Auto-generated catch block
    				e.printStackTrace();
        		}
            }

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
        });

		JSONClient client = new JSONClient(this, l);
		
		// Set url with sort json encoded
		String url = "http://exchng.ca/rates?%7b%22$sort%22:%7b%22order%22:1%7d%7d";
		client.execute(url);
	}
 
    GetJSONListener l = new GetJSONListener(){
 
		@Override
		public void onRemoteCallComplete(JSONArray jsonFromNet) {
			rates = jsonFromNet;

			EditText fromInput = (EditText) findViewById(R.id.fromInput);
			
			try {
				fromRate = new BigDecimal(rates.getJSONObject(fromSelected).getString("rate").toString());
				fromInput.setText("1.00");
				toRate = new BigDecimal(rates.getJSONObject(toSelected).getString("rate").toString());
				calculateExchangeRate();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void calculateExchangeRate() {
		try {
			fromRate = new BigDecimal(rates.getJSONObject(fromSelected).getString("rate").toString());
			toRate = new BigDecimal(rates.getJSONObject(toSelected).getString("rate").toString());
			EditText fromInput = (EditText) findViewById(R.id.fromInput);
			double value = Double.parseDouble(fromInput.getText().toString());
			double exchangeRate = fromRate.doubleValue() / toRate.doubleValue();
			double answer = value * exchangeRate;
		
			EditText answerText = (EditText) findViewById(R.id.answer);
			answerText.setText(String.format("%.2f", answer));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void swapCurrencies(View v) {
		// Back up flag
		ImageView fromFlag = (ImageView) findViewById(R.id.fromImage);
		ImageView toFlag = (ImageView) findViewById(R.id.toImage);
		
		// Swap
		try {
			String file = rates.getJSONObject(fromSelected).getString("shortcode").toString().toLowerCase() + "_flag";
			int resID = getResources().getIdentifier(file , "drawable", MainActivity.PACKAGE_NAME);
			toFlag.setImageResource(resID);
			file = rates.getJSONObject(toSelected).getString("shortcode").toString().toLowerCase() + "_flag";
			resID = getResources().getIdentifier(file , "drawable", MainActivity.PACKAGE_NAME);
			fromFlag.setImageResource(resID);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int tempSelected = toSelected;
		toSelected = fromSelected;
		fromSelected = tempSelected;
		
		if (fromList != null) {
			fromList.setItemChecked(fromSelected, true);
		}
		if (toList != null) {
			toList.setItemChecked(toSelected, true);
		}
		
		Button selectFromButton = (Button) findViewById(R.id.selectConvertFrom);
		String labelTo = (String) selectFromButton.getText();
	    
	    Button selectToButton = (Button) findViewById(R.id.selectConvertTo);
	    String labelFrom = (String) selectToButton.getText();
	    
	    selectFromButton.setText(labelFrom);
	    selectToButton.setText(labelTo);
		
		calculateExchangeRate();
	}
	
	public void showSelectFrom(View v) {
		if (fromDialog == null) {
			// custom dialog
			fromDialog = new Dialog(getBaseContext());
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Select Currency");
			builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    dialog.dismiss();
                }
            });
			
			
			fromList = new ListView(this);
			
			ratesList = new ArrayList(rates.length());
			fromList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			
	        // Fill the songs array by using a for loop
	        for(int i=0; i < rates.length(); i++){
	        	try {
	        		ratesList.add(new Currency(rates.getJSONObject(i).getString("label").toString(), rates.getJSONObject(i).getString("shortcode").toString()));
	        	} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        
	        fromAdapter = new RateAdapter(this, R.layout.list_row, ratesList, getResources(), fromSelected);

			fromList.setAdapter(fromAdapter);
			
			fromList.setItemChecked(fromSelected, true);
			
			// Defining the item click listener for listView
	        OnItemClickListener itemClickListener = new OnItemClickListener() {
	            @Override
	            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
	            	MainActivity.this.selectNewFrom(position);
	            	fromDialog.dismiss();
	             }
	        };
	        
	        // Setting the item click listener for listView
	        fromList.setOnItemClickListener(itemClickListener);
	        fromList.setBackgroundColor(R.color.almostBlack);
	        
			builder.setView(fromList);
			fromDialog = builder.create();
		}
 
		fromDialog.show();
	}
	
	public void showSelectTo(View v) {
		if (toDialog == null) {
			// custom dialog
			toDialog = new Dialog(getBaseContext());
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Select Currency");
			builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    dialog.dismiss();
                }
            });
			
			toList = new ListView(this);
			
			ratesList = new ArrayList(rates.length());
			toList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			
	        // Fill the songs array by using a for loop
	        for(int i=0; i < rates.length(); i++){
	        	try {
	        		ratesList.add(new Currency(rates.getJSONObject(i).getString("label").toString(), rates.getJSONObject(i).getString("shortcode").toString()));
	        	} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        
	        toAdapter = new RateAdapter(this, R.layout.list_row, ratesList, getResources(), toSelected);

			toList.setAdapter(toAdapter);
			toList.setItemChecked(toSelected, true);
			
			// Defining the item click listener for listView
	        OnItemClickListener itemClickListener = new OnItemClickListener() {
	            @Override
	            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
	            	MainActivity.this.selectNewTo(position);
	            	toDialog.dismiss();
	            }
	        };
	        
	        // Setting the item click listener for listView
	        toList.setOnItemClickListener(itemClickListener);
	        toList.setBackgroundColor(R.color.almostBlack);
	        
			builder.setView(toList);
			toDialog = builder.create();
		}
		
		toDialog.show();
	}
	
	public boolean selectNewFrom(int selected) {
	    fromSelected = selected;
	    fromList.setItemChecked(fromSelected, true);
	    
	    fromAdapter.setSelected(fromSelected);
	    
	    Button button = (Button) findViewById(R.id.selectConvertFrom);
	    ImageView flag = (ImageView) findViewById(R.id.fromImage);
	    
	    try {
	    	button.setText(rates.getJSONObject(fromSelected).getString("label").toString());
			fromRate = new BigDecimal(rates.getJSONObject(fromSelected).getString("rate").toString());
			String file = rates.getJSONObject(fromSelected).getString("shortcode").toString().toLowerCase() + "_flag";
			int resID = getResources().getIdentifier(file , "drawable", MainActivity.PACKAGE_NAME);
			flag.setImageResource(resID);
			calculateExchangeRate();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    return true;
	}
	
	public boolean selectNewTo(int selected) {
	    toSelected = selected;
	    toList.setItemChecked(toSelected, true);
	    
	    toAdapter.setSelected(toSelected);
	    
	    Button button = (Button) findViewById(R.id.selectConvertTo);
	    ImageView flag = (ImageView) findViewById(R.id.toImage);
	    
	    try {
	    	button.setText(rates.getJSONObject(toSelected).getString("label").toString());
			fromRate = new BigDecimal(rates.getJSONObject(toSelected).getString("rate").toString());
			String file = rates.getJSONObject(toSelected).getString("shortcode").toString().toLowerCase() + "_flag";
			int resID = getResources().getIdentifier(file , "drawable", MainActivity.PACKAGE_NAME);
			flag.setImageResource(resID);
			calculateExchangeRate();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    return true;
	}
	
	public static void hideSoftKeyboard(Activity activity) {
	    InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
	    inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
	}
}
