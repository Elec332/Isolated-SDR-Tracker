package elec332.isdrt.setup;

import elec332.isdrt.util.ISetup;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Elec332 on 6-10-2019
 */
public abstract class AbstractSetup implements ISetup {

    public static void mapFile(File file, Consumer<Map<String, String>> dataBuilder) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        List<String> l = mapLines(reader.lines(), dataBuilder);
        reader.close();
        writeToFile(file, l);
    }

    public static List<String> mapLines(Stream<String> lines, Consumer<Map<String, String>> dataBuilder) {
        Map<String, String> data = new HashMap<>();
        dataBuilder.accept(data);
        return lines.map(s -> {
            for (Map.Entry<String, String> e : data.entrySet()){
                if (s.contains(e.getKey())){
                    return e.getValue();
                }
            }
            return s;
        }).collect(Collectors.toList());
    }

    public static void writeToFile(File file, Consumer<Consumer<String>> w) throws IOException {
        List<String> buf = new ArrayList<>();
        w.accept(buf::add);
        writeToFile(file, buf);
    }

    public static void writeToFile(File file, List<String> w) throws IOException {
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()){
            throw new IOException();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for (String s : w){
            writer.append(s);
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

}
