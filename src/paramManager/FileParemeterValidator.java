package paramManager;

import com.beust.jcommander.*;
import java.nio.file.*;

/**
 * La classe FileParemeterValidator implementa la interfície
 * {@code IParameterValidator} de JCommander. La seva finalitat és
 * comprovar si els camins obtinguts per paràmetres, corresponen a
 * fitxer existents dins el sistema.
 *
 * @see <a href="https://github.com/cbeust/jcommander/blob/master/src/main/java/com/beust/jcommander/IParameterValidator.java">IParameterValidator</a>
 */
public class FileParemeterValidator implements IParameterValidator {

    /**
     * Comprova si hi ha fitxer a la localització passada per
     * paràmetre {@code path}.
     * @param nom
     *        Nom del paràmetre que es vol validar.
     * @param path
     *        Camí on es troba el fitxer que es vol validar.
     * @throws ParameterException
     *         Si el {@code path} no correspon a un fitxer o simplement,
     *         aquest és inexistent.
     */
    @Override
    public void validate(String nom, String path) throws ParameterException {
        Path pathToConfigDir = Paths.get(path);
        if(!Files.exists(pathToConfigDir, LinkOption.NOFOLLOW_LINKS)) {
            String msg = "param " + nom + ":\n\t" + path + " no existeix: ";
            throw new ParameterException(msg);
        }

        if(!Files.isRegularFile(pathToConfigDir, LinkOption.NOFOLLOW_LINKS)) {
            String msg = "param " + nom + ":\n\t" + path + " no és un fitxer: ";
            throw new ParameterException(msg);
        }
    }
}
