package us.teamtinker.tim.magicbookshelf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tim Clancy
 * @version 2.10.14
 */
public class TextFile {

    private final String filePath;

    TextFile(String filePath) {
        this.filePath = filePath;
    }

    boolean hasShelfCoordinate(int x, int y, int z, String worldName) {
        try {
            File file = new File(filePath);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("c" + x + "_" + y + "_" + z + "_" + worldName)) {
                    scanner.close();
                    return true;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TextFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    void insert(int x, int y, int z, String worldName, int xp) {
        BufferedWriter out = null;
        try {
            File file = new File(filePath);
            String lineToAdd = "c" + x + "_" + y + "_" + z + "_" + worldName + " -- " + xp;
            out = new BufferedWriter(new FileWriter(file, true));
            out.write(lineToAdd);
            out.newLine();
            out.flush();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(TextFile.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(TextFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    boolean remove(int x, int y, int z, String worldName) {
        File inputFile = new File(filePath);
        File tempFile = new File("temporaryShelves.txt");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            String lineToRemove = "c" + x + "_" + y + "_" + z + "_" + worldName;
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                // trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                if (trimmedLine.contains(lineToRemove)) {
                    continue;
                }
                writer.write(currentLine);
                writer.newLine();
            }
            //This following chunk is necessary to unlock the file--Java glitch.
            writer.flush();
            writer.close();
            writer = null;
            reader.close();
            reader = null;
            System.gc();
            inputFile.setWritable(true);
            //
            inputFile.delete();
            boolean successful = tempFile.renameTo(inputFile);
            return successful;
        } catch (IOException ex) {
        }
        return false;
    }

    int getShelfXP(int x, int y, int z, String worldName) {
        try {
            File file = new File(filePath);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("c" + x + "_" + y + "_" + z + "_" + worldName)) {
                    line = line.replace("c" + x + "_" + y + "_" + z + "_" + worldName + " -- ", " ");
                    return Integer.parseInt(line.trim());
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TextFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
}
