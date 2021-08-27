import json
import os
from nltk.parse import stanford
import stanza 
from nltk.stem import WordNetLemmatizer
from nltk.tokenize import word_tokenize
from nltk.tokenize import sent_tokenize
from nltk.corpus import stopwords
from nltk.parse.stanford import StanfordParser
from nltk.tree import *

from flask import Flask,request,render_template,send_from_directory,jsonify

app =Flask(__name__,static_folder='static', static_url_path='')

import stanza
from stanza.server import CoreNLPClient
import pprint 


BASE_DIR = os.path.dirname(os.path.realpath(__file__))
print(BASE_DIR)


# Download zip file from https://nlp.stanford.edu/software/stanford-parser-full-2015-04-20.zip and extract in stanford-parser-full-2015-04-20 folder in higher directory
os.environ['CLASSPATH'] = os.path.join(BASE_DIR, 'stanford-parser-full-2018-10-17')
os.environ['STANFORD_MODELS'] = os.path.join(BASE_DIR,
                                             'stanford-parser-full-2018-10-17/edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz')
os.environ['NLTK_DATA'] = '/usr/local/share/nltk_data/'





en_nlp = stanza.Pipeline('en',processors={'tokenize':'spacy'})	
# print(stopwords.words('english'))

# stop words that are not to be included in ISL
stop_words = set(["am","are","is","was","were","be","being","been","have","has","had",
					"does","did","could","should","would","can","shall","will","may","might","must","let"]);

# test_input='''
# 					How are you.
# 					Chris\'s car was towed.
# 					I can go there.
# 					I have not had breakfast yet.
# 					That is something.
# 					I am stronger than him.
# 					I am the strongest.
# 					I love cats and dogs.
# 					I live in India.
# 					My flight was called off.
# 					this is a test sentence.
# 					'''.strip().replace("\n","").replace("\t","")
# test_input2=""
# for word in test_input.split("."):
# 	test_input2+= word.capitalize()+".";
# some_text= en_nlp(test_input2);





# sentences array
sent_list = [];
# sentences array with details provided by stanza
sent_list_detailed=[];

# word array
word_list=[];
# word array with details provided by stanza
word_list_detailed=[];

def convert_to_sentence_list(text):
	print("text here is ",text)
	for sentence in text.sentences:
		sent_list.append(sentence.text)
		sent_list_detailed.append(sentence)


# converts to words array for each sentence. ex=[ ["This","is","a","test","sentence"]];
def convert_to_word_list(sentences):
	temp_list=[]
	temp_list_detailed=[]
	for sentence in sentences:
		for word in sentence.words:
			temp_list.append(word.text)
			temp_list_detailed.append(word)
		word_list.append(temp_list.copy())
		word_list_detailed.append(temp_list_detailed.copy())
		temp_list.clear();
		temp_list_detailed.clear();


# removes stop words
def filter_words(word_list):
	temp_list=[];
	final_words=[];
	# removing stop words from word_list
	for words in word_list:
		temp_list.clear();
		for word in words:
			if word not in stop_words:
				temp_list.append(word);
			# print("temp_list",temp_list)
		final_words.append(temp_list.copy());
	# print("here");
	# removes stop words from word_list_detailed 
	for words in word_list_detailed:
		# print("words",words)
		for i,word in enumerate(words):
			# print(word.upos)
			# print("i:",i,"word",word)
			# print(words[i].upos)
			if(words[i].text in stop_words):
				del words[i];
				break;
			# word_list_detailed[:] = [x for x in word if x.upos !='PUNCT']
	
	return final_words;
# 

# removes punctutation 
def remove_punct(word_list):
	# removes punctutation from word_list_detailed
	# print("list",word_list_detailed)
	for words,words_detailed in zip(word_list,word_list_detailed):
		for i,(word,word_detailed) in enumerate(zip(words,words_detailed)):
			# print("word",word,"word_detailed",word_detailed,"i",i);
			# print("word with array ",words_detailed[i]);
		# 	# print(word.upos)
		# 	# print("i:",i,"word",word)
		# 	# print(words[i].upos)
			if(word_detailed.upos=='PUNCT'):
				del words_detailed[i];
				words.remove(word_detailed.text);
				break;
			# word_list_detailed[:] = [x for x in word if x.upos !='PUNCT']
	# print("after removing",word_list_detailed);
	# print("after removing",word_list);


# lemmatizes words
def lemmatize(final_word_list):
	for words,final in zip(word_list_detailed,final_word_list):
		for i,(word,fin) in enumerate(zip(words,final)):
			if fin in word.text:
				final[i]=word.lemma;
				# print("here",word.lemma)
	
	for word in final_word_list:
		print("final_words",word);

def label_parse_subtrees(parent_tree):
    tree_traversal_flag = {}

    for sub_tree in parent_tree.subtrees():
        tree_traversal_flag[sub_tree.treeposition()] = 0
    return tree_traversal_flag


def handle_noun_clause(i, tree_traversal_flag, modified_parse_tree, sub_tree):
    # if clause is Noun clause and not traversed then insert them in new tree first
    if tree_traversal_flag[sub_tree.treeposition()] == 0 and tree_traversal_flag[sub_tree.parent().treeposition()] == 0:
        tree_traversal_flag[sub_tree.treeposition()] = 1
        modified_parse_tree.insert(i, sub_tree)
        i = i + 1
    return i, modified_parse_tree


