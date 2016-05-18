package eu.urho.accelmonitor;

import android.util.Log;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sopi on 5/18/16.
 */
public class Storage  {
  // log tag
  private static final String TAG = Storage.class.getName();

  // available types
  public enum Type {
    CSV
  };

  // data store in memory (to be dumped)
  List<String> data = new ArrayList<>();

  // directory name where db or files created
  private String dbDirectory = "AndroidApp";

  // name of the data output (filename, database name, etc.)
  private String dbName = "dummy";
  private Type dbType = Type.CSV;

  // header for CSV file dump
  private char separator = ',';
  private String header = "";

  /**
   * @param type @link StorageType
   */
  public Storage(Type type) {
    if (type == null) type = Type.CSV;
    this.dbType = type;
  }

  /**
   * Sets the name of the cirectory where files or the db is created
   *
   * @param name
   */
  public void setDirectory(String name) {
    this.dbDirectory = name;
  }

  /**
   * Sets the name of the data output
   *
   * @param name
   */
  public void setName(String name) {
    this.dbName = name;
  }

  /**
   * Sets the header of the CSV file
   *
   * @param header
   */
  public void setHeader(String header) {
    this.header = header;
  }

  /**
   * Sets the sepataor in the CSV file
   *
   * @param sep
   */
  public void setSeparator(char sep) {
    this.separator = sep;
  }

  /**
   * Adds a new line to the data List
   * @param data
   */
  public void add(String data) {
    this.data.add(data);
  }

  /**
   * To dump the data storage to file or db or wherever..
   *
   * @throws IOException
   */
  public boolean dump() throws IOException {
    boolean retVal = false;
    switch (this.dbType) {
      case CSV:
        retVal = this.dumpCSV();
        break;
      default:
        Log.d(TAG, "Can not dump to unknown data storage: " + this.dbType);
    }
    return retVal;
  }

  /**
   * Creates the CSV dump
   * @throws IOException
   */
  private boolean dumpCSV() throws IOException {
    File csvFile;
    Log.d(TAG, "Create file in " + dbDirectory + "/" + dbName);
    Log.d(TAG, "--> header: " + header);
    Log.d(TAG, "--> separator: " + separator);

    csvFile = new File(dbDirectory, dbName);
    csvFile.createNewFile();
    FileOutputStream fileOut = new FileOutputStream(csvFile);
    OutputStreamWriter streamWriter = new OutputStreamWriter(fileOut);

    streamWriter.append(header);
    streamWriter.append("\n");

    // iterate through the saved data
    for (int i = 0; i < data.size(); i++) {
      streamWriter.append(data.get(i));
      streamWriter.append("\n");
    }

    streamWriter.close();
    fileOut.close();
    return true;
  }
}
