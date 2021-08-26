import os
from nltk.parse import stanford
import stanza 
from nltk.stem import WordNetLemmatizer
from nltk.tokenize import word_tokenize
from nltk.tokenize import sent_tokenize
import stanza
from stanza.server import CoreNLPClient




# text = "Chris Manning is a nice person. Chris wrote a simple sentence. He also gives oranges to people."
# with CoreNLPClient(annotators=['tokenize','ssplit','pos','lemma','ner', 'parse'], timeout=30000, memory='16G') as client:
#     # submit the request to the server
#     ann = client.annotate(text)

#     # get the first sentence
#     sentence = ann.sentence[0]

#     # get the constituency parse of the first sentence
#     print('---')
#     print('constituency parse of first sentence')
#     constituency_parse = sentence.parseTree
#     print(constituency_parse)

#     # get the first subtree of the constituency parse
#     print('---')
#     print('first subtree of constituency parse')
#     print(constituency_parse.child[0])

# will only work after downloading stanford parser and extracting it in the root folder
# os.environ['STANFORD_PARSER'] = 'stanford-parser-full-2018-10-17'
# os.environ['STANFORD_MODELS'] = 'stanford-parser-full-2018-10-17/edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz'


# test_text= '''
# 					Root words examples:
# 					playing
# 					singing
# 					acrobat
# 					will
# 					then
# 					whose''';
# parser = stanford.StanfordParser(path_to_models_jar="stanford-parser-full-2018-10-17/edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz")
# sentences = parser.raw_parse(test_text.replace("\n"," "))
# test_text=test_text.strip().replace("\n"," ");
# # print(word_tokenize(test_text))

# pipeline for english model in stanza
en_nlp = stanza.Pipeline('en',processors={'tokenize':'spacy'})	


# # text for parsing in stanza
some_text= en_nlp('''
					Hello, how are you?
					Today is a fine day.
					I am hungry.
					He is not there.
					What are you doing?
					Will you leave?
					I am good.
					''');



# stanford parser 
# for line in sentences:
# 	print(line)
# 	for sentence in line:
# 		print(sentence)

# sentence segmentation
# for sentence in some_text.sentences:
# 	print(sentence.text);


def parse_from_stanza(text):
	lemmas=[]
	words=[]
	for sentence in text.sentences:
		lemmas.append(get_lemma_from_sentence(sentence))
		words.append(get_xpos_of_words(sentence))
	return lemmas,words


def sentence_segmentation(text):
	sentences=[]
	for sentence in text.sentences:
		sentences.append(sentence.text)
	return sentence

def get_lemma_from_sentence(sentence):
	lemmatized_words=[]
	for word in sentence.words:
		lemmatized_words.append(word.lemma)
	return lemmatized_words

# # gets the POS (language specific)
# def get_pos_of_words(sentence):
# 	pos=[];
# 	try:
# 		for word in sentence.words:
# 			pos.append((word.text,word.pos))
# 		return pos
# 	except AttributeError:
# 		for sentence in sentence.sentences:
# 			for word in sentence.words:
# 				pos.append((word.text,word.pos))
# 			return pos
# 	except exception:
# 		print(exception)

# Gets the xpos (penn tree bank pos)
def get_xpos_of_words(sentence):	
	pos=[];
	try:
		for word in sentence.words:
			pos.append((word.text,word.xpos))
		return pos
	except AttributeError:
		for sentence in sentence.sentences:
			for word in sentence.words:
				pos.append((word.text,word.xpos))
			return pos
	except exception:
		print(exception)

# removes unwanted POS words
def remove_stop_words(sentence):
	if final_text:
		final_text.clear()
	for sentence in sentence.sentences:
		temp_text=[]
		for words in sentence.words:
			if((words.xpos not in unwanted_pos) and words.text not in linking_verbs and words.upos!="PUNCT"):
				temp_text.append(words.text)
		final_text.append(temp_text)

final_text = [];

# captialize first character for sentence splitting (stanza)
# def capitalize_first_char(sentence):

