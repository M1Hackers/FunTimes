import base64
import requests
import json
import PIL
from PIL import Image

API = 'AIzaSyAPPm6FmMfhXHxKoScqLuRcD-9H3QSm8f4'

# img = Image.open("test.jpg")
# img = img.resize((1024,768), Image.ANTIALIAS)
# img.save("new.jpg")


encoded = base64.b64encode(open("new.jpg", 'rb').read())
# print(encoded)
pics = ["test.png", "Scan 2.jpeg"]
categories = []
for i in pics:
  name = i
  data = {
    "requests":[
      {
        "image":{
          "source":{
              "imageUri":
              "gs://photoss/" +name,
            }
          },
        "features":[
          {
            "type":"LABEL_DETECTION",
            "maxResults":2
          }
        ]
      }
    ]
  }
  print(len(str(encoded)))

  data = json.dumps(data)
  #print(data)
  r = requests.post("https://vision.googleapis.com/v1/images:annotate?key="+API,data=data)
  #print(r.json())
  # json1_file = open('r')
  categories.append(r.json()['responses'][0]['labelAnnotations'][0]["description"])
  # print(json1_data)

print(categories)