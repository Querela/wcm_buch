import elasticsearch
import os.path
import sys
import file_io as io
import dnb_ttl_parser as parser
import model
### parse all files in input folder

ES_URL = "localhost:9200"

infolder = "./DNB_Data"
infiles = io.get_infiles(infolder)

es = elasticsearch.Elasticsearch()  # use default of localhost, port 9200

def es_indexing_ger(file_index,json_items):
    es.index(index='dnb_ger', doc_type='item', id=file_index, body=json_item)

def es_indexing_eng(file_index,json_items):
    es.index(index='dnb_eng', doc_type='item', id=file_index, body=json_item)

# def parse_folder(folder):
outfolder_counter = 0
for infile in infiles:
    outfolder_counter += 1
    text = io.read_text(infile)
    filename = os.path.basename(infile)
    outfile_name = filename+'_output.txt'
    translated_docs = []
    # json_items = model.Itemlist([])
    json_items = []
    outfile = os.path.join(outfolder, outfile_name)
    # if output file already exists, delete
    try:
        os.remove(outfile)
    except OSError:
        pass
    items = parser.split_item(text)
    translated_docs = parser.select_items(items)
    for index, translated_doc in enumerate(translated_docs):
        file_index = index
        fields = translated_doc.split('\n')
        parsed_item = parser.parse_item(index,fields)
        # translate Item object to dictionary
        item_to_dict = parsed_item.dict()
        # save items into two indexes in ES by language
        if item_to_dict['language'] == 'ger':
            json_item = parser.dic_to_json(item_to_dict)
            es_indexing_ger(file_index,json_item)
        if item_to_dict['language'] == 'eng':
            json_item = parser.dic_to_json(item_to_dict)
            es_indexing_eng(file_index,json_item)
        json_items.append(json_item)
        print(json_items)
