# Conway's Game of Life

### In Memory of John Conway (Dec 1937 - Apr 2020)

*He helped us see the beauty of math in everything.*

## Running
To run use java in a terminal on the jar

```java -jar life.jar```

You can also compile from source.

### Windows Double Click
To enable .jar files to run properly on a double click from the GUI, editing the registy can fix this.

- Go to ```Computer\HKEY_CLASSES_ROOT\jarfile\shell\open\command``` in the registry.

- Edit the value adding ```-jar``` to the default.

- eg. ```"C:\Program Files (x86)\Common Files\Oracle\Java\javapath\javaw.exe" (-jar gets added here) "%1" %*```

You should now be able to run .jar files from the GUI.

This fixes running .jar files from all locations I've run into on the Windows GUI. (Taskbar, Desktop, Search, etc...)