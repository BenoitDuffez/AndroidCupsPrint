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
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.jonbanjo.cups.PpdItem;
import com.jonbanjo.cups.PpdItemList;

public class EnumEdit extends Spinner{

	private PpdItemList section;
	
	public EnumEdit(Context context){
		super(context);
	}
	
	public EnumEdit(int id, Context context, int resourceId, PpdItemList section){
		super(context);
	 	setId(id);
		this.section = section;
		ArrayAdapter <PpdItem> aa = 
				new ArrayAdapter<PpdItem>(getContext(), resourceId, section);
		setAdapter(aa);
		int size = section.size();
		for (int i=0; i<size; i++){
			if (section.get(i).getValue().equals(section.getSavedValue())){
				this.setSelection(i);
				break;
			}
		}
		
	 }
	
	 public boolean validate(){
		 return true;
	 }
	
	 public void update(){
	 	PpdItem item = (PpdItem) this.getSelectedItem();
	 	section.setSavedValue(item.getValue());
	 }
}
