package paramManager;

import com.beust.jcommander.IStringConverter;

/**
 * Classe per afegir el file-format .zip al nom del camí.
 */
public class FileOutputConverter implements IStringConverter<String> {

    /**
     * Afegeix el .zip si és necessari.
     * @param s String original del cami entrada per l'usuari.
     * @return String modificada amb el .zip.
     */
    @Override
    public String convert(String s) {
        if(!s.contains(".zip")) s += ".zip";
        return s;
    }
}
