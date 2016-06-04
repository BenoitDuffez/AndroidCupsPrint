THIS IS THE FDROID VERSON OF THE APP
====================================


# AndroidCupsPrint
Port of cups4j to Android. Original work was created by Jon Freeman, it included an app that reacts to the SEND intent to print documents.

See it live on the Play Store: https://play.google.com/store/apps/details?id=io.github.benoitduffez.cupsprint

## Original work

Original work can be found here: http://mobd.jonbanjo.com/jfcupsprint/default.php  
Original work found via: http://android.stackexchange.com/q/43774/63883

## Modifications

This app was modified in several ways:

* project structure converted to gradle format
* added support for Android PrintService so that it can print documents straight from almost all apps
* removed all legacy code that allowed printing without the use of Android PrintService (this is removed because of minSdkVersion=19, meaning all targets of this app are PrintService-compliant)
 
### Print Service

Print service works at the framework level:

<img alt="Android framework Print Services" src="http://i.imgur.com/FIBi7vl.png" width="300" />

See the [Wiki](https://github.com/BenoitDuffez/AndroidCupsPrint/wiki) for more information about how to use it.

As per the code, the following has been added:

* a service in the AndroidManifest.xml file that registers the app as a PrintService
* `CupsPrinterDiscoverySession.java`: handles printer discovery and printer management
* `CupsService.java`: handles Android framework connectivity and print jobs management
* removed all legacy code (pre API 19)

# Contribute

This app wasn't widely tested, it needs your help for better quality. If you find bugs, either submit a new issue or fork/fix/submit PR.

## Branches

* The `master` branch is code published to Google Play.
* The `jonbanjo` branch is the app containing all the legacy code written by Jon Freeman

# License

LGPL

```
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.
This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
MA 02110-1301  USA
```

Original license information by Jon Freeman:

```
Redistribution and use of AndroidCupsPrint in source and binary forms,
with or without modification, is permitted provided this notice
is retained in source code redistributions and that recipients
agree that AndroidCupsPrint is provided "as is", without warranty of
any kind, express or " implied, including but not limited to the
warranties of merchantability, fitness for a particular purpose,
title and non-infringement. In no event shall the copyright holders
or anyone distributing the software be liable for any damages or
other liability, whether in contract, tort or otherwise, arising
from, out of or in connection with the software or the use or
other dealings in the software.
```

## External libraries

* A modified version of cups4j 0.63. The original source code and further details about cups4j may be found at http://www.cups4j.org/ (licensed under the LGPL license)
* JmDNS This is licensed under the Apache Licence

