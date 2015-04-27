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
        opts, args = getopt.getopt(argv,"hi:epf:",["infolder_type=","inpath="])
    except getopt.GetoptError:
        print("Error: dnb2es.py -f <path to parse> -i <data or test> -e <es indexing> -p <print>")
        sys.exit(2)
    esIn = False
    printout = False
    for opt, arg in opts:
        if opt == '-h':
            print('dnb2es.py -f <path to parse> -i <data or test> -e <es indexing> -p <print>')
            sys.exit()
        elif opt in ("-f", "--inpath"):
            infolder = arg
        elif opt in ("-i", "--infolder_type"):
            if infolder:
                pass
            elif arg == "data":
                infolder = "./DNB_Data"
            elif arg == "test":
                infolder = "./Test_Data"
        elif opt == '-e':
            esIn = True
        elif opt =='-p':
            printout = True
    return infolder,esIn, printout


ES_URL = "localhost:9200"
es = elasticsearch.Elasticsearch()  # use default of localhost, port 9200

def es_indexing(json_item):
    es.index(index='dnb_db', doc_type='item', body=json_item)

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
   infolder,esIn,printout = main(sys.argv[1:])
   print("infolder =", infolder, "| indexing = ",esIn, "| print = ",printout)
parse(infolder,esIn,printout)


