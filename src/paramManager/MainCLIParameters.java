package paramManager;

import com.beust.jcommander.*;
import com.beust.jcommander.converters.PathConverter;

import java.nio.file.Path;

/**
 * Classe que defineix els paràmetres d'entrada per a l'aplicació de codificació i descodificació d'imatges.
 */
@Parameters(separators = "=")
public class MainCLIParameters {

    @Parameter(names = {"-h", "--help"},
            help = true,
            description = "Mostra l'ajuda dels arguments d'execució.")
    private boolean help;

    @Parameter(names = {"-i", "--input"},
            required = true,
            validateWith = FileParemeterValidator.class,
            converter = PathConverter.class,
            description = "<path to file.zip> : Fitxer d’entrada. Argument obligatori.")
    private Path input;

    @Parameter(names = {"-o", "--output"},
            validateWith = FileParemeterValidator.class,
            description = "<path to file> : Nom del fitxer en format propi amb la seqüència d’imatges de sortida i la " +
                    "informació necessària per la descodificació.")
    private String output;

    @Parameter(names = {"-e", "--encode"},
            description = "Argument que indica que s’haurà d’aplicar la codificació sobre el conjunt d’imatges d’input.")
    private boolean encode;

    @Parameter(names = {"-d", "--decode"},
            description = "Argument que indica que s’haurà d’aplicar la descodificació sobre el conjunt d’imatges d’input.")
    private boolean decode;

    @Parameter(names = {"--fps"},
            validateWith = PositiveIntegerValidation.class,
            description = "<value> : Nombre d’imatges per segon amb les quals és reproduirà el vídeo.")
    private int fps;

    // TODO: Introduir paràmetres pels filtres que implementem


    // TODO: Definir quin tipus d'input volem
    @Parameter(names = {"--nTiles"},
            //validateWith = IntegerArrayValidation.class,
            description = "<value, ...> : Nombre de tessel·les en la qual dividir la imatge. Es poden indicar diferents " +
                    "valors per l’eix vertical i horitzontal, o bé especificar la mida de les tessel·les en píxels.")
    private Object tiles;

    @Parameter(names = {"--seekRange"},
            validateWith = PositiveIntegerValidation.class,
            description = "<value> : Desplaçament màxim en la cerca de tessel·les coincidents.")
    private Integer seekRange;

    @Parameter(names = {"--GOP"},
            validateWith = PositiveIntegerValidation.class,
            description = "<value> : Nombre d'imatges entre dos frames de referència.")
    private Integer gop;

    @Parameter(names = {"--quality"},
            validateWith = PositiveIntegerValidation.class,
            description = "<value> : Factor de qualitat que determinarà quan dos tessel·les és consideren coincidents")
    private Integer quality;

    @Parameter(names = {"-b", "--batch"},
            description = "Mode d'execució sense GUI, al terminal")
    private boolean batch;


    /**
     * Un getter per saber si s'ha executat el programa amb la finalitat de saber quins paràmetres es poden utilitzar.
     * @return Si el codi s'està executant amb la funció de mostrar les opcions d'execució.
     */
    public boolean isHelp(){return help;}

    public Path getInputPath() {return input;}

    public int getFps() {return fps;}
}

