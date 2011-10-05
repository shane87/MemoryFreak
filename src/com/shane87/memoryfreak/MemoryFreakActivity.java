package com.shane87.memoryfreak;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.OutputStreamWriter;

import com.shane87.memoryfreak.R;
import com.shane87.memoryfreak.ShellInterface;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MemoryFreakActivity extends Activity {
	
	int fgAppMem;
	int swappiness;
	int zramDiskSize;
	int hidAppMem;
	int empAppMem;
	
	int Tfgappadj = 0, Tvisappadj = 0, Tpercappadj = 0, Thwappadj = 0, Tsecseradj = 0,
    		Tbupappadj = 0, Thomeappadj = 0, Thidappminadj = 0, Tempappadj = 0;
    int Tfgappmem = 0, Tvisappmem = 0, Tpercappmem = 0, Thwappmem = 0, Tsecsermem = 0,
    		Tbupappmem = 0, Thomeappmem = 0, Thidappmem = 0, Tempappmem = 0;
    String TlmkParAdj = "", TlmkParMin = "";
    
    boolean talDefLoaded;
	
	TextView zramTxt;
	TextView swapTxt;
	TextView perfSizeTxt;
	Spinner lmkSpin;
	SeekBar zramSeek;
	SeekBar swapSeek;
	SeekBar perfSeek;
	Button helpBtn;
	Button resetBtn;
	Button drpCacheBtn;
	
	int lmkSelection;
	
	Menu mMenu;
	
	ArrayAdapter<String> adapterForLmk;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!checkKernel())
        {
        	setContentView(R.layout.kerr);
        	showKErrNote();
        }
        else
        {
        	setContentView(R.layout.main);
        	
        	if(loadCurSettings())
        	{
        		setupControls();
        	}
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	mMenu = menu;
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.mainmenu, mMenu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    	case(R.id.exit):
    	{
    		this.finish();
    		break;
    	}
    	case(R.id.save):
    	{
    		saveSettings();
    		break;
    	}
    	case(R.id.apply):
    	{
    		applySettings();
    		break;
    	}
    	case(R.id.about):
    	{
    		showAboutScreen();
    		break;
    	}
    	}
    	return true;
    }
    
    private boolean checkKernel()
    {
    	if(!checkSU())
    		return false;
    	
    	if(!checkKVer())
    		return false;
    	
    	return true;
    }
    
    private boolean checkSU()
    {
    	if(ShellInterface.isSuAvailable())
    		return true;
    	
    	return false;
    }
    
    private boolean checkKVer()
    {
    	String kver;
    	kver = ShellInterface.getProcessOutput("cat /proc/version");
    	
    	if(kver.contains("talon"))
    		return true;
    	else
    		return false;
    }
    
    private boolean loadCurSettings()
    {
    	talDefLoaded = loadTalonSettings();
    	try
    	{
    		BufferedReader reader = new BufferedReader(new FileReader("/etc/ram.conf"));
    		
    		String temp;
    		String tmpArr[];
    		
    		do
    		{
    			temp = reader.readLine();
    		}while(!temp.startsWith("FOREGROUND_APP_MEM"));
    		
    		tmpArr = temp.split("=");
    		fgAppMem = Integer.parseInt(tmpArr[1].trim());
    		
    		switch(fgAppMem)
    		{
    		case(2560):
    		{
    			lmkSelection = 0;
    			break;
    		}
    		case(2048):
    		{
    			lmkSelection = 1;
    			break;
    		}
    		default:
    		{
    			lmkSelection = 2;
    			do
    			{
    				temp = reader.readLine();
    			}while(!temp.startsWith("HIDDEN_APP_MEM"));
    			
    			tmpArr = temp.split("=");
    			hidAppMem = Integer.parseInt(tmpArr[1].trim());
    			
    			do
    			{
    				temp = reader.readLine();
    			}while(!temp.startsWith("EMPTY_APP_MEM"));
    			
    			tmpArr = temp.split("=");
    			empAppMem = Integer.parseInt(tmpArr[1].trim());
    			break;
    		}
    		}
    		
    		do
    		{
    			temp = reader.readLine();
    		}while(!temp.startsWith("ZRAM_SIZE"));
    		
    		tmpArr = temp.split("=");
    		zramDiskSize = Integer.parseInt(tmpArr[1].trim());
    		
    		do
    		{
    			temp = reader.readLine();
    		}while(!temp.startsWith("SWAPPINESS"));
    		
    		tmpArr = temp.split("=");
    		swappiness = Integer.parseInt(tmpArr[1].trim());
    		
    		reader.close();
    		
    	}catch(Exception Ignored){ return false; }
    	
    	return true;
    }
    
    private boolean setupControls()
    {
    	//Get refs to our controls, and save them in our global vars
    	zramTxt = (TextView)findViewById(R.id.zramTV);
    	swapTxt = (TextView)findViewById(R.id.swappinessTV);
    	perfSizeTxt = (TextView)findViewById(R.id.perfSizeTV);
    	zramSeek = (SeekBar)findViewById(R.id.zramSB);
    	swapSeek = (SeekBar)findViewById(R.id.swappinessSB);
    	perfSeek = (SeekBar)findViewById(R.id.perfSeekbar);
    	lmkSpin = (Spinner)findViewById(R.id.lmkSpinner);
    	helpBtn = (Button)findViewById(R.id.perfHelpBtn);
    	resetBtn = (Button)findViewById(R.id.perfResetBtn);
    	drpCacheBtn = (Button)findViewById(R.id.drpCacheBtn);
    	
    	if(lmkSelection == 2)
    	{
    		perfSeek.setVisibility(View.VISIBLE);
    		perfSizeTxt.setVisibility(View.VISIBLE);
    		resetBtn.setVisibility(View.VISIBLE);
    	}
    	else
    	{
    		perfSeek.setVisibility(View.GONE);
    		perfSizeTxt.setVisibility(View.GONE);
    		resetBtn.setVisibility(View.GONE);
    	}
    	
    	//Now lets create our array adapter for the lmk spinner, set the propper vals, and
    	//link it to the spinner
    	
    	//First, initialize it
    	adapterForLmk = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
    	
    	//Next, lets set the dropdown view resource and lets add the values
    	adapterForLmk.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	adapterForLmk.add("Samsung Stock");
    	adapterForLmk.add("Gingerbread Stock (Nexus S)");
    	adapterForLmk.add("Talon");
    	
    	//Then we can link the adapter to the spinner
    	lmkSpin.setAdapter(adapterForLmk);
    	
    	//Finally, we will set the default selection
    	lmkSpin.setSelection(lmkSelection);
    	
    	//Now we need the OnItemSelected listener for the lmk spinner
    	OnItemSelectedListener lmkListener = new Spinner.OnItemSelectedListener()
    	{

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) 
			{
				lmkSelection = (int)arg0.getSelectedItemId();
				if(lmkSelection == 2)
				{
					perfSeek.setVisibility(View.VISIBLE);
					perfSizeTxt.setVisibility(View.VISIBLE);
					resetBtn.setVisibility(View.VISIBLE);
				}
				else
				{
					perfSeek.setVisibility(View.GONE);
					perfSizeTxt.setVisibility(View.GONE);
					resetBtn.setVisibility(View.GONE);
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {}
    		
    	};
    	//And then we can link it to the spinner
    	lmkSpin.setOnItemSelectedListener(lmkListener);
    	
    	//Now that the spinner is setup, lets set up our seekbars, starting with zram
    	//First, we will set the seek bar to the proper progress
    	zramSeek.setMax(21);
    	zramSeek.setProgress(zramDiskSize / 32);
    	//Now we will build the listener
    	zramSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
    	{
    		//this method is not seeded, since we dont interact when the user lifts
    		//their finger from the seekbar
			public void onStopTrackingTouch(SeekBar seekBar){}
			//again, we dont interact when they start touching the seek bar either
			public void onStartTrackingTouch(SeekBar seekBar) {}
			//this method, we do need. this will let us know what the user sets the
			//zram size to, so that we can make changes when settings are applied
			//it will also update the zramtxt info so the user can see what they have set
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser)
			{
				zramDiskSize = progress * 32;
				zramTxt.setText("ZRAM Size: " + Integer.toString(zramDiskSize));
			}
		});
    	
    	//Now lets setup the swappiness seekbar
    	//first the initial progress
    	swapSeek.setMax(10);
    	swapSeek.setProgress(swappiness / 10);
    	//now the listener
    	swapSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
    	{
			
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser)
			{
				swappiness = progress * 10;
				swapTxt.setText("Swappiness: " + Integer.toString(swappiness));
			}
		});
    	
    	perfSeek.setMax(6);
    	perfSeek.setProgress((hidAppMem / 1024) - 7);
    	perfSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				hidAppMem = (progress + 7) * 1024;
				if(hidAppMem <= 9216)
					empAppMem = 12288;
				else
					empAppMem = hidAppMem + 2048;
				
				perfSizeTxt.setText("Memory for Background Apps (MB): " 
						+ Integer.toString((hidAppMem * 4) / 1024));
				
			}
		});
    	
    	helpBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				showHelp();
				
			}
		});
    	resetBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				resetHAppM();
				
			}
		});
    	drpCacheBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				dropCache();
				
			}
		});
    	
    	//finally, lets set our initial txt strings with the correct current info
    	zramTxt.setText("ZRAM Size: " + Integer.toString(zramDiskSize));
    	swapTxt.setText("Swappiness: " + Integer.toString(swappiness));
    	perfSizeTxt.setText("Memory for Background Apps (MB): " 
    			+ Integer.toString((hidAppMem * 4) / 1024));
    	
    	return true;
    }
    
    private boolean saveSettings()
    {
    	int fgappadj = 0, visappadj = 0, percappadj = 0, hwappadj = 0, secseradj = 0,
    		bupappadj = 0, homeappadj = 0, hidappminadj = 0, empappadj = 0;
    	int fgappmem = 0, visappmem = 0, percappmem = 0, hwappmem = 0, secsermem = 0,
    		bupappmem = 0, homeappmem = 0, hidappmem = 0, empappmem = 0;
    	String lmkParAdj = "", lmkParMin = "";
    	
    	String[] TlmkParMinParts;
    	
    	switch(lmkSelection)
    	{
    	case(0):
    	{
    		fgappadj = 0;
    		visappadj = 1;
    		percappadj = 1;
    		hwappadj = 2;
    		secseradj = 2;
    		bupappadj = 2;
    		homeappadj = 4;
    		hidappminadj = 7;
    		empappadj = 15;
    		
    		fgappmem = 2560;
    		visappmem = 4096;
    		percappmem = 4096;
    		hwappmem = 4096;
    		secsermem = 6144;
    		bupappmem = 6144;
    		homeappmem = 6144;
    		hidappmem = 10240;
    		empappmem = 12288;
    		
    		lmkParAdj = "0,1,2,7,14,15";
    		lmkParMin = "2560,4096,6144,10240,11264,12288";
    		break;
    	}
    	case(1):
    	{
    		fgappadj = 0;
    		visappadj = 1;
    		percappadj = 2;
    		hwappadj = 3;
    		secseradj = 4;
    		bupappadj = 5;
    		homeappadj = 6;
    		hidappminadj = 7;
    		empappadj = 15;
    		
    		fgappmem = 2048;
    		visappmem = 3072;
    		percappmem = 4096;
    		hwappmem = 4096;
    		secsermem = 6144;
    		bupappmem = 6144;
    		homeappmem = 6144;
    		hidappmem = 7168;
    		empappmem = 8192;
    		
    		lmkParAdj = "0,1,2,4,7,15";
    		lmkParMin = "2048,3072,4096,6144,7168,8192";
    		break;
    	}
    	case(2):
    	{
    		if(talDefLoaded)
    		{
    			fgappadj = Tfgappadj;
    			visappadj = Tvisappadj;
    			percappadj = Tpercappadj;
    			hwappadj = Thwappadj;
    			secseradj = Tsecseradj;
    			bupappadj = Tbupappadj;
    			homeappadj = Thomeappadj;
    			hidappminadj = Thidappminadj;
    			empappadj = Tempappadj;
    		
    			fgappmem = Tfgappmem;
    			visappmem = Tvisappmem;
    			percappmem = Tpercappmem;
    			hwappmem = Thwappmem;
    			secsermem = Tsecsermem;
    			bupappmem = Tbupappmem;
    			homeappmem = Thomeappmem;
    			hidappmem = hidAppMem;
    			empappmem = empAppMem;
    		
    			lmkParAdj = TlmkParAdj;
    			
    			TlmkParMinParts = TlmkParMin.split(",");
    			lmkParMin = TlmkParMinParts[0] + "," + TlmkParMinParts[1] + "," +
    					TlmkParMinParts[2] + "," + TlmkParMinParts[3] + "," +
    					Integer.toString(hidappmem) + "," + Integer.toString(empappmem) + "\"";
    		}
    		else
    		{
    			fgappadj = 0;
    			visappadj = 1;
    			percappadj = 2;
    			hwappadj = 3;
    			secseradj = 3;
    			bupappadj = 3;
    			homeappadj = 2;
    			hidappminadj = 5;
    			empappadj = 15;
    		
    			fgappmem = 1024;
    			visappmem = 2048;
    			percappmem = 3072;
    			hwappmem = 4096;
    			secsermem = 4096;
    			bupappmem = 4096;
    			homeappmem = 3072;
    			hidappmem = hidAppMem;
    			empappmem = empAppMem;
    		
    			lmkParAdj = "0,1,2,3,7,15";
    			lmkParMin = "1024,2048,3072,4096," + Integer.toString(hidappmem) + ","
    				+ Integer.toString(empappmem);
    		}
    		break;
    	}
    	}
    	try
    	{
    		OutputStreamWriter writer = new OutputStreamWriter(openFileOutput("ram.conf", 0));
    		BufferedReader reader = new BufferedReader(new FileReader("/etc/ram.conf"));
    		String temp;
    		String tempArr[];
    		
    		for(;;)
    		{
    			temp = reader.readLine();
    			if(temp == null)
    				break;
    			
    			if(temp.startsWith("#") || temp.startsWith("RAM_CONF"))
    				writer.write(temp.trim() + "\n");
    			else if(temp.startsWith("LOWMEM_RESERVE_RATIO"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				if(zramDiskSize < 128)
    					temp = temp.concat("=32\n");
    				else
    					temp = temp.concat("=256\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("FOREGROUND_APP_ADJ"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(fgappadj) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("VISIBLE_APP_ADJ"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(visappadj) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("PERCEPTIBLE_APP_ADJ"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(percappadj) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("HEAVY_WEIGHT_APP_ADJ"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(hwappadj) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("SECONDARY_SERVER_ADJ"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(secseradj) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("BACKUP_APP_ADJ"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(bupappadj) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("HOME_APP_ADJ"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(homeappadj) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("HIDDEN_APP_MIN_ADJ"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(hidappminadj) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("EMPTY_APP_ADJ"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(empappadj) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("FOREGROUND_APP_MEM"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(fgappmem) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("VISIBLE_APP_MEM"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(visappmem) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("PERCEPTIBLE_APP_MEM"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(percappmem) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("HEAVY_WEIGHT_APP_MEM"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(hwappmem) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("SECONDARY_SERVER_MEM"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(secsermem) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("BACKUP_APP_MEM"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(bupappmem) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("HOME_APP_MEM"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(homeappmem) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("HIDDEN_APP_MEM"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(hidappmem) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("EMPTY_APP_MEM"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(empappmem) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("LMK_ADJ"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + lmkParAdj + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("LMK_MINFREE"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + lmkParMin + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("ZRAM_SIZE"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(zramDiskSize) + "\n");
    				writer.write(temp);
    			}
    			else if(temp.startsWith("SWAPPINESS"))
    			{
    				tempArr = temp.split("=");
    				temp = tempArr[0];
    				temp = temp.concat("=" + Integer.toString(swappiness) + "\n");
    				writer.write(temp);
    			}
    			else
    			{
    				writer.write(temp + "\n");
    			}
    		}
    		
    		writer.close();
    		reader.close();
    	}catch(Exception Ignored){ return false; }
    	
    	ShellInterface.runCommand("busybox mount -o remount,rw /system");
    	ShellInterface.runCommand("busybox rm /etc/ram.conf");
    	ShellInterface.runCommand("busybox cp /data/data/com.shane87.memoryfreak/files/ram.conf /etc/ram.conf");
    	ShellInterface.runCommand("chmod 666 /etc/ram.conf");
    	ShellInterface.runCommand("busybox rm /data/data/com.shane87.memoryfreak/files/ram.conf");
    	ShellInterface.runCommand("busybox mount -o remount,ro /system");
    	
    	Toast.makeText(this, "Settings Saved to /etc/ram.conf", Toast.LENGTH_LONG).show();
    	return true;
    }
    
    private boolean applySettings()
    {
    	if(saveSettings())
    	{
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle("Reboot to Apply Settings?");
    		builder.setMessage("Memory Freak has saved your low memeory killer settings to ram.conf." +
    				" To apply these settings, your phone will now be rebooted. Press" +
    				" \"Yes\" to continue, or \"No\" to return to Memory Freak without" +
    				" rebooting.");
    		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {}
			});
    		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which)
				{
					ShellInterface.runCommand("reboot");
				}
			});
    		
    		AlertDialog alert = builder.create();
    		
    		alert.show();
    	}
    	return true;
    }
    
    private void showKErrNote()
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Wrong Kernel!");
    	builder.setMessage("Uh Oh!!\nIt looks like you have an incompatible kernel" +
    			"installed!\n\nThis app is designed for, and indeed only works with," +
    			" Talon Kernel, by eXistZ, kodos96, ytt3r, and zacharias.maladroit.\n\n" +
    			"For more information, or to get Talon kernel, please visit XDA by" +
    			" selecting \"More Info\".");
    	builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				finish();
				
			}
		});
    	builder.setNeutralButton("More Info", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				 String url = "http://forum.xda-developers.com/showthread.php?t=1050206";
				 Intent i = new Intent(Intent.ACTION_VIEW);
				 i.setData(Uri.parse(url));
				 startActivity(i);
				 finish();
				
			}
		});
    	builder.setCancelable(false);
    	
    	AlertDialog alert = builder.create();
    	alert.show();
    }
    
    private void showAboutScreen()
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Memory Freak v" + getResources().getString(R.string.version));
    	builder.setMessage("Memory Freak is an app for setting and adjusting" +
    			" lmk values and other memory related settings. The main lmk settings" +
    			" are provided in three pre-defined presets. The provided presets" +
    			" are Samsung Stock (Galaxy S), Gingerbread Stock (Nexus S), and Talon" +
    			" (Talon Kernel Default). Also exposed are controls for ZRAM," +
    			" Swappiness, and, when the Talon preset is selected, Memory for Background Apps.\n\n" +
    			"A HUGE thanks goes to:\n" +
    			"eXistZ @ xda-developers.com for the Talon Kernel, and for the initial " +
    			"idea for the app\nkodos96 @ xda-developers.com for co-dev on the Talon " +
    			"Kernel, and designing the ram.conf system that Memory Freak utilizes " +
    			"to set the lmk values\nytt3r @ xda-developers.com for co-dev of the " +
    			"Talon Kernel\nzacharias.maladroit @ xda-developers.com for co-dev " +
    			"of the Talon Kernel\n\nAnd to YOU, for using this app!\nPlease remember " +
    			"that feedback is some of the most appreciated help!");
    	builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {}
		});
    	builder.setNeutralButton("More Info", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				 String url = "http://forum.xda-developers.com/showthread.php?t=1050206";
				 Intent i = new Intent(Intent.ACTION_VIEW);
				 i.setData(Uri.parse(url));
				 startActivity(i);
				 finish();
				
			}
		});
    	builder.setPositiveButton("Donate", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				 String url = "http://forum.xda-developers.com/donatetome.php?u=3482571";
				 Intent i = new Intent(Intent.ACTION_VIEW);
				 i.setData(Uri.parse(url));
				 startActivity(i);
				
			}
		});
    	
    	AlertDialog alert = builder.create();
    	alert.show();
    }
    
    private void resetHAppM()
    {
    	if(talDefLoaded)
    	{
    		hidAppMem = Thidappmem;
    		empAppMem = Tempappmem;
    	}
    	else
    	{
    		hidAppMem = 9216;
    		empAppMem = 12288;
    	}
    	
    	if(perfSeek != null)
    		perfSeek.setProgress((hidAppMem / 1024) -7);
    	if(perfSizeTxt != null)
    		perfSizeTxt.setText("Memory for Background Apps (MB): " + 
    						Integer.toString((hidAppMem * 4) / 1024));
    }
    
    private void showHelp()
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Memory Freak Help");
    	builder.setMessage("Memory freak is designed to place control of the lmk " +
    					   "values in the user's hands, while trying to prevent " +
    					   "some of the more common lmk adjustment errors. Here " +
    					   "are some tips to help get you started.\n\n" +
    					   "1. The Low Memory Killer Settings dropdown allows you to choose one of " +
    					   "three preset values, Samsung Stock, Gingerbread Stock (Nexus S), and Talon. Samsung Stock is the setup " +
    					   "designed by Samsung for the Gingerbread platform on the " +
    					   "Galaxy S. Gingerbread Stock (Nexus S) is the setup originaly designed by " +
    					   "google for the AOSP Gingerbread platform. Talon is the " +
    					   "current setup designed by kodos96 for the Talon Kernel\n\n" +
    					   "2. The ZRAM slider adjusts ZRAM size, in MB, from 0 (disabled) " +
    					   "to 672 in steps of 32MB. The higher this value is, the more " +
    					   "RAM the kernel can compress and store.\n\n" +
    					   "NOTICE: Higher ZRAM settings will allow more data " +
    					   "to be stored in RAM. HOWEVER, all of the data stored " +
    					   "in ZRAM is compressed. This means that the data must " +
    					   "be decompressed prior to use, and must be recompressed " +
    					   "before being transfered back to ZRAM. This means that larger " +
    					   "ZRAM sizes MAY introduce lag, due to the overhed of repeated " +
    					   "compress/decompress cycles. You have been warned!!\n\n" +
    					   "3. The Swappiness slider lets you adjust how frequently " +
    					   "the kernel swaps pages from RAM to ZRAM. This is " +
    					   "adjustable from 0 (Swap Disabled) to 100 in steps of 10.\n\n" +
    					   "4. The Memory for Background Apps slider, which is only available " +
    					   "when the Talon preset is selected, allows the user to fine-" +
    					   "tune their lmk for their particular needs, whether it be " +
    					   "more single task oriented, or " +
    					   "multitasking ability. The reset button will return " +
    					   "the value back to the default Talon setting.\n\n" +
    					   "5. The \"Drop Caches\" button will call \"free\" followed by " +
    					   "\"sync\" before telling the kernel to drop caches. After the " +
    					   "caches are dropped, it calls \"free\" again. After it finishes " +
    					   "it will display the results of both calls to \"free\" " +
    					   "to show you caches were dropped.");
    	builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {}
		});
    	/*builder.setNeutralButton("More Info", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				 String url = "http://forum.xda-developers.com/showthread.php?t=1050206";
				 Intent i = new Intent(Intent.ACTION_VIEW);
				 i.setData(Uri.parse(url));
				 startActivity(i);
				 finish();
				
			}
		});*/
    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {}
		});
    	
    	AlertDialog alert = builder.create();
    	alert.show();
    }
    
    private boolean loadTalonSettings()
    {
    	try
    	{
    		BufferedReader reader = new BufferedReader(new FileReader("/bin/ram.conf.default"));
    		String temp;
    		String tempArr[];
		
    		for(;;)
    		{
    			temp = reader.readLine();
    			if(temp == null)
    				break;
    			else if(temp.startsWith("FOREGROUND_APP_ADJ"))
    			{
    				tempArr = temp.split("=");
    				Tfgappadj = Integer.parseInt(tempArr[1]);
    			}
    			else if(temp.startsWith("VISIBLE_APP_ADJ"))
    			{
    				tempArr = temp.split("=");
    				Tvisappadj = Integer.parseInt(tempArr[1]);
    			}
    			else if(temp.startsWith("PERCEPTIBLE_APP_ADJ"))
    			{
    				tempArr = temp.split("=");
    				Tpercappadj = Integer.parseInt(tempArr[1]);
    			}
    			else if(temp.startsWith("HEAVY_WEIGHT_APP_ADJ"))
    			{
    				tempArr = temp.split("=");
    				Thwappadj = Integer.parseInt(tempArr[1]);
    			}
    			else if(temp.startsWith("SECONDARY_SERVER_ADJ"))
    			{
    				tempArr = temp.split("=");
    				Tsecseradj = Integer.parseInt(tempArr[1]);
    			}
    			else if(temp.startsWith("BACKUP_APP_ADJ"))
    			{
    				tempArr = temp.split("=");
    				Tbupappadj = Integer.parseInt(tempArr[1]);
    			}
    			else if(temp.startsWith("HOME_APP_ADJ"))
    			{
    				tempArr = temp.split("=");
    				Thomeappadj = Integer.parseInt(tempArr[1]);
    			}
    			else if(temp.startsWith("HIDDEN_APP_MIN_ADJ"))
    			{
    				tempArr = temp.split("=");
    				Thidappminadj = Integer.parseInt(tempArr[1]);
    			}
    			else if(temp.startsWith("EMPTY_APP_ADJ"))
    			{
    				tempArr = temp.split("=");
    				Tempappadj = Integer.parseInt(tempArr[1]);
    			}
    			else if(temp.startsWith("FOREGROUND_APP_MEM"))
    			{
    				tempArr = temp.split("=");
    				Tfgappmem = Integer.parseInt(tempArr[1]);
    			}
    			else if(temp.startsWith("VISIBLE_APP_MEM"))
    			{
    				tempArr = temp.split("=");
    				Tvisappmem = Integer.parseInt(tempArr[1]);
    			}
    			else if(temp.startsWith("PERCEPTIBLE_APP_MEM"))
    			{
    				tempArr = temp.split("=");
    				Tpercappmem = Integer.parseInt(tempArr[1]);
    			}
    			else if(temp.startsWith("HEAVY_WEIGHT_APP_MEM"))
    			{
    				tempArr = temp.split("=");
    				Thwappmem = Integer.parseInt(tempArr[1]);
    			}
    			else if(temp.startsWith("SECONDARY_SERVER_MEM"))
    			{
    				tempArr = temp.split("=");
    				Tsecsermem = Integer.parseInt(tempArr[1]);
    			}
    			else if(temp.startsWith("BACKUP_APP_MEM"))
    			{
    				tempArr = temp.split("=");
    				Tbupappmem = Integer.parseInt(tempArr[1]);
    			}
    			else if(temp.startsWith("HOME_APP_MEM"))
    			{
    				tempArr = temp.split("=");
    				Thomeappmem = Integer.parseInt(tempArr[1]);
    			}
    			else if(temp.startsWith("HIDDEN_APP_MEM"))
    			{
    				tempArr = temp.split("=");
    				Thidappmem = Integer.parseInt(tempArr[1]);
    			}
    			else if(temp.startsWith("EMPTY_APP_MEM"))
    			{
    				tempArr = temp.split("=");
    				Tempappmem = Integer.parseInt(tempArr[1]);
    			}
    			else if(temp.startsWith("LMK_ADJ"))
    			{
    				tempArr = temp.split("=");
    				TlmkParAdj = tempArr[1];
    			}
    			else if(temp.startsWith("LMK_MINFREE"))
    			{
    				tempArr = temp.split("=");
    				TlmkParMin = tempArr[1];
    			}
    			else
    			{
    				continue;
    			}
    		
    		}
    	}catch(Exception Ignored){return false;}
    	
    	return true;
    }
    
    private void dropCache()
    {
    	String out = "Dropping caches.\n";
    	out = out.concat(ShellInterface.getProcessOutput("free") + "\n");
    	ShellInterface.runCommand("sync");
    	out = out.concat("Sync Successful!\n");
    	ShellInterface.runCommand("echo \"3\" > /proc/sys/vm/drop_caches");
    	out = out.concat("Caches Dropped!\n");
    	out = out.concat(ShellInterface.getProcessOutput("free"));
    	
    	Toast.makeText(this, out, Toast.LENGTH_LONG).show();
    }
}