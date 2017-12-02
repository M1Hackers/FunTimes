import base64
import requests
import json
import PIL
from PIL import Image
from collections import Counter
import operator

API = 'AIzaSyAPPm6FmMfhXHxKoScqLuRcD-9H3QSm8f4'

# img = Image.open("test.jpg")
# img = img.resize((1024,768), Image.ANTIALIAS)
# img.save("new.jpg")

lat = 41.316324
lon = -72.922343
encoded = base64.b64encode(open("new.jpg", 'rb').read())
# print(encoded)
pics = ["test.png", "Scan 2.jpeg"]
categories = []
for i in pics:
    name = i
    data = {
        "requests": [
            {
                "image": {
                    "source": {
                        "imageUri":
                            "gs://photoss/" + name,
                    }
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
    print(len(str(encoded)))

    data = json.dumps(data)
    # print(data)
    r = requests.post("https://vision.googleapis.com/v1/images:annotate?key=" + API, data=data)
    # print(r.json())
    # json1_file = open('r')
    categories.append(r.json()['responses'][0]['labelAnnotations'][0]["description"])
category_dict = dict(Counter(categories))
sorted_categories = sorted(category_dict.items(), key=operator.itemgetter(1))
id_s=[]
for key in sorted_categories:
    r = requests.get(
        "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=" + API + "keyword=" + key + "location=" + lat + "," + lon + "radius=1000+rankby=prominence")
    d=r.json()["results"]
    for i in range(5):
        id_s.append(d[i]["id"])
print(id_s)
    # print(json1_data)

print(categories)
