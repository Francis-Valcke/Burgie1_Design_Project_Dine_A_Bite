package cobol.commons;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CSV {

    private static CSV instance;
    private HashMap<String, String[]> csv;
    private File file;

    private CSV(String path){
        this.file = new File(path);
        csv = new HashMap<>();
    }

    /**
     * Reads the actual .csv file into the csv structure.
     * @return return this object.
     */
    public CSV open() throws IOException {
        BufferedReader csvReader = new BufferedReader(new FileReader(file));
        
        String row;
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",");
            csv.put(data[0],data);
        }
        csvReader.close();

        return this;
    }

    /**
     * Writes the csv structure into the actual .csv file.
     */
    public void close() throws IOException {
        BufferedWriter csvWriter = new BufferedWriter(new FileWriter(file));

        for (Map.Entry<String, String[]> entry : csv.entrySet()) {

            StringBuilder builder = new StringBuilder();
            for (String string : entry.getValue()) {
                builder.append(string);
                builder.append(',');
            }
            String line = builder.toString();
            csvWriter.write(line.substring(0, line.length()-1));
            csvWriter.newLine();
        }
    }

    /**
     * CSV is a singleton
     * @return return singleton instance.
     */
    public static CSV getInstance(String path){
        if (instance==null){
            instance = new CSV(path);
        }
        return instance;
    }


}
