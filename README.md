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


## UPDATE 26th Feb 2024
 I have added a dockerfile, now you can run this project in docker without worrying about errors.
 Steps:
 - Download docker and start it
 - run 
 ```sh 
 docker compose build 
 docker compose up
 ```
 - Now it should be running. 
 - you can access the project on localhost:8080 when running with docker
## UPDATE
Stanford Parser is no longer accessible so please download this [zip](https://drive.google.com/file/d/1lEafb759ZbA33VNvwZOr0fznhtC4Kf4P/view)
and paste it where your main.py is.
And set JAVA_HOME environment variable and add %JAVA_HOME%\bin in your path variable 
refer [this](https://github.com/shoebham/text_to_isl/issues/11) for more clarity.


## Installation
This project needs flask and python to run.

Install the dependencies and start the server.

```sh
pip install -r requirements.txt
pip install spacy
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
