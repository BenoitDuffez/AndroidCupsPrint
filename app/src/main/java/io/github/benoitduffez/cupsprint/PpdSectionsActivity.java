package io.github.benoitduffez.cupsprint;

import com.jonbanjo.cups.PpdSectionList;
import com.jonbanjo.cupscontrols.CupsTableLayout;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/*Copyright (C) 2013 Jon Freeman

This program is free software; you can redistribute it and/or modify it under
the terms of the GNU Lesser General Public License as published by the Free
Software Foundation; either version 3 of the License, or (at your option) any
later version.
 
This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE.
 
See the GNU Lesser General Public License for more details. You should have
received a copy of the GNU Lesser General Public License along with this
program; if not, see <http://www.gnu.org/licenses/>.
*/

public class PpdSectionsActivity extends Activity {

	private PpdSectionList group;
	boolean uiSet = false;
	@Override

	protected void onCreate(Bundle savedInstanceState) {
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ppd_sections);
		Intent intent = getIntent();
		int index = intent.getIntExtra("section", 0);
		group = PrintJobActivity.getPpd().getExtraList().get(index);
		setControls();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.aboutmenu, menu);
		return true;
	}

	@Override
	public void onConfigurationChanged (Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		if (uiSet){
			setControls();
		}
	}

	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case R.id.about:
	    		Intent intent = new Intent(this, AboutActivity.class);
	    		intent.putExtra("printer", "");
	    		startActivity(intent);
	    		break;
	    }
	    return super.onContextItemSelected(item);
	 }

	private void setControls(){
		final CupsTableLayout layout = (CupsTableLayout) findViewById(R.id.sectionsViewLayout);
		layout.reset();
		layout.addSection(group);
		TableRow row = new TableRow(this);
		row.addView(new TextView(this));

		Button btn = new Button(this);
		btn.setText("OK");
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!layout.update())
                	return;
                finish();
            }
        });
		row.addView(btn);
		layout.addView(row,new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		uiSet = true;
	}

}
