import urllib.request as urlre
import re,json,sys

endpoint = 'http://localhost:9200/dnb_db'

book_title = str(sys.argv[1])

# book_title = 'Harry Potter und der Feuerkelch'
dQuotes = '\".*\"'
header = re.compile(r'original_titel')

def build_q_string(in_string):
    q_words = in_string.split()
    q_string = '+'.join(q_words)
    return q_string

def extract_original_title(field):
    m = re.search(dQuotes, field)
    original_title = m.group()[1:-1]
    return original_title

query = '_search?q=title:'+build_q_string(book_title)+'&size=5&pretty=true'

url_list = [endpoint, query]

my_url = '/'.join(url_list)

print('QUERY = ', my_url)

out = urlre.urlopen(my_url)
data = out.read()
print ('OUTPUT = ',data)

# fields = data.split('\n')
# print('splitted_data = ', fields)
# if header in str(data):
#     original_title =extract_original_title(data)
#     print(original_title)


# data_json = json.loads(data)
# print(data_json['hits']['total'])



