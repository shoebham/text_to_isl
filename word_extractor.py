import re
import os


files = os.listdir('./static/signfiles');
word=re.compile(r'[^\/]+(?=\.)');
words_file = open("words.txt",'w')

# creates a file of words whose sign files are available 
for f in files:
	if(word.match(f)):
		# print(word.match(f).group())
		words_file.write(word.match(f).group())
		words_file.write("\n")


words_file.close();


