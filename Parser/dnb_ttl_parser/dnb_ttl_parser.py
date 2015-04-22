import re
# from json import dumps
import json
import logging
log =logging.getLogger(__name__)
import model
# from elasticsearch import Elasticsearch
import elasticsearch

# \r: return
r_line = re.compile(r'([^\r\n]*)(?:\r\n|\r|\n)')
item_spliter = re.compile(r'\n{2,}')
item_start = re.compile(r'<http://d-nb.info/')
item_end = re.compile(r'a bibo:Document ;\Z')
item_part = re.compile(r'dcterms:isPartOf')
dQuotes = '\".*\"'
aBracket = '.*<'
bBracket = '.*>'
abBracket = r'<.*>'

item_original_title = re.compile('dcterms:alternative')
item_lang = re.compile('dcterms:language')

def split_item(text):
    '''
    split items from file and save as items (e.g. book is an item)
    items are those sperated by 'item_spliter'
    '''
    items = re.split(item_spliter, text)
    return items

def select_items(items):
    '''
    select all TRANSLATED items that are written in different languages
    remove isPartOf relation
    only select item with language-field
    '''
    translated_docs = []
    for pre_item in items:
        fields = pre_item.split('\n')
        #: might be a book
        e = item_end.search(fields[0])
        # get rid of "isPartOf"
        p = item_part.search(fields[0])
        # translated books
        ot = item_original_title.search(pre_item)
        # get rid of books without 'language'-field (mostly non Ger/Eng books)
        lang = item_lang.search(pre_item)
        if e and ot and lang and not p :
            translated_docs.append(pre_item)
    return translated_docs


def parse_item(fields):
    '''
    parse fields of an item and save into dictionary
    '''
    author_tags = ['marcRole:cre', 'dcterms:creator']
    author = []
    for field in fields:
        if 'dcterms:alternative' in field:
            original_title = parse_original_title(field)
        if 'dc:title' in field:
            title = parse_title(field)
        if 'dcterms:language' in field:
            language = parse_language(field)
        # if 'dcterms:issued' in field:
        #     year = parse_year(field)
        # for author_tag in author_tags:
        #     if author_tag in field:
        #         author = parse_author(field)
    parsed_item = model.Item({
        # 'ID':index,
        'title':title,
        'original_title':original_title,
        'language':language})
        # 'year':year,
        # 'author':author
    return parsed_item

def parse_original_title(field):
    '''
    parse original title of a translated item
    DNB-header: 'dcterms:alternative'
    '''
    m = re.search(dQuotes, field)
    if m:
        alternativeWithLang = m.group()[1:-1]
        s = re.search(aBracket,alternativeWithLang)
        if '<' in alternativeWithLang:
            original_title = s.group()[0:-2]
        else:
             original_title = alternativeWithLang
    else: original_title = 'no original title found'
    return original_title

def parse_title(field):
    '''
    parse item title
    DNB-header: 'dc:title'
    '''
    m = re.search(dQuotes,field)
    if m:
        title = m.group()[1:-1]
    else: title = 'no title is found'
    return title

def parse_author(field):
    '''
    parse fields of author, contributor etc.
    DNB-header: author_tag = ['marcRole:cre', 'dcterms:creator', 'marcRole:red','marcRole:edt','dcterms:contributor'
    '''
    author = []
    m = re.findall(abBracket,field)
    author = m[0].split(',')
    return author

def parse_year(field):
    '''
    parse item published year
    DNB-header: 'dcterms:issued'
    '''
    m = re.search(dQuotes,field)
    year = m.group()[1:-1]
    return year

def parse_language(field):
    '''
    parse item written in which language
    DNB-header: 'dcterms:language'
    '''
    m = re.search(bBracket,field)
    language = m.group()[-4:-1]
    return language


def dic_to_json(item):
    '''
    transfter items in dictionary to JSON objects
    '''
    json_item = json.dumps(item, sort_keys=True, indent=4)
    return json_item

def es_indexing(file_index,json_items):
    es = elasticsearch.Elasticsearch()  # use default of localhost, port 9200
    es.index(index='dnb', doc_type='item', id=file_index, body=json_items)

