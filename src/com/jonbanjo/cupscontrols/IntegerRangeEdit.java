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

import android.content.Context;
import android.text.InputType;
import android.widget.EditText;

import com.jonbanjo.cups.PpdItemList;

	public class IntegerRangeEdit extends EditText{
		

		private PpdItemList section;
			
		public IntegerRangeEdit(Context context){
			super(context);
		}
		public IntegerRangeEdit(int id, Context context, PpdItemList section){
			super(context);
	 		setId(id);
			setText(section.getSavedValue());
		 	setInputType(InputType.TYPE_CLASS_TEXT);
		 	setTextSize(CupsControl.TextSize);
		 	setTextScaleX(CupsControl.TextScale);
		 	this.section = section;
		 	
		 }
		
		 public boolean validate(){
		 		return true;
		 }
		
		 public void update(){
		 	section.setSavedValue(getText().toString());
		 }

}
