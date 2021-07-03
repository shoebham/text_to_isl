import os
from nltk.parse import stanford
import stanza 
from nltk.stem import WordNetLemmatizer

# will only work after downloading stanford parser and extracting it in the root folder
os.environ['STANFORD_PARSER'] = 'stanford-parser-full-2018-10-17'
os.environ['STANFORD_MODELS'] = 'stanford-parser-full-2018-10-17/edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz'

parser = stanford.StanfordParser(path_to_models_jar="stanford-parser-full-2018-10-17/edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz")
sentences = parser.raw_parse("Hi, i am shubham how are you")

print(sentences)

# pipeline for english model in stanza
en_nlp = stanza.Pipeline('en')


# text for parsing in stanza
some_text= en_nlp("This is a test sentence to test lemmatizer, i need to type some words and get their root words");

# stanford parser 
# for line in sentences:
# 	print(line)
# 	for sentence in line:
# 		sentence.draw()


def parse_from_stanza(text):
	for sentence in text.sentences:
		lemmas=get_lemma_from_sentence(sentence)
		words=get_pos_of_words(sentence)
	return lemmas,words

def get_lemma_from_sentence(sentence):
	lemmatized_words=[]
	for word in sentence.words:
		lemmatized_words.append(word.lemma)
	return lemmatized_words


def get_pos_of_words(sentence):
	pos=[];
	for word in sentence.words:
		pos.append((word.text,word.pos))
	return pos


lemma_words,pos_words=parse_from_stanza(some_text)


for word in lemma_words:
	print(word)
for word in pos_words:
	print(word)

# lemmatizer = WordNetLemmatizer();
# print(lemmatizer.lemmatize("their"))

