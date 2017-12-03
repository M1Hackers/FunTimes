import base64
import requests
import json
import PIL
from PIL import Image
from collections import Counter
import operator

API = 'AIzaSyAPPm6FmMfhXHxKoScqLuRcD-9H3QSm8f4'
mapsApi = 'AIzaSyAuDZiTlOHlYclPk_YvOvj3rCv9y_lymzQ'
# img = Image.open("test.jpg")
# img = img.resize((1024,768), Image.ANTIALIAS)
# img.save("new.jpg")

lat = 41.316324
lon = -72.922343
#encoded = base64.b64encode(open("new.jpg", 'rb').read())
# print(encoded)
def encode_image(image):
  image_content = open(image, 'rb').read()
  return base64.b64encode(image_content)
pics = ["test.png", "Scan 2.jpeg"]
categories = []
for i in pics:
    encoded = str(encode_image(i),'utf-8') #encode_image(i))
    print(type(encoded))
    #print(encoded)
    #print(base64.b64decode(encoded))
    
    data = {
        "requests": [
            {
                "image": {
                  "content": encoded
                  },
                "features": [
                    {
                        "type": "LABEL_DETECTION",
                        "maxResults": 2
                    }
                ]
            }
        ]
    }
    #print(len(str(encoded)))

    # data = json.dumps(data)
    # print(data)
    r = requests.post("https://vision.googleapis.com/v1/images:annotate?key=" + API, json=data)
    #print(r.json())
    # json1_file = open('r')
    categories.append(r.json()['responses'][0]['labelAnnotations'][0]["description"])

print(categories)
category_dict = dict(Counter(categories))
sorted_categories = sorted(category_dict.items(), key=operator.itemgetter(1))
print(sorted_categories)
sorted_categories = [('food', 1), ('park', 1)]
id_s=[]
for key in sorted_categories:
    r = requests.get(
        "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=" + mapsApi + "&keyword=" + key[0] + "&location=" + str(lat) + "," + str(lon) + "&radius=1000&rankby=prominence")
    d=r.json()
    print("d", d)
    for i in range(5):
        id_s.append(d[i]["id"])
print(id_s)
    # print(json1_data)

print(categories)