# TO, POS(possessive ending), MD(Modals),
# FW(Foreign word), CC(coordinating conjuction), some DT(determiners like a, an,the), 
# JJR, JJS(adjectives, comparative and superlative), 
# NNS, NNPS(nouns plural, proper plural), RP(particles), SYM(symbols)
# VBD- Past tense verb ,VBZ- Verb 3rd person present ,VBG- verb present participle 
# VBP- Non 3rd person singular present, VBN- verb past participle 
unwanted_pos= ["TO","POS","MD","FW","CC","DT","JJR","JJS","NNS","NNPS","RP","SYM","VBD","VBZ","VBG","VBN","PUNCT"]
linking_verbs = ["am","is","are","was","were","have","be"]
test_sentence_with_unwanted_pos=en_nlp('hello'.strip().replace("\n",""))

# test_sentence_with_unwanted_pos= en_nlp('''Chris\'s car <-POS.
# 					to go <-TO.
# 					can could should must might<-MD.
# 					Bonjour<-FW.
# 					for and nor but or yet so<-CC.
# 					a an the that this those<-DT.
# 					stronger bigger larger happier better<-JJR (adj comparitive).
# 					strongest biggest largest happiest best <-JJS.
# 					cats dogs animals humans<-NNS.
# 					India, Delhi, Mumbai <-NNPS.
# 					call off, lay in on, throw up, lay off, get a grip <- RP.
# 					+-*/<-SYM.
# 					''');
# test_unwanted_pos=get_xpos_of_words(test_sentence_with_unwanted_pos);

# def remove_punc(sentence):


lemma_words,pos_words=parse_from_stanza(test_sentence_with_unwanted_pos)
# for words in test_unwanted_pos:
# 	print(words)


for i, sentence in enumerate(test_sentence_with_unwanted_pos.sentences):
    print(f'====== Sentence {i+1} tokens =======')
    print(*[f'id: {token.id}\ttext: {token.text}' for token in sentence.tokens], sep='\n')

# lemmatized words
print("---------Lemmatized sentences--------------")
test_sentence_with_unwanted_pos_lemmatized ='';
for word in lemma_words:
	test_sentence_with_unwanted_pos_lemmatized+= ' '.join(word)+' '+'\n';

# print(en_nlp(test_sentence_with_unwanted_pos_lemmatized))
print(test_sentence_with_unwanted_pos_lemmatized)
test_sentence_with_unwanted_pos_lemmatized=test_sentence_with_unwanted_pos_lemmatized.strip().replace("\n","")
print(test_sentence_with_unwanted_pos_lemmatized)


# for i, sentence in enumerate(en_nlp(test_sentence_with_unwanted_pos).sentences):
#     print(f'====== Sentence {i+1} tokens =======')
#     print(*[f'id: {token.id}\ttext: {token.text}' for token in sentence.tokens], sep='\n')



# stop word removal without lemmatization 
print("=====================stop word removal before lemmatization====================")
remove_stop_words(test_sentence_with_unwanted_pos)
print(final_text)

# stop word removal with lemmatization 
print("=====================stop word removal after lemmatization====================")
remove_stop_words(en_nlp(test_sentence_with_unwanted_pos_lemmatized))
print(final_text)


# print("-----Lemmas--------------")
# for word in lemma_words:
# 	print(word)

# # POS WORDS
print("-----POS-----------------")
for word in pos_words:
	print(word)



# lemma_words,pos_words=parse_from_stanza(some_text)

# print("-----Lemmas--------------")
# for word in lemma_words:
# 	print(word)
# print("-----POS-----------------")
# for word in pos_words:
# 	print(word)

# lemmatizer = WordNetLemmatizer();
# print(lemmatizer.lemmatize("playing"))
# for word in word_tokenize(test_text):
# 	print(lemmatizer.lemmatize(word))	

# for i, sentence in enumerate(some_text.sentences):
#     print(f'====== Sentence {i+1} tokens =======')
#     # print(*[f'id: {token.id}\ttext: {token.text}' for token in sentence.tokens], sep='\n')
#     print(sentence.tokens[0].id)
#     print(*[f'id: {token.id}\ttext: {token.text}\ttoken:{token}' for token in sentence.tokens], sep='\n')
    

