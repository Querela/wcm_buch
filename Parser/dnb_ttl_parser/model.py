import pprint
pp = pprint.PrettyPrinter(indent=4)

class Item():
    def __init__(self, fields):
        # self.item_no = fields['ID']
        self.title = fields['title']
        self.original_title = fields['original_title']
        self.language = fields['language']
        # self.year = fields['year']
        # self.author = fields['author']
        # self.fields = fields

    def count_fields(self):
        return len(self.fields)

    def append(self,index, fields):
        self.fields.append(field)
        self.index = index

    # def __str__(self):
    #     text = '}, {'.join(["\'%s\':%s" % (k, v) for k, v in self.fields.items()])
    #     wrap_text = '[{'+text+'}'
    #     return wrap_text

    def __len__(self):
        return len(self.fields)

    def __str__(self):
        return pp.pprint(self)

    def dict(self):
        return self.__dict__


class Itemlist():
    def __init__(self, items):
        if items:
            self.items = items
        else:
            self.items = []

    def append(self, item):
        self.items.append(item)

    # def __str__(self):
    #     text = '], '.join(str(Item) for Item in self.items)
    #     wrap_text = text+']'
    #     return wrap_text

    def __str__(self):
        return pp.pprint(self)
