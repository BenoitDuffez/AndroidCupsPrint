# JfCupsPrint
Port of cups4j to Android. Original work was created by Jon Freeman, it included an app that reacts to the SEND intent to print documents.

This app was modified in several ways:

* project structure converted to gradle format
* added support for Android PrintService so that it can print documents straight from almost all apps

## Original work

Original work can be found here: http://mobd.jonbanjo.com/jfcupsprint/default.php  
Original work found via: http://android.stackexchange.com/q/43774/63883

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
Redistribution and use of JfCupsPrint in source and binary forms,
with or without modification, is permitted provided this notice
is retained in source code redistributions and that recipients
agree that JfCupsPrint is provided "as is", without warranty of
any kind, express or " implied, including but not limited to the
warranties of merchantability, fitness for a particular purpose,
title and non-infringement. In no event shall the copyright holders
or anyone distributing the software be liable for any damages or
other liability, whether in contract, tort or otherwise, arising
from, out of or in connection with the software or the use or
other dealings in the software.
```

## External libraries

From the Apache Commons Project

* commons-codec
* commons-logging
* commons-validator

These are licensed under the Apache licence:

* A modified version of cups4j 0.63. The original source code and further details about cups4j may be found at http://www.cups4j.org/ 
* ini4j This is licensed under the Apache Licence
* JmDNS This is licensed under the Apache Licence

