package io.github.benoitduffez.cupsprint;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
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

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		TextView tv = (TextView) findViewById(R.id.abouttext);
		PackageInfo pInfo;
		String version = "";

		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
		}
		String html = "<h1>JfCupsPrint " + version + "</h1>";

		html = html + "<p>Copyright &copy; Jon Freeman 2013</p>";

		html = html +
				"<p>This software uses ini4j, jmdns and libraries from the Apache Commons Project. These are " +
				"licenced under the Apache Licence. This software also uses the Cups4j library. " +
				"Further details may be found at " +
				"<a href=\"http://mobd.jonbanjo.com/jfcupsprint/licence.php\">http://mobd.jonbanjo.com/jfcupsprint/licence.php</a>";

		html = html +
				"<p>Redistribution and use of JfCupsPrint in source and binary forms, with or without modification, is permitted " +
				"provided this notice is retained in source code redistributions and that recipients agree that JfCupsPrint is provided " +
				"\"as is\", without warranty of any kind, express or implied, including but not limited to the warranties of merchantability, " +
				"fitness for a particular purpose, title and non-infringement. In no event shall the copyright holders or anyone distributing " +
				"the software be liable for any damages or other liability, whether in contract, tort or otherwise, arising from, out of" +
				"or in connection with the software or the use or other dealings in the software.</p>";


		tv.setText(Html.fromHtml(html));
		tv.setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}
