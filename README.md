# Text to Indian Sign Language

A Summer project that will translate your text to Indian sign Language animations

# Demo
![demo](demo.gif)
## Tech used

- Python
- Flask 
- Javascript
- [Stanza](https://stanfordnlp.github.io/stanza/)
- [Nltk](https://www.nltk.org/)
- [Stanford Parser](https://nlp.stanford.edu/software/lex-parser.shtml)
- [SIGML](https://vh.cmp.uea.ac.uk/index.php/SiGML)


## Installation
This project needs flask and python to run.

Install the dependencies and start the server.

```sh
pip install -r requirements.txt
python main.py
```
After running ```main.py``` stanford parser will be downloaded 
you may run into some errors related to classpath of java, google them they shouldn't be so hard to fix 
Open the browser and go to http://127.0.0.1:5000/  and see the project in action.

## NOTE
The project uses SIGML files for animating the words and they may not be accurate as making SIGML through HamNoSys is a long and tedious task and whoever made the sigml files which this project uses may not be accurate. 

## Credits
Word reordering and stanford parser download logic: -  https://github.com/sahilkhoslaa/AudioToSignLanguageConverter

https://github.com/human-divanshu/Text-to-Sign-Language

SIGML player: - https://vh.cmp.uea.ac.uk/index.php/CWA_Signing_Avatars
