package paramManager;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
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
            converter = FileOutputConverter.class,
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

    @Parameter(names = {"--averaging"},
            validateWith = PositiveIntegerValidation.class,
            description = "<averaging> : valor value on es farà l'averaging de pixels RGB en un kernel de value x value.")
    private int averaging_value;

    @Parameter(names = {"--negative"},
            description = " : Argument que indica que aplicarà el filtre per fer el negatiu de la imatge.")
    private boolean negative;

    @Parameter(names = {"-v", "--verbose"},
                description = "Argument que indica si es volen mostrar els fps.")
    private boolean verbose;

    @Parameter(names = {"--nTiles"},
            validateWith = PositiveIntegerValidation.class,
            description = "<value> : Nombre de tessel·les en la qual dividir la imatge.")
    private int tiles;

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
     * Getters dels parameters.
     */

    public boolean isHelp(){return help;}

    public Path getInputPath() {return input;}

    public String getOutputPath() {return output;}

    public int getFps() {return fps;}

    public int getAveraging_value() {return averaging_value;}

    public boolean isVerbose() {return verbose;}

    public boolean applyNegative() {return negative;}

    public boolean hasWindow() {return batch;}

    public boolean getEncode() {return encode;}

    public boolean getDecode() {return decode;}

    public int getnTiles() {return tiles;}

    public int getSeekRange() {return seekRange;}

    public int getGOP() {return gop;}

    public int getQuality() {return quality;}
}

