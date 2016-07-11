package org.eclipse.smarthome.tools.docgenerator.util;

import org.eclipse.smarthome.tools.docgenerator.GeneratorException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public final class XmlUtils {

    /**
     * Consumes data.
     *
     * @param <T> Type of data.
     */
    public interface Consumer<T> {
        /**
         * Consumes data.
         *
         * @param data the data to consume
         */
        void accept(T data);
    }

    private XmlUtils() {/* do not allow instances */}

    /**
     * Scans the given directory for XML files and invokes fileConsumer on found files.
     * Note: This method also checks whether directory exists and whether it is a directory (not a file).
     *
     * @param directory    the directory that gets scanned for XML files
     * @param fileConsumer the consumer function that gets invoked on found files. It's argument it the found File.
     * @throws IllegalArgumentException if the given directory's files can't be listed
     */
    public static void handleXmlFiles(File directory, Consumer<File> fileConsumer) {
        if (directory.exists() && directory.isDirectory()) {
            File[] fileList = directory.listFiles();
            if (fileList == null) {
                throw new GeneratorException("Can not list files of directory " + directory.toString());
            }
            for (File file : fileList) {
                if (file != null && file.isFile() && file.getName().endsWith(".xml")) {
                    fileConsumer.accept(file);
                }
            }
        }
    }

    /**
     * Reads the given (XML) file and unmarshalls it to the given class type.
     *
     * @param file  the XML file to parse
     * @param clazz the class to convert the XML description to
     * @param <T>   the type to convert the XML description to
     * @return the object of type class that represents the contents of the XML file
     * @throws IllegalArgumentException if the XML can not be unmarshalled
     */
    public static <T> T convertXmlToObject(File file, Class<T> clazz) {
        T description;
        try {
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            description = (T) unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Can not unmarshall XML description of file " + file.toString() + " to object of type " + clazz.toString(), e);
        }
        return description;
    }

}
