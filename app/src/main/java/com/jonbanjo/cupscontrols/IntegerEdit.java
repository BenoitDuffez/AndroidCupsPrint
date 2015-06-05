package com.jonbanjo.cupscontrols;

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

import com.jonbanjo.cups.PpdItemList;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;

public class IntegerEdit extends EditText{
	

	private PpdItemList section;
	
	public IntegerEdit(Context context){
		super(context);
	}
		
	public IntegerEdit(int id, Context context, PpdItemList section){
		super(context);
		setId(id);
		setText(section.getSavedValue());
 		setInputType(InputType.TYPE_CLASS_NUMBER);
	 	setTextSize(CupsControl.TextSize);
	 	setTextScaleX(CupsControl.TextScale);
	 	this.section = section;
	 }
	
	 public boolean validate(){
		String text = getText().toString();
	 	try {
	 		@SuppressWarnings("unused")
	 		int i = Integer.parseInt(text);
	 		return true;
	 		}
	 		catch (Exception e){
	 			AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
	 			builder.setMessage(section.getName() + " must be an integer")
	 			       .setTitle("error");
	 			AlertDialog dialog = builder.create();	
	 			dialog.show();
	 			this.requestFocus();
	 			return false;
	 		}
	 }
	 
	 public void update(){
		 section.setSavedValue(getText().toString());
	 }
	
}