def handle_verb_prop_clause(i, tree_traversal_flag, modified_parse_tree, sub_tree):
    # if clause is Verb clause or Proportion clause recursively check for Noun clause
    for child_sub_tree in sub_tree.subtrees():
        if child_sub_tree.label() == "NP" or child_sub_tree.label() == 'PRP':
            if tree_traversal_flag[child_sub_tree.treeposition()] == 0 and tree_traversal_flag[child_sub_tree.parent().treeposition()] == 0:
                tree_traversal_flag[child_sub_tree.treeposition()] = 1
                modified_parse_tree.insert(i, child_sub_tree)
                i = i + 1
    return i, modified_parse_tree


def modify_tree_structure(parent_tree):
    # Mark all subtrees position as 0
    tree_traversal_flag = label_parse_subtrees(parent_tree)
    # Initialize new parse tree
    modified_parse_tree = Tree('ROOT', [])
    i = 0
    for sub_tree in parent_tree.subtrees():
        if sub_tree.label() == "NP":
            i, modified_parse_tree = handle_noun_clause(i, tree_traversal_flag, modified_parse_tree, sub_tree)
        if sub_tree.label() == "VP" or sub_tree.label() == "PRP":
            i, modified_parse_tree = handle_verb_prop_clause(i, tree_traversal_flag, modified_parse_tree, sub_tree)

    # recursively check for omitted clauses to be inserted in tree
    for sub_tree in parent_tree.subtrees():
        for child_sub_tree in sub_tree.subtrees():
            if len(child_sub_tree.leaves()) == 1:  #check if subtree leads to some word
                if tree_traversal_flag[child_sub_tree.treeposition()] == 0 and tree_traversal_flag[child_sub_tree.parent().treeposition()] == 0:
                    tree_traversal_flag[child_sub_tree.treeposition()] = 1
                    modified_parse_tree.insert(i, child_sub_tree)
                    i = i + 1

    return modified_parse_tree


def reorder_eng_to_isl(input_string):

	if len(input_string) is 1:
		return input_string

	parser = StanfordParser()
	# Generates all possible parse trees sort by probability for the sentence
	possible_parse_tree_list = [tree for tree in parser.parse(input_string)]
	# Get most probable parse tree
	parse_tree = possible_parse_tree_list[0]
	# print(parse_tree)
	# Convert into tree data structure
	parent_tree = ParentedTree.convert(parse_tree)
	
	modified_parse_tree = modify_tree_structure(parent_tree)
	
	parsed_sent = modified_parse_tree.leaves()
	return parsed_sent


final_words= [];
final_words_detailed=[];


# pre processing text
def pre_process(text):
	 #converts sentences to words
	# print(sent_list_detailed)
	# global final_words;
	remove_punct(word_list)
	final_words.extend(filter_words(word_list));
	lemmatize(final_words)

# print("--------------------Sent List------------------------");
# pprint.pprint(sent_list)
# # print("--------------------Word List Detailed------------------------");
# # print("word_list_detailed",word_list_detailed)
# # print("--------------------Sent List Detailed------------------------");
# # print("sent_list_detailed",sent_list_detailed)
# print("final_words");
# pprint.pprint(final_words);





def final_output(input):
	final_string=""
	valid_words=open("words.txt",'r').read();

	fin_words=[]
	for word in input:
		word=word.lower()
		if(word not in valid_words):
			for letter in word:
				# final_string+=" "+letter
				fin_words.append(letter);
		else:
			fin_words.append(word);

	return fin_words

final_output_in_sent=[];

def convert_to_final():
	for words in final_words:
		final_output_in_sent.append(final_output(words));



def take_input(text):
	test_input=text.strip().replace("\n","").replace("\t","")
	test_input2=""
	if(len(test_input)==1):
		test_input2=test_input;
	else:
		for word in test_input.split("."):
			test_input2+= word.capitalize()+".";


	some_text= en_nlp(test_input2);
	convert(some_text);


def convert(some_text):
	convert_to_sentence_list(some_text);
	convert_to_word_list(sent_list_detailed)

	# reorders the words in input
	for i,words in enumerate(word_list):
		# print(words)
		# print(reorder_eng_to_isl(words))
		word_list[i]=reorder_eng_to_isl(words)
		# for word in words:
	# 		print("".join(word))
	# removes punctuation and lemmatizes words
	pre_process(some_text);
	convert_to_final();
	print_lists();
	

def print_lists():
	print("--------------------Word List------------------------");
	pprint.pprint(word_list)
	print("--------------------Final Words------------------------");
	pprint.pprint(final_words);
	print("---------------Final sentence with letters--------------");
	pprint.pprint(final_output_in_sent)

def clear_all():
	sent_list.clear();
	sent_list_detailed.clear();
	word_list.clear();
	word_list_detailed.clear();
	final_words.clear();
	final_words_detailed.clear();
	final_output_in_sent.clear();
	final_words_dict.clear();


final_words_dict = {};
@app.route('/',methods=['GET'])
def index():
	clear_all();
	return render_template('index.html')


@app.route('/',methods=['GET','POST'])
def flask_test():
	clear_all()
	text = request.form.get('text')
	print("text is", text)
	take_input(text)
	# result = [{"text1":"testing/////////......."}];
	# clear_all()
	# final_output_in_json = {'isl_text_with_letters':final_output_in_sent}
	for words in final_output_in_sent:
		for i,word in enumerate(words,start=1):
			final_words_dict[i]=word;
	print("---------------Final words dict--------------");
	print(final_words_dict)
	return final_words_dict;
	# return render_template('index.html',result = final_words,signres=final_words_dict)


@app.route('/static/<path:path>')
def serve_signfiles(path):
	print("here");
	return send_from_directory('static',path)

# my_form();


if __name__=="__main__":
	app.run(debug=True)