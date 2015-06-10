package io.github.benoitduffez.cupsprint;

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

import android.app.Application;
import android.content.Context;

public class CupsPrintApp extends Application {

	public static final String LOG_TAG = "CUPS";

	private static CupsPrintApp instance;

	public static CupsPrintApp getInstance() {
		return instance;
	}

	public static Context getContext() {
		return instance.getApplicationContext();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
	}
}
