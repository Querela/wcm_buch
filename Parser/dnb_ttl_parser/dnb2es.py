import elasticsearch
import os.path
import sys, getopt
import file_io as io
import dnb_ttl_parser as parser
import model
import pprint
pp = pprint.PrettyPrinter(indent=4)
### parse all files in input folder


def main(argv):
    try:
        opts, args = getopt.getopt(argv,"hi:o:ep",["inpath=","--index_name="])
    except getopt.GetoptError:
        print("Error: dnb2es.py -i <path to parse> -o <ES index name> -e <ES indexing> -p <print>")
        sys.exit(2)
    esIn = False
    printout = False
    es_index = 'dnb_db'
    for opt, arg in opts:
        if opt == '-h':
            print('dnb2es.py -i <path to parse> -o <ES index name>  -e <ES indexing> -p <print>')
            sys.exit()
        elif opt in ("-i", "--inpath"):
            infolder = arg
        elif opt in ("-o", "--index_name"):
            es_index = arg
            print('es_index', arg)
        elif opt == '-e':
            esIn = True
        elif opt =='-p':
            printout = True
    return infolder,es_index,esIn, printout


ES_URL = "localhost:9200"
es = elasticsearch.Elasticsearch()  # use default of localhost, port 9200


def es_indexing(json_item):
    es.index(index=es_index, doc_type='item', body=json_item)

def parse(infolder, esIn, printout):
    infiles = io.get_infiles(infolder)
    infile_counter = 0
    for infile in infiles:
        infile_counter += 1
        text = io.read_text(infile)
        filename = os.path.basename(infile)
        outfile_name = filename+'_output.txt'
        translated_docs = []
        items = parser.split_item(text)
        translated_docs = parser.select_items(items)
        for index, translated_doc in enumerate(translated_docs):
            fields = translated_doc.split('\n')
            parsed_item = parser.parse_item(fields)
            # translate Item object to dictionary
            item_to_dict = parsed_item.dict()

            # ONLY save german and english books to Elasticsearch
            if item_to_dict['language'] == 'eng' or item_to_dict['language'] =='ger':
                json_item = parser.dic_to_json(item_to_dict)
                if esIn == True:
                    es_indexing(json_item)
                if printout == True:
                    pp.pprint(json_item)
        if esIn ==True:
            print('............ File: ',infile,'\t is indexed. Total_indexed: ',infile_counter, ' files ..............')

if __name__ == "__main__":
    infolder,es_index,esIn,printout = main(sys.argv[1:])
    print("infolder =", infolder, "| indexing = ",esIn, "| print = ",printout)
    print('es_index', es_index)
parse(infolder,esIn,printout)



