# Codec de Video
Autors:
- Joan Subirana Adell
- Martí Altimira Cebrian
### Explicació
En aquest projecte de l'assignatura de Tecnologies Multimedia de la Universitat de Barcelona, es planteja el 
desenvolupament d'un software codec de video, el qual permeti codificar i decodificar fitxers per mostrar-los com a 
videos. A més també hi ha d'haver la possibilitat d'afegir filtres d'imatge, i mostrar el video conforma es realitza 
el procés. En el nostre cas, el projecte està dut a terme amb Java.

### Compilació del codi
Perquè el programa compili, s'han d'afegir les llibreries de [Jcommander](https://jcommander.org/), recomanem que siguin
de la versió 1.82 o superiors. Pel que fa a la versió de Java, recomanem el JDK 17.

### Execució
Un cop compilat el codi, es pot procedir a l'execució, per això s'han de conèixer els arguments:

* -h, --help :  
  Mostra l'ajuda i informació dels arguments d'execució.
* -i, --input \<path to file.zip> :  
  Fitxer d’entrada, és un argument obligatori.
* -o, --output \<path to file> :  
  Nom del fitxer en format propi amb la seqüència d’imatges de sortida i la informació necessària per a la descodificació.
* ~~-e, --encode :  
  Argument que indica que s’haurà d’aplicar la codificació sobre el conjunt d’imatges d’input.~~
* ~~-d, --decode :  
  Argument que indica que s’haurà d’aplicar la descodificació sobre el conjunt d’imatges d’input.~~
* --fps \<value> :  
  Nombre d’imatges per segon amb les quals és reproduirà el vídeo.  
    _Default: 0_
* --negative :  
  Argument que indica que aplicarà el filtre per fer el negatiu de la imatge.
* --averaging \<averaging> :  
  valor value on es farà l'averaging de pixels RGB en un kernel de value x value.  
    _Default: 0_
* ~~--nTiles \<value, ...> :  
  Nombre de tessel·les en la qual dividir la imatge. Es poden indicar diferents valors per l’eix vertical i horitzontal, o bé
    especificar la mida de les tessel·les en píxels.~~
* ~~--seekRange \<value> :  
  Desplaçament màxim en la cerca de tessel·les coincidents.~~
*  ~~--GOP \<value> :  
  Nombre d'imatges entre dos frames de referència.~~
* -b, --batch :  
  Mode d'execució sense GUI, al terminal.
* -v, --verbose :
  Argument que indica si es volen mostrar els fps.
