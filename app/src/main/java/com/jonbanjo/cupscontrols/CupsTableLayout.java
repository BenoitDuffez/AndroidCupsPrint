package com.jonbanjo.cupscontrols;

import java.util.ArrayList;

import com.jonbanjo.cups.PpdItemList;
import com.jonbanjo.cups.PpdSectionList;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.WindowManager;
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

public class CupsTableLayout extends TableLayout{

	private boolean showName = true;
	
	private Context context;
	private PpdItemList section;
	@SuppressWarnings("rawtypes")
	private ArrayList<CupsControl> controls;
	int spinnerResId;
	
	public CupsTableLayout(Context context) {
		super(context);
		this.context = context;
	}
	
	public CupsTableLayout(Context context, AttributeSet attrs){
		super(context, attrs);
		this.context = context;
	}

	@SuppressWarnings("rawtypes")
	public void reset(){
		if (!(controls == null)){
			this.removeAllViews();
			this.update();
		}
		controls = new ArrayList<CupsControl>();
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        System.out.println(metrics.widthPixels);
        if (metrics.widthPixels > 720){
            spinnerResId = io.github.benoitduffez.cupsprint.R.layout.textspinner;
        	CupsControl.TextSize = 18;
        	CupsControl.TextScale = 0.8f;
        }
        else{
            spinnerResId = io.github.benoitduffez.cupsprint.R.layout.textspinnersmall;
        	CupsControl.TextSize = 14;
        	CupsControl.TextScale = 0.8f;
        }
		//int orientation = context.getResources().getConfiguration().orientation;
        //if (orientation == Configuration.ORIENTATION_LANDSCAPE)
	}

	public void setShowName(boolean val){
		showName = val;
	}
	
	private TextView getTextView(String text){
		text = text + " ";
		TextView tv = new TextView(this.getContext());
		int len = text.length();
		if (len > 20){
			text = text.substring(0, 18) + "..";
		}
		tv.setText(text);
		tv.setTextSize(CupsControl.TextSize);
		tv.setTextScaleX(CupsControl.TextScale);
		return tv;
	}
	
	public IntegerEdit addInteger(int id){
		IntegerEdit editor =  new IntegerEdit(id, context, section);
		controls.add(new IntegerControl(editor));
		return editor;
	}
	
	public KeywordEdit addKeyword(int id){
		KeywordEdit editor =  new KeywordEdit(id, context, spinnerResId, section);
		controls.add(new KeywordControl(editor));
		return editor;
	}
	
	
	public EnumEdit addEnum(int id){
		EnumEdit editor =  new EnumEdit(id, context, spinnerResId, section);
		controls.add(new EnumControl(editor));
		return editor;
	}
	
	
	public BooleanEdit addBoolean(int id){
		BooleanEdit editor =  new BooleanEdit(id, context, section);
		controls.add(new BooleanControl(editor));
		return editor;
	}
	
	public IntegerRangeEdit addIntegerRange(int id){
		IntegerRangeEdit editor =  new IntegerRangeEdit(id, context, section);
		controls.add(new IntegerRangeControl(editor));
		return editor;
	}
	
	public void addSection(PpdSectionList group){
		int id = 500;
		for (PpdItemList sect: group){
			section = sect;
			TableRow row = new TableRow(context);
			if (showName)
				row.addView(getTextView(section.getName()));
			else
				row.addView(getTextView(section.getText()));
				
			switch (section.getCommandType()){
				case KEYWORD:
					row.addView(addKeyword(id));
					break;
				case INTEGER:
					row.addView(addInteger(id));
					break;
				case BOOLEAN:
					row.addView(addBoolean(id));
					break;
				case ENUM:
					row.addView(addEnum(id));
					break;
				case SETOFRANGEOFINTEGER:
					row.addView(addIntegerRange(id));
					break;
				default:
					row.addView(getTextView(section.getCommandType().toString()));
			}
			this.addView(row,new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			id++;
		}
		
	}
	
	private boolean validate(){
		for (@SuppressWarnings("rawtypes") CupsControl control: controls){
			if (!control.validate())
				return false;
		}
		return true;
	}
	
	public boolean update(){
		
		if (!validate())
			return false;
		
		for (@SuppressWarnings("rawtypes") CupsControl control: controls){
			control.update();
		}
		return true;
		
	}
	
}
